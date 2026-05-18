package com.zhiyun.meeting.meeting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyun.meeting.common.result.Result;
import com.zhiyun.meeting.meeting.dto.MeetingControlRequest;
import com.zhiyun.meeting.meeting.service.MeetingControlService;
import com.zhiyun.meeting.meeting.vo.MeetingControlResponse;
import com.zhiyun.meeting.signal.manager.MeetingWebSocketSessionManager;
import com.zhiyun.meeting.signal.model.WebSocketMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import com.zhiyun.meeting.common.util.CurrentUserUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 会议会控接口
 *
 * 会控统一走 HTTP 接口。
 *
 * 原因：
 * 1. HTTP 接口方便做权限校验
 * 2. 校验通过后再通过 WebSocket 广播给会议内成员
 * 3. 避免普通成员直接通过 WebSocket 伪造会控消息
 */
@Tag(name = "会议会控接口", description = "主持人全体静音、静音成员、关闭摄像头、移除成员相关接口")
@RestController
@RequestMapping("/meeting/control")
public class MeetingControlController {

    private final MeetingControlService meetingControlService;

    private final MeetingWebSocketSessionManager sessionManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public MeetingControlController(MeetingControlService meetingControlService,
                                    MeetingWebSocketSessionManager sessionManager) {
        this.meetingControlService = meetingControlService;
        this.sessionManager = sessionManager;
    }

    /**
     * 全体静音
     *
     * 请求地址：
     * POST /meeting/control/muteAll
     *
     * 前端调用场景：
     * 主持人在成员列表或更多面板中点击“全体静音”。
     */
    @Operation(
            summary = "全体静音",
            description = "主持人对会议内所有在线成员执行全体静音，默认不静音主持人自己"
    )
    @PostMapping("/muteAll")
    public Result<MeetingControlResponse> muteAll(@RequestBody MeetingControlRequest request,
                                                  HttpServletRequest httpRequest) {
        // 不再相信前端传来的 operatorUserId
        // 操作人以后端 token 识别出的当前用户为准
        Long currentUserId = CurrentUserUtil.getCurrentUserId(httpRequest);
        request.setOperatorUserId(currentUserId);

        MeetingControlResponse response = meetingControlService.muteAll(request);

        // 会控成功后，通过 WebSocket 广播给会议内所有在线成员
        broadcastControl(response, buildData(response, request));

        return Result.success(response);
    }

    /**
     * 静音某个成员
     *
     * 请求地址：
     * POST /meeting/control/muteMember
     *
     * 前端调用场景：
     * 主持人在成员列表中点击某个成员的麦克风开关。
     */
    @Operation(
            summary = "静音或取消静音某个成员",
            description = "主持人修改指定成员的麦克风状态，targetUserId 为被操作成员ID"
    )
    @PostMapping("/muteMember")
    public Result<MeetingControlResponse> muteMember(@RequestBody MeetingControlRequest request,
                                                     HttpServletRequest httpRequest) {
        // 不再相信前端传来的 operatorUserId
        Long currentUserId = CurrentUserUtil.getCurrentUserId(httpRequest);
        request.setOperatorUserId(currentUserId);

        MeetingControlResponse response = meetingControlService.muteMember(request);

        // 会控成功后，广播给会议内所有成员
        broadcastControl(response, buildData(response, request));

        return Result.success(response);
    }

    /**
     * 关闭或打开某个成员摄像头
     *
     * 请求地址：
     * POST /meeting/control/cameraMember
     *
     * 前端调用场景：
     * 主持人在成员列表中点击某个成员的摄像头开关。
     */
    @Operation(
            summary = "关闭或打开某个成员摄像头",
            description = "主持人修改指定成员的摄像头状态，targetUserId 为被操作成员ID"
    )
    @PostMapping("/cameraMember")
    public Result<MeetingControlResponse> cameraMember(@RequestBody MeetingControlRequest request,
                                                       HttpServletRequest httpRequest) {
        // 不再相信前端传来的 operatorUserId
        Long currentUserId = CurrentUserUtil.getCurrentUserId(httpRequest);
        request.setOperatorUserId(currentUserId);

        MeetingControlResponse response = meetingControlService.cameraMember(request);

        // 会控成功后，广播给会议内所有成员
        broadcastControl(response, buildData(response, request));

        return Result.success(response);
    }

    /**
     * 移除成员
     *
     * 请求地址：
     * POST /meeting/control/removeMember
     *
     * 前端调用场景：
     * 主持人在成员列表中点击“移除成员”。
     */
    @Operation(
            summary = "移除成员",
            description = "主持人将指定成员移出会议，被移除成员的 online 状态会被置为 false"
    )
    @PostMapping("/removeMember")
    public Result<MeetingControlResponse> removeMember(@RequestBody MeetingControlRequest request,
                                                       HttpServletRequest httpRequest) {
        // 不再相信前端传来的 operatorUserId
        Long currentUserId = CurrentUserUtil.getCurrentUserId(httpRequest);
        request.setOperatorUserId(currentUserId);

        MeetingControlResponse response = meetingControlService.removeMember(request);

        // 会控成功后，广播给会议内所有成员
        broadcastControl(response, buildData(response, request));

        return Result.success(response);
    }

    /**
     * 构建 WebSocket 广播 data
     *
     * 不同会控事件会携带不同字段。
     * 前端可以根据 event 判断如何处理。
     */
    private Map<String, Object> buildData(MeetingControlResponse response, MeetingControlRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("controlId", response.getControlId());
        data.put("operatorUserId", response.getOperatorUserId());
        data.put("targetUserId", response.getTargetUserId());
        data.put("event", response.getEvent());
        data.put("message", response.getMessage());
        data.put("success", response.getSuccess());
        data.put("timestamp", response.getTimestamp());

        // 不同会控事件可能携带不同参数
        data.put("allowSelfUnmute", request.getAllowSelfUnmute());
        data.put("micOn", request.getMicOn());
        data.put("cameraOn", request.getCameraOn());
        data.put("reason", request.getReason());

        return data;
    }

    /**
     * 广播会控消息
     *
     * 前端会议页只需要监听：
     * type = meeting_control
     */
    private void broadcastControl(MeetingControlResponse response, Map<String, Object> data) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType("meeting_control");
        message.setEvent(response.getEvent());
        message.setMeetingId(response.getMeetingId());
        message.setUserId(response.getOperatorUserId());
        message.setTargetUserId(response.getTargetUserId());
        message.setData(data);
        message.setTimestamp(System.currentTimeMillis());

        sessionManager.broadcastToMeeting(response.getMeetingId(), toJson(message));
    }

    /**
     * Java 对象转 JSON 字符串
     */
    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            return "{}";
        }
    }
}