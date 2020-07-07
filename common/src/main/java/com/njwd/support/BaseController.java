
package com.njwd.support;


import com.njwd.common.Constant;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.exception.UserException;
import com.njwd.utils.FastUtils;
import feign.FeignException;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;


/**
 * 公共 Controller。
 *
 * @author CJ
 */
@RestControllerAdvice
public class BaseController {

    public final Logger logger;

    {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    protected <T> Result<T> ok(T data) {
        return new Result<T>().ok(data);
    }

    protected Result ok() {
        Result result = new Result().ok();
        return result;
    }

    protected Result ok(ResultCode resultCode, Object data) {
        Result result = new Result(resultCode);
        result.setData(data);
        return result;
    }

    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    }

    protected Result error(String msg) {
        return error(msg, ResultCode.BAD_REQUEST);
    }

    protected Result error(ResultCode resultCode) {
        throw new ServiceException(resultCode);
    }

    protected Result error(String msg, ResultCode resultCode) {
        throw new ServiceException(msg, resultCode);
    }

    protected Result result(ResultCode resultCode) {
        return new Result(resultCode);
    }

    protected Result<Boolean> confirm(int result) {
        return result > 0 ? ok(true) : error(ResultCode.OPERATION_FAILURE);
    }

    public <T> Result<T> error(ResultCode resultCode, T data) {
        throw new ServiceException(resultCode, data);
    }

    /**
     * 进行异常处理,该方法与出错的方法必须在同一个Controller里面,使用该方法时,所有的Controller都需要继承该类
     */
    @ExceptionHandler(Exception.class)
    @SuppressWarnings("all")
    public Result handleException(Exception e, HttpServletRequest request) {
        Result result;
        String requestUri = request.getRequestURI();
        if (e instanceof UnauthenticatedException) {
            result = new Result(Constant.ReqResult.FAIL, ResultCode.SYS_USER_INVALID);
        } else if (e instanceof AuthorizationException) {
            result = new Result(Constant.ReqResult.FAIL, ResultCode.PERMISSION_NOT);
        } else if (e instanceof UserException) {
            result = new Result(Constant.ReqResult.FAIL, ((UserException) e).getResultCode());
        } else if (e instanceof ShiroException) {
            result = new Result(Constant.ReqResult.FAIL, ResultCode.SHIRO_ERROR);
        } else if (e instanceof LoginException) {
            result = new Result(Constant.ReqResult.FAIL, ResultCode.ACCOUNT_NOT);
        } else if (e instanceof HttpMessageNotReadableException || e instanceof HttpMediaTypeNotSupportedException) {
            result = new Result(Constant.ReqResult.FAIL, ResultCode.PARAMS_NOT);
        } else if (e instanceof ServiceException) {
            ServiceException se = (ServiceException) e;
            result = new Result(Constant.ReqResult.FAIL, se.code, se.message, se.data);
        } else if (e instanceof FeignException) {
            result = new Result(Constant.ReqResult.ERROR, ResultCode.FEIGN_CONNECT_ERROR.code, String.format(ResultCode.FEIGN_CONNECT_ERROR.message, e.getMessage()), null);
        } else if (e instanceof MyBatisSystemException) {
            String message = e.getCause().getMessage();
            if (message.indexOf(ResultCode.SYS_USER_INVALID.message) > -1) {
                result = new Result(Constant.ReqResult.FAIL, ResultCode.SYS_USER_INVALID);
            } else {
                result = new Result(Constant.ReqResult.ERROR, ResultCode.INTERNAL_SERVER_ERROR.code, message, null);
            }
        } else {
            result = new Result(Constant.ReqResult.ERROR, ResultCode.INTERNAL_SERVER_ERROR);
        }
        switch (requestUri) {
            // 结账反结账异常单独处理,结账反结账不抛出fail,信息为success
            case Constant.RequestUri.SETTLE:
                result = transformResult2Success(e, result, requestUri, ResultCode.SETTLE_ERROR);
                break;
            case Constant.RequestUri.CANCEL_SETTLE:
                result = transformResult2Success(e, result, requestUri, ResultCode.CANCEL_SETTLE_ERROR);
                break;
            // 初始化反初始化异常单独处理,结账反结账不抛出fail,信息为success
            case Constant.RequestUri.BALANCE_INIT:
                if(result.getCode() == ResultCode.INTERNAL_SERVER_ERROR.code){
                    result = transformResult2Success(e, result, requestUri, ResultCode.BALANCE_INIT_ERROR);
                }else {
                    result = transformResult2Success(e, result, requestUri, ResultCode.BALANCE_IGNORE);
                }
                break;
            case Constant.RequestUri.BALANCE_DISINIT:
                if(result.getCode() == ResultCode.INTERNAL_SERVER_ERROR.code){
                    result = transformResult2Success(e, result, requestUri, ResultCode.BALANCE_DIS_INIT_ERROR);
                }else {
                    result = transformResult2Success(e, result, requestUri, ResultCode.BALANCE_IGNORE);
                }
                break;
                default:
        }
        // 记录业务异常日志
        logger.error("接口: {} 异常，异常状态码 {}，异常信息：{}", requestUri, result.getCode(), result.getMessage(), e);
        FastUtils.loggerRequestBody(logger, request);
        return result;
    }

    /**
     * 将异常result转为SUCCESS
     *
     * @param e          e
     * @param result     原result
     * @param requestUri requestUri
     * @param errorCode  数据异常转换的ResultCode
     * @return com.njwd.support.Result<T>
     * @author xyyxhcj@qq.com
     * @date 2019/10/17 13:53
     **/
    private <T> Result<T> transformResult2Success(Exception e, Result<T> result, String requestUri, ResultCode errorCode) {
        if (result.getCode() == ResultCode.DATA_ERROR.code) {
            logger.error("接口: {} 发现数据异常，异常信息：{}", requestUri, result.getMessage(), e);
            T errorData = result.getData();
            result = new Result<>(errorCode);
            result.setData(errorData);
        } else if (result.getCode() == ResultCode.INTERNAL_SERVER_ERROR.code) {
            result = new Result<>(errorCode);
        }
        result.setStatus(Constant.ReqResult.SUCCESS);
        return result;
    }
}
