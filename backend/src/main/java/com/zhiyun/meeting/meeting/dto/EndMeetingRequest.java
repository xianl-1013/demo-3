package com.zhiyun.meeting.meeting.dto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 结束会议请求参数
 *
 * 主持人结束整个会议时提交。
 */
@Schema(description = "结束会议请求参数")
public class EndMeetingRequest {

    /**
     * 会议ID
     */
    @Schema(description = "会议ID", example = "Mf5544e2d088a4daaafabd277f6857fcc")
    private String meetingId;

    /**
     * 操作人用户ID
     *
     * 前端不需要传。
     * 后端根据 token 自动设置。
     */
    @JsonIgnore
    @Schema(hidden = true)
    private Long userId;

    public EndMeetingRequest() {
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