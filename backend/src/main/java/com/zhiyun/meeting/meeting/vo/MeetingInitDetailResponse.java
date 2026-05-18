package com.zhiyun.meeting.meeting.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 会议页初始化聚合响应
 *
 * 前端进入会议页时调用一次即可拿到：
 * 1. 当前用户会议状态
 * 2. 在线成员列表
 * 3. 聊天历史列表
 */
@Schema(description = "会议页初始化聚合响应")
public class MeetingInitDetailResponse {

    /**
     * 当前用户在会议中的状态
     */
    @Schema(description = "当前用户在会议中的状态")
    private MeetingCurrentUserStatusResponse currentUserStatus;

    /**
     * 当前会议在线成员列表
     */
    @Schema(description = "当前会议在线成员列表")
    private List<MeetingMemberResponse> memberList;

    /**
     * 聊天记录列表
     *
     * 第一版默认返回第一页聊天记录。
     */
    @Schema(description = "聊天记录列表")
    private List<MeetingChatMessageResponse> chatList;

    public MeetingInitDetailResponse() {
    }

    public MeetingCurrentUserStatusResponse getCurrentUserStatus() {
        return currentUserStatus;
    }

    public List<MeetingMemberResponse> getMemberList() {
        return memberList;
    }

    public List<MeetingChatMessageResponse> getChatList() {
        return chatList;
    }

    public void setCurrentUserStatus(MeetingCurrentUserStatusResponse currentUserStatus) {
        this.currentUserStatus = currentUserStatus;
    }

    public void setMemberList(List<MeetingMemberResponse> memberList) {
        this.memberList = memberList;
    }

    public void setChatList(List<MeetingChatMessageResponse> chatList) {
        this.chatList = chatList;
    }
}