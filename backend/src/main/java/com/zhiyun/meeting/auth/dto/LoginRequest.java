package com.zhiyun.meeting.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 登录请求参数
 *
 * 前端登录时提交 username 和 password。
 */
@Schema(description = "登录请求参数")
public class LoginRequest {

    /**
     * 登录账号
     */
    @Schema(description = "登录账号", example = "admin")
    private String username;

    /**
     * 登录密码
     */
    @Schema(description = "登录密码", example = "123456")
    private String password;

    public LoginRequest() {
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}