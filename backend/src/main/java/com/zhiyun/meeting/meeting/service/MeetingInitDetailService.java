package com.zhiyun.meeting.meeting.service;

import com.zhiyun.meeting.common.exception.BusinessException;
import com.zhiyun.meeting.common.result.ResultCode;
import com.zhiyun.meeting.meeting.vo.MeetingChatMessageResponse;
import com.zhiyun.meeting.meeting.vo.MeetingCurrentUserStatusResponse;
import com.zhiyun.meeting.meeting.vo.MeetingInitDetailResponse;
import com.zhiyun.meeting.meeting.vo.MeetingMemberResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会议页初始化聚合 Service
 *
 * 作用：
 * 前端进入会议页时，不需要分别调用多个接口。
 * 直接调用一个初始化接口即可拿到页面首屏需要的数据。
 */
@Service
public class MeetingInitDetailService {

    private final MeetingRoomService meetingRoomService;

    private final MeetingChatMessageService meetingChatMessageService;

    public MeetingInitDetailService(MeetingRoomService meetingRoomService,
                                    MeetingChatMessageService meetingChatMessageService) {
        this.meetingRoomService = meetingRoomService;
        this.meetingChatMessageService = meetingChatMessageService;
    }

    /**
     * 查询会议页初始化数据
     *
     * @param meetingId     会议ID
     * @param currentUserId 当前登录用户ID，来自 token
     * @param chatPageSize  聊天记录条数
     * @return 初始化聚合数据
     */
    public MeetingInitDetailResponse getInitDetail(String meetingId,
                                                   Long currentUserId,
                                                   Integer chatPageSize) {
        if (meetingId == null || meetingId.trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "会议ID不能为空");
        }

        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未获取到当前登录用户");
        }

        int safeChatPageSize = chatPageSize == null || chatPageSize < 1 ? 20 : chatPageSize;

        // 1. 查询当前用户在会议中的状态
        MeetingCurrentUserStatusResponse currentUserStatus =
                meetingRoomService.getCurrentUserStatus(meetingId, currentUserId);

        // 2. 查询在线成员列表
        List<MeetingMemberResponse> memberList =
                meetingRoomService.getMemberList(meetingId);

        // 3. 查询聊天记录第一页
        List<MeetingChatMessageResponse> chatList =
                meetingChatMessageService.getMessageList(meetingId, 1, safeChatPageSize);

        MeetingInitDetailResponse response = new MeetingInitDetailResponse();
        response.setCurrentUserStatus(currentUserStatus);
        response.setMemberList(memberList);
        response.setChatList(chatList);

        return response;
    }
}