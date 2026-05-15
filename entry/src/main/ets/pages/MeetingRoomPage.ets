import promptAction from '@ohos.promptAction'
import { HMRouter, HMRouterMgr } from '@hadss/hmrouter'
import { MeetingApi } from '../api/MeetingApi'
import { MeetingSocketClient } from '../api/MeetingSocketClient'
import {
  MeetingChatMessageResponse,
  MeetingMemberResponse
} from '../api/model/MeetingModels'
import { MeetingSocketMessage } from '../api/model/MeetingSocketModels'
import { MeetingRuntimeStore } from '../utils/MeetingRuntimeStore'
import { MeetingBackParam } from '../utils/MeetingBackParam'


@HMRouter({
  pageUrl: 'MeetingRoomPage'
})
@Component
export struct MeetingRoomPage {
  @State meetingId: string = ''
  @State meetingNo: string = ''
  @State signalUrl: string = ''
  @State title: string = ''

  @State statusText: string = '会议中'
  @State chatText: string = ''
  @State memberList: MeetingMemberResponse[] = []
  @State chatList: MeetingChatMessageResponse[] = []

  @State isHost: boolean = false
  @State currentUserId: number = 0
  @State memberRefreshVersion: number = 0

  private isPageAlive: boolean = false
  private isLeavingRoom: boolean = false
  private isEndingMeeting: boolean = false
  private isCallingLeaveApi: boolean = false
  private socketClient: MeetingSocketClient = new MeetingSocketClient()

  private toBooleanValue(value: boolean | number | string | undefined | null): boolean {
    if (value === true) {
      return true
    }

    if (value === 1) {
      return true
    }

    if (value === '1') {
      return true
    }

    if (value === 'true') {
      return true
    }

    return false
  }
  private getLatestMember(userId: number): MeetingMemberResponse | null {
    for (let i = 0; i < this.memberList.length; i++) {
      const item: MeetingMemberResponse = this.memberList[i]

      if (item.userId === userId) {
        return item
      }
    }

    return null
  }
  private updateLocalMemberMicStatus(userId: number, micOn: boolean): void {
    const newList: MeetingMemberResponse[] = []

    for (let i = 0; i < this.memberList.length; i++) {
      const oldItem: MeetingMemberResponse = this.memberList[i]

      const newItem: MeetingMemberResponse = {
        userId: oldItem.userId,
        userName: oldItem.userName,
        avatar: oldItem.avatar,
        role: oldItem.role,
        micOn: oldItem.userId === userId ? micOn : oldItem.micOn,
        cameraOn: oldItem.cameraOn,
        online: oldItem.online,
        joinedAt: oldItem.joinedAt
      }

      newList.push(newItem)
    }

    this.memberList = newList
    this.memberRefreshVersion++
  }
  private log(tag: string, message: string): void {
    console.info('[MeetingRoomPage][' + tag + '] ' + message)
  }

  private logObj(tag: string, label: string, value: Object | null | undefined): void {
    try {
      console.info('[MeetingRoomPage][' + tag + '] ' + label + '=' + JSON.stringify(value))
    } catch (error) {
      console.info('[MeetingRoomPage][' + tag + '] ' + label + '=JSON.stringify failed')
    }
  }

  private updateLocalMemberCameraStatus(userId: number, cameraOn: boolean): void {
    const newList: MeetingMemberResponse[] = []

    for (let i = 0; i < this.memberList.length; i++) {
      const oldItem: MeetingMemberResponse = this.memberList[i]

      const newItem: MeetingMemberResponse = {
        userId: oldItem.userId,
        userName: oldItem.userName,
        avatar: oldItem.avatar,
        role: oldItem.role,
        micOn: oldItem.micOn,
        cameraOn: oldItem.userId === userId ? cameraOn : oldItem.cameraOn,
        online: oldItem.online,
        joinedAt: oldItem.joinedAt
      }

      newList.push(newItem)
    }

    this.memberList = newList
    this.memberRefreshVersion++
  }


  private closeSocketSafely(tag: string): void {
    try {
      this.socketClient.close()
    } catch (error) {
      console.error('[MeetingRoomPage][' + tag + '] close socket error=' + JSON.stringify(error))
    }
  }

