package com.zhiyun.meeting.meeting.dto;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 创建会议请求参数
 *
 * 前端在创建会议时提交这个对象。
 */
@Schema(description = "创建会议请求参数")
public class CreateMeetingRequest {

    /**
     * 会议标题
     *
     * 为空时后端默认使用“即时会议”。
     */
    @Schema(description = "会议标题", example = "智云项目会议")
    private String title;

    /**
     * 会议密码
     *
     * 可以为空。
     * 为空表示加入会议时不需要密码。
     */
    @Schema(description = "会议密码，可以为空", example = "")
    private String password;

    /**
     * 主持人用户ID
     *
     * 注意：
     * 前端不需要传。
     * 后端会根据 token 自动设置。
     */
    @JsonIgnore
    @Schema(hidden = true)
    private Long hostUserId;

    public CreateMeetingRequest() {
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

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setHostUserId(Long hostUserId) {
        this.hostUserId = hostUserId;
    }
}