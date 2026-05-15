import webSocket from '@ohos.net.webSocket'
import { TokenStorage } from '../utils/TokenStorage'
import { MeetingSocketListener, MeetingSocketMessage } from './model/MeetingSocketModels'

/**
 * 会议 WebSocket 客户端
 *
 * 作用：
 * 1. 连接会议 WebSocket
 * 2. 自动携带 meetingId 和 token
 * 3. 自动心跳
 * 4. 断线自动重连
 * 5. 统一分发后端消息
 */
export class MeetingSocketClient {
  private ws: webSocket.WebSocket | null = null

  private meetingId: string = ''

  private signalUrl: string = ''

  private listener: MeetingSocketListener | null = null

  private heartbeatTimer: number = -1

  private reconnectTimer: number = -1

  private reconnectCount: number = 0

  private manualClose: boolean = false

  private connected: boolean = false

  /**
   * 心跳间隔：30秒
   */
  private readonly heartbeatInterval: number = 30000

  /**
   * 最大重连间隔：15秒
   */
  private readonly maxReconnectDelay: number = 15000

  /**
   * 连接 WebSocket
   *
   * @param signalUrl 后端返回的 signalUrl，例如 ws://192.168.1.100:8080/ws/meeting
   * @param meetingId 会议ID
   * @param listener 消息监听
   */
  connect(signalUrl: string, meetingId: string, listener: MeetingSocketListener): void {
    this.signalUrl = signalUrl
    this.meetingId = meetingId
    this.listener = listener
    this.manualClose = false

    this.doConnect()
  }

  /**
   * 真正执行连接
   */
  private doConnect(): void {
    if (this.ws !== null && this.connected) {
      console.info('[MeetingSocketClient][doConnect] 已连接，不重复连接')
      return
    }

    const token: string = TokenStorage.getToken()
    if (token.length === 0) {
      console.error('[MeetingSocketClient][doConnect] token为空，不能连接WebSocket')
      this.notifyError('token为空，不能连接WebSocket')
      return
    }

    if (this.signalUrl.length === 0 || this.meetingId.length === 0) {
      console.error('[MeetingSocketClient][doConnect] signalUrl或meetingId为空')
      this.notifyError('signalUrl或meetingId为空')
      return
    }

    const url: string = this.signalUrl
      + '?meetingId='
      + encodeURIComponent(this.meetingId)
      + '&token='
      + encodeURIComponent(token)

    console.info('[MeetingSocketClient][doConnect] url=' + url)

    this.ws = webSocket.createWebSocket()

    this.ws.on('open', () => {
      console.info('[MeetingSocketClient][open] WebSocket已连接')

      this.connected = true
      this.reconnectCount = 0

      this.startHeartbeat()

      if (this.listener && this.listener.onOpen) {
        this.listener.onOpen()
      }
    })

    this.ws.on('message', (error, value: string | ArrayBuffer) => {
      if (error) {
        console.error('[MeetingSocketClient][message] error=' + JSON.stringify(error))
        this.notifyError('WebSocket消息接收异常')
        return
      }

      let text: string = ''

      if (typeof value === 'string') {
        text = value
      } else {
        text = this.arrayBufferToString(value)
      }

      this.handleMessage(text)
    })

    this.ws.on('close', (error, value: webSocket.CloseResult) => {
      console.info('[MeetingSocketClient][close] error=' + JSON.stringify(error))
      console.info('[MeetingSocketClient][close] value=' + JSON.stringify(value))

      this.connected = false
      this.stopHeartbeat()

      let code: number = 0
      let reason: string = ''

      if (value) {
        code = value.code
        reason = value.reason
      }

      if (this.listener && this.listener.onClose) {
        this.listener.onClose(code, reason)
      }

      if (!this.manualClose) {
        this.scheduleReconnect()
      }
    })

    this.ws.on('error', (error) => {
      console.error('[MeetingSocketClient][error] ' + JSON.stringify(error))
      this.notifyError('WebSocket连接异常')
    })

    this.ws.connect(url, (error, value) => {
      if (error) {
        console.error('[MeetingSocketClient][connect callback] error=' + JSON.stringify(error))
        this.connected = false
        this.notifyError('WebSocket连接失败')

        if (!this.manualClose) {
          this.scheduleReconnect()
        }
        return
      }

      console.info('[MeetingSocketClient][connect callback] value=' + JSON.stringify(value))
    })
  }