  /**
   * 统一返回会议首页
   *
   * action:
   * left    主动离开
   * ended   会议结束
   * removed 被主持人移除
   */
  private backToHomeWithResult(action: string, message: string): void {
    this.log('backToHomeWithResult',
      '进入方法 action=' + action +
        ', message=' + message +
        ', isLeavingRoom=' + String(this.isLeavingRoom) +
        ', meetingId=' + this.meetingId +
        ', meetingNo=' + this.meetingNo
    )

    if (this.isLeavingRoom) {
      this.log('backToHomeWithResult', '已经在退出中，忽略重复退出 action=' + action)
      return
    }

    this.isLeavingRoom = true
    this.isCallingLeaveApi = false

    let status: string = 'running'
    let online: boolean = false

    if (action === 'ended') {
      status = 'ended'
      online = false
    } else if (action === 'left') {
      status = 'running'
      online = false
    } else if (action === 'removed') {
      status = 'running'
      online = false
    }

    const backParam: MeetingBackParam = new MeetingBackParam()
    backParam.fromMeetingRoom = true
    backParam.meetingId = this.meetingId
    backParam.meetingNo = this.meetingNo
    backParam.action = action
    backParam.status = status
    backParam.online = online
    backParam.refresh = true
    backParam.timestamp = Date.now()

    this.logObj('backToHomeWithResult', '准备返回参数 backParam', backParam)

    if (this.isPageAlive) {
      this.statusText = message
      this.showToast(message)
    }

    this.closeSocketSafely('backToHomeWithResult')

    MeetingRuntimeStore.clear()

    setTimeout(() => {
      this.logObj('backToHomeWithResult', '执行 replace 到 MeetingHomePage，param', backParam)

      HMRouterMgr.replace({
        pageUrl: 'MeetingHomePage',
        param: backParam
      })
    }, 300)
  }

  /**
   * 兼容旧调用：默认按主动离开处理
   */
  private finishMeetingAndBack(message: string): void {
    if (message.indexOf('结束') >= 0) {
      this.backToHomeWithResult('ended', message)
      return
    }

    this.backToHomeWithResult('left', message)
  }


  onBackPress(): boolean {
    this.handleLeavePage()
    return true
  }
  aboutToAppear(): void {
    this.log('aboutToAppear', '页面进入')
    this.isPageAlive = true
    this.isLeavingRoom = false
    this.isEndingMeeting = false

    this.meetingId = MeetingRuntimeStore.getMeetingId()
    this.meetingNo = MeetingRuntimeStore.getMeetingNo()
    this.signalUrl = MeetingRuntimeStore.getSignalUrl()
    this.title = MeetingRuntimeStore.getTitle()

    if (this.meetingId.length === 0) {
      this.showToast('会议信息为空')
      HMRouterMgr.pop()
      return
    }

    this.loadInitDetail()
    this.connectSocket()
  }

  aboutToDisappear(): void {
    this.log('aboutToDisappear', '页面消失，关闭 socket')
    this.isPageAlive = false

    try {
      this.socketClient.close()
    } catch (error) {
      console.error('[MeetingRoomPage][aboutToDisappear] close error=' + JSON.stringify(error))
    }
  }

