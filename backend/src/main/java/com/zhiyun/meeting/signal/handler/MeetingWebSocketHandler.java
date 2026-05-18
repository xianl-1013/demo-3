package com.zhiyun.meeting.signal.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyun.meeting.meeting.repository.MeetingMemberRepository;
import com.zhiyun.meeting.signal.manager.MeetingWebSocketSessionManager;
import com.zhiyun.meeting.signal.model.WebSocketMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.zhiyun.meeting.meeting.dto.SendChatMessageRequest;
import com.zhiyun.meeting.meeting.service.MeetingChatMessageService;
import com.zhiyun.meeting.meeting.vo.MeetingChatMessageResponse;
import com.zhiyun.meeting.auth.repository.AuthTokenRepository;
import com.zhiyun.meeting.auth.vo.TokenInfo;
import com.zhiyun.meeting.signal.util.WebSocketLogUtil;
@Component
public class MeetingWebSocketHandler extends TextWebSocketHandler {
    private final AuthTokenRepository authTokenRepository;

    private final MeetingWebSocketSessionManager sessionManager;

    private final MeetingMemberRepository meetingMemberRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final MeetingChatMessageService meetingChatMessageService;

    public MeetingWebSocketHandler(MeetingWebSocketSessionManager sessionManager,
                                   MeetingMemberRepository meetingMemberRepository,
                                   MeetingChatMessageService meetingChatMessageService,
                                   AuthTokenRepository authTokenRepository) {
        this.sessionManager = sessionManager;
        this.meetingMemberRepository = meetingMemberRepository;
        this.meetingChatMessageService = meetingChatMessageService;
        this.authTokenRepository = authTokenRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String meetingId = getQueryParam(session, "meetingId");
        String token = getQueryParam(session, "token");

        if (meetingId == null || meetingId.trim().isEmpty()) {
            closeSession(session, CloseStatus.BAD_DATA);
            return;
        }

        if (token == null || token.trim().isEmpty()) {
            closeSession(session, CloseStatus.NOT_ACCEPTABLE.withReason("缺少token"));
            return;
        }

        // WebSocket 不再相信前端传来的 userId
        // 通过 token 查询当前登录用户
        TokenInfo tokenInfo = authTokenRepository.findValidToken(token);

        if (tokenInfo == null) {
            closeSession(session, CloseStatus.NOT_ACCEPTABLE.withReason("token无效或已过期"));
            return;
        }

        Long userId = tokenInfo.getUserId();

        session.getAttributes().put("meetingId", meetingId);
        session.getAttributes().put("userId", userId);

        // 添加连接。如果同一个用户重复连接，会自动替换旧连接。
        boolean reconnect = sessionManager.addSession(meetingId, userId, session);

        // WebSocket 连接成功后，把成员状态更新为在线
        meetingMemberRepository.updateOnlineStatus(meetingId, userId, true);
        WebSocketLogUtil.logConnect(meetingId, userId, session.getId(), reconnect);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("online", true);

        WebSocketMessage message = new WebSocketMessage();
        message.setType("meeting_signal");
        message.setEvent(reconnect ? "member_reconnected" : "member_connected");
        message.setMeetingId(meetingId);
        message.setUserId(userId);
        message.setData(data);
        message.setTimestamp(System.currentTimeMillis());

        broadcast(meetingId, message);

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        String payload = textMessage.getPayload();
        WebSocketMessage message;
        try {
            message = objectMapper.readValue(payload, WebSocketMessage.class);
        } catch (Exception e) {
            sendError(session, "消息格式错误");
            return;
        }

        String meetingId = getSessionMeetingId(session);
        Long userId = getSessionUserId(session);

        if (meetingId == null || userId == null) {
            sendError(session, "连接信息异常");
            return;
        }
        // 收到任意消息，都刷新连接活跃时间
        sessionManager.touchSession(meetingId, userId);

        message.setMeetingId(meetingId);
        message.setUserId(userId);
        message.setTimestamp(System.currentTimeMillis());

        String event = message.getEvent();
        if (event == null || event.trim().isEmpty()) {
            sendError(session, "event不能为空");
            return;
        }
        WebSocketLogUtil.logReceive(meetingId, userId, session.getId(), event, payload);
        switch (event) {
            case "ping":
                handlePing(session, message);
                break;
            case "mic_changed":
                handleMicChanged(message);
                break;
            case "camera_changed":
                handleCameraChanged(message);
                break;
            case "chat_message":
                handleChatMessage(message);
                break;
            case "mute_all":
            case "mute_member":
            case "camera_member":
            case "remove_member":
                // 会控不能直接通过 WebSocket 执行
                // 必须走 HTTP 接口 /meeting/control/*
                // 因为 HTTP 接口会校验操作者是否是主持人

                sendControlError(
                        meetingId,
                        userId,
                        event,
                        "会控操作请调用HTTP接口，不能直接通过WebSocket绕过权限"
                );
                break;
            case "member_joined":
                broadcast(meetingId, message);
                break;
            case "member_left":
                broadcast(meetingId, message);
                break;
            default:
                sendError(session, "未知event: " + event);
                break;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String meetingId = getSessionMeetingId(session);
        Long userId = getSessionUserId(session);

        if (meetingId == null || userId == null) {
            return;
        }

        // 判断这次关闭是否由“会议结束”触发
        boolean meetingEnded = Boolean.TRUE.equals(session.getAttributes().get("meetingEnded"));

        // 只有真正移除了当前连接，才认为用户离线。
        // 如果关闭的是旧连接，不要广播离线，也不要改数据库 online。
        boolean removed = sessionManager.removeSession(meetingId, userId, session);

        if (!removed) {
            return;
        }

        // 如果是会议结束导致的关闭：
        // 1. meetingRoomService.endMeeting 已经把所有成员 offline
        // 2. Controller 已经广播 meeting_ended
        // 所以这里不要再重复广播 member_disconnected
        if (meetingEnded) {
            System.out.println("[WebSocket][closedByMeetingEnded] meetingId="
                    + meetingId
                    + ", userId="
                    + userId
                    + ", status="
                    + status);
            return;
        }

        // 普通断开才更新该用户离线状态
        meetingMemberRepository.updateOnlineStatus(meetingId, userId, false);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("online", false);

        WebSocketMessage message = new WebSocketMessage();
        message.setType("meeting_signal");
        message.setEvent("member_disconnected");
        message.setMeetingId(meetingId);
        message.setUserId(userId);
        message.setData(data);
        message.setTimestamp(System.currentTimeMillis());

        broadcast(meetingId, message);

        WebSocketLogUtil.logClose(
                meetingId,
                userId,
                session.getId(),
                status.getCode(),
                status.getReason(),
                meetingEnded
        );
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        String meetingId = getSessionMeetingId(session);
        Long userId = getSessionUserId(session);

        WebSocketLogUtil.logError(
                meetingId,
                userId,
                session == null ? "" : session.getId(),
                exception == null ? "" : exception.getMessage()
        );
    }

    private void handleMicChanged(WebSocketMessage message) {
        Boolean micOn = getBooleanFromData(message.getData(), "micOn");
        if (micOn != null) {
            meetingMemberRepository.updateMicStatus(message.getMeetingId(), message.getUserId(), micOn);
        }

        message.setType("meeting_signal");
        broadcast(message.getMeetingId(), message);
    }

    private void handleCameraChanged(WebSocketMessage message) {
        Boolean cameraOn = getBooleanFromData(message.getData(), "cameraOn");
        if (cameraOn != null) {
            meetingMemberRepository.updateCameraStatus(message.getMeetingId(), message.getUserId(), cameraOn);
        }

        message.setType("meeting_signal");
        broadcast(message.getMeetingId(), message);
    }
    /**
     * 处理前端心跳 ping
     */
    private void handlePing(WebSocketSession session, WebSocketMessage message) {
        Map<String, Object> data = new HashMap<>();
        data.put("serverTime", System.currentTimeMillis());

        if (message.getData() != null && message.getData().get("clientTime") != null) {
            data.put("clientTime", message.getData().get("clientTime"));
        }

        WebSocketMessage pong = new WebSocketMessage();
        pong.setType("heartbeat");
        pong.setEvent("pong");
        pong.setMeetingId(message.getMeetingId());
        pong.setUserId(message.getUserId());
        pong.setData(data);
        pong.setTimestamp(System.currentTimeMillis());

        try {
            session.sendMessage(new TextMessage(toJson(pong)));

            WebSocketLogUtil.logHeartbeat(
                    message.getMeetingId(),
                    message.getUserId(),
                    session.getId(),
                    "pong"
            );
        } catch (Exception e) {
            WebSocketLogUtil.logError(
                    message.getMeetingId(),
                    message.getUserId(),
                    session.getId(),
                    "pong发送失败: " + e.getMessage()
            );
        }
    }
    /**
     * 处理 WebSocket 聊天消息
     *
     * 前端发送：
     * {
     *   "event": "chat_message",
     *   "data": {
     *     "content": "大家好",
     *     "messageType": "text"
     *   }
     * }
     *
     * 成功：
     * 后端保存到数据库，然后广播 meeting_chat。
     *
     * 失败：
     * 后端只给发送人返回 meeting_chat_error，不广播给其他人。
     */
    private void handleChatMessage(WebSocketMessage message) {
        String content = getStringFromData(message.getData(), "content");
        String messageType = getStringFromData(message.getData(), "messageType");

        if (content == null || content.trim().isEmpty()) {
            sendChatError(
                    message.getMeetingId(),
                    message.getUserId(),
                    "消息内容不能为空"
            );
            return;
        }

        SendChatMessageRequest request = new SendChatMessageRequest();
        request.setMeetingId(message.getMeetingId());
        request.setFromUserId(message.getUserId());
        request.setContent(content);
        request.setMessageType(messageType == null ? "text" : messageType);

        try {
            MeetingChatMessageResponse response = meetingChatMessageService.sendMessage(request);

            Map<String, Object> data = new HashMap<>();
            data.put("messageId", response.getMessageId());
            data.put("meetingId", response.getMeetingId());
            data.put("meetingNo", response.getMeetingNo());
            data.put("roomId", response.getRoomId());
            data.put("fromUserId", response.getFromUserId());
            data.put("fromUserName", response.getFromUserName());
            data.put("avatar", response.getAvatar());
            data.put("messageType", response.getMessageType());
            data.put("content", response.getContent());
            data.put("sendTime", response.getSendTime());

            message.setType("meeting_chat");
            message.setEvent("chat_message");
            message.setData(data);
            message.setTimestamp(System.currentTimeMillis());

            broadcast(message.getMeetingId(), message);
        } catch (Exception e) {
            sendChatError(
                    message.getMeetingId(),
                    message.getUserId(),
                    e.getMessage()
            );
        }
    }
    /**
     * 发送聊天错误消息
     *
     * 注意：
     * 聊天错误只发给当前发送人。
     * 不广播给会议内其他成员。
     *
     * 前端收到：
     * {
     *   "type": "meeting_chat_error",
     *   "event": "chat_error",
     *   "data": {
     *     "message": "会议不存在"
     *   }
     * }
     */
    private void sendChatError(String meetingId, Long userId, String errorMsg) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", errorMsg == null ? "聊天消息发送失败" : errorMsg);

        WebSocketMessage errorMessage = new WebSocketMessage();
        errorMessage.setType("meeting_chat_error");
        errorMessage.setEvent("chat_error");
        errorMessage.setMeetingId(meetingId);
        errorMessage.setUserId(userId);
        errorMessage.setData(data);
        errorMessage.setTimestamp(System.currentTimeMillis());

        sessionManager.sendToUser(meetingId, userId, toJson(errorMessage));
    }
    /**
     * 发送会控错误消息
     *
     * 注意：
     * 会控错误只发给当前操作者。
     * 不广播给会议内其他成员。
     *
     * 前端收到：
     * {
     *   "type": "meeting_control_error",
     *   "event": "control_error",
     *   "data": {
     *     "sourceEvent": "mute_all",
     *     "message": "会控操作请调用HTTP接口，不能直接通过WebSocket绕过权限"
     *   }
     * }
     */
    private void sendControlError(String meetingId, Long userId, String sourceEvent, String errorMsg) {
        Map<String, Object> data = new HashMap<>();
        data.put("sourceEvent", sourceEvent);
        data.put("message", errorMsg == null ? "会控操作失败" : errorMsg);

        WebSocketMessage errorMessage = new WebSocketMessage();
        errorMessage.setType("meeting_control_error");
        errorMessage.setEvent("control_error");
        errorMessage.setMeetingId(meetingId);
        errorMessage.setUserId(userId);
        errorMessage.setData(data);
        errorMessage.setTimestamp(System.currentTimeMillis());

        sessionManager.sendToUser(meetingId, userId, toJson(errorMessage));
    }
    /**
     * 从 data 中读取字符串
     */
    private String getStringFromData(Map<String, Object> data, String key) {
        if (data == null || !data.containsKey(key)) {
            return null;
        }

        Object value = data.get(key);
        if (value == null) {
            return null;
        }

        return String.valueOf(value);
    }

