package com.zhiyun.meeting.meeting.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 聊天消息响应数据
 *
 * 后端返回给前端的聊天消息结构。
 */
@Schema(description = "聊天消息响应数据")
public class MeetingChatMessageResponse {

    /**
     * 消息ID
     */
    @Schema(description = "消息ID", example = "msg_abc123")
    private String messageId;

    /**
     * 会议ID
     */
    @Schema(description = "会议ID", example = "Mf5544e2d088a4daaafabd277f6857fcc")
    private String meetingId;

    /**
     * 会议号
     */
    @Schema(description = "会议号", example = "869497")
    private String meetingNo;

    /**
     * RTC房间ID
     */
    @Schema(description = "RTC房间ID", example = "room_869497")
    private String roomId;

    /**
     * 发送人用户ID
     */
    @Schema(description = "发送人用户ID", example = "1")
    private Long fromUserId;

    /**
     * 发送人昵称
     */
    @Schema(description = "发送人昵称", example = "管理员")
    private String fromUserName;

    /**
     * 发送人头像
     */
    @Schema(description = "发送人头像", example = "")
    private String avatar;

    /**
     * 消息类型
     */
    @Schema(description = "消息类型：text文本，system系统消息", example = "text")
    private String messageType;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容", example = "大家好")
    private String content;

    /**
     * 发送时间
     */
    @Schema(description = "发送时间", example = "2026-05-12 13:30:00")
    private String sendTime;

    public MeetingChatMessageResponse() {
    }

    public MeetingChatMessageResponse(String messageId, String meetingId, String meetingNo, String roomId,
                                      Long fromUserId, String fromUserName, String avatar,
                                      String messageType, String content, String sendTime) {
        this.messageId = messageId;
        this.meetingId = meetingId;
        this.meetingNo = meetingNo;
        this.roomId = roomId;
        this.fromUserId = fromUserId;
        this.fromUserName = fromUserName;
        this.avatar = avatar;
        this.messageType = messageType;
        this.content = content;
        this.sendTime = sendTime;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public String getMeetingNo() {
        return meetingNo;
    }

    public String getRoomId() {
        return roomId;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getContent() {
        return content;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public void setMeetingNo(String meetingNo) {
        this.meetingNo = meetingNo;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }
}