package com.zhiyun.meeting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 智云会议后端启动类
 *
 * @EnableScheduling：
 * 开启 Spring 定时任务功能。
 * 后面 WebSocket 心跳清理任务需要用到。
 */
@EnableScheduling
@SpringBootApplication
public class ZhiyunMeetingServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiyunMeetingServerApplication.class, args);
    }
}