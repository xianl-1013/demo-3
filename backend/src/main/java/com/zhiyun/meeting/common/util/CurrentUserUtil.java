package com.zhiyun.meeting.common.util;

import com.zhiyun.meeting.common.exception.BusinessException;
import com.zhiyun.meeting.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 当前登录用户工具类
 *
 * 作用：
 * 从 AuthInterceptor 放入 request 的 currentUserId 中获取当前登录用户ID。
 *
 * 为什么要这样做：
 * 1. userId 不能相信前端传参
 * 2. 当前登录用户应该以后端 token 解析结果为准
 * 3. 这样可以防止普通成员伪造主持人 userId
 */
public class CurrentUserUtil {

    /**
     * 从 request 中获取当前登录用户ID
     *
     * @param request HTTP请求对象
     * @return 当前登录用户ID
     */
    public static Long getCurrentUserId(HttpServletRequest request) {
        Object value = request.getAttribute("currentUserId");

        if (value == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未获取到当前登录用户");
        }

        try {
            return Long.valueOf(String.valueOf(value));
        } catch (Exception e) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "当前登录用户信息异常");
        }
    }
}