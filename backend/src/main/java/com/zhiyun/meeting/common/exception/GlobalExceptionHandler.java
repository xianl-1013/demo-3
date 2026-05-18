package com.zhiyun.meeting.common.exception;

import com.zhiyun.meeting.common.result.Result;
import com.zhiyun.meeting.common.result.ResultCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * 作用：
 * Controller 里不再每个接口都 try-catch。
 * Service 里直接 throw BusinessException。
 * 这里统一拦截异常并返回 Result。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * 例如：
     * 403：只有主持人可以执行会控操作
     * 404：会议不存在
     * 400：参数错误
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Object> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理其他未知异常
     *
     * 真正的代码 bug、数据库异常、空指针等会走这里。
     */
    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception e) {
        e.printStackTrace();
        return Result.error(ResultCode.SERVER_ERROR, "服务器异常：" + e.getMessage());
    }
}