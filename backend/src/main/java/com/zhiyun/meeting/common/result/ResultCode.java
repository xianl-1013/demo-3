package com.zhiyun.meeting.common.result;

/**
 * 接口返回状态码
 *
 * 统一管理所有接口的 code 和默认 msg。
 */
public enum ResultCode {

    /**
     * 操作成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 请求参数错误
     */
    BAD_REQUEST(400, "请求参数错误"),

    /**
     * 未登录或登录失败
     */
    UNAUTHORIZED(401, "未登录或登录失败"),

    /**
     * 没有权限
     */
    FORBIDDEN(403, "没有权限"),

    /**
     * 数据不存在
     */
    NOT_FOUND(404, "数据不存在"),

    /**
     * 服务器异常
     */
    SERVER_ERROR(500, "服务器异常");

    private final Integer code;

    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}