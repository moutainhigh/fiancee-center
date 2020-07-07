package com.njwd.support;


import com.njwd.common.Constant;
import com.njwd.exception.ResultCode;

/**
 * 结果类
 * @author CJ
 */
public class Result<T> {
    public static final int SUCCESS = 200;
    /**
     * success：成功，fail：业务返回的失败，error：非业务异常失败
     */
    private String status = Constant.ReqResult.SUCCESS;
    /**
     * 状态码
     **/
    private int code;

    /**
     * 结果数据
     **/
    private T data;

    /**
     * 结果描述
     **/
    private String message;

    public Result() {
    }

    public Result(ResultCode resultCode) {
        this.code = resultCode.code;
        this.message = resultCode.message;
    }

    public Result(ResultCode resultCode, String message, T data) {
        this.code = resultCode.code;
        this.message = message;
        this.data = data;
    }

    public Result(String status, int code, String message, T data) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Result(String status, ResultCode resultCode) {
        this.status = status;
        this.code = resultCode.code;
        this.message = resultCode.message;
    }

    public Result<T> ok(T data) {
        this.code = SUCCESS;
        this.data = data;
        return this;
    }

    public Result ok() {
        this.code = SUCCESS;
        return this;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
