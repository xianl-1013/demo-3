package com.zhiyun.meeting.meeting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyun.meeting.common.result.Result;
import com.zhiyun.meeting.common.result.ResultCode;
import com.zhiyun.meeting.common.util.CurrentUserUtil;
import com.zhiyun.meeting.meeting.dto.CreateMeetingRequest;
import com.zhiyun.meeting.meeting.dto.EndMeetingRequest;
import com.zhiyun.meeting.meeting.dto.JoinMeetingRequest;
import com.zhiyun.meeting.meeting.dto.LeaveMeetingRequest;
import com.zhiyun.meeting.meeting.service.MeetingRoomService;
import com.zhiyun.meeting.meeting.vo.CreateMeetingResponse;
import com.zhiyun.meeting.meeting.vo.JoinMeetingResponse;
import com.zhiyun.meeting.meeting.vo.MeetingCurrentUserStatusResponse;
import com.zhiyun.meeting.meeting.vo.MeetingInfoResponse;
import com.zhiyun.meeting.meeting.vo.MeetingMemberResponse;
import com.zhiyun.meeting.signal.manager.MeetingWebSocketSessionManager;
import com.zhiyun.meeting.signal.model.WebSocketMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会议基础接口
 *
 * 负责会议的创建、查询、加入、退出、结束和成员列表查询。
 *
 * 当前接口安全规则：
 * 1. 不相信前端传来的 userId
 * 2. 当前用户身份统一从 token 中获取
 * 3. 业务异常统一交给 GlobalExceptionHandler 处理
 */
@Tag(name = "会议基础接口", description = "会议创建、查询、加入、退出、结束、成员列表相关接口")
@RestController
@RequestMapping("/meeting")
public class MeetingRoomController {

    private final MeetingRoomService meetingRoomService;

    private final MeetingWebSocketSessionManager sessionManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public MeetingRoomController(MeetingRoomService meetingRoomService,
                                 MeetingWebSocketSessionManager sessionManager) {
        this.meetingRoomService = meetingRoomService;
        this.sessionManager = sessionManager;
    }

    /**
     * 创建会议
     *
     * 请求地址：
     * POST /meeting/create
     *
     * 前端请求体：
     * {
     *   "title": "会议名称",
     *   "password": ""
     * }
     *
     * hostUserId 不相信前端传参。
     * 后端会从 token 中获取当前登录用户ID。
     */
    @Operation(
            summary = "创建会议",
            description = "创建一个即时会议，返回 meetingId、meetingNo、roomId、主持人ID、会议状态等信息"
    )
    @PostMapping("/create")
    public Result<CreateMeetingResponse> createMeeting(@RequestBody CreateMeetingRequest request,
                                                       HttpServletRequest httpRequest) {
        Long currentUserId = CurrentUserUtil.getCurrentUserId(httpRequest);
        request.setHostUserId(currentUserId);

        CreateMeetingResponse response = meetingRoomService.createMeeting(request);
        return Result.success(response);
    }

    /**
     * 查询会议信息
     *
     * 请求地址：
     * GET /meeting/info?meetingNo=xxxxxx
     */
    @Operation(
            summary = "查询会议信息",
            description = "根据会议号 meetingNo 查询会议是否存在、是否需要密码、会议状态、主持人ID等信息"
    )
    @GetMapping("/info")
    public Result<MeetingInfoResponse> getMeetingInfo(
            @Parameter(description = "会议号，例如 869497", required = true)
            @RequestParam String meetingNo
    ) {
        MeetingInfoResponse response = meetingRoomService.getMeetingInfo(meetingNo);

        if (response == null) {
            return Result.error(ResultCode.NOT_FOUND, "会议不存在");
        }

        return Result.success(response);
    }

    /**
     * 加入会议
     *
     * 请求地址：
     * POST /meeting/join
     *
     * 前端请求体：
     * {
     *   "meetingNo": "869497",
     *   "password": "",
     *   "micOn": true,
     *   "cameraOn": true
     * }
     *
     * userId 不相信前端传参。
     * 后端会从 token 中获取当前登录用户ID。
     */
    @Operation(
            summary = "加入会议",
            description = "用户加入会议，校验会议号、密码和用户信息，成功后返回 RTC 参数和 WebSocket 信令参数"
    )
    @PostMapping("/join")
    public Result<JoinMeetingResponse> joinMeeting(@RequestBody JoinMeetingRequest request,
                                                   HttpServletRequest httpRequest) {
        Long currentUserId = CurrentUserUtil.getCurrentUserId(httpRequest);
        request.setUserId(currentUserId);

        JoinMeetingResponse response = meetingRoomService.joinMeeting(request);
        return Result.success(response);
    }