    private void handleMuteAll(WebSocketMessage message) {
        message.setType("meeting_control");
        broadcast(message.getMeetingId(), message);
    }

    private void handleMuteMember(WebSocketMessage message) {
        message.setType("meeting_control");

        Long targetUserId = message.getTargetUserId();
        if (targetUserId != null) {
            sessionManager.sendToUser(message.getMeetingId(), targetUserId, toJson(message));
        }

        broadcast(message.getMeetingId(), message);
    }

    private void handleRemoveMember(WebSocketMessage message) {
        message.setType("meeting_control");

        Long targetUserId = message.getTargetUserId();
        if (targetUserId != null) {
            sessionManager.sendToUser(message.getMeetingId(), targetUserId, toJson(message));
        }

        broadcast(message.getMeetingId(), message);
    }

    private void broadcast(String meetingId, WebSocketMessage message) {
        sessionManager.broadcastToMeeting(meetingId, toJson(message));
    }

    private void sendError(WebSocketSession session, String errorMsg) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", errorMsg);

        WebSocketMessage message = new WebSocketMessage();
        message.setType("error");
        message.setEvent("error");
        message.setData(data);
        message.setTimestamp(System.currentTimeMillis());

        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(toJson(message)));
            } catch (Exception e) {
                System.out.println("[WebSocket][sendError] " + e.getMessage());
            }
        }
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String getSessionMeetingId(WebSocketSession session) {
        Object value = session.getAttributes().get("meetingId");
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    private Long getSessionUserId(WebSocketSession session) {
        Object value = session.getAttributes().get("userId");
        if (value == null) {
            return null;
        }

        try {
            return Long.valueOf(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private String getQueryParam(WebSocketSession session, String key) {
        URI uri = session.getUri();
        if (uri == null || uri.getQuery() == null) {
            return null;
        }

        String query = uri.getQuery();
        String[] pairs = query.split("&");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2 && key.equals(keyValue[0])) {
                return URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
            }
        }

        return null;
    }

    private Long getLongQueryParam(WebSocketSession session, String key) {
        String value = getQueryParam(session, key);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return Long.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean getBooleanFromData(Map<String, Object> data, String key) {
        if (data == null || !data.containsKey(key)) {
            return null;
        }

        Object value = data.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof String) {
            return Boolean.valueOf((String) value);
        }

        return null;
    }

    private void closeSession(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (Exception e) {
            System.out.println("[WebSocket][closeSession] " + e.getMessage());
        }
    }
}