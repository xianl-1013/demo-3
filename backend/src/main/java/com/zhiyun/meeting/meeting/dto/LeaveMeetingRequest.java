package com.zhiyun.meeting.meeting.dto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 退出会议请求参数
 *
 * 用户主动退出会议时提交。
 */
@Schema(description = "退出会议请求参数")
public class LeaveMeetingRequest {

    /**
     * 会议ID
     */
    @Schema(description = "会议ID", example = "Mf5544e2d088a4daaafabd277f6857fcc")
    private String meetingId;

    /**
     * 退出会议的用户ID
     *
     * 前端不需要传。
     * 后端根据 token 自动设置。
     */
    @JsonIgnore
    @Schema(hidden = true)
    private Long userId;

    public LeaveMeetingRequest() {
    }

    public String getMeetingId() {
        return meetingId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}