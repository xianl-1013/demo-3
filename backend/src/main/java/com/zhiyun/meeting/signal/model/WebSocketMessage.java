package com.zhiyun.meeting.signal.model;

import java.util.Map;

public class WebSocketMessage {

    private String type;

    private String event;

    private String meetingId;

    private Long userId;

    private Long targetUserId;

    private Map<String, Object> data;

    private Long timestamp;

    public WebSocketMessage() {
    }

    public WebSocketMessage(String type, String event, String meetingId, Long userId,
                            Long targetUserId, Map<String, Object> data, Long timestamp) {
        this.type = type;
        this.event = event;
        this.meetingId = meetingId;
        this.userId = userId;
        this.targetUserId = targetUserId;
        this.data = data;
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}