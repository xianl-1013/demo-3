package com.zhiyun.meeting.meeting.service;

import com.zhiyun.meeting.common.config.AppServerConfig;
import com.zhiyun.meeting.common.exception.BusinessException;
import com.zhiyun.meeting.common.result.ResultCode;
import com.zhiyun.meeting.meeting.dto.CreateMeetingRequest;
import com.zhiyun.meeting.meeting.dto.EndMeetingRequest;
import com.zhiyun.meeting.meeting.dto.JoinMeetingRequest;
import com.zhiyun.meeting.meeting.dto.LeaveMeetingRequest;
import com.zhiyun.meeting.meeting.entity.MeetingMember;
import com.zhiyun.meeting.meeting.entity.MeetingRoom;
import com.zhiyun.meeting.meeting.repository.MeetingMemberRepository;
import com.zhiyun.meeting.meeting.repository.MeetingRoomRepository;
import com.zhiyun.meeting.meeting.vo.CreateMeetingResponse;
import com.zhiyun.meeting.meeting.vo.JoinMeetingResponse;
import com.zhiyun.meeting.meeting.vo.MeetingCurrentUserStatusResponse;
import com.zhiyun.meeting.meeting.vo.MeetingInfoResponse;
import com.zhiyun.meeting.meeting.vo.MeetingMemberResponse;
import com.zhiyun.meeting.user.entity.SysUser;
import com.zhiyun.meeting.user.repository.SysUserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * 会议基础业务 Service
 *
 * 负责：
 * 1. 创建会议
 * 2. 查询会议
 * 3. 加入会议
 * 4. 退出会议
 * 5. 结束会议
 * 6. 查询成员列表
 * 7. 查询当前用户会议状态
 */
@Service
public class MeetingRoomService {

    private final AppServerConfig appServerConfig;

    private final MeetingRoomRepository meetingRoomRepository;

    private final MeetingMemberRepository meetingMemberRepository;

    private final SysUserRepository sysUserRepository;

    private final Random random = new Random();

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public MeetingRoomService(MeetingRoomRepository meetingRoomRepository,
                              MeetingMemberRepository meetingMemberRepository,
                              SysUserRepository sysUserRepository,
                              AppServerConfig appServerConfig) {
        this.meetingRoomRepository = meetingRoomRepository;
        this.meetingMemberRepository = meetingMemberRepository;
        this.sysUserRepository = sysUserRepository;
        this.appServerConfig = appServerConfig;
    }

    /**
     * 统一处理会议标题
     *
     * 前端传了标题：使用前端标题
     * 前端没传标题：使用默认标题
     */
    private String normalizeMeetingTitle(String title) {
        if (title == null) {
            return "未命名会议";
        }

        String cleanTitle = title.trim();

        if (cleanTitle.isEmpty()) {
            return "未命名会议";
        }

        return cleanTitle;
    }

    /**
     * 统一处理会议密码
     */
    private String normalizePassword(String password) {
        if (password == null) {
            return "";
        }

        return password.trim();
    }

    /**
     * 创建会议
     *
     * hostUserId 不相信前端传参。
     * Controller 会从 token 中获取当前登录用户ID，然后 setHostUserId。
     */
    public CreateMeetingResponse createMeeting(CreateMeetingRequest request) {
        if (request.getHostUserId() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未获取到主持人用户ID");
        }

        SysUser hostUser = sysUserRepository.findById(request.getHostUserId());
        if (hostUser == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "主持人用户不存在");
        }

        String title = normalizeMeetingTitle(request.getTitle());
        String password = normalizePassword(request.getPassword());

        System.out.println("[MeetingRoomService][createMeeting] requestTitle="
                + request.getTitle()
                + ", finalTitle="
                + title
                + ", hostUserId="
                + request.getHostUserId());

        String meetingNo = generateMeetingNo();
        String meetingId = "M" + UUID.randomUUID().toString().replace("-", "");
        String roomId = "room_" + meetingNo;

        LocalDateTime now = LocalDateTime.now();

        MeetingRoom room = new MeetingRoom();
        room.setMeetingId(meetingId);
        room.setMeetingNo(meetingNo);
        room.setRoomId(roomId);
        room.setTitle(title);
        room.setPassword(password);
        room.setHostUserId(request.getHostUserId());
        room.setStatus("running");
        room.setStartTime(now);

        meetingRoomRepository.save(room);

