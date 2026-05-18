package com.zhiyun.meeting.meeting.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 会议会控响应结果
 *
 * 这个 VO 会返回给前端，同时也会作为 WebSocket 广播内容的一部分。
 */
@Schema(description = "会议会控响应结果")
public class MeetingControlResponse {

    /**
     * 会控操作ID
     */
    @Schema(description = "会控操作ID", example = "ctrl_abc123")
    private String controlId;

    /**
     * 会议ID
     */
    @Schema(description = "会议ID", example = "Mf5544e2d088a4daaafabd277f6857fcc")
    private String meetingId;

    /**
     * 操作人用户ID
     */
    @Schema(description = "操作人用户ID", example = "1")
    private Long operatorUserId;

    /**
     * 被操作人用户ID
     */
    @Schema(description = "被操作人用户ID", example = "2")
    private Long targetUserId;

    /**
     * 会控事件
     *
     * mute_all：全体静音
     * mute_member：静音某个成员
     * camera_member：关闭某个成员摄像头
     * remove_member：移除成员
     */
    @Schema(description = "会控事件", example = "mute_member")
    private String event;

    /**
     * 操作是否成功
     */
    @Schema(description = "操作是否成功", example = "true")
    private Boolean success;

    /**
     * 返回提示
     */
    @Schema(description = "返回提示", example = "成员麦克风状态修改成功")
    private String message;

    /**
     * 操作时间戳
     */
    @Schema(description = "操作时间戳", example = "1778551200000")
    private Long timestamp;

    public MeetingControlResponse() {
    }

    public MeetingControlResponse(String controlId, String meetingId, Long operatorUserId,
                                  Long targetUserId, String event, Boolean success,
                                  String message, Long timestamp) {
        this.controlId = controlId;
        this.meetingId = meetingId;
        this.operatorUserId = operatorUserId;
        this.targetUserId = targetUserId;
        this.event = event;
        this.success = success;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getControlId() {
        return controlId;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public Long getOperatorUserId() {
        return operatorUserId;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public String getEvent() {
        return event;
    }

    public Boolean getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setControlId(String controlId) {
        this.controlId = controlId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public void setOperatorUserId(Long operatorUserId) {
        this.operatorUserId = operatorUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}