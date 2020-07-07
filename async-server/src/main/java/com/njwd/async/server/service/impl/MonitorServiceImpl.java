package com.njwd.async.server.service.impl;

import com.njwd.async.server.service.MonitorService;
import com.njwd.entity.base.Monitor;
import com.njwd.entity.base.MonitorBean;
import com.njwd.utils.JsonUtils;
import com.njwd.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *@description:
 *@author: fancl
 *@create: 2019-09-19 
 */
@Component
public class MonitorServiceImpl implements MonitorService {

    //用于更新监控的全量数据(Map类型)
    public static Map<String, Monitor> monitorMap = new LinkedHashMap<>();

    //唯一键值
    String key = null;

    //日志对象
    Logger logger = LoggerFactory.getLogger(MonitorServiceImpl.class);

    /***
     * 更新监控数据
     * @param monitorBeanStr 用于接收的MQ字符串
     */
    @Override
    @RabbitListener(queues = "monitor-queue")
    @RabbitHandler
    public void updateMonitor(String monitorBeanStr) {
        //解析字符串为对象
        if (!StringUtil.isEmpty(monitorBeanStr)) {
            MonitorBean monitorBean = JsonUtils.json2Pojo(monitorBeanStr, MonitorBean.class);
            StringBuffer key = null;
            for (Monitor monior : monitorBean.getMonitorList()) {
                key = new StringBuffer();
                //如果包含同一个id+appCode的才算同一个应用
                key.append(monior.getId()).append(":").append(monior.getAppCode());
                monitorMap.put(key.toString(), monior);
            }
            //记录更新信息
            //打印数据
            for (Map.Entry<String, Monitor> entry : monitorMap.entrySet()) {
                logger.info(entry.getKey());
                logger.info(entry.getValue().toString());
            }
        }
    }

    /***
     * 获取监控数据
     * @return
     */
    @Override
    public Map<String, Monitor> getMonitorData() {
        return monitorMap;
    }


}
