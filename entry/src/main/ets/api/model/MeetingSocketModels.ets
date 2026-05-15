/**
 * WebSocket 消息 data
 *
 * 这里把后端可能返回的字段都声明成可选字段，
 * 避免 ArkTS 使用 any。
 */
export interface MeetingSocketData {
  userId?: number
  online?: boolean

  clientTime?: number
  serverTime?: number

  messageId?: string
  meetingId?: string
  meetingNo?: string
  roomId?: string
  fromUserId?: number
  fromUserName?: string
  avatar?: string
  messageType?: string
  content?: string
  sendTime?: string

  controlId?: string
  operatorUserId?: number
  targetUserId?: number
  sourceEvent?: string
  message?: string
  success?: boolean
  micOn?: boolean
  cameraOn?: boolean
  allowSelfUnmute?: boolean
  reason?: string
}

/**
 * 后端 WebSocket 消息统一结构
 */
export interface MeetingSocketMessage {
  type?: string
  event?: string
  meetingId?: string
  userId?: number
  targetUserId?: number
  data?: MeetingSocketData
  timestamp?: number
}

/**
 * WebSocket 回调
 */
export interface MeetingSocketListener {
  onOpen?: () => void
  onClose?: (code: number, reason: string) => void
  onError?: (message: string) => void
  onMessage?: (message: MeetingSocketMessage) => void
  onPong?: (message: MeetingSocketMessage) => void
  onChatMessage?: (message: MeetingSocketMessage) => void
  onChatError?: (message: MeetingSocketMessage) => void
  onControlMessage?: (message: MeetingSocketMessage) => void
  onControlError?: (message: MeetingSocketMessage) => void
  onMemberSignal?: (message: MeetingSocketMessage) => void
}