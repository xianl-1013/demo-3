package com.zhiyun.meeting.meeting.controller;

import com.zhiyun.meeting.common.result.Result;
import com.zhiyun.meeting.common.util.CurrentUserUtil;
import com.zhiyun.meeting.meeting.service.MeetingInitDetailService;
import com.zhiyun.meeting.meeting.vo.MeetingInitDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 会议详情初始化接口
 *
 * 用于鸿蒙前端进入会议页时一次性获取首屏数据。
 */
@Tag(name = "会议详情初始化接口", description = "会议页初始化聚合接口")
@RestController
@RequestMapping("/meeting/detail")
public class MeetingInitDetailController {

    private final MeetingInitDetailService meetingInitDetailService;

    public MeetingInitDetailController(MeetingInitDetailService meetingInitDetailService) {
        this.meetingInitDetailService = meetingInitDetailService;
    }

    /**
     * 会议页初始化接口
     *
     * 请求地址：
     * GET /meeting/detail/init?meetingId=xxx&chatPageSize=20
     *
     * 前端调用场景：
     * 用户进入会议页时调用。
     *
     * 返回内容：
     * 1. 当前用户会议状态
     * 2. 在线成员列表
     * 3. 聊天记录列表
     */
    @Operation(
            summary = "会议页初始化",
            description = "进入会议页时一次性获取当前用户状态、在线成员列表、聊天记录"
    )
    @GetMapping("/init")
    public Result<MeetingInitDetailResponse> getInitDetail(
            @Parameter(description = "会议ID，不是会议号", required = true)
            @RequestParam String meetingId,

            @Parameter(description = "聊天记录条数，默认20", example = "20")
            @RequestParam(required = false) Integer chatPageSize,

            HttpServletRequest request
    ) {
        Long currentUserId = CurrentUserUtil.getCurrentUserId(request);

        MeetingInitDetailResponse response =
                meetingInitDetailService.getInitDetail(meetingId, currentUserId, chatPageSize);

        return Result.success(response);
    }
}