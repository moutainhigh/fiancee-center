package com.njwd.async.server.schedule;


import com.njwd.async.server.mapper.SysLogMapper;
import com.njwd.utils.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 *  定时任务 中心
 */
@Component
@SpringBootConfiguration
@EnableScheduling
public class ScheduleCenter {

    @Resource
    SysLogMapper sysLogMapper;
    //记录日志
    Logger logger = LoggerFactory.getLogger(ScheduleCenter.class);

    /**
     * 每月1号00:00:01 生成一个sys_log_201903样式的表
     */
    @Scheduled(cron = "1 0 0 1 * ?")
    public void generateMonthLogTable(){
        //当前年月
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        String currentYearMonth = sdf.format(d);
        logger.info("格式化后的年月："+currentYearMonth);
        //判断表是否存在
        String tName = "wd_sys_log_" + currentYearMonth;

        int tableExists = sysLogMapper.isTableExists(tName);
        if(tableExists==0){
            sysLogMapper.generateMonthTable(tName);
            logger.info("新生成表:"+tName);
            //设置到redis中
            RedisUtils.set("currentYearMonth", currentYearMonth, 7, TimeUnit.DAYS);
            logger.info("设置到年月缓存:"+currentYearMonth);

        }
    }

}
