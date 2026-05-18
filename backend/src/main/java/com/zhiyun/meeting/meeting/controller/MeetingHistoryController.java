package com.zhiyun.meeting.meeting.controller;

import com.zhiyun.meeting.common.result.PageResult;
import com.zhiyun.meeting.common.result.Result;
import com.zhiyun.meeting.common.util.CurrentUserUtil;
import com.zhiyun.meeting.meeting.service.MeetingHistoryService;
import com.zhiyun.meeting.meeting.vo.MeetingHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 会议历史接口
 *
 * 用于查询当前登录用户的会议历史记录。
 */
@Tag(name = "会议历史接口", description = "查询我创建过、我加入过的会议记录")
@RestController
@RequestMapping("/meeting/history")
public class MeetingHistoryController {

    private final MeetingHistoryService meetingHistoryService;

    public MeetingHistoryController(MeetingHistoryService meetingHistoryService) {
        this.meetingHistoryService = meetingHistoryService;
    }

    /**
     * 查询当前用户会议历史
     *
     * 请求地址：
     * GET /meeting/history/list?type=all&pageNum=1&pageSize=20
     *
     * type：
     * all：全部会议
     * created：我创建的会议
     * joined：我加入的会议，不包含我创建的
     */
    @Operation(
            summary = "查询当前用户会议历史",
            description = "根据当前登录用户 token 查询会议历史，type支持 all、created、joined"
    )
    @GetMapping("/list")
    public Result<PageResult<MeetingHistoryResponse>> getMyMeetingHistory(
            @Parameter(description = "类型：all全部，created我创建的，joined我加入的", example = "all")
            @RequestParam(required = false) String type,

            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(required = false) Integer pageNum,

            @Parameter(description = "每页条数", example = "20")
            @RequestParam(required = false) Integer pageSize,

            HttpServletRequest request
    ) {
        Long currentUserId = CurrentUserUtil.getCurrentUserId(request);

        PageResult<MeetingHistoryResponse> response =
                meetingHistoryService.getMyMeetingHistory(currentUserId, type, pageNum, pageSize);

        return Result.success(response);
    }
}