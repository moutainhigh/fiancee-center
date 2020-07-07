package com.njwd.platform.support;

import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.platform.config.YmlProperties;
import com.njwd.utils.CheckUrlSignUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author lj
 * @Description 接口校验
 * @Date:9:47 2019/6/28
 **/
public class CheckInterfaceInterceptor implements HandlerInterceptor {

    public final Logger logger;

    {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Autowired
    private YmlProperties ymlProperties;

    /**
     * @return boolean
     * @Description 接口校验
     * @Author lj
     * @Date:9:55
     * @Param [request, response, handler]
     **/
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        logger.debug("当前url:" + request.getRequestURI());
        try {
            if (ymlProperties.getCheckInterfaceFlag()) {
                CheckUrlSignUtil.checkFeignSign(request);
            }
        } catch (Exception e) {
            throw new ServiceException(ResultCode.FORBIDDEN);
        }
        return true;
    }
}
