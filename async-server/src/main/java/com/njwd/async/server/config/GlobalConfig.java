package com.njwd.async.server.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 *@description:
 *@author: fancl
 *@create: 2019-09-12 
 */
@Configuration
public class GlobalConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> customizer() {
        return (factory) -> {
            factory.addContextCustomizers((context) -> { //模块中webapp相对路径
                //consumer为你子项目的module名称，而不是你修改的application name这点需要注意；
                String relativePath = "async-server/src/main/webapp";
                File docBaseFile = new File(relativePath); // 如果路径不存在，则把这个路径加入进去
                if (docBaseFile.exists()) {
                    context.setDocBase(docBaseFile.getAbsolutePath());
                }
            });
        };
    }
}
