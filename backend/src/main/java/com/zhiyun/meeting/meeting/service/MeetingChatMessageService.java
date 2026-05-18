package com.zhiyun.meeting.meeting.service;

import com.zhiyun.meeting.common.exception.BusinessException;
import com.zhiyun.meeting.common.result.ResultCode;
import com.zhiyun.meeting.meeting.dto.SendChatMessageRequest;
import com.zhiyun.meeting.meeting.entity.MeetingChatMessage;
import com.zhiyun.meeting.meeting.entity.MeetingRoom;
import com.zhiyun.meeting.meeting.repository.MeetingChatMessageRepository;
import com.zhiyun.meeting.meeting.repository.MeetingRoomRepository;
import com.zhiyun.meeting.meeting.vo.MeetingChatMessageResponse;
import com.zhiyun.meeting.user.entity.SysUser;
import com.zhiyun.meeting.user.repository.SysUserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 会议聊天消息业务 Service
 *
 * 负责：
 * 1. 发送会议聊天消息
 * 2. 保存聊天消息到数据库
 * 3. 查询会议聊天历史
 *
 * 注意：
 * fromUserId 不相信前端传参。
 * Controller 会从 token 中获取当前登录用户ID，然后 setFromUserId。
 */
@Service
public class MeetingChatMessageService {

    private final MeetingChatMessageRepository meetingChatMessageRepository;

    private final MeetingRoomRepository meetingRoomRepository;

    private final SysUserRepository sysUserRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public MeetingChatMessageService(MeetingChatMessageRepository meetingChatMessageRepository,
                                     MeetingRoomRepository meetingRoomRepository,
                                     SysUserRepository sysUserRepository) {
        this.meetingChatMessageRepository = meetingChatMessageRepository;
        this.meetingRoomRepository = meetingRoomRepository;
        this.sysUserRepository = sysUserRepository;
    }

    /**
     * 发送聊天消息
     *
     * @param request 发送聊天请求参数
     * @return 已保存的聊天消息
     */
    public MeetingChatMessageResponse sendMessage(SendChatMessageRequest request) {
        if (request.getMeetingId() == null || request.getMeetingId().trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "会议ID不能为空");
        }

        if (request.getFromUserId() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未获取到发送人ID");
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "消息内容不能为空");
        }

        MeetingRoom room = meetingRoomRepository.findByMeetingId(request.getMeetingId());
        if (room == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会议不存在");
        }

        if (!"running".equals(room.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "会议已结束，不能发送消息");
        }

        SysUser user = sysUserRepository.findById(request.getFromUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "发送人不存在");
        }

        String messageType = request.getMessageType();
        if (messageType == null || messageType.trim().isEmpty()) {
            messageType = "text";
        }

        LocalDateTime now = LocalDateTime.now();

        MeetingChatMessage message = new MeetingChatMessage();
        message.setMessageId("msg_" + UUID.randomUUID().toString().replace("-", ""));
        message.setMeetingId(room.getMeetingId());
        message.setMeetingNo(room.getMeetingNo());
        message.setRoomId(room.getRoomId());
        message.setFromUserId(user.getId());
        message.setFromUserName(user.getUserName());
        message.setAvatar(user.getAvatar());
        message.setMessageType(messageType);
        message.setContent(request.getContent());
        message.setSendTime(now);

        meetingChatMessageRepository.save(message);

        return toResponse(message);
    }

    /**
     * 查询会议聊天历史
     *
     * @param meetingId 会议ID
     * @param pageNum   页码
     * @param pageSize  每页条数
     * @return 聊天消息列表
     */
    public List<MeetingChatMessageResponse> getMessageList(String meetingId, Integer pageNum, Integer pageSize) {
        if (meetingId == null || meetingId.trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "会议ID不能为空");
        }

        MeetingRoom room = meetingRoomRepository.findByMeetingId(meetingId);
        if (room == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会议不存在");
        }

        List<MeetingChatMessage> list =
                meetingChatMessageRepository.findListByMeetingId(meetingId, pageNum, pageSize);

        List<MeetingChatMessageResponse> responses = new ArrayList<>();

        for (MeetingChatMessage message : list) {
            responses.add(toResponse(message));
        }

        return responses;
    }

    /**
     * 实体对象转响应对象
     */
    private MeetingChatMessageResponse toResponse(MeetingChatMessage message) {
        String sendTime = "";
        if (message.getSendTime() != null) {
            sendTime = message.getSendTime().format(DATE_TIME_FORMATTER);
        }

        return new MeetingChatMessageResponse(
                message.getMessageId(),
                message.getMeetingId(),
                message.getMeetingNo(),
                message.getRoomId(),
                message.getFromUserId(),
                message.getFromUserName(),
                message.getAvatar(),
                message.getMessageType(),
                message.getContent(),
                sendTime
        );
    }
}