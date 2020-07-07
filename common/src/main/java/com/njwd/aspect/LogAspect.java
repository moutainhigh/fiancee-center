package com.njwd.aspect;

import com.njwd.common.Constant;
import com.njwd.support.BaseController;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;


/**
 * 日志 切面
 *
 * @author xyyxhcj@qq.com
 * @since 2018-11-30
 */
@Component
@Aspect
public class LogAspect {


    @Around("@annotation(org.springframework.web.bind.annotation.PostMapping) || @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object process(ProceedingJoinPoint joinPoint) throws Throwable {
        Object target = joinPoint.getTarget();
        if (target instanceof BaseController) {
            BaseController controller = (BaseController) target;
            String methodName = joinPoint.getSignature().getName();
            controller.logger.info(methodName + " 开始------");
            long start = System.currentTimeMillis();
            Object result = joinPoint.proceed();
            long end = System.currentTimeMillis();
            controller.logger.info(methodName + " 结束------");
            long time = (end - start);
            if (time > Constant.SysConfig.LONG_TIME_THRESHOLD) {
                // 记录耗时大于1秒的接口
                controller.logger.error(Constant.SysConfig.LONG_TIME_LOG, methodName, time);
            }
            return result;
        }
        return joinPoint.proceed();
    }

}
