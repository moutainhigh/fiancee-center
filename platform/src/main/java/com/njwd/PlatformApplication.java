package com.njwd;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/05/06
 */
@EnableDiscoveryClient
@EnableFeignClients(defaultConfiguration = FeignClientsConfiguration.class)
@SpringBootApplication
@MapperScan({"com.njwd.**.mapper*"})
public class PlatformApplication {
	public static void main(String[] args) {
		SpringApplication.run(PlatformApplication.class, args);
	}
}
