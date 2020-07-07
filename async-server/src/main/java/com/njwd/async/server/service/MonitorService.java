package com.njwd.async.server.service;

import com.njwd.entity.base.Monitor;

import java.util.Map;

/**
 * @description 监控服务类接口
 * @author fancl
 * @date 2019/9/19
 * @param
 * @return
 */
public interface MonitorService {
    /***
     * 更新monitor数据
     * @param monitorBeanStr 用于接收的MQ字符串
     */
    void updateMonitor(String monitorBeanStr);

    /***
     * 获取监控数据
     * @return
     */
    Map<String, Monitor> getMonitorData();
}
