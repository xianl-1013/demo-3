package com.zhiyun.meeting.meeting.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 会议历史列表响应数据
 *
 * 用于前端展示“我创建过 / 我加入过”的会议记录。
 */
@Schema(description = "会议历史列表响应数据")
public class MeetingHistoryResponse {

    @Schema(description = "会议ID", example = "Mf5544e2d088a4daaafabd277f6857fcc")
    private String meetingId;

    @Schema(description = "会议号", example = "869497")
    private String meetingNo;

    @Schema(description = "RTC房间ID", example = "room_869497")
    private String roomId;

    @Schema(description = "会议标题", example = "智云项目会议")
    private String title;

    @Schema(description = "会议状态：running进行中，ended已结束", example = "running")
    private String status;

    @Schema(description = "主持人用户ID", example = "1")
    private Long hostUserId;

    @Schema(description = "当前用户在会议中的角色：host主持人，participant普通成员", example = "host")
    private String myRole;

    @Schema(description = "当前用户是否主持人", example = "true")
    private Boolean host;

    @Schema(description = "当前用户是否仍在线", example = "true")
    private Boolean online;

    @Schema(description = "会议开始时间", example = "2026-05-12 12:06:17")
    private String startTime;

    @Schema(description = "会议结束时间", example = "2026-05-12 13:06:17")
    private String endTime;

    @Schema(description = "当前用户加入时间", example = "2026-05-12 12:10:00")
    private String joinedAt;

    @Schema(description = "当前用户离开时间", example = "2026-05-12 13:00:00")
    private String leftAt;

    public MeetingHistoryResponse() {
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

    public String getStatus() {
        return status;
    }

    public Long getHostUserId() {
        return hostUserId;
    }

    public String getMyRole() {
        return myRole;
    }

    public Boolean getHost() {
        return host;
    }

    public Boolean getOnline() {
        return online;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public String getLeftAt() {
        return leftAt;
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

    public void setStatus(String status) {
        this.status = status;
    }

    public void setHostUserId(Long hostUserId) {
        this.hostUserId = hostUserId;
    }

    public void setMyRole(String myRole) {
        this.myRole = myRole;
    }

    public void setHost(Boolean host) {
        this.host = host;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setJoinedAt(String joinedAt) {


        this.joinedAt = joinedAt;
    }

    public void setLeftAt(String leftAt) {
        this.leftAt = leftAt;
    }
}