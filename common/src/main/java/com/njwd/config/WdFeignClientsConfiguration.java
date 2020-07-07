package com.njwd.config;

import com.njwd.common.Constant;
import com.njwd.support.BaseController;
import com.njwd.utils.CheckUrlSignUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 自定义的feign配置,用于添加请求校验参数
 *
 * @author xyyxhcj@qq.com
 * @since 2019/06/25
 */
@SpringBootConfiguration
public class WdFeignClientsConfiguration extends FeignClientsConfiguration {
	@Value("${spring.application.name}")
	private String appName;

	@Bean
	public FeignBasicAuthRequestInterceptor basicAuthRequestInterceptor() {
		return new FeignBasicAuthRequestInterceptor();
	}

	public class FeignBasicAuthRequestInterceptor implements RequestInterceptor {
		@Override
		public void apply(RequestTemplate template) {
			template.header(CheckUrlSignUtil.WD_AUTH, CheckUrlSignUtil.getAuthStr(appName));
			template.header(Constant.ShiroConfig.SESSION_ID_KEY, BaseController.getRequest().getHeader(Constant.ShiroConfig.SESSION_ID_KEY));
		}
	}
}