        // 创建会议后，主持人自动加入会议成员表
        MeetingMember hostMember = new MeetingMember();
        hostMember.setMeetingId(meetingId);
        hostMember.setMeetingNo(meetingNo);
        hostMember.setRoomId(roomId);
        hostMember.setUserId(hostUser.getId());
        hostMember.setUserName(hostUser.getUserName());
        hostMember.setAvatar(hostUser.getAvatar());
        hostMember.setRole("host");
        hostMember.setMicOn(true);
        hostMember.setCameraOn(true);
        hostMember.setOnline(true);
        hostMember.setJoinedAt(now);

        meetingMemberRepository.saveOrUpdate(hostMember);

        return new CreateMeetingResponse(
                meetingId,
                meetingNo,
                roomId,
                title,
                password,
                request.getHostUserId(),
                "running",
                now.format(DATE_TIME_FORMATTER)
        );
    }

    /**
     * 根据会议号查询会议信息
     */
    public MeetingInfoResponse getMeetingInfo(String meetingNo) {
        if (meetingNo == null || meetingNo.trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "会议号不能为空");
        }

        MeetingRoom room = meetingRoomRepository.findByMeetingNo(meetingNo.trim());

        if (room == null) {
            return null;
        }

        boolean needPassword = room.getPassword() != null && !room.getPassword().trim().isEmpty();

        return new MeetingInfoResponse(
                room.getMeetingId(),
                room.getMeetingNo(),
                room.getRoomId(),
                room.getTitle(),
                needPassword,
                room.getStatus(),
                room.getHostUserId()
        );
    }

    /**
     * 加入会议
     *
     * userId 不相信前端传参。
     * Controller 会从 token 中获取当前登录用户ID，然后 setUserId。
     */
    public JoinMeetingResponse joinMeeting(JoinMeetingRequest request) {
        if (request.getMeetingNo() == null || request.getMeetingNo().trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "会议号不能为空");
        }

        if (request.getUserId() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未获取到当前登录用户");
        }

        MeetingRoom room = meetingRoomRepository.findByMeetingNo(request.getMeetingNo().trim());
        if (room == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会议不存在");
        }

        if (!"running".equals(room.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "会议已结束，无法加入");
        }

        String roomPassword = room.getPassword();
        if (roomPassword != null && !roomPassword.trim().isEmpty()) {
            String inputPassword = request.getPassword();

            if (inputPassword == null || !roomPassword.equals(inputPassword.trim())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "会议密码错误");
            }
        }

        SysUser user = sysUserRepository.findById(request.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        String role = room.getHostUserId().equals(user.getId()) ? "host" : "participant";

        Boolean micOn = request.getMicOn();
        if (micOn == null) {
            micOn = true;
        }

        Boolean cameraOn = request.getCameraOn();
        if (cameraOn == null) {
            cameraOn = true;
        }

        LocalDateTime now = LocalDateTime.now();

        MeetingMember member = new MeetingMember();
        member.setMeetingId(room.getMeetingId());
        member.setMeetingNo(room.getMeetingNo());
        member.setRoomId(room.getRoomId());
        member.setUserId(user.getId());
        member.setUserName(user.getUserName());
        member.setAvatar(user.getAvatar());
        member.setRole(role);
        member.setMicOn(micOn);
        member.setCameraOn(cameraOn);
        member.setOnline(true);
        member.setJoinedAt(now);

        meetingMemberRepository.saveOrUpdate(member);

        JoinMeetingResponse response = new JoinMeetingResponse();
        response.setMeetingId(room.getMeetingId());
        response.setMeetingNo(room.getMeetingNo());
        response.setRoomId(room.getRoomId());
        response.setTitle(room.getTitle());
        response.setUserId(user.getId());
        response.setUserName(user.getUserName());
        response.setAvatar(user.getAvatar());
        response.setRole(role);
        response.setMicOn(micOn);
        response.setCameraOn(cameraOn);

        // 第一版 RTC 参数先返回模拟值
        // 后面接真实 RTC / WebRTC 服务时再替换
        response.setRtcAppId("demo_rtc_app_id");
        response.setRtcRoomId(room.getRoomId());
        response.setRtcUserId(String.valueOf(user.getId()));
        response.setRtcToken(UUID.randomUUID().toString().replace("-", ""));
        response.setRtcExpireTime(3600);

        // WebSocket 连接地址
        // 前端实际连接时使用：
        // signalUrl + "?meetingId=" + meetingId + "&token=" + 登录token
        response.setSignalUrl(appServerConfig.getMeetingWebSocketUrl());

        // 第一版 WebSocket 使用登录 token 校验
        // signalToken 暂时保留字段，但不参与校验
        response.setSignalToken("");

        return response;
    }

    /**
     * 退出会议
     */
    public void leaveMeeting(LeaveMeetingRequest request) {
        if (request.getMeetingId() == null || request.getMeetingId().trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "会议ID不能为空");
        }

        if (request.getUserId() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未获取到当前登录用户");
        }

        MeetingRoom room = meetingRoomRepository.findByMeetingId(request.getMeetingId());
        if (room == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会议不存在");
        }

        MeetingMember member =
                meetingMemberRepository.findByMeetingIdAndUserId(request.getMeetingId(), request.getUserId());

        if (member == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "当前用户不在该会议中");
        }

        meetingMemberRepository.leaveMeeting(request.getMeetingId(), request.getUserId());
    }

    /**
     * 结束会议
     *
     * 只有主持人可以结束会议。
     */
    public void endMeeting(EndMeetingRequest request) {
        if (request.getMeetingId() == null || request.getMeetingId().trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "会议ID不能为空");
        }

        if (request.getUserId() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未获取到当前登录用户");
        }

        MeetingRoom room = meetingRoomRepository.findByMeetingId(request.getMeetingId());
        if (room == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会议不存在");
        }

        if (!"running".equals(room.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "会议已经结束");
        }

        if (!room.getHostUserId().equals(request.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只有主持人可以结束会议");
        }

        meetingRoomRepository.endMeeting(request.getMeetingId());
        meetingMemberRepository.offlineAllByMeetingId(request.getMeetingId());
    }

    /**
     * 查询会议在线成员列表
     */
    public List<MeetingMemberResponse> getMemberList(String meetingId) {
        if (meetingId == null || meetingId.trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "会议ID不能为空");
        }

        MeetingRoom room = meetingRoomRepository.findByMeetingId(meetingId);
        if (room == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会议不存在");
        }

        List<MeetingMember> members = meetingMemberRepository.findOnlineListByMeetingId(meetingId);
        List<MeetingMemberResponse> responses = new ArrayList<>();

        for (MeetingMember member : members) {
            String joinedAt = "";
            if (member.getJoinedAt() != null) {
                joinedAt = member.getJoinedAt().format(DATE_TIME_FORMATTER);
            }

            responses.add(new MeetingMemberResponse(
                    member.getUserId(),
                    member.getUserName(),
                    member.getAvatar(),
                    member.getRole(),
                    member.getMicOn(),
                    member.getCameraOn(),
                    member.getOnline(),
                    joinedAt
            ));
        }

        return responses;
    }

    /**
     * 查询当前登录用户在会议中的状态
     *
     * currentUserId 必须来自 token。
     */
    public MeetingCurrentUserStatusResponse getCurrentUserStatus(String meetingId, Long currentUserId) {
        if (meetingId == null || meetingId.trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "会议ID不能为空");
        }

        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未获取到当前登录用户");
        }

        MeetingRoom room = meetingRoomRepository.findByMeetingId(meetingId);
        if (room == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会议不存在");
        }

        MeetingMember member =
                meetingMemberRepository.findByMeetingIdAndUserId(meetingId, currentUserId);

        MeetingCurrentUserStatusResponse response = new MeetingCurrentUserStatusResponse();
        response.setMeetingId(room.getMeetingId());
        response.setMeetingNo(room.getMeetingNo());
        response.setRoomId(room.getRoomId());
        response.setTitle(room.getTitle());
        response.setMeetingStatus(room.getStatus());
        response.setUserId(currentUserId);

        boolean isHost = room.getHostUserId() != null && room.getHostUserId().equals(currentUserId);
        response.setHost(isHost);

        if (member == null) {
            response.setJoined(false);
            response.setOnline(false);
            response.setRole(isHost ? "host" : "participant");
            response.setMicOn(false);
            response.setCameraOn(false);
            return response;
        }

        response.setJoined(true);
        response.setOnline(Boolean.TRUE.equals(member.getOnline()));
        response.setRole(member.getRole());
        response.setMicOn(Boolean.TRUE.equals(member.getMicOn()));
        response.setCameraOn(Boolean.TRUE.equals(member.getCameraOn()));

        return response;
    }

    /**
     * 生成 6 位会议号
     */
    private String generateMeetingNo() {
        for (int i = 0; i < 10; i++) {
            int number = 100000 + random.nextInt(900000);
            String meetingNo = String.valueOf(number);

            if (!meetingRoomRepository.existsByMeetingNo(meetingNo)) {
                return meetingNo;
            }
        }

        throw new BusinessException(ResultCode.SERVER_ERROR, "生成会议号失败，请重试");
    }
}