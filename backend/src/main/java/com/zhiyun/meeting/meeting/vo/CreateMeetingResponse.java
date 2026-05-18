package com.zhiyun.meeting.meeting.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 创建会议响应数据
 */
@Schema(description = "创建会议响应数据")
public class CreateMeetingResponse {

    @Schema(description = "会议ID", example = "Mf5544e2d088a4daaafabd277f6857fcc")
    private String meetingId;

    @Schema(description = "会议号", example = "869497")
    private String meetingNo;

    @Schema(description = "RTC房间ID", example = "room_869497")
    private String roomId;

    @Schema(description = "会议标题", example = "智云项目会议")
    private String title;

    @Schema(description = "会议密码", example = "")
    private String password;

    @Schema(description = "主持人用户ID", example = "1")
    private Long hostUserId;

    @Schema(description = "会议状态：running进行中，ended已结束", example = "running")
    private String status;

    @Schema(description = "会议开始时间", example = "2026-05-12 12:06:17")
    private String startTime;

    public CreateMeetingResponse() {
    }

    public CreateMeetingResponse(String meetingId, String meetingNo, String roomId, String title,
                                 String password, Long hostUserId, String status, String startTime) {
        this.meetingId = meetingId;
        this.meetingNo = meetingNo;
        this.roomId = roomId;
        this.title = title;
        this.password = password;
        this.hostUserId = hostUserId;
        this.status = status;
        this.startTime = startTime;
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

    public String getTitle() {
        return title;
    }

    public String getPassword() {
        return password;
    }

    public Long getHostUserId() {
        return hostUserId;
    }

    public String getStatus() {
        return status;
    }

    public String getStartTime() {
        return startTime;
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

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setHostUserId(Long hostUserId) {
        this.hostUserId = hostUserId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}