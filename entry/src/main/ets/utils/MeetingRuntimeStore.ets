/**
 * 会议运行时数据
 *
 * 用于页面之间临时共享当前会议信息。
 * 同时用于会议房间页返回首页时，通知首页刷新历史列表。
 */
export class MeetingRuntimeStore {
  private static meetingId: string = ''
  private static meetingNo: string = ''
  private static signalUrl: string = ''
  private static title: string = ''

  /**
   * 首页历史列表刷新标记
   *
   * 注意：
   * clear() 不要清这几个字段。
   * 因为房间页返回首页前会先 clear 当前会议信息，
   * 但首页还需要读取这个刷新标记。
   */
  private static historyNeedRefresh: boolean = false
  private static changedMeetingId: string = ''
  private static changedStatus: string = ''

  /**
   * 保存当前会议信息
   */
  static saveMeeting(meetingId: string, meetingNo: string, signalUrl: string, title: string): void {
    MeetingRuntimeStore.meetingId = meetingId
    MeetingRuntimeStore.meetingNo = meetingNo
    MeetingRuntimeStore.signalUrl = signalUrl
    MeetingRuntimeStore.title = title
  }

  static getMeetingId(): string {
    return MeetingRuntimeStore.meetingId
  }

  static getMeetingNo(): string {
    return MeetingRuntimeStore.meetingNo
  }

  static getSignalUrl(): string {
    return MeetingRuntimeStore.signalUrl
  }

  static getTitle(): string {
    return MeetingRuntimeStore.title
  }

  /**
   * 清空当前会议信息
   *
   * 重要：
   * 这里只清当前会议数据。
   * 不要清 historyNeedRefresh / changedMeetingId / changedStatus。
   */
  static clear(): void {
    MeetingRuntimeStore.meetingId = ''
    MeetingRuntimeStore.meetingNo = ''
    MeetingRuntimeStore.signalUrl = ''
    MeetingRuntimeStore.title = ''
  }

  static hasMeeting(): boolean {
    return MeetingRuntimeStore.meetingId.length > 0
  }

  /**
   * 标记首页历史列表需要刷新
   *
   * @param meetingId 发生变化的会议ID
   * @param status running 或 ended
   */
  static markHistoryNeedRefresh(meetingId: string, status: string): void {
    MeetingRuntimeStore.historyNeedRefresh = true
    MeetingRuntimeStore.changedMeetingId = meetingId
    MeetingRuntimeStore.changedStatus = status
  }

  /**
   * 首页是否需要刷新历史列表
   */
  static isHistoryNeedRefresh(): boolean {
    return MeetingRuntimeStore.historyNeedRefresh
  }

  /**
   * 获取发生变化的会议ID
   */
  static getChangedMeetingId(): string {
    return MeetingRuntimeStore.changedMeetingId
  }

  /**
   * 获取变化后的会议状态
   *
   * running：会议仍在进行，但当前用户可能已离开
   * ended：会议已结束
   */
  static getChangedStatus(): string {
    return MeetingRuntimeStore.changedStatus
  }

  /**
   * 首页处理完刷新标记后调用
   */
  static clearHistoryRefreshFlag(): void {
    MeetingRuntimeStore.historyNeedRefresh = false
    MeetingRuntimeStore.changedMeetingId = ''
    MeetingRuntimeStore.changedStatus = ''
  }
}