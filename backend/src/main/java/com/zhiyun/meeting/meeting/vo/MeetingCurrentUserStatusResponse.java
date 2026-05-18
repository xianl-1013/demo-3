package com.zhiyun.meeting.meeting.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 当前用户在会议中的状态响应数据
 *
 * 前端进入会议页后，可以调用这个接口判断：
 * 1. 当前用户是否已加入会议
 * 2. 当前用户是否主持人
 * 3. 当前用户麦克风 / 摄像头状态
 * 4. 当前会议是否还在进行中
 */
@Data
@Schema(description = "当前用户在会议中的状态响应数据")
public class MeetingCurrentUserStatusResponse {

    @Schema(description = "会议ID", example = "Mf5544e2d088a4daaafabd277f6857fcc")
    private String meetingId;

    @Schema(description = "会议号", example = "869497")
    private String meetingNo;

    @Schema(description = "RTC房间ID", example = "room_869497")
    private String roomId;

    @Schema(description = "会议标题", example = "智云项目会议")
    private String title;

    @Schema(description = "会议状态：running进行中，ended已结束", example = "running")
    private String meetingStatus;

    @Schema(description = "当前登录用户ID", example = "1")
    private Long userId;

    @Schema(description = "当前用户是否已加入会议", example = "true")
    private Boolean joined;

    @Schema(description = "当前用户是否在线", example = "true")
    private Boolean online;

    @Schema(description = "当前用户角色：host主持人，participant普通成员", example = "host")
    private String role;

    @Schema(description = "当前用户是否主持人", example = "true")
    private Boolean host;

    @Schema(description = "当前用户麦克风是否开启", example = "true")
    private Boolean micOn;

    @Schema(description = "当前用户摄像头是否开启", example = "true")
    private Boolean cameraOn;


}