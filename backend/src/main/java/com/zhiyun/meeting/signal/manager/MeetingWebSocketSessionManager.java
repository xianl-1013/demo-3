package com.zhiyun.meeting.signal.manager;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import com.zhiyun.meeting.signal.util.WebSocketLogUtil;
/**
 * 会议 WebSocket Session 管理器
 *
 * 作用：
 * 1. 保存会议中的 WebSocket 连接
 * 2. 支持按会议广播消息
 * 3. 支持给指定用户发送消息
 * 4. 记录每个连接最后活跃时间
 * 5. 清理超时无心跳连接
 */
@Component
public class MeetingWebSocketSessionManager {
    /**
     * 关闭某个会议下的所有 WebSocket 连接
     *
     * 使用场景：
     * 主持人结束会议后，后端主动通知所有成员会议已结束，
     * 然后关闭该会议下的全部 WebSocket 连接。
     *
     * @param meetingId 会议ID
     * @param reason 关闭原因
     */
    public void closeMeetingSessions(String meetingId, String reason) {
        Map<Long, WebSocketSession> userSessionMap = meetingSessionMap.get(meetingId);

        if (userSessionMap == null || userSessionMap.isEmpty()) {
            System.out.println("[WebSocket][closeMeetingSessions] no session, meetingId=" + meetingId);
            return;
        }

        List<WebSocketSession> sessions = new ArrayList<>(userSessionMap.values());

        for (WebSocketSession session : sessions) {
            if (session == null || !session.isOpen()) {
                continue;
            }

            try {
                // 标记这是因为会议结束导致的关闭
                // afterConnectionClosed 里会根据这个标记避免重复广播 member_disconnected
                session.getAttributes().put("meetingEnded", true);

                session.close(CloseStatus.NORMAL.withReason(reason));
            } catch (Exception e) {
                System.out.println("[WebSocket][closeMeetingSessions] 关闭连接失败: " + e.getMessage());
            }
        }
    }
    /**
     * meetingId -> userId -> WebSocketSession
     */
    private final Map<String, Map<Long, WebSocketSession>> meetingSessionMap = new ConcurrentHashMap<>();

    /**
     * meetingId -> userId -> lastActiveTime
     *
     * lastActiveTime 是毫秒时间戳。
     * 每次连接建立、收到消息、收到 ping 时都会刷新。
     */
    private final Map<String, Map<Long, Long>> lastActiveTimeMap = new ConcurrentHashMap<>();

    /**
     * 添加 WebSocket 连接
     *
     * 如果同一个会议、同一个用户已经存在旧连接，
     * 先关闭旧连接，再保存新连接。
     *
     * @param meetingId 会议ID
     * @param userId 用户ID
     * @param session 新的 WebSocket 连接
     * @return true 表示这是重连，false 表示第一次连接
     */
    public boolean addSession(String meetingId, Long userId, WebSocketSession session) {
        Map<Long, WebSocketSession> userSessionMap =
                meetingSessionMap.computeIfAbsent(meetingId, key -> new ConcurrentHashMap<>());

        WebSocketSession oldSession = userSessionMap.get(userId);

        boolean reconnect = oldSession != null
                && oldSession.isOpen()
                && !oldSession.getId().equals(session.getId());

        if (reconnect) {
            System.out.println("[WebSocket][addSession] 发现旧连接，准备替换 oldSessionId="
                    + oldSession.getId()
                    + ", newSessionId="
                    + session.getId());

            try {
                oldSession.close(CloseStatus.NORMAL.withReason("replaced by new connection"));
            } catch (Exception e) {
                System.out.println("[WebSocket][addSession] 关闭旧连接失败: " + e.getMessage());
            }
        }

        userSessionMap.put(userId, session);

        touchSession(meetingId, userId);

        System.out.println("[WebSocket][addSession] meetingId="
                + meetingId
                + ", userId="
                + userId
                + ", sessionId="
                + session.getId()
                + ", reconnect="
                + reconnect);

        return reconnect;
    }

    /**
     * 移除 WebSocket 连接
     *
     * 注意：
     * 只有当前关闭的 session 和管理器里保存的 session 是同一个时，才真正移除。
     * 这样可以避免旧连接关闭时，把新连接误删。
     *
     * @param meetingId 会议ID
     * @param userId 用户ID
     * @param closedSession 当前关闭的 WebSocket 连接
     * @return true 表示真正移除了当前连接，false 表示没有移除
     */
    public boolean removeSession(String meetingId, Long userId, WebSocketSession closedSession) {
        Map<Long, WebSocketSession> userSessionMap = meetingSessionMap.get(meetingId);
        if (userSessionMap == null) {
            return false;
        }

        WebSocketSession currentSession = userSessionMap.get(userId);

        if (currentSession == null) {
            return false;
        }

        if (closedSession != null && !currentSession.getId().equals(closedSession.getId())) {
            System.out.println("[WebSocket][removeSession] 当前关闭的是旧连接，不移除新连接 oldSessionId="
                    + closedSession.getId()
                    + ", currentSessionId="
                    + currentSession.getId());
            return false;
        }

        userSessionMap.remove(userId);

        if (userSessionMap.isEmpty()) {
            meetingSessionMap.remove(meetingId);
        }

        Map<Long, Long> activeMap = lastActiveTimeMap.get(meetingId);
        if (activeMap != null) {
            activeMap.remove(userId);

            if (activeMap.isEmpty()) {
                lastActiveTimeMap.remove(meetingId);
            }
        }

        System.out.println("[WebSocket][removeSession] meetingId="
                + meetingId
                + ", userId="
                + userId
                + ", sessionId="
                + (closedSession == null ? "" : closedSession.getId()));

        return true;
    }

