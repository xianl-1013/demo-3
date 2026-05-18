package com.zhiyun.meeting.signal.manager;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * WebSocket 心跳清理定时任务
 *
 * 作用：
 * 定时检查 WebSocket 连接是否长时间没有心跳。
 * 如果超过指定时间没有任何消息，就主动关闭连接。
 *
 * 说明：
 * 关闭连接后，MeetingWebSocketHandler.afterConnectionClosed 会被触发，
 * 然后广播 member_disconnected。
 */
@Component
public class MeetingWebSocketHeartbeatTask {

    private final MeetingWebSocketSessionManager sessionManager;

    /**
     * 超时时间：90秒
     *
     * 前端建议每30秒发送一次 ping。
     * 后端设置90秒超时，允许丢失1到2次心跳。
     */
    private static final long HEARTBEAT_TIMEOUT_MILLIS = 90 * 1000L;

    public MeetingWebSocketHeartbeatTask(MeetingWebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * 每30秒检查一次无心跳连接
     */
    @Scheduled(fixedDelay = 30 * 1000L)
    public void checkInactiveSessions() {
        sessionManager.closeInactiveSessions(HEARTBEAT_TIMEOUT_MILLIS);
    }
}