package com.njwd.async.server.service.impl;

import com.njwd.async.server.mapper.SysLogMapper;
import com.njwd.async.server.service.ReceiverService;
import com.njwd.entity.base.SysLogCommon;
import com.njwd.utils.JsonUtils;
import com.njwd.utils.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @description: 消息处理服务类
 * @author: fancl
 * @create: 2019-05-22
 */
@Component
public class ReceiverServiceImpl implements ReceiverService {
    @Resource
    SysLogMapper sysLogMapper;

    //日志对象
    Logger logger = LoggerFactory.getLogger(ReceiverServiceImpl.class);



    @Override
    @RabbitListener(queues = "log-queue")
    @RabbitHandler
    public void addSysLog(String sysLogCommonStr) {
        //先从redis获取当前年月
        String currentYearMonth = RedisUtils.get("currentYearMonth");
        //记录日志

        logger.info(sysLogCommonStr);
        if (currentYearMonth == null) {
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
            currentYearMonth = sdf.format(d);
            logger.info("格式化后的年月：" + currentYearMonth);
            //设置到redis中
            RedisUtils.set("currentYearMonth", currentYearMonth, 7, TimeUnit.DAYS);
        }
        //消费者决定往哪个月份表插数据
        SysLogCommon sysLogCommon = JsonUtils.json2Pojo(sysLogCommonStr, SysLogCommon.class);
        try {
            //先判断有没有这个年月的表
            int tSize = sysLogMapper.isTableExists("wd_sys_log_" + currentYearMonth);
            if(tSize >0){
                sysLogMapper.insertDynamic(sysLogCommon, currentYearMonth);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
