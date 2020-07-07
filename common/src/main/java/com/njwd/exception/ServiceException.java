package com.njwd.exception;



/**
 * 业务异常类(业务处理时手动抛出异常)
 *
 * @author CJ
 */
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = -6914996666319154848L;
    public final int code;
    public final String message;
    public Object data;

    public ServiceException(String message, ResultCode resultCode) {
        super(message);
        this.code = resultCode.code;
        this.message = message;
    }

    public ServiceException(String message, ResultCode resultCode, Object data) {
        this(message, resultCode);
        this.data = data;
    }

    public ServiceException(ResultCode resultCode) {
        this(resultCode.message, resultCode);
    }

    public ServiceException(ResultCode resultCode, Object data) {
        this(resultCode.message, resultCode, data);
    }
}
