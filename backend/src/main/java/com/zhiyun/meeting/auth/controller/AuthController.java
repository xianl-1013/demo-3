package com.zhiyun.meeting.auth.controller;

import com.zhiyun.meeting.auth.dto.LoginRequest;
import com.zhiyun.meeting.auth.service.AuthService;
import com.zhiyun.meeting.auth.vo.LoginResponse;
import com.zhiyun.meeting.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 登录认证接口
 *
 * 负责用户登录、返回 token 和基础用户信息。
 */
@Tag(name = "登录认证", description = "用户登录相关接口")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 构造方法注入 AuthService
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录接口
     *
     * 请求地址：
     * POST /auth/login
     */
    @Operation(summary = "用户登录", description = "根据 username 和 password 登录，成功后返回 token、用户ID、昵称、头像、手机号")
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);

        if (loginResponse == null) {
            return Result.error(401, "账号或密码错误");
        }

        return Result.success(loginResponse);
    }
}