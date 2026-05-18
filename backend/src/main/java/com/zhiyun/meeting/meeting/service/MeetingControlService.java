package com.zhiyun.meeting.meeting.service;

import com.zhiyun.meeting.meeting.dto.MeetingControlRequest;
import com.zhiyun.meeting.meeting.entity.MeetingMember;
import com.zhiyun.meeting.meeting.entity.MeetingRoom;
import com.zhiyun.meeting.meeting.repository.MeetingControlLogRepository;
import com.zhiyun.meeting.meeting.repository.MeetingMemberRepository;
import com.zhiyun.meeting.meeting.repository.MeetingRoomRepository;
import com.zhiyun.meeting.meeting.vo.MeetingControlResponse;
import org.springframework.stereotype.Service;
import com.zhiyun.meeting.common.exception.BusinessException;
import com.zhiyun.meeting.common.result.ResultCode;
import java.util.UUID;

/**
 * 会议会控业务 Service
 *
 * Service 的职责：
 * 1. 校验会议是否存在
 * 2. 校验会议是否还在进行中
 * 3. 校验操作者是否是主持人
 * 4. 更新成员状态
 * 5. 保存会控日志
 */
@Service
public class MeetingControlService {

    private final MeetingRoomRepository meetingRoomRepository;

    private final MeetingMemberRepository meetingMemberRepository;

    private final MeetingControlLogRepository meetingControlLogRepository;

    public MeetingControlService(MeetingRoomRepository meetingRoomRepository,
                                 MeetingMemberRepository meetingMemberRepository,
                                 MeetingControlLogRepository meetingControlLogRepository) {
        this.meetingRoomRepository = meetingRoomRepository;
        this.meetingMemberRepository = meetingMemberRepository;
        this.meetingControlLogRepository = meetingControlLogRepository;
    }

    /**
     * 主持人全体静音
     */
    public MeetingControlResponse muteAll(MeetingControlRequest request) {
        MeetingRoom room = checkHostPermission(request);

        Boolean allowSelfUnmute = request.getAllowSelfUnmute();
        if (allowSelfUnmute == null) {
            allowSelfUnmute = false;
        }

        // 全体静音：默认把其他在线成员 mic_on 改成 false
        meetingMemberRepository.updateAllMicStatusExceptOperator(
                room.getMeetingId(),
                request.getOperatorUserId(),
                false
        );

        String content = "主持人执行全体静音，allowSelfUnmute=" + allowSelfUnmute;

        return saveLogAndBuildResponse(
                room.getMeetingId(),
                request.getOperatorUserId(),
                null,
                "mute_all",
                content,
                "全体静音成功"
        );
    }

    /**
     * 主持人静音某个成员
     */
    public MeetingControlResponse muteMember(MeetingControlRequest request) {
        MeetingRoom room = checkHostPermission(request);

        if (request.getTargetUserId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "被操作人用户ID不能为空");
        }

        MeetingMember targetMember =
                meetingMemberRepository.findByMeetingIdAndUserId(room.getMeetingId(), request.getTargetUserId());

        if (targetMember == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "被操作成员不存在");
        }

        Boolean micOn = request.getMicOn();
        if (micOn == null) {
            micOn = false;
        }

        meetingMemberRepository.updateMicStatus(room.getMeetingId(), request.getTargetUserId(), micOn);

        String content = "主持人修改成员麦克风状态，targetUserId="
                + request.getTargetUserId()
                + "，micOn="
                + micOn;

        return saveLogAndBuildResponse(
                room.getMeetingId(),
                request.getOperatorUserId(),
                request.getTargetUserId(),
                "mute_member",
                content,
                "成员麦克风状态修改成功"
        );
    }

    /**
     * 主持人关闭或打开某个成员摄像头
     */
    public MeetingControlResponse cameraMember(MeetingControlRequest request) {
        MeetingRoom room = checkHostPermission(request);

        if (request.getTargetUserId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "被操作人用户ID不能为空");
        }

        MeetingMember targetMember =
                meetingMemberRepository.findByMeetingIdAndUserId(room.getMeetingId(), request.getTargetUserId());

        if (targetMember == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "被操作成员不存在");
        }

        Boolean cameraOn = request.getCameraOn();
        if (cameraOn == null) {
            cameraOn = false;
        }

        meetingMemberRepository.updateCameraStatus(room.getMeetingId(), request.getTargetUserId(), cameraOn);

        String content = "主持人修改成员摄像头状态，targetUserId="
                + request.getTargetUserId()
                + "，cameraOn="
                + cameraOn;

        return saveLogAndBuildResponse(
                room.getMeetingId(),
                request.getOperatorUserId(),
                request.getTargetUserId(),
                "camera_member",
                content,
                "成员摄像头状态修改成功"
        );
    }

    /**
     * 主持人移除成员
     */
    public MeetingControlResponse removeMember(MeetingControlRequest request) {
        MeetingRoom room = checkHostPermission(request);

        if (request.getTargetUserId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "被移除人用户ID不能为空");
        }

        if (room.getHostUserId().equals(request.getTargetUserId())) {
            throw new RuntimeException("不能移除主持人自己");
        }

        MeetingMember targetMember =
                meetingMemberRepository.findByMeetingIdAndUserId(room.getMeetingId(), request.getTargetUserId());

        if (targetMember == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "被移除成员不存在");
        }

        // 移除成员本质上就是把这个成员 online 改成 0
        meetingMemberRepository.leaveMeeting(room.getMeetingId(), request.getTargetUserId());

        String reason = request.getReason();
        if (reason == null || reason.trim().isEmpty()) {
            reason = "主持人移除成员";
        }

        String content = "主持人移除成员，targetUserId="
                + request.getTargetUserId()
                + "，reason="
                + reason;

        return saveLogAndBuildResponse(
                room.getMeetingId(),
                request.getOperatorUserId(),
                request.getTargetUserId(),
                "remove_member",
                content,
                "成员移除成功"
        );
    }

    /**
     * 校验主持人权限
     * 所有会控操作都必须先走这个方法。
     */
    private MeetingRoom checkHostPermission(MeetingControlRequest request) {
        if (request.getMeetingId() == null || request.getMeetingId().trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "会议ID不能为空");
        }

        if (request.getOperatorUserId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "操作人用户ID不能为空");
        }

        MeetingRoom room = meetingRoomRepository.findByMeetingId(request.getMeetingId());
        if (room == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会议不存在");
        }

        if (!"running".equals(room.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "会议已结束，不能执行会控");
        }

        if (!room.getHostUserId().equals(request.getOperatorUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只有主持人可以执行会控操作");
        }

        return room;
    }

    /**
     * 保存会控日志，并统一构建返回结果
     */
    private MeetingControlResponse saveLogAndBuildResponse(String meetingId,
                                                           Long operatorUserId,
                                                           Long targetUserId,
                                                           String event,
                                                           String content,
                                                           String message) {
        String controlId = "ctrl_" + UUID.randomUUID().toString().replace("-", "");

        meetingControlLogRepository.save(
                controlId,
                meetingId,
                operatorUserId,
                targetUserId,
                event,
                content
        );

        return new MeetingControlResponse(
                controlId,
                meetingId,
                operatorUserId,
                targetUserId,
                event,
                true,
                message,
                System.currentTimeMillis()
        );
    }
}