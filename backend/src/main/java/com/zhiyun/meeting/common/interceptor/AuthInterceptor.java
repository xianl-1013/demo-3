package com.zhiyun.meeting.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyun.meeting.auth.repository.AuthTokenRepository;
import com.zhiyun.meeting.auth.vo.TokenInfo;
import com.zhiyun.meeting.common.result.Result;
import com.zhiyun.meeting.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录 token 拦截器
 *
 * 作用：
 * 1. 拦截需要登录的 HTTP 接口
 * 2. 从请求头 Authorization 中获取 token
 * 3. 校验 token 是否存在、是否过期
 * 4. 校验通过后，把当前用户ID放入 request attribute
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthTokenRepository authTokenRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthInterceptor(AuthTokenRepository authTokenRepository) {
        this.authTokenRepository = authTokenRepository;
    }

    /**
     * Controller 执行前调用
     *
     * 返回 true：放行
     * 返回 false：拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 处理浏览器预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = getTokenFromRequest(request);

        if (token == null || token.trim().isEmpty()) {
            writeUnauthorized(response, "缺少登录token");
            return false;
        }

        TokenInfo tokenInfo = authTokenRepository.findValidToken(token);

        if (tokenInfo == null) {
            writeUnauthorized(response, "token无效或已过期");
            return false;
        }

        // 把当前登录用户ID放到 request 里
        // 后面的 Controller / Service 如果需要，可以从 request 中取
        request.setAttribute("currentUserId", tokenInfo.getUserId());

        return true;
    }

    /**
     * 从请求头里获取 token
     *
     * 支持两种写法：
     * 1. Authorization: Bearer xxxxx
     * 2. Authorization: xxxxx
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || authorization.trim().isEmpty()) {
            return null;
        }

        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        return authorization;
    }

    /**
     * 返回 401 JSON
     */
    private void writeUnauthorized(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(200);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        Result<Object> result = Result.error(ResultCode.UNAUTHORIZED, msg);

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}