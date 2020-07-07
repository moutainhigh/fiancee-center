package com.njwd.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author CJ
 */
@Configuration
public class CrossConfig {
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 允许域名
        corsConfiguration.addAllowedOrigin("*");
        // 允许请求头
        corsConfiguration.addAllowedHeader("*");
        // 允许请求方法(post get等)
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addExposedHeader("Content-Disposition");
        // 设置路径
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }
} 