package com.zhiyun.meeting.meeting.dto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 会议会控请求参数
 *
 * 这个 DTO 用于接收前端发来的会控操作。
 */
@Schema(description = "会议会控请求参数")
public class MeetingControlRequest {

    /**
     * 会议ID
     *
     * 不是 meetingNo，而是创建会议后返回的 meetingId。
     */
    @Schema(description = "会议ID，不是会议号", example = "Mf5544e2d088a4daaafabd277f6857fcc")
    private String meetingId;

    /**
     * 操作人用户ID
     *
     * 前端不需要传。
     * 后端根据 token 自动设置。
     */
    @JsonIgnore
    @Schema(hidden = true)
    private Long operatorUserId;

    /**
     * 被操作人用户ID
     *
     * 全体静音时可以为空。
     * 静音某人、关闭某人摄像头、移除某人时必须传。
     */
    @Schema(description = "被操作人用户ID，全体静音时可以为空", example = "2")
    private Long targetUserId;

    /**
     * 是否允许成员自己解除静音
     *
     * 全体静音时使用。
     */
    @Schema(description = "是否允许成员自己解除静音，全体静音时使用", example = "false")
    private Boolean allowSelfUnmute;

    /**
     * 麦克风状态
     *
     * muteMember 接口使用。
     * false 表示关闭麦克风。
     * true 表示打开麦克风。
     */
    @Schema(description = "麦克风状态，false关闭，true打开", example = "false")
    private Boolean micOn;

    /**
     * 摄像头状态
     *
     * cameraMember 接口使用。
     * false 表示关闭摄像头。
     * true 表示打开摄像头。
     */
    @Schema(description = "摄像头状态，false关闭，true打开", example = "false")
    private Boolean cameraOn;

    /**
     * 操作原因
     *
     * 例如：主持人移除、违规发言等。
     */
    @Schema(description = "操作原因", example = "主持人移除成员")
    private String reason;

    public MeetingControlRequest() {
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

    public Boolean getAllowSelfUnmute() {
        return allowSelfUnmute;
    }

    public Boolean getMicOn() {
        return micOn;
    }

    public Boolean getCameraOn() {
        return cameraOn;
    }

    public String getReason() {
        return reason;
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

    public void setAllowSelfUnmute(Boolean allowSelfUnmute) {
        this.allowSelfUnmute = allowSelfUnmute;
    }

    public void setMicOn(Boolean micOn) {
        this.micOn = micOn;
    }

    public void setCameraOn(Boolean cameraOn) {
        this.cameraOn = cameraOn;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}