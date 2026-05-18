package com.zhiyun.meeting.auth.service;

import com.zhiyun.meeting.auth.dto.LoginRequest;
import com.zhiyun.meeting.auth.repository.AuthTokenRepository;
import com.zhiyun.meeting.auth.vo.LoginResponse;
import com.zhiyun.meeting.user.entity.SysUser;
import com.zhiyun.meeting.user.repository.SysUserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 登录认证业务 Service
 *
 * 负责：
 * 1. 校验账号密码
 * 2. 生成 token
 * 3. 保存 token
 * 4. 返回登录用户信息
 */
@Service
public class AuthService {

    private final SysUserRepository sysUserRepository;

    private final AuthTokenRepository authTokenRepository;

    /**
     * token 过期时间，单位：小时
     *
     * 第一版先设置 24 小时。
     * 后面可以改成配置文件。
     */
    private static final int TOKEN_EXPIRE_HOURS = 24;

    public AuthService(SysUserRepository sysUserRepository,
                       AuthTokenRepository authTokenRepository) {
        this.sysUserRepository = sysUserRepository;
        this.authTokenRepository = authTokenRepository;
    }

    /**
     * 用户登录
     *
     * @param request 登录请求参数
     * @return 登录成功信息；失败时返回 null
     */
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        if (password == null || password.trim().isEmpty()) {
            return null;
        }

        // 根据账号查询用户
        SysUser user = sysUserRepository.findByUsername(username);

        if (user == null) {
            return null;
        }

        // 第一版暂时明文密码校验
        // 后面可以升级为 BCrypt 加密密码
        if (!password.equals(user.getPassword())) {
            return null;
        }

        // 生成随机 token
        String token = UUID.randomUUID().toString().replace("-", "");

        // 一个用户只保留一个 token
        // 重新登录后旧 token 失效
        authTokenRepository.deleteByUserId(user.getId());

        // 保存新 token
        authTokenRepository.saveToken(user.getId(), token, TOKEN_EXPIRE_HOURS);

        return new LoginResponse(
                token,
                String.valueOf(user.getId()),
                user.getUserName(),
                user.getAvatar(),
                user.getPhone()
        );
    }
}