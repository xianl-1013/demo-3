package com.zhiyun.meeting.meeting.vo;

public class JoinMeetingResponse {

    private String meetingId;

    private String meetingNo;

    private String roomId;

    private String title;

    private Long userId;

    private String userName;

    private String avatar;

    private String role;

    private Boolean micOn;

    private Boolean cameraOn;

    private String rtcAppId;

    private String rtcRoomId;

    private String rtcUserId;

    private String rtcToken;

    private Integer rtcExpireTime;

    private String signalUrl;

    private String signalToken;

    public JoinMeetingResponse() {
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getMeetingNo() {
        return meetingNo;
    }

    public void setMeetingNo(String meetingNo) {
        this.meetingNo = meetingNo;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getMicOn() {
        return micOn;
    }

    public void setMicOn(Boolean micOn) {
        this.micOn = micOn;
    }

    public Boolean getCameraOn() {
        return cameraOn;
    }

    public void setCameraOn(Boolean cameraOn) {
        this.cameraOn = cameraOn;
    }

    public String getRtcAppId() {
        return rtcAppId;
    }

    public void setRtcAppId(String rtcAppId) {
        this.rtcAppId = rtcAppId;
    }

    public String getRtcRoomId() {
        return rtcRoomId;
    }

    public void setRtcRoomId(String rtcRoomId) {
        this.rtcRoomId = rtcRoomId;
    }

    public String getRtcUserId() {
        return rtcUserId;
    }

    public void setRtcUserId(String rtcUserId) {
        this.rtcUserId = rtcUserId;
    }

    public String getRtcToken() {
        return rtcToken;
    }

    public void setRtcToken(String rtcToken) {
        this.rtcToken = rtcToken;
    }

    public Integer getRtcExpireTime() {
        return rtcExpireTime;
    }

    public void setRtcExpireTime(Integer rtcExpireTime) {
        this.rtcExpireTime = rtcExpireTime;
    }

    public String getSignalUrl() {
        return signalUrl;
    }

    public void setSignalUrl(String signalUrl) {
        this.signalUrl = signalUrl;
    }

    public String getSignalToken() {
        return signalToken;
    }

    public void setSignalToken(String signalToken) {
        this.signalToken = signalToken;
    }
}