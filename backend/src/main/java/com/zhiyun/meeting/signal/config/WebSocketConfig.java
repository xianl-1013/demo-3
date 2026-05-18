package com.zhiyun.meeting.signal.config;

import com.zhiyun.meeting.signal.handler.MeetingWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MeetingWebSocketHandler meetingWebSocketHandler;

    public WebSocketConfig(MeetingWebSocketHandler meetingWebSocketHandler) {
        this.meetingWebSocketHandler = meetingWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(meetingWebSocketHandler, "/ws/meeting")
                .setAllowedOriginPatterns("*");
    }
}