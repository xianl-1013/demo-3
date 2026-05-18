package com.zhiyun.meeting.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 后端对外访问地址配置
 *
 * 作用：
 * 统一管理 HTTP 地址和 WebSocket 地址。
 *
 * 例如：
 * http://192.168.1.100:8080
 * ws://192.168.1.100:8080/ws/meeting
 */
@Component
public class AppServerConfig {

    @Value("${app.public-host:localhost}")
    private String publicHost;

    @Value("${app.public-port:8080}")
    private Integer publicPort;

    @Value("${app.http-scheme:http}")
    private String httpScheme;

    @Value("${app.ws-scheme:ws}")
    private String wsScheme;

    public String getPublicHost() {
        return publicHost;
    }

    public Integer getPublicPort() {
        return publicPort;
    }

    public String getHttpScheme() {
        return httpScheme;
    }

    public String getWsScheme() {
        return wsScheme;
    }

    /**
     * HTTP 基础地址
     */
    public String getHttpBaseUrl() {
        return httpScheme + "://" + publicHost + ":" + publicPort;
    }

    /**
     * WebSocket 会议地址
     */
    public String getMeetingWebSocketUrl() {
        return wsScheme + "://" + publicHost + ":" + publicPort + "/ws/meeting";
    }
}