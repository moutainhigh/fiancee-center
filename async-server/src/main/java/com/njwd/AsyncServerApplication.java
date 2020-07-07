package com.njwd;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/05/06
 */
@SpringBootApplication
@MapperScan("com.njwd.**.mapper*")
public class AsyncServerApplication extends SpringBootServletInitializer {
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(AsyncServerApplication.class);
	}
	public static void main(String[] args) {

		SpringApplication.run(AsyncServerApplication.class, args);
	}


}
