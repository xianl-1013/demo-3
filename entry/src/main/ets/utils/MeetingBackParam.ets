/**
 * 会议房间页返回会议首页时携带的参数
 */
export class MeetingBackParam {
  /**
   * 是否来自会议房间页
   */
  fromMeetingRoom: boolean = false

  /**
   * 发生变化的会议ID
   */
  meetingId: string = ''

  /**
   * 会议号
   */
  meetingNo: string = ''

  /**
   * 返回动作：
   * left    主动离开会议
   * ended   主持人结束会议
   * removed 被主持人移除
   */
  action: string = ''

  /**
   * 会议状态：
   * running 会议仍在进行
   * ended   会议已结束
   */
  status: string = ''

  /**
   * 当前用户是否在线
   */
  online: boolean = false

  /**
   * 是否刷新历史
   */
  refresh: boolean = true

  /**
   * 防止重复处理旧参数
   */
  timestamp: number = 0
}