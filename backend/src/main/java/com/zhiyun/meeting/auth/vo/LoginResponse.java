package com.zhiyun.meeting.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 登录响应数据
 *
 * 登录成功后返回给前端。
 */
@Schema(description = "登录响应数据")
public class LoginResponse {

    /**
     * 登录 token
     *
     * 第一版这里是随机 UUID。
     * 后面可以升级为 JWT。
     */
    @Schema(description = "登录token", example = "a1b2c3d4")
    private String token;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private String userId;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称", example = "管理员")
    private String userName;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像", example = "")
    private String avatar;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800000000")
    private String phone;

    public LoginResponse() {
    }

    public LoginResponse(String token, String userId, String userName, String avatar, String phone) {
        this.token = token;
        this.userId = userId;
        this.userName = userName;
        this.avatar = avatar;
        this.phone = phone;
    }

    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getPhone() {
        return phone;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}