package com.njwd.ledger.logger;

import com.njwd.entity.base.SysLogCommon;
import com.njwd.logger.SenderService;
import com.njwd.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description: 异步服务类
 * @author: fancl
 * @create: 2019-05-22
 */
@Component
public class SenderServiceImpl implements SenderService {

    @Autowired
    AmqpTemplate amqpTemplate;

    /**
     * 日志发送
     *
     * @param sysLogCommon 日志对象
     */
    @Override
    public void sendLog(SysLogCommon sysLogCommon) {

        Logger logger = LoggerFactory.getLogger(SenderServiceImpl.class);
        //将对象转为字符串
        String jsonStr = JsonUtils.object2Json(sysLogCommon);
        try {
            amqpTemplate.convertAndSend("log-queue",jsonStr);
        }catch (Exception e){
            logger.info("发送日志异常:" +e.getMessage());
            e.printStackTrace();
        }

    }

}
