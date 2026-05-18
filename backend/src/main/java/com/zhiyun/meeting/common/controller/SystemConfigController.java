package com.zhiyun.meeting.common.controller;

import com.zhiyun.meeting.common.config.AppServerConfig;
import com.zhiyun.meeting.common.result.Result;
import com.zhiyun.meeting.common.vo.SystemConfigResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 系统配置接口
 *
 * 用于给前端返回后端基础地址、WebSocket 地址等配置。
 */
@Tag(name = "系统配置接口", description = "返回后端基础地址和WebSocket地址")
@RestController
@RequestMapping("/system")
public class SystemConfigController {

    private final AppServerConfig appServerConfig;

    public SystemConfigController(AppServerConfig appServerConfig) {
        this.appServerConfig = appServerConfig;
    }

    /**
     * 获取系统地址配置
     *
     * 请求地址：
     * GET /system/config
     */
    @Operation(
            summary = "获取系统地址配置",
            description = "返回 HTTP 基础地址和会议 WebSocket 地址"
    )
    @GetMapping("/config")
    public Result<SystemConfigResponse> getSystemConfig() {
        SystemConfigResponse response = new SystemConfigResponse(
                appServerConfig.getHttpBaseUrl(),
                appServerConfig.getMeetingWebSocketUrl()
        );

        return Result.success(response);
    }
}