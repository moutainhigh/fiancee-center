package com.njwd.async.server.config.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @description: 队列配置 可配置多个队列
 * @author: fancl
 * @create: 2019-05-22
 */
@SpringBootConfiguration
public class QueueConfig {

    /**
     * 日志队列
     * @return
     */
    @Bean
    public Queue logQueue(){
        /**
         durable="true" 持久化 rabbitmq重启的时候不需要创建新的队列
         auto-delete 表示消息队列没有在使用时将被自动删除 默认是false
         exclusive  表示该消息队列是否只在当前connection生效,默认是false
         */

        return new Queue("log-queue",true,false,false);
    }

    /**
     * 监控队列
     */
    @Bean
    public Queue monitorQueue(){
        /**
         durable="true" 持久化 rabbitmq重启的时候不需要创建新的队列
         auto-delete 表示消息队列没有在使用时将被自动删除 默认是false
         exclusive  表示该消息队列是否只在当前connection生效,默认是false
         */

        return new Queue("monitor-queue",true,false,false);
    }
}
