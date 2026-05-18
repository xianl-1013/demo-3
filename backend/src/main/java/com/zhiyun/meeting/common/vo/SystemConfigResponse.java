package com.zhiyun.meeting.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 系统配置响应数据
 *
 * 前端启动时可以调用这个接口，
 * 获取后端 HTTP 地址和 WebSocket 地址。
 */
@Schema(description = "系统配置响应数据")
public class SystemConfigResponse {

    @Schema(description = "HTTP基础地址", example = "http://192.168.1.100:8080")
    private String httpBaseUrl;

    @Schema(description = "会议WebSocket地址", example = "ws://192.168.1.100:8080/ws/meeting")
    private String meetingWsUrl;

    public SystemConfigResponse() {
    }

    public SystemConfigResponse(String httpBaseUrl, String meetingWsUrl) {
        this.httpBaseUrl = httpBaseUrl;
        this.meetingWsUrl = meetingWsUrl;
    }

    public String getHttpBaseUrl() {
        return httpBaseUrl;
    }

    public String getMeetingWsUrl() {
        return meetingWsUrl;
    }

    public void setHttpBaseUrl(String httpBaseUrl) {
        this.httpBaseUrl = httpBaseUrl;
    }

    public void setMeetingWsUrl(String meetingWsUrl) {
        this.meetingWsUrl = meetingWsUrl;
    }
}