    /**
     * 刷新连接活跃时间
     *
     * @param meetingId 会议ID
     * @param userId    用户ID
     */
    public void touchSession(String meetingId, Long userId) {
        if (meetingId == null || userId == null) {
            return;
        }

        lastActiveTimeMap
                .computeIfAbsent(meetingId, key -> new ConcurrentHashMap<>())
                .put(userId, System.currentTimeMillis());
    }

    /**
     * 获取某个连接最后活跃时间
     */
    public Long getLastActiveTime(String meetingId, Long userId) {
        Map<Long, Long> activeMap = lastActiveTimeMap.get(meetingId);
        if (activeMap == null) {
            return null;
        }

        return activeMap.get(userId);
    }

    /**
     * 广播消息给会议内所有在线 WebSocket 连接
     *
     * @param meetingId 会议ID
     * @param message   JSON字符串
     */
    public void broadcastToMeeting(String meetingId, String message) {
        Map<Long, WebSocketSession> userSessionMap = meetingSessionMap.get(meetingId);
        if (userSessionMap == null || userSessionMap.isEmpty()) {
            WebSocketLogUtil.logBroadcast(meetingId, 0, message);
            return;
        }

        WebSocketLogUtil.logBroadcast(meetingId, userSessionMap.size(), message);

        for (Map.Entry<Long, WebSocketSession> entry : userSessionMap.entrySet()) {
            WebSocketSession session = entry.getValue();
            sendMessage(session, message);
        }
    }

    /**
     * 给会议内指定用户发送消息
     *
     * @param meetingId 会议ID
     * @param userId    用户ID
     * @param message   JSON字符串
     */
    public void sendToUser(String meetingId, Long userId, String message) {
        Map<Long, WebSocketSession> userSessionMap = meetingSessionMap.get(meetingId);
        if (userSessionMap == null) {
            WebSocketLogUtil.logSend(meetingId, userId, "", message);
            return;
        }

        WebSocketSession session = userSessionMap.get(userId);

        if (session != null) {
            WebSocketLogUtil.logSend(meetingId, userId, session.getId(), message);
        } else {
            WebSocketLogUtil.logSend(meetingId, userId, "", message);
        }

        sendMessage(session, message);
    }

    /**
     * 清理超时无心跳连接
     *
     * @param timeoutMillis 超时时间，单位毫秒
     */
    public void closeInactiveSessions(long timeoutMillis) {
        long now = System.currentTimeMillis();

        for (Map.Entry<String, Map<Long, WebSocketSession>> meetingEntry : meetingSessionMap.entrySet()) {
            String meetingId = meetingEntry.getKey();
            Map<Long, WebSocketSession> userSessionMap = meetingEntry.getValue();

            for (Map.Entry<Long, WebSocketSession> userEntry : userSessionMap.entrySet()) {
                Long userId = userEntry.getKey();
                WebSocketSession session = userEntry.getValue();

                Long lastActiveTime = getLastActiveTime(meetingId, userId);

                if (lastActiveTime == null) {
                    continue;
                }

                long inactiveTime = now - lastActiveTime;

                if (inactiveTime > timeoutMillis) {
                    System.out.println("[WebSocket][heartbeatTimeout] meetingId="
                            + meetingId
                            + ", userId="
                            + userId
                            + ", inactiveTime="
                            + inactiveTime);

                    closeSession(session, CloseStatus.SESSION_NOT_RELIABLE.withReason("heartbeat timeout"));
                }
            }
        }
    }

    /**
     * 发送文本消息
     */
    private void sendMessage(WebSocketSession session, String message) {
        if (session == null || !session.isOpen()) {
            return;
        }

        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (IOException e) {
            WebSocketLogUtil.logError("", null, session.getId(), "发送失败: " + e.getMessage());
        }
    }

    /**
     * 主动关闭连接
     */
    private void closeSession(WebSocketSession session, CloseStatus status) {
        if (session == null || !session.isOpen()) {
            return;
        }

        try {
            session.close(status);
        } catch (Exception e) {
            System.out.println("[WebSocket][closeSession] " + e.getMessage());
        }
    }
}