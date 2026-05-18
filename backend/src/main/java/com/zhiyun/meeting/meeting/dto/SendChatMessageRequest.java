package com.zhiyun.meeting.meeting.dto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 发送聊天消息请求参数
 *
 * 前端通过 HTTP 发送聊天消息时提交这个对象。
 */
@Schema(description = "发送聊天消息请求参数")
public class SendChatMessageRequest {

    /**
     * 会议ID
     *
     * 注意：
     * 这里传 meetingId，不是 meetingNo。
     */
    @Schema(description = "会议ID，不是会议号", example = "Mf5544e2d088a4daaafabd277f6857fcc")
    private String meetingId;

    /**
     * 发送人用户ID
     *
     * 前端不需要传。
     * 后端根据 token 自动设置。
     */
    @JsonIgnore
    @Schema(hidden = true)
    private Long fromUserId;

    /**
     * 聊天内容
     */
    @Schema(description = "聊天内容", example = "大家好")
    private String content;

    /**
     * 消息类型
     *
     * text：文本消息
     * system：系统消息
     *
     * 第一版主要使用 text。
     */
    @Schema(description = "消息类型：text文本，system系统消息", example = "text")
    private String messageType;

    public SendChatMessageRequest() {
    }

    public String getMeetingId() {
        return meetingId;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public String getContent() {
        return content;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}