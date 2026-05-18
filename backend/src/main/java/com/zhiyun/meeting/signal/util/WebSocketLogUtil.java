package com.zhiyun.meeting.signal.util;

/**
 * WebSocket 日志工具类
 *
 * 作用：
 * 1. 统一 WebSocket 日志格式
 * 2. 自动截断过长消息
 * 3. 自动脱敏 token
 * 4. 方便后面鸿蒙前端联调排查问题
 */
public class WebSocketLogUtil {

    /**
     * WebSocket 日志最大打印长度
     */
    private static final int MAX_LOG_LENGTH = 2000;

    /**
     * 打印连接日志
     */
    public static void logConnect(String meetingId,
                                  Long userId,
                                  String sessionId,
                                  Boolean reconnect) {
        System.out.println("\n==================== WS CONNECT ====================");
        System.out.println("[WS][CONNECT] meetingId=" + safe(meetingId)
                + ", userId=" + userId
                + ", sessionId=" + safe(sessionId)
                + ", reconnect=" + reconnect);
        System.out.println("===================== WS CONNECT END =====================\n");
    }

    /**
     * 打印接收消息日志
     */
    public static void logReceive(String meetingId,
                                  Long userId,
                                  String sessionId,
                                  String event,
                                  String payload) {
        System.out.println("\n==================== WS RECEIVE ====================");
        System.out.println("[WS][RECEIVE] meetingId=" + safe(meetingId)
                + ", userId=" + userId
                + ", sessionId=" + safe(sessionId)
                + ", event=" + safe(event));
        System.out.println("[WS][PAYLOAD] " + truncate(maskSensitive(payload)));
        System.out.println("===================== WS RECEIVE END =====================\n");
    }

    /**
     * 打印发送消息日志
     */
    public static void logSend(String meetingId,
                               Long userId,
                               String sessionId,
                               String message) {
        System.out.println("\n==================== WS SEND ====================");
        System.out.println("[WS][SEND] meetingId=" + safe(meetingId)
                + ", userId=" + userId
                + ", sessionId=" + safe(sessionId));
        System.out.println("[WS][MESSAGE] " + truncate(maskSensitive(message)));
        System.out.println("===================== WS SEND END =====================\n");
    }

    /**
     * 打印广播消息日志
     */
    public static void logBroadcast(String meetingId,
                                    Integer count,
                                    String message) {
        System.out.println("\n==================== WS BROADCAST ====================");
        System.out.println("[WS][BROADCAST] meetingId=" + safe(meetingId)
                + ", sessionCount=" + count);
        System.out.println("[WS][MESSAGE] " + truncate(maskSensitive(message)));
        System.out.println("===================== WS BROADCAST END =====================\n");
    }

    /**
     * 打印关闭连接日志
     */
    public static void logClose(String meetingId,
                                Long userId,
                                String sessionId,
                                Integer code,
                                String reason,
                                Boolean meetingEnded) {
        System.out.println("\n==================== WS CLOSE ====================");
        System.out.println("[WS][CLOSE] meetingId=" + safe(meetingId)
                + ", userId=" + userId
                + ", sessionId=" + safe(sessionId)
                + ", code=" + code
                + ", reason=" + safe(reason)
                + ", meetingEnded=" + meetingEnded);
        System.out.println("===================== WS CLOSE END =====================\n");
    }

    /**
     * 打印错误日志
     */
    public static void logError(String meetingId,
                                Long userId,
                                String sessionId,
                                String message) {
        System.out.println("\n==================== WS ERROR ====================");
        System.out.println("[WS][ERROR] meetingId=" + safe(meetingId)
                + ", userId=" + userId
                + ", sessionId=" + safe(sessionId)
                + ", message=" + safe(message));
        System.out.println("===================== WS ERROR END =====================\n");
    }

    /**
     * 打印心跳日志
     *
     * 注意：
     * 心跳日志比较频繁，如果后面觉得控制台太多，可以注释掉调用处。
     */
    public static void logHeartbeat(String meetingId,
                                    Long userId,
                                    String sessionId,
                                    String event) {
        System.out.println("[WS][HEARTBEAT] meetingId=" + safe(meetingId)
                + ", userId=" + userId
                + ", sessionId=" + safe(sessionId)
                + ", event=" + safe(event));
    }

    /**
     * 字符串空值保护
     */
    private static String safe(String value) {
        return value == null ? "" : value;
    }

    /**
     * 截断过长日志
     */
    private static String truncate(String text) {
        if (text == null) {
            return "";
        }

        if (text.length() <= MAX_LOG_LENGTH) {
            return text;
        }

        return text.substring(0, MAX_LOG_LENGTH) + "...[内容过长，已截断]";
    }

    /**
     * 脱敏敏感信息
     */
    private static String maskSensitive(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String result = text;

        result = result.replaceAll("(\"token\"\\s*:\\s*\")([^\"]*)(\")", "$1******$3");
        result = result.replaceAll("(\"rtcToken\"\\s*:\\s*\")([^\"]*)(\")", "$1******$3");
        result = result.replaceAll("(\"signalToken\"\\s*:\\s*\")([^\"]*)(\")", "$1******$3");
        result = result.replaceAll("(token=)([^&\\s]+)", "$1******");

        return result;
    }
}