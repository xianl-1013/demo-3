package com.zhiyun.meeting.meeting.entity;

import java.time.LocalDateTime;

public class MeetingChatMessage {

    private Long id;

    private String messageId;

    private String meetingId;

    private String meetingNo;

    private String roomId;

    private Long fromUserId;

    private String fromUserName;

    private String avatar;

    private String messageType;

    private String content;

    private LocalDateTime sendTime;

    private LocalDateTime createTime;

    public MeetingChatMessage() {
    }

    public Long getId() {
        return id;
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

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}