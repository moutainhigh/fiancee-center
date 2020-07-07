package com.njwd.config;

import com.njwd.common.Constant;
import com.njwd.support.BaseController;
import com.njwd.utils.CheckUrlSignUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author CJ
 */
@SpringBootConfiguration
public class AppConfig {

    @Value("${spring.application.name}")
    private String appName;

    @Bean
    public RestTemplate restTemplate() {
        OkHttp3ClientHttpRequestFactory requestFactory = new OkHttp3ClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);
        return new RestTemplate(requestFactory);
    }

    @Bean(name="restTemplate0")
    @LoadBalanced
    public RestTemplate restTemplate0(){
        RestTemplate restTemplate = new RestTemplate();
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add((request, body, execution) -> {
            HttpHeaders headers = request.getHeaders();
            headers.add(CheckUrlSignUtil.WD_AUTH, CheckUrlSignUtil.getAuthStr(appName));
            headers.add(Constant.ShiroConfig.SESSION_ID_KEY, BaseController.getRequest().getHeader(Constant.ShiroConfig.SESSION_ID_KEY));
            return execution.execute(request,body);
        });
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }

}