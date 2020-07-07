package com.njwd;

import com.njwd.config.WdFeignClientsConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/05/06
 */
@EnableDiscoveryClient
@EnableFeignClients(defaultConfiguration = WdFeignClientsConfiguration.class)
@SpringBootApplication
@MapperScan({"com.njwd.**.mapper*"})
public class BaseDataApplication {
	public static void main(String[] args) {
		SpringApplication.run(BaseDataApplication.class, args);
	}
}
