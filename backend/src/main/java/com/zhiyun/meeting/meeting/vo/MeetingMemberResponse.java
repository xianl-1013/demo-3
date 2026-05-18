package com.zhiyun.meeting.meeting.vo;

public class MeetingMemberResponse {

    private Long userId;

    private String userName;

    private String avatar;

    private String role;

    private Boolean micOn;

    private Boolean cameraOn;

    private Boolean online;

    private String joinedAt;

    public MeetingMemberResponse() {
    }

    public MeetingMemberResponse(Long userId, String userName, String avatar, String role,
                                 Boolean micOn, Boolean cameraOn, Boolean online, String joinedAt) {
        this.userId = userId;
        this.userName = userName;
        this.avatar = avatar;
        this.role = role;
        this.micOn = micOn;
        this.cameraOn = cameraOn;
        this.online = online;
        this.joinedAt = joinedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getRole() {
        return role;
    }

    public Boolean getMicOn() {
        return micOn;
    }

    public Boolean getCameraOn() {
        return cameraOn;
    }

    public Boolean getOnline() {
        return online;
    }

    public String getJoinedAt() {
        return joinedAt;
    }
}