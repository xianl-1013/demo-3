package com.zhiyun.meeting.auth.vo;

import java.time.LocalDateTime;

/**
 * Token 信息对象
 *
 * 这个类用于表示从 sys_user_token 表中查出来的 token 信息。
 * 主要给拦截器使用。
 */
public class TokenInfo {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * token 字符串
     */
    private String token;

    /**
     * token 过期时间
     */
    private LocalDateTime expireTime;

    public TokenInfo() {
    }

    public TokenInfo(Long userId, String token, LocalDateTime expireTime) {
        this.userId = userId;
        this.token = token;
        this.expireTime = expireTime;
    }

    public Long getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }
}