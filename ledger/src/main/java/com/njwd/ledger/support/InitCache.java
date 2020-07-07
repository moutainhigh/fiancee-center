package com.njwd.ledger.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 服务启动执行
 *
 * @author CJ
 */
@Component
public class InitCache implements CommandLineRunner {
    private Logger logger = LoggerFactory.getLogger(InitCache.class);
    @Override
    public void run(String... arg0) {
        logger.info(">>>>>>>>>>>>>>>容器启动时执行加载数据等操作<<<<<<<<<<<<<");
        logger.info(">>>>>>>>>>>>>>>数据加载完毕<<<<<<<<<<<<<");
    }
}
