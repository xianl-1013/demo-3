import { ApiClient } from './ApiClient'
import { ApiResult } from './model/ApiModels'
import {
  CreateMeetingRequest,
  CreateMeetingResponse,
  EndMeetingRequest,
  JoinMeetingRequest,
  JoinMeetingResponse,
  LeaveMeetingRequest,
  MeetingCurrentUserStatusResponse,
  MeetingHistoryResponse,
  MeetingInitDetailResponse,
  MeetingMemberResponse,
  PageResult,
  CameraMemberRequest,
  MeetingControlResponse,
  MuteMemberRequest,
  RemoveMemberRequest,
  MeetingInfoResponse
} from './model/MeetingModels'

/**
 * 会议接口封装
 */
export class MeetingApi {
  /**
   * 创建会议
   *
   * 对应后端：
   * POST /meeting/create
   */
  static async createMeeting(
    title: string,
    password: string
  ): Promise<ApiResult<CreateMeetingResponse>> {
    const bodyObj: CreateMeetingRequest = {
      title: title,
      password: password
    }

    console.info('[MeetingApi][createMeeting] body=' + JSON.stringify(bodyObj))

    return ApiClient.post<CreateMeetingResponse>(
      '/meeting/create',
      JSON.stringify(bodyObj)
    )
  }

  /**
   * 加入会议
   *
   * 对应后端：
   * POST /meeting/join
   */
  static async joinMeeting(
    meetingNo: string,
    password: string,
    micOn: boolean,
    cameraOn: boolean
  ): Promise<ApiResult<JoinMeetingResponse>> {
    const bodyObj: JoinMeetingRequest = {
      meetingNo: meetingNo,
      password: password,
      micOn: micOn,
      cameraOn: cameraOn
    }

    return ApiClient.post<JoinMeetingResponse>(
      '/meeting/join',
      JSON.stringify(bodyObj)
    )
  }

  /**
   * 退出会议
   *
   * 对应后端：
   * POST /meeting/leave
   */
  static async leaveMeeting(meetingId: string): Promise<ApiResult<string>> {
    const bodyObj: LeaveMeetingRequest = {
      meetingId: meetingId
    }

    return ApiClient.post<string>(
      '/meeting/leave',
      JSON.stringify(bodyObj)
    )
  }

  /**
   * 结束会议
   *
   * 对应后端：
   * POST /meeting/end
   */
  static async endMeeting(meetingId: string): Promise<ApiResult<string>> {
    const bodyObj: EndMeetingRequest = {
      meetingId: meetingId
    }

    return ApiClient.post<string>(
      '/meeting/end',
      JSON.stringify(bodyObj)
    )
  }

  /**
   * 查询成员列表
   *
   * 对应后端：
   * GET /meeting/member/list
   */
  static async getMemberList(meetingId: string): Promise<ApiResult<MeetingMemberResponse[]>> {
    return ApiClient.get<MeetingMemberResponse[]>(
      '/meeting/member/list?meetingId=' + encodeURIComponent(meetingId)
    )
  }

  /**
   * 查询当前用户会议状态
   *
   * 对应后端：
   * GET /meeting/my/status
   */
  static async getMyStatus(meetingId: string): Promise<ApiResult<MeetingCurrentUserStatusResponse>> {
    return ApiClient.get<MeetingCurrentUserStatusResponse>(
      '/meeting/my/status?meetingId=' + encodeURIComponent(meetingId)
    )
  }

  /**
   * 会议页初始化聚合接口
   *
   * 对应后端：
   * GET /meeting/detail/init
   */
  static async getMeetingInitDetail(
    meetingId: string,
    chatPageSize: number
  ): Promise<ApiResult<MeetingInitDetailResponse>> {
    return ApiClient.get<MeetingInitDetailResponse>(
      '/meeting/detail/init?meetingId='
        + encodeURIComponent(meetingId)
        + '&chatPageSize='
        + chatPageSize
    )
  }
  /**
   * 查询会议信息
   *
   * 对应后端：
   * GET /meeting/info?meetingNo=xxx
   */
  static async getMeetingInfo(meetingNo: string): Promise<ApiResult<MeetingInfoResponse>> {
    return ApiClient.get<MeetingInfoResponse>(
      '/meeting/info?meetingNo=' + encodeURIComponent(meetingNo)
    )
  }
  /**
   * 查询会议历史
   *
   * type:
   * all 全部
   * created 我创建的
   * joined 我加入的
   */
  static async getHistoryList(
    type: string,
    pageNum: number,
    pageSize: number
  ): Promise<ApiResult<PageResult<MeetingHistoryResponse>>> {
    return ApiClient.get<PageResult<MeetingHistoryResponse>>(
      '/meeting/history/list?type='
        + encodeURIComponent(type)
        + '&pageNum='
        + pageNum
        + '&pageSize='
        + pageSize
    )
  }
  /**
   * 静音 / 取消静音成员
   *
   * 对应后端：
   * POST /meeting/control/muteMember
   */
  static async muteMember(
    meetingId: string,
    targetUserId: number,
    micOn: boolean
  ): Promise<ApiResult<MeetingControlResponse>> {
    const bodyObj: MuteMemberRequest = {
      meetingId: meetingId,
      targetUserId: targetUserId,
      micOn: micOn
    }

    return ApiClient.post<MeetingControlResponse>(
      '/meeting/control/muteMember',
      JSON.stringify(bodyObj)
    )
  }

  /**
   * 关闭 / 打开成员摄像头
   *
   * 对应后端：
   * POST /meeting/control/cameraMember
   */
  static async cameraMember(
    meetingId: string,
    targetUserId: number,
    cameraOn: boolean
  ): Promise<ApiResult<MeetingControlResponse>> {
    const bodyObj: CameraMemberRequest = {
      meetingId: meetingId,
      targetUserId: targetUserId,
      cameraOn: cameraOn
    }

    return ApiClient.post<MeetingControlResponse>(
      '/meeting/control/cameraMember',
      JSON.stringify(bodyObj)
    )
  }

  /**
   * 移除成员
   *
   * 对应后端：
   * POST /meeting/control/removeMember
   */
  static async removeMember(
    meetingId: string,
    targetUserId: number,
    reason: string
  ): Promise<ApiResult<MeetingControlResponse>> {
    const bodyObj: RemoveMemberRequest = {
      meetingId: meetingId,
      targetUserId: targetUserId,
      reason: reason
    }

    return ApiClient.post<MeetingControlResponse>(
      '/meeting/control/removeMember',
      JSON.stringify(bodyObj)
    )
  }
}