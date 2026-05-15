/**
 * 创建会议请求参数
 *
 * 注意：
 * hostUserId 不需要前端传。
 * 后端会根据 token 自动识别主持人。
 */
export interface CreateMeetingRequest {
  title: string
  password: string
}

/**
 * 创建会议响应数据
 */
export interface CreateMeetingResponse {
  meetingId: string
  meetingNo: string
  roomId: string
  title: string
  password: string
  hostUserId: number
  status: string
  startTime: string
}

/**
 * 加入会议请求参数
 *
 * 注意：
 * userId 不需要前端传。
 * 后端会根据 token 自动识别当前用户。
 */
export interface JoinMeetingRequest {
  meetingNo: string
  password: string
  micOn: boolean
  cameraOn: boolean
}

/**
 * 加入会议响应数据
 */
export interface JoinMeetingResponse {
  meetingId: string
  meetingNo: string
  roomId: string
  title: string
  userId: number
  userName: string
  avatar: string
  role: string
  micOn: boolean
  cameraOn: boolean
  rtcAppId: string
  rtcRoomId: string
  rtcUserId: string
  rtcToken: string
  rtcExpireTime: number
  signalUrl: string
  signalToken: string
}

/**
 * 退出会议请求参数
 */
export interface LeaveMeetingRequest {
  meetingId: string
}

/**
 * 结束会议请求参数
 */
export interface EndMeetingRequest {
  meetingId: string
}

/**
 * 会议成员
 */
export interface MeetingMemberResponse {
  userId: number
  userName: string
  avatar: string
  role: string
  micOn: boolean
  cameraOn: boolean
  online: boolean
  joinedAt: string
}

/**
 * 当前用户会议状态
 */
export interface MeetingCurrentUserStatusResponse {
  meetingId: string
  meetingNo: string
  roomId: string
  title: string
  meetingStatus: string
  userId: number
  joined: boolean
  online: boolean
  role: string
  host: boolean
  micOn: boolean
  cameraOn: boolean
}

/**
 * 聊天消息
 */
export interface MeetingChatMessageResponse {
  messageId: string
  meetingId: string
  meetingNo: string
  roomId: string
  fromUserId: number
  fromUserName: string
  avatar: string
  messageType: string
  content: string
  sendTime: string
}

/**
 * 会议初始化聚合数据
 */
export interface MeetingInitDetailResponse {
  currentUserStatus: MeetingCurrentUserStatusResponse
  memberList: MeetingMemberResponse[]
  chatList: MeetingChatMessageResponse[]
}

/**
 * 分页返回
 */
export interface PageResult<T> {
  pageNum: number
  pageSize: number
  total: number
  rows: T[]
}

/**
 * 会议历史记录
 */
export interface MeetingHistoryResponse {
  meetingId: string
  meetingNo: string
  roomId: string
  title: string
  status: string
  hostUserId: number
  myRole: string
  host: boolean
  online: boolean
  startTime: string
  endTime: string
  joinedAt: string
  leftAt: string
}
/**
 * 会控响应数据
 */
export interface MeetingControlResponse {
  controlId: string
  meetingId: string
  operatorUserId: number
  targetUserId: number
  event: string
  success: boolean
  message: string
  timestamp: number
}

/**
 * 静音成员请求参数
 */
export interface MuteMemberRequest {
  meetingId: string
  targetUserId: number
  micOn: boolean
}

/**
 * 摄像头会控请求参数
 */
export interface CameraMemberRequest {
  meetingId: string
  targetUserId: number
  cameraOn: boolean
}

/**
 * 移除成员请求参数
 */
export interface RemoveMemberRequest {
  meetingId: string
  targetUserId: number
  reason: string
}
/**
 * 会议信息响应
 *
 * 对应后端：
 * GET /meeting/info?meetingNo=xxx
 */
export interface MeetingInfoResponse {
  meetingId: string
  meetingNo: string
  roomId: string
  title: string
  needPassword: boolean
  status: string
  hostUserId: number
}