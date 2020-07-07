package com.njwd.async.server.controller;

import com.njwd.async.server.service.MonitorService;
import com.njwd.entity.base.Monitor;
import com.njwd.support.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *@description: 监控Controller
 *@author: fancl
 *@create: 2019-09-12 
 */
@Controller
@RequestMapping("monitor")
public class MonitorController {

    @Autowired
    MonitorService monitorService;

    /***
     * 跳转到监控页面
     * @return
     */
    @RequestMapping("jsp")
    public String index() {
        return "index";
    }


    /***
     * 获取监控数据
     * @return
     */
    @RequestMapping("getMonitors")
    @ResponseBody
    public Result<Map<String, Monitor>> getMonitors() {
        Result<Map<String, Monitor>> result = new Result<>().ok();
        //Map<String, Monitor> monitorData = monitorService.getMonitorData();

        //模拟数据
        Map<String, Monitor> monitorData = getTestData();
        result.setData(monitorData);
        return result;
    }

    //组装测试数据
    private Map<String, Monitor> getTestData() {
        Map<String, Monitor> monitorMap = new LinkedHashMap<>();
        Monitor monitor1 = new Monitor();
        monitor1.setId(1);
        monitor1.setAppCode("base-data");
        monitor1.setIsOk((byte) 1);
        monitor1.setIp("192.168.1.110");
        monitor1.setAppName("基础资料");
        Monitor monitor2 = new Monitor();
        monitor2.setId(2);
        monitor2.setAppCode("ledger");
        monitor2.setIsOk((byte) 1);
        monitor2.setIp("192.168.1.110");
        monitor2.setAppName("总账1");
        Monitor monitor3 = new Monitor();
        monitor3.setId(3);
        monitor3.setAppCode("redis");
        monitor3.setIsOk((byte) 0);
        monitor3.setIp("192.168.1.110");
        monitor3.setAppName("总账1");
        Monitor monitor4 = new Monitor();
        monitor4.setId(4);
        monitor4.setAppCode("asset");
        monitor4.setIsOk((byte) 1);
        monitor4.setIp("192.168.1.110");
        monitor4.setAppName("资产");
        Monitor monitor5 = new Monitor();
        monitor4.setId(5);
        monitor4.setAppCode("asset");
        monitor4.setIsOk((byte) 2);
        monitor4.setIp("192.168.1.110");
        monitor4.setAppName("资产");

        //将数据放入map
        monitorMap.put(monitor1.getId() + ":" + monitor1.getAppCode(), monitor1);
        monitorMap.put(monitor2.getId() + ":" + monitor2.getAppCode(), monitor2);
        monitorMap.put(monitor3.getId() + ":" + monitor3.getAppCode(), monitor3);
        monitorMap.put(monitor4.getId() + ":" + monitor4.getAppCode(), monitor4);
        monitorMap.put(monitor5.getId() + ":" + monitor5.getAppCode(), monitor5);
        return monitorMap;
    }

}
