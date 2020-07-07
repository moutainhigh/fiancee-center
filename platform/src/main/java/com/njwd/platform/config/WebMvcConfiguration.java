package com.njwd.platform.config;

import com.njwd.platform.support.CheckInterfaceInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * @Author lj
 * @Description 拦截器配置
 * @Date:10:12 2019/6/28
 **/
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    public final Logger logger;

    {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Bean
    public CheckInterfaceInterceptor checkInterfaceInterceptor(){
        return new CheckInterfaceInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        logger.info("添加接口校验拦截器");
        InterceptorRegistration interceptorRegistration = registry.addInterceptor(checkInterfaceInterceptor());
        //拦截规则
        interceptorRegistration.addPathPatterns("/**");
        //忽略url
        interceptorRegistration.excludePathPatterns(Arrays.asList("/","/csrf","/swagger**","/swagger-resources/**","/error","/webjars/**","/views/**", "/res/**"));
    }
}