  /**
   * 处理后端消息
   */
  private handleMessage(text: string): void {
    console.info('[MeetingSocketClient][message] ' + text)

    let message: MeetingSocketMessage

    try {
      message = JSON.parse(text) as MeetingSocketMessage
    } catch (error) {
      console.error('[MeetingSocketClient][handleMessage] JSON解析失败=' + JSON.stringify(error))
      this.notifyError('WebSocket消息解析失败')
      return
    }

    if (this.listener && this.listener.onMessage) {
      this.listener.onMessage(message)
    }

    const type: string = message.type ? message.type : ''
    const event: string = message.event ? message.event : ''

    if (type === 'heartbeat' && event === 'pong') {
      if (this.listener && this.listener.onPong) {
        this.listener.onPong(message)
      }
      return
    }

    if (type === 'meeting_signal') {
      if (this.listener && this.listener.onMemberSignal) {
        this.listener.onMemberSignal(message)
      }
      return
    }

    if (type === 'meeting_chat' && event === 'chat_message') {
      if (this.listener && this.listener.onChatMessage) {
        this.listener.onChatMessage(message)
      }
      return
    }

    if (type === 'meeting_chat_error' && event === 'chat_error') {
      if (this.listener && this.listener.onChatError) {
        this.listener.onChatError(message)
      }
      return
    }

    if (type === 'meeting_control') {
      if (this.listener && this.listener.onControlMessage) {
        this.listener.onControlMessage(message)
      }
      return
    }

    if (type === 'meeting_control_error' && event === 'control_error') {
      if (this.listener && this.listener.onControlError) {
        this.listener.onControlError(message)
      }
    }
  }

  /**
   * 发送聊天消息
   */
  sendChatMessage(content: string): void {
    const message: MeetingSocketMessage = {
      event: 'chat_message',
      data: {
        content: content,
        messageType: 'text'
      }
    }

    this.sendMessage(message)
  }

  /**
   * 发送麦克风状态变化
   */
  sendMicChanged(micOn: boolean): void {
    const message: MeetingSocketMessage = {
      event: 'mic_changed',
      data: {
        micOn: micOn
      }
    }

    this.sendMessage(message)
  }

  /**
   * 发送摄像头状态变化
   */
  sendCameraChanged(cameraOn: boolean): void {
    const message: MeetingSocketMessage = {
      event: 'camera_changed',
      data: {
        cameraOn: cameraOn
      }
    }

    this.sendMessage(message)
  }

  /**
   * 发送心跳 ping
   */
  private sendPing(): void {
    const message: MeetingSocketMessage = {
      event: 'ping',
      data: {
        clientTime: Date.now()
      }
    }

    this.sendMessage(message)
  }

  /**
   * 统一发送消息
   */
  private sendMessage(message: MeetingSocketMessage): void {
    if (this.ws === null || !this.connected) {
      console.error('[MeetingSocketClient][sendMessage] WebSocket未连接')
      this.notifyError('WebSocket未连接')
      return
    }

    const text: string = JSON.stringify(message)

    console.info('[MeetingSocketClient][sendMessage] ' + text)

    this.ws.send(text, (error, value) => {
      if (error) {
        console.error('[MeetingSocketClient][sendMessage] error=' + JSON.stringify(error))
        this.notifyError('WebSocket消息发送失败')
        return
      }

      console.info('[MeetingSocketClient][sendMessage] success=' + JSON.stringify(value))
    })
  }

  /**
   * 启动心跳
   */
  private startHeartbeat(): void {
    this.stopHeartbeat()

    this.heartbeatTimer = setInterval(() => {
      if (this.connected) {
        this.sendPing()
      }
    }, this.heartbeatInterval)
  }

  /**
   * 停止心跳
   */
  private stopHeartbeat(): void {
    if (this.heartbeatTimer !== -1) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = -1
    }
  }

  /**
   * 安排自动重连
   */
  private scheduleReconnect(): void {
    if (this.reconnectTimer !== -1) {
      return
    }

    this.reconnectCount++

    let delay: number = this.reconnectCount * 3000
    if (delay > this.maxReconnectDelay) {
      delay = this.maxReconnectDelay
    }

    console.info('[MeetingSocketClient][scheduleReconnect] ' + delay + 'ms 后重连，第 ' + this.reconnectCount + ' 次')

    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = -1
      this.doConnect()
    }, delay)
  }

  /**
   * 主动关闭连接
   *
   * 主动关闭后不会自动重连。
   */
  close(): void {
    this.manualClose = true
    this.connected = false

    this.stopHeartbeat()

    if (this.reconnectTimer !== -1) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = -1
    }

    if (this.ws !== null) {
      this.ws.close((error, value) => {
        if (error) {
          console.error('[MeetingSocketClient][close] error=' + JSON.stringify(error))
        }

        console.info('[MeetingSocketClient][close] value=' + JSON.stringify(value))
      })

      this.ws = null
    }
  }

  /**
   * 是否已连接
   */
  isConnected(): boolean {
    return this.connected
  }

  /**
   * 通知错误
   */
  private notifyError(message: string): void {
    if (this.listener && this.listener.onError) {
      this.listener.onError(message)
    }
  }

  /**
   * ArrayBuffer 转字符串
   */
  private arrayBufferToString(buffer: ArrayBuffer): string {
    const uint8Array: Uint8Array = new Uint8Array(buffer)
    let text: string = ''

    for (let i = 0; i < uint8Array.length; i++) {
      text += String.fromCharCode(uint8Array[i])
    }

    return decodeURIComponent(escape(text))
  }
}