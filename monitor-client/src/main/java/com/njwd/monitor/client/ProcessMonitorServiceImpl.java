package com.njwd.monitor.client;

import com.njwd.common.Constant;
import com.njwd.entity.base.Monitor;
import com.njwd.entity.base.MonitorBean;
import com.njwd.support.Result;
import com.njwd.utils.JsonUtils;
import com.njwd.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *@description: 监控client 服务类
 *@author: fancl
 *@create: 2019-09-16 
 */
@SpringBootConfiguration
@EnableScheduling
public class ProcessMonitorServiceImpl {
    //http 方式监控的进程列表
    @Value("${process.httpListStr}")
    String httpListStr;

    //ls方式监控的进程列表
    @Value("${process.lsListStr}")
    String lsListStr;

    @Value("${process.timeLong}")
    String timeLong;

    @Resource(name = "restTemplateMonitor")
    private RestTemplate restTemplate;

    @Autowired
    AmqpTemplate amqpTemplate;

    private static RestTemplate CLIENT;

    //本机ip
    private String ip;
    //H主机名
    private String hostName;
    //发送给MQ的监控数据
    MonitorBean monitorBean = new MonitorBean();
    //内部monitor列表
    List<Monitor> monitorList = new ArrayList<>();
    //记录日志
    Logger logger = LoggerFactory.getLogger(ProcessMonitorServiceImpl.class);


    //初始化 生成用于发送数据的List元素对象
    @PostConstruct
    protected void doInit() {
        CLIENT = restTemplate;
        //获取IP主机名
        getIpHost();
        //每个元素四个部分：id,appCode,appName,url
        //将http应用放入list
        dealConfig(httpListStr, MonitorClientConstant.typeHttp);
        logger.info("httpListStr:" + httpListStr);
        //将ls应用放入list中
        dealConfig(lsListStr, MonitorClientConstant.typeLs);
        logger.info("lsListStr:" + lsListStr);

        for (Monitor m : monitorList) {
            logger.info(m.toString());
        }
    }


    //解析配置信息,转为monitorList
    private void dealConfig(String configStr, String type) {
        if (!StringUtil.isEmpty(configStr)) {
            String[] arr = configStr.split(MonitorClientConstant.splitOneLevel);
            //如果为空
            for (String httpName : arr) {
                String[] httpArr = httpName.split(MonitorClientConstant.splitSecondLevel);
                Monitor monitor = new Monitor();
                monitor.setId(Integer.valueOf(httpArr[0]));
                monitor.setType(type);
                monitor.setAppCode(httpArr[1]);
                monitor.setAppName(httpArr[2]);
                monitor.setUrl(httpArr[3]);
                monitor.setIp(ip);
                monitor.setHostName(hostName);
                monitorList.add(monitor);
            }
        }
    }


    //获取ip 主机名
    private void getIpHost() {
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.ip = addr.getHostAddress(); //获取本机ip
        this.hostName = addr.getHostName(); //获取本机计算机名称
        logger.info(String.format(MonitorClientConstant.ip, ip));
        logger.info(String.format(MonitorClientConstant.hostName, hostName));
    }

    @Scheduled(cron = "0/10 * * * * ?")
    protected void monitor() {
        //循环检测
        for (Monitor monitor : monitorList) {
            //http检测
            if (MonitorClientConstant.typeHttp.equals(monitor.getType())) {
                try {
                    Result result = this.sendHttp(monitor.getUrl());
                    //状态成功发送正确日志
                    if (result.getCode() == MonitorClientConstant.code200) {
                        monitor.setIsOk(Constant.Is.YES);
                    }
                } catch (Exception e) {
                    //异常之后发送
                    e.printStackTrace();
                    logger.error(monitor.getAppName());
                    logger.error(String.format(MonitorClientConstant.errMessageHttp, monitor.getIp(), monitor.getAppName()));
                    monitor.setIsOk(Constant.Is.NO);
                }
            }
            //进程检测
            else if (MonitorClientConstant.typeLs.equals(monitor.getType())) {
                cmdLs(monitor);
            }
            //更新时间
            monitor.setUpdateTime(new Date());
        }
        //发送MQ
        send();

    }

    //发送对象到MQ
    private void send() {
        //设置发送对象并发送
        monitorBean.setMonitorList(monitorList);
        try {
            String jsonStr = JsonUtils.object2Json(monitorBean);
            for (Monitor m : monitorList) {
                logger.info(m.toString());
            }
            amqpTemplate.convertAndSend(MonitorClientConstant.queueName, jsonStr);
            logger.info(String.format(MonitorClientConstant.sendContent, jsonStr));
        } catch (AmqpException e) {
            e.printStackTrace();
            logger.error(String.format(MonitorClientConstant.errMessageWhenSend, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @description http检测
     * @author fancl
     * @date 2019/9/16
     * @param
     * @return
     */
    private Result sendHttp(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(MonitorClientConstant.httpHead, MonitorClientConstant.httpFormCode);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity entity = new HttpEntity<>(null, headers);
        // 执行HTTP请求
        ResponseEntity<Result> response = CLIENT.exchange(url, HttpMethod.GET, entity, Result.class);
        // 打印日志
        logger.info(JsonUtils.object2Str(JsonUtils.NON_NULL_MAPPER, response.getBody()));
        return response.getBody();
    }

    /**
     * @description 进程检测命令
     * @author fancl
     * @date 2019/9/17
     * @param
     * @return
     */
    private void cmdLs(Monitor monitor) {
        String[] cmd = {MonitorClientConstant.shHead, MonitorClientConstant.shC, MonitorClientConstant.shPs + monitor.getAppCode()};
        String line = null;
        try {
            java.lang.Process process = Runtime.getRuntime().exec(cmd);
            monitor.setUpdateTime(new Date());
            if (process != null) {
                BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(process.getInputStream()));
                logger.info("process:" + process);
                while ((line = bufferedReader1.readLine()) != null) {
                    if (line.indexOf(monitor.getUrl()) != -1) {
                        monitor.setIsOk(Constant.Is.YES);
                        logger.info(String.format(MonitorClientConstant.processOk, monitor.getIp(), monitor.getAppCode()));
                        return;
                    }
                }
                //执行到这说明没找到进程
                monitor.setIsOk(Constant.Is.NO);
                logger.info(String.format(MonitorClientConstant.processBad, monitor.getIp(), monitor.getAppCode()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            //出异常了
            monitor.setIsOk(Constant.Is.NO);
            logger.error(String.format(MonitorClientConstant.errMessageCmd, monitor.getIp(), monitor.getAppCode()));
        }
    }


    @Bean(name = "restTemplateMonitor")
    public RestTemplate restTemplateMonitor() {
        OkHttp3ClientHttpRequestFactory requestFactory = new OkHttp3ClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);
        return new RestTemplate(requestFactory);
    }


}
