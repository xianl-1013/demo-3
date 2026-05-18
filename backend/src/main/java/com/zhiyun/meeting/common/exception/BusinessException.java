package com.zhiyun.meeting.common.exception;

import com.zhiyun.meeting.common.result.ResultCode;

/**
 * 业务异常
 *
 * 用于主动抛出可预期的业务错误。
 *
 * 例如：
 * 1. 会议不存在
 * 2. 只有主持人可以执行会控
 * 3. 被操作成员不存在
 * 4. 参数不能为空
 */
public class BusinessException extends RuntimeException {

    /**
     * 业务错误码
     */
    private final Integer code;

    /**
     * 使用 ResultCode 构造业务异常
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    /**
     * 使用 ResultCode + 自定义 msg 构造业务异常
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    /**
     * 直接传 code + msg
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}