    /**
     * 退出会议
     *
     * 请求地址：
     * POST /meeting/leave
     *
     * 前端请求体：
     * {
     *   "meetingId": "Mf5544e2d088a4daaafabd277f6857fcc"
     * }
     *
     * userId 由后端根据 token 自动设置。
     */
    @Operation(
            summary = "退出会议",
            description = "用户主动退出会议，将 meeting_member 中该用户的 online 状态改为 0"
    )
    @PostMapping("/leave")
    public Result<String> leaveMeeting(@RequestBody LeaveMeetingRequest request,
                                       HttpServletRequest httpRequest) {
        Long currentUserId = CurrentUserUtil.getCurrentUserId(httpRequest);
        request.setUserId(currentUserId);

        meetingRoomService.leaveMeeting(request);
        return Result.success("退出会议成功");
    }

    /**
     * 结束会议
     *
     * 请求地址：
     * POST /meeting/end
     *
     * 前端请求体：
     * {
     *   "meetingId": "Mf5544e2d088a4daaafabd277f6857fcc"
     * }
     *
     * userId 由后端根据 token 自动设置。
     */
    @Operation(
            summary = "结束会议",
            description = "主持人结束整个会议，结束后所有成员都会被置为离线，会议状态变为 ended，并通过 WebSocket 通知所有成员退出"
    )
    @PostMapping("/end")
    public Result<String> endMeeting(@RequestBody EndMeetingRequest request,
                                     HttpServletRequest httpRequest) {
        Long currentUserId = CurrentUserUtil.getCurrentUserId(httpRequest);
        request.setUserId(currentUserId);

        meetingRoomService.endMeeting(request);

        // 会议结束后，先广播 meeting_ended
        broadcastMeetingEnded(request.getMeetingId(), currentUserId);

        // 再关闭该会议下所有 WebSocket 连接
        sessionManager.closeMeetingSessions(request.getMeetingId(), "meeting ended");

        return Result.success("会议已结束");
    }

    /**
     * 查询会议成员列表
     *
     * 请求地址：
     * GET /meeting/member/list?meetingId=xxx
     */
    @Operation(
            summary = "查询会议成员列表",
            description = "根据 meetingId 查询当前会议在线成员列表，包含角色、麦克风、摄像头、在线状态等信息"
    )
    @GetMapping("/member/list")
    public Result<List<MeetingMemberResponse>> getMemberList(
            @Parameter(description = "会议ID，不是会议号，例如 Mf5544e2d088a4daaafabd277f6857fcc", required = true)
            @RequestParam String meetingId
    ) {
        List<MeetingMemberResponse> response = meetingRoomService.getMemberList(meetingId);
        return Result.success(response);
    }

    /**
     * 查询当前用户在会议中的状态
     *
     * 请求地址：
     * GET /meeting/my/status?meetingId=xxx
     */
    @Operation(
            summary = "查询当前用户会议状态",
            description = "根据 meetingId 查询当前登录用户在会议中的状态，当前用户身份从 token 获取，不相信前端传 userId"
    )
    @GetMapping("/my/status")
    public Result<MeetingCurrentUserStatusResponse> getCurrentUserStatus(
            @Parameter(description = "会议ID，不是会议号", required = true)
            @RequestParam String meetingId,
            HttpServletRequest httpRequest
    ) {
        Long currentUserId = CurrentUserUtil.getCurrentUserId(httpRequest);

        MeetingCurrentUserStatusResponse response =
                meetingRoomService.getCurrentUserStatus(meetingId, currentUserId);

        return Result.success(response);
    }

    /**
     * 广播会议已结束消息
     *
     * 前端收到 meeting_ended 后，应立即：
     * 1. 停止音视频
     * 2. 关闭 WebSocket
     * 3. 清理本地会议状态
     * 4. 返回会议首页或显示“会议已结束”
     */
    private void broadcastMeetingEnded(String meetingId, Long operatorUserId) {
        Map<String, Object> data = new HashMap<>();
        data.put("meetingId", meetingId);
        data.put("operatorUserId", operatorUserId);
        data.put("message", "会议已结束");

        WebSocketMessage message = new WebSocketMessage();
        message.setType("meeting_control");
        message.setEvent("meeting_ended");
        message.setMeetingId(meetingId);
        message.setUserId(operatorUserId);
        message.setData(data);
        message.setTimestamp(System.currentTimeMillis());

        sessionManager.broadcastToMeeting(meetingId, toJson(message));

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