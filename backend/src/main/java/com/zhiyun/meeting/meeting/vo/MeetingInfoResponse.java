package com.zhiyun.meeting.meeting.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 会议信息响应数据
 */
@Schema(description = "会议信息响应数据")
public class MeetingInfoResponse {

    @Schema(description = "会议ID", example = "Mf5544e2d088a4daaafabd277f6857fcc")
    private String meetingId;

    @Schema(description = "会议号", example = "869497")
    private String meetingNo;

    @Schema(description = "RTC房间ID", example = "room_869497")
    private String roomId;

    @Schema(description = "会议标题", example = "智云项目会议")
    private String title;

    @Schema(description = "是否需要会议密码", example = "false")
    private Boolean needPassword;

    @Schema(description = "会议状态", example = "running")
    private String status;

    @Schema(description = "主持人用户ID", example = "1")
    private Long hostUserId;

    public MeetingInfoResponse() {
    }

    public MeetingInfoResponse(String meetingId, String meetingNo, String roomId, String title,
                               Boolean needPassword, String status, Long hostUserId) {
        this.meetingId = meetingId;
        this.meetingNo = meetingNo;
        this.roomId = roomId;
        this.title = title;
        this.needPassword = needPassword;
        this.status = status;
        this.hostUserId = hostUserId;
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

    public Boolean getNeedPassword() {
        return needPassword;
    }

    public String getStatus() {
        return status;
    }

    public Long getHostUserId() {
        return hostUserId;
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

    public void setNeedPassword(Boolean needPassword) {
        this.needPassword = needPassword;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setHostUserId(Long hostUserId) {
        this.hostUserId = hostUserId;
    }
}