  build() {
    Column() {
      this.buildHeader()

      Column() {
        this.buildMemberArea()
        this.buildChatArea()
        this.buildBottomBar()
      }
      .layoutWeight(1)
      .width('100%')
      .padding(12)
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F6FA')
  }

  @Builder
  private buildHeader() {
    Column() {
      Row() {
        Button('返回')
          .height(36)
          .backgroundColor('#FFFFFF')
          .fontColor('#2F68FF')
          .onClick(async () => {
            await this.handleLeavePage()
          })

        Column() {
          Text(this.title.length > 0 ? this.title : '会议房间')
            .fontSize(20)
            .fontWeight(FontWeight.Bold)
            .fontColor('#FFFFFF')
            .maxLines(1)
            .textOverflow({ overflow: TextOverflow.Ellipsis })

          Text('会议号：' + this.meetingNo + '  ' + this.statusText)
            .fontSize(12)
            .fontColor('#EAF0FF')
            .margin({ top: 4 })
        }
        .layoutWeight(1)
        .alignItems(HorizontalAlign.Center)

        if (this.isHost) {
          Button('结束')
            .height(36)
            .backgroundColor('#E84A4A')
            .fontColor('#FFFFFF')
            .onClick(async () => {
              await this.handleEndMeeting()
            })
        }
      }
      .width('100%')
      .padding({ left: 12, right: 12, top: 48 })
    }
    .width('100%')
    .height(120)
    .backgroundColor('#2F68FF')
  }

  @Builder
  private buildMemberArea() {
    Column() {
      Row() {
        Text('成员列表')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor('#222222')
          .layoutWeight(1)

        Text(this.isHost ? '当前：主持人' : '当前：成员')
          .fontSize(12)
          .fontColor(this.isHost ? '#2F68FF' : '#666666')
      }
      .width('100%')
      .margin({ bottom: 8 })

      if (this.memberList.length === 0) {
        Text('暂无在线成员')
          .fontSize(14)
          .fontColor('#999999')
          .width('100%')
          .textAlign(TextAlign.Center)
          .margin({ top: 8, bottom: 8 })
      } else {
        ForEach(this.memberList, (item: MeetingMemberResponse) => {
          this.buildMemberItem(item)
        }, (item: MeetingMemberResponse) => {
          return String(item.userId)
            + '_mic_' + (item.micOn ? '1' : '0')
            + '_camera_' + (item.cameraOn ? '1' : '0')
            + '_v_' + this.memberRefreshVersion
        })
      }
    }
    .width('100%')
    .padding(14)
    .borderRadius(14)
    .backgroundColor('#FFFFFF')
  }
  @Builder
  private buildMemberItem(item: MeetingMemberResponse) {
    Column() {
      Row() {
        Column() {
          Row() {
            Text(item.userName)
              .fontSize(15)
              .fontWeight(FontWeight.Medium)
              .fontColor('#222222')
              .maxLines(1)
              .textOverflow({ overflow: TextOverflow.Ellipsis })

            if (item.userId === this.currentUserId) {
              Text('我')
                .fontSize(11)
                .fontColor('#FFFFFF')
                .padding({ left: 6, right: 6, top: 2, bottom: 2 })
                .margin({ left: 6 })
                .borderRadius(8)
                .backgroundColor('#2F68FF')
            }

            if (item.role === 'host') {
              Text('主持人')
                .fontSize(11)
                .fontColor('#FFFFFF')
                .padding({ left: 6, right: 6, top: 2, bottom: 2 })
                .margin({ left: 6 })
                .borderRadius(8)
                .backgroundColor('#FF9900')
            }
          }

          Row() {
            Text(item.micOn ? '麦克风：开启' : '麦克风：关闭')
              .fontSize(12)
              .fontColor(item.micOn ? '#2F68FF' : '#999999')
              .margin({ right: 12 })

            Text(item.cameraOn ? '摄像头：开启' : '摄像头：关闭')
              .fontSize(12)
              .fontColor(item.cameraOn ? '#2F68FF' : '#999999')
          }
          .margin({ top: 5 })
        }
        .layoutWeight(1)

        if (this.isHost && item.userId !== this.currentUserId) {
          Text('可会控')
            .fontSize(12)
            .fontColor('#2F68FF')
        }
      }
      .width('100%')

      if (this.isHost && item.userId !== this.currentUserId) {
        Row() {
          Button(item.micOn ? '静音' : '开麦')
            .height(32)
            .fontSize(12)
            .layoutWeight(1)
            .margin({ right: 6 })
            .onClick(async () => {
              await this.handleMuteMember(item)
            })

          Button(item.cameraOn ? '关摄像头' : '开摄像头')
            .height(32)
            .fontSize(12)
            .layoutWeight(1)
            .margin({ right: 6 })
            .onClick(async () => {
              await this.handleCameraMember(item)
            })

          Button('移除')
            .height(32)
            .fontSize(12)
            .layoutWeight(1)
            .backgroundColor('#E84A4A')
            .onClick(async () => {
              await this.handleRemoveMember(item)
            })
        }
        .width('100%')
        .margin({ top: 10 })
      }
    }
    .width('100%')
    .padding(12)
    .margin({ bottom: 10 })
    .borderRadius(10)
    .backgroundColor('#F5F6FA')
  }
  @Builder
  private buildChatArea() {
    Column() {
      Text('聊天')
        .fontSize(18)
        .fontWeight(FontWeight.Bold)
        .fontColor('#222222')
        .width('100%')
        .margin({ bottom: 8 })

      Scroll() {
        Column() {
          if (this.chatList.length === 0) {
            Text('暂无聊天消息')
              .fontSize(14)
              .fontColor('#999999')
              .width('100%')
              .textAlign(TextAlign.Center)
              .margin({ top: 20 })
          } else {
            ForEach(this.chatList, (item: MeetingChatMessageResponse) => {
              Column() {
                Text(item.fromUserName + '：' + item.content)
                  .fontSize(14)
                  .fontColor('#222222')
                  .width('100%')

                Text(item.sendTime)
                  .fontSize(11)
                  .fontColor('#999999')
                  .width('100%')
                  .margin({ top: 4 })
              }
              .width('100%')
              .padding(10)
              .margin({ bottom: 8 })
              .borderRadius(8)
              .backgroundColor('#F5F6FA')
            }, (item: MeetingChatMessageResponse) => {
              return item.messageId
            })
          }
        }
        .width('100%')
      }
      .height(260)
      .width('100%')
    }
    .width('100%')
    .padding(14)
    .margin({ top: 12 })
    .borderRadius(14)
    .backgroundColor('#FFFFFF')
  }

  @Builder
  private buildBottomBar() {
    Row() {
      TextInput({ placeholder: '输入聊天内容', text: this.chatText })
        .layoutWeight(1)
        .height(44)
        .backgroundColor('#FFFFFF')
        .borderRadius(8)
        .padding({ left: 12, right: 12 })
        .onChange((value: string) => {
          this.chatText = value
        })

      Button('发送')
        .height(44)
        .margin({ left: 8 })
        .onClick(() => {
          this.handleSendChat()
        })
    }
    .width('100%')
    .margin({ top: 12 })
  }

  private async loadInitDetail(): Promise<void> {
    const result = await MeetingApi.getMeetingInitDetail(this.meetingId, 20)
    console.info('[MeetingRoomPage][loadInitDetail] ' + JSON.stringify(result))

    if (result.code === 200 && result.data) {
      const newMemberList: MeetingMemberResponse[] = []

      for (let i = 0; i < result.data.memberList.length; i++) {
        const item: MeetingMemberResponse = result.data.memberList[i]

        const newItem: MeetingMemberResponse = {
          userId: item.userId,
          userName: item.userName,
          avatar: item.avatar,
          role: item.role,
          micOn: this.toBooleanValue(item.micOn),
          cameraOn: this.toBooleanValue(item.cameraOn),
          online: this.toBooleanValue(item.online),
          joinedAt: item.joinedAt
        }

        newMemberList.push(newItem)
      }

      this.memberList = newMemberList
      this.chatList = result.data.chatList
      this.memberRefreshVersion++

      if (result.data.currentUserStatus) {
        this.isHost = this.toBooleanValue(result.data.currentUserStatus.host)
        this.currentUserId = result.data.currentUserStatus.userId
        this.statusText = this.isHost ? '主持人 已同步' : '成员 已同步'
      } else {
        this.statusText = '已同步'
      }

      return
    }

    this.statusText = '同步失败'
    this.showToast(result.msg)
  }

  private connectSocket(): void {
    if (this.signalUrl.length === 0) {
      this.showToast('signalUrl为空')
      return
    }

    this.socketClient.connect(this.signalUrl, this.meetingId, {
      onOpen: () => {
        this.statusText = '已连接'
      },
      onClose: (code: number, reason: string) => {
        console.info('[MeetingRoomPage][socket close] code=' + code + ', reason=' + reason)

        if (!this.isPageAlive || this.isLeavingRoom) {
          return
        }

        this.statusText = '连接关闭'
      },
      onError: (message: string) => {
        if (!this.isPageAlive || this.isLeavingRoom) {
          return
        }

        this.statusText = '连接异常'
        this.showToast(message)
      },
      onPong: (_msg: MeetingSocketMessage) => {
        console.info('[MeetingRoomPage][pong]')
      },
      onMemberSignal: async (_msg: MeetingSocketMessage) => {
        await this.loadInitDetail()
      },
      onChatMessage: (msg: MeetingSocketMessage) => {
        if (!msg.data) {
          return
        }

        const chat: MeetingChatMessageResponse = {
          messageId: msg.data.messageId ? msg.data.messageId : String(Date.now()),
          meetingId: msg.data.meetingId ? msg.data.meetingId : this.meetingId,
          meetingNo: msg.data.meetingNo ? msg.data.meetingNo : this.meetingNo,
          roomId: msg.data.roomId ? msg.data.roomId : '',
          fromUserId: msg.data.fromUserId ? msg.data.fromUserId : 0,
          fromUserName: msg.data.fromUserName ? msg.data.fromUserName : '',
          avatar: msg.data.avatar ? msg.data.avatar : '',
          messageType: msg.data.messageType ? msg.data.messageType : 'text',
          content: msg.data.content ? msg.data.content : '',
          sendTime: msg.data.sendTime ? msg.data.sendTime : ''
        }

        this.chatList = this.chatList.concat([chat])
      },
      onChatError: (msg: MeetingSocketMessage) => {
        const message: string = msg.data && msg.data.message ? msg.data.message : '聊天发送失败'
        this.showToast(message)
      },
      onControlMessage: async (msg: MeetingSocketMessage) => {
        console.info('[MeetingRoomPage][onControlMessage] ' + JSON.stringify(msg))

        const event: string = msg.event ? msg.event : ''

        if (event === 'meeting_ended') {
          this.handleMeetingEndedBySocket()
          return
        }

        if (event === 'mute_all') {
          await this.handleMuteAllBySocket(msg)
          return
        }

        if (event === 'mute_member') {
          await this.handleMuteMemberBySocket(msg)
          return
        }

        if (event === 'camera_member') {
          await this.handleCameraMemberBySocket(msg)
          return
        }

        if (event === 'remove_member') {
          this.handleRemoveMemberBySocket(msg)
          return
        }

        await this.loadInitDetail()
      },
      onControlError: (msg: MeetingSocketMessage) => {
        const message: string = msg.data && msg.data.message ? msg.data.message : '会控失败'
        this.showToast(message)
      }
    })
  }
  private async handleMuteMember(item: MeetingMemberResponse): Promise<void> {
    const latestMember: MeetingMemberResponse | null = this.getLatestMember(item.userId)
    const currentItem: MeetingMemberResponse = latestMember ? latestMember : item

    const nextMicOn: boolean = !currentItem.micOn

    console.info('[MeetingRoomPage][handleMuteMember] userId=' + currentItem.userId
      + ', oldMicOn=' + currentItem.micOn
      + ', nextMicOn=' + nextMicOn)

    const result = await MeetingApi.muteMember(
      this.meetingId,
      currentItem.userId,
      nextMicOn
    )

    console.info('[MeetingRoomPage][handleMuteMember] result=' + JSON.stringify(result))

    if (result.code === 200) {
      this.updateLocalMemberMicStatus(currentItem.userId, nextMicOn)

      this.showToast(nextMicOn ? '已打开成员麦克风' : '已静音成员')

      setTimeout(() => {
        this.loadInitDetail()
      }, 300)

      return
    }

    this.showToast(result.msg)
  }
  private async handleCameraMember(item: MeetingMemberResponse): Promise<void> {
    const latestMember: MeetingMemberResponse | null = this.getLatestMember(item.userId)
    const currentItem: MeetingMemberResponse = latestMember ? latestMember : item

    const nextCameraOn: boolean = !currentItem.cameraOn

    console.info('[MeetingRoomPage][handleCameraMember] userId=' + currentItem.userId
      + ', oldCameraOn=' + currentItem.cameraOn
      + ', nextCameraOn=' + nextCameraOn)

    const result = await MeetingApi.cameraMember(
      this.meetingId,
      currentItem.userId,
      nextCameraOn
    )

    console.info('[MeetingRoomPage][handleCameraMember] result=' + JSON.stringify(result))

    if (result.code === 200) {
      this.updateLocalMemberCameraStatus(currentItem.userId, nextCameraOn)

      this.showToast(nextCameraOn ? '已打开成员摄像头' : '已关闭成员摄像头')

      setTimeout(() => {
        this.loadInitDetail()
      }, 300)

      return
    }

    this.showToast(result.msg)
  }

  private async handleRemoveMember(item: MeetingMemberResponse): Promise<void> {
    const result = await MeetingApi.removeMember(
      this.meetingId,
      item.userId,
      '主持人移除成员'
    )

    console.info('[MeetingRoomPage][handleRemoveMember] ' + JSON.stringify(result))

    if (result.code === 200) {
      this.showToast('已移除成员')
      await this.loadInitDetail()
      return
    }

    this.showToast(result.msg)
  }
  private handleSendChat(): void {
    if (!this.socketClient.isConnected()) {
      this.showToast('WebSocket未连接')
      return
    }

    if (this.chatText.trim().length === 0) {
      this.showToast('请输入聊天内容')
      return
    }

    this.socketClient.sendChatMessage(this.chatText)
    this.chatText = ''
  }

  private handleMeetingEndedBySocket(): void {
    if (this.isEndingMeeting) {
      console.info('[MeetingRoomPage][handleMeetingEndedBySocket] 主持人正在结束会议，忽略重复 meeting_ended')
      return
    }

    this.backToHomeWithResult('ended', '会议已结束')
  }

  private async handleMuteAllBySocket(msg: MeetingSocketMessage): Promise<void> {
    const allowSelfUnmute: boolean =
      msg.data && msg.data.allowSelfUnmute !== undefined
        ? this.toBooleanValue(msg.data.allowSelfUnmute)
        : false

    const newList: MeetingMemberResponse[] = []

    for (let i = 0; i < this.memberList.length; i++) {
      const oldItem: MeetingMemberResponse = this.memberList[i]

      const shouldMute: boolean = oldItem.userId !== this.currentUserId || !allowSelfUnmute

      const newItem: MeetingMemberResponse = {
        userId: oldItem.userId,
        userName: oldItem.userName,
        avatar: oldItem.avatar,
        role: oldItem.role,
        micOn: shouldMute ? false : oldItem.micOn,
        cameraOn: oldItem.cameraOn,
        online: oldItem.online,
        joinedAt: oldItem.joinedAt
      }

      newList.push(newItem)
    }

    this.memberList = newList
    this.memberRefreshVersion++

    this.showToast('主持人已执行全体静音')

    setTimeout(() => {
      this.loadInitDetail()
    }, 300)
  }

  private async handleMuteMemberBySocket(msg: MeetingSocketMessage): Promise<void> {
    const targetUserId: number = this.getTargetUserIdFromSocketMessage(msg)

    if (targetUserId <= 0) {
      await this.loadInitDetail()
      return
    }

    const micOn: boolean =
      msg.data && msg.data.micOn !== undefined
        ? this.toBooleanValue(msg.data.micOn)
        : false

    this.updateLocalMemberMicStatus(targetUserId, micOn)

    if (targetUserId === this.currentUserId) {
      this.showToast(micOn ? '主持人已允许你打开麦克风' : '主持人已将你静音')
      this.statusText = micOn ? '麦克风已开启' : '麦克风已关闭'
    }

    setTimeout(() => {
      this.loadInitDetail()
    }, 300)
  }

  private async handleCameraMemberBySocket(msg: MeetingSocketMessage): Promise<void> {
    const targetUserId: number = this.getTargetUserIdFromSocketMessage(msg)

    if (targetUserId <= 0) {
      await this.loadInitDetail()
      return
    }

    const cameraOn: boolean =
      msg.data && msg.data.cameraOn !== undefined
        ? this.toBooleanValue(msg.data.cameraOn)
        : false

    this.updateLocalMemberCameraStatus(targetUserId, cameraOn)

    if (targetUserId === this.currentUserId) {
      this.showToast(cameraOn ? '主持人已允许你打开摄像头' : '主持人已关闭你的摄像头')
      this.statusText = cameraOn ? '摄像头已开启' : '摄像头已关闭'
    }

    setTimeout(() => {
      this.loadInitDetail()
    }, 300)
  }

  private handleRemoveMemberBySocket(msg: MeetingSocketMessage): void {
    const targetUserId: number = this.getTargetUserIdFromSocketMessage(msg)

    if (targetUserId <= 0) {
      this.loadInitDetail()
      return
    }

    if (targetUserId === this.currentUserId) {
      this.backToHomeWithResult('removed', '你已被主持人移出会议')
      return
    }

    const newList: MeetingMemberResponse[] = []

    for (let i = 0; i < this.memberList.length; i++) {
      const item: MeetingMemberResponse = this.memberList[i]

      if (item.userId !== targetUserId) {
        newList.push(item)
      }
    }

    this.memberList = newList
    this.memberRefreshVersion++

    setTimeout(() => {
      this.loadInitDetail()
    }, 300)
  }

  private getTargetUserIdFromSocketMessage(msg: MeetingSocketMessage): number {
    if (msg.targetUserId !== undefined && msg.targetUserId !== null) {
      return msg.targetUserId
    }

    if (msg.data && msg.data.targetUserId !== undefined && msg.data.targetUserId !== null) {
      return msg.data.targetUserId
    }

    return 0
  }

  private async handleEndMeeting(): Promise<void> {
    this.log('handleEndMeeting',
      '点击结束会议，isEndingMeeting=' + String(this.isEndingMeeting) +
        ', isLeavingRoom=' + String(this.isLeavingRoom) +
        ', meetingId=' + this.meetingId
    )

    if (this.isEndingMeeting || this.isLeavingRoom) {
      this.log('handleEndMeeting', '正在结束或退出中，忽略重复点击')
      return
    }

    if (this.meetingId.length === 0) {
      this.log('handleEndMeeting', '会议ID为空')
      this.showToast('会议ID为空')
      return
    }

    this.isEndingMeeting = true

    try {
      this.log('handleEndMeeting', '开始调用 MeetingApi.endMeeting meetingId=' + this.meetingId)

      const result = await MeetingApi.endMeeting(this.meetingId)

      this.logObj('handleEndMeeting', 'end result', result)

      if (!this.isPageAlive) {
        this.log('handleEndMeeting', '页面已不存活，停止处理')
        return
      }

      if (result.code === 200) {
        this.log('handleEndMeeting', '结束会议成功，准备返回 ended')
        this.backToHomeWithResult('ended', '会议已结束')
        return
      }

      this.isEndingMeeting = false
      this.showToast(result.msg)
    } catch (error) {
      this.isEndingMeeting = false
      console.error('[MeetingRoomPage][handleEndMeeting] error=' + JSON.stringify(error))
      this.showToast('结束会议失败')
    }
  }

  private async handleLeavePage(): Promise<void> {
    this.log('handleLeavePage',
      '点击返回，isLeavingRoom=' + String(this.isLeavingRoom) +
        ', isEndingMeeting=' + String(this.isEndingMeeting) +
        ', isCallingLeaveApi=' + String(this.isCallingLeaveApi) +
        ', meetingId=' + this.meetingId
    )

    if (this.isLeavingRoom || this.isEndingMeeting || this.isCallingLeaveApi) {
      this.log('handleLeavePage', '正在退出或结束会议，忽略重复点击')
      return
    }

    if (this.meetingId.length === 0) {
      this.log('handleLeavePage', 'meetingId 为空，直接返回首页')
      this.backToHomeWithResult('left', '已退出会议')
      return
    }

    this.isCallingLeaveApi = true
    this.statusText = '正在退出会议...'

    try {
      this.log('handleLeavePage', '开始调用 MeetingApi.leaveMeeting meetingId=' + this.meetingId)

      const result = await MeetingApi.leaveMeeting(this.meetingId)

      this.logObj('handleLeavePage', 'leave result', result)

      if (result.code !== 200) {
        this.log('handleLeavePage', '退出会议接口异常 msg=' + result.msg)
        this.showToast(result.msg)
      }
    } catch (error) {
      console.error('[MeetingRoomPage][handleLeavePage] leave error=' + JSON.stringify(error))
    }

    this.isCallingLeaveApi = false

    this.log('handleLeavePage', '准备 backToHomeWithResult left')
    this.backToHomeWithResult('left', '已退出会议')
  }

  private showToast(message: string): void {
    promptAction.showToast({
      message: message,
      duration: 2000
    })
  }
}