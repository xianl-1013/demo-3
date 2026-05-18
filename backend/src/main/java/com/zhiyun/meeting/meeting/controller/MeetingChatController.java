package com.zhiyun.meeting.meeting.controller;

import com.zhiyun.meeting.common.result.Result;
import com.zhiyun.meeting.common.util.CurrentUserUtil;
import com.zhiyun.meeting.meeting.dto.SendChatMessageRequest;
import com.zhiyun.meeting.meeting.service.MeetingChatMessageService;
import com.zhiyun.meeting.meeting.vo.MeetingChatMessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会中聊天接口
 *
 * 负责会议中的文本聊天消息发送和历史消息查询。
 *
 * 当前聊天有两种发送方式：
 * 1. HTTP 接口发送：POST /meeting/chat/send
 * 2. WebSocket 发送：event = chat_message
 *
 * 两种方式最终都会保存到 meeting_chat_message 表。
 */
@Tag(name = "会中聊天接口", description = "会议聊天消息发送、聊天历史查询相关接口")
@RestController
@RequestMapping("/meeting/chat")
public class MeetingChatController {

    private final MeetingChatMessageService meetingChatMessageService;

    public MeetingChatController(MeetingChatMessageService meetingChatMessageService) {
        this.meetingChatMessageService = meetingChatMessageService;
    }

    /**
     * 发送聊天消息
     *
     * 请求地址：
     * POST /meeting/chat/send
     *
     * 注意：
     * fromUserId 不需要前端传。
     * 后端会根据 token 自动设置发送人。
     */
    @Operation(
            summary = "发送聊天消息",
            description = "向指定会议发送一条聊天消息，后端保存到 meeting_chat_message 表，并返回消息完整信息"
    )
    @PostMapping("/send")
    public Result<MeetingChatMessageResponse> sendMessage(@RequestBody SendChatMessageRequest request,
                                                          HttpServletRequest httpRequest) {
        Long currentUserId = CurrentUserUtil.getCurrentUserId(httpRequest);
        request.setFromUserId(currentUserId);

        MeetingChatMessageResponse response = meetingChatMessageService.sendMessage(request);
        return Result.success(response);
    }

    /**
     * 查询聊天历史
     *
     * 请求地址：
     * GET /meeting/chat/list?meetingId=xxx&pageNum=1&pageSize=20
     */
    @Operation(
            summary = "查询聊天历史",
            description = "根据 meetingId 分页查询会议聊天历史记录，按发送时间正序返回"
    )
    @GetMapping("/list")
    public Result<List<MeetingChatMessageResponse>> getMessageList(
            @Parameter(description = "会议ID，不是会议号", required = true)
            @RequestParam String meetingId,

            @Parameter(description = "页码，从 1 开始", example = "1")
            @RequestParam(required = false) Integer pageNum,

            @Parameter(description = "每页条数", example = "20")
            @RequestParam(required = false) Integer pageSize
    ) {
        List<MeetingChatMessageResponse> response =
                meetingChatMessageService.getMessageList(meetingId, pageNum, pageSize);

        return Result.success(response);
    }
}