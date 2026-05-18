package com.zhiyun.meeting.meeting.dto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 加入会议请求参数
 *
 * 用户通过会议号加入会议时提交。
 */
@Schema(description = "加入会议请求参数")
public class JoinMeetingRequest {

    /**
     * 会议号
     *
     * 用户输入的 6 位会议号。
     */
    @Schema(description = "会议号", example = "869497")
    private String meetingNo;

    /**
     * 会议密码
     *
     * 如果会议没有设置密码，可以传空字符串。
     */
    @Schema(description = "会议密码，没有密码时传空字符串", example = "")
    private String password;

    /**
     * 当前加入会议的用户ID
     *
     * 前端不需要传。
     * 后端根据 token 自动设置。
     */
    @JsonIgnore
    @Schema(hidden = true)
    private Long userId;

    /**
     * 入会时是否开启麦克风
     */
    @Schema(description = "入会时是否开启麦克风", example = "true")
    private Boolean micOn;

    /**
     * 入会时是否开启摄像头
     */
    @Schema(description = "入会时是否开启摄像头", example = "true")
    private Boolean cameraOn;

    public JoinMeetingRequest() {
    }

    public String getMeetingNo() {
        return meetingNo;
    }

    public String getPassword() {
        return password;
    }

    public Long getUserId() {
        return userId;
    }

    public Boolean getMicOn() {
        return micOn;
    }

    public Boolean getCameraOn() {
        return cameraOn;
    }

    public void setMeetingNo(String meetingNo) {
        this.meetingNo = meetingNo;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setMicOn(Boolean micOn) {
        this.micOn = micOn;
    }

    public void setCameraOn(Boolean cameraOn) {
        this.cameraOn = cameraOn;
    }
}