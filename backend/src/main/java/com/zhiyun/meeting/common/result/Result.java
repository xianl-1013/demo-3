package com.zhiyun.meeting.common.result;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 统一接口返回对象
 *
 * 所有 HTTP 接口都使用这个格式返回。
 *
 * @param <T> data 的实际数据类型
 */
@Schema(description = "统一接口返回对象")
public class Result<T> {

    /**
     * 状态码
     *
     * 200：成功
     * 400：参数错误
     * 401：未登录或登录失败
     * 403：无权限
     * 404：数据不存在
     * 500：服务器异常
     */
    @Schema(description = "状态码", example = "200")
    private Integer code;

    /**
     * 返回提示信息
     */
    @Schema(description = "返回提示信息", example = "操作成功")
    private String msg;

    /**
     * 返回数据
     */
    @Schema(description = "返回数据")
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功返回，不带 data
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    /**
     * 成功返回，带 data
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    /**
     * 成功返回，自定义 msg 和 data
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    /**
     * 失败返回，默认服务器异常
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(ResultCode.SERVER_ERROR.getCode(), msg, null);
    }

    /**
     * 失败返回，自定义 code 和 msg
     */
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 失败返回，使用 ResultCode
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMsg(), null);
    }

    /**
     * 失败返回，使用 ResultCode，但自定义 msg
     */
    public static <T> Result<T> error(ResultCode resultCode, String msg) {
        return new Result<>(resultCode.getCode(), msg, null);
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setData(T data) {
        this.data = data;
    }
}