package com.njwd.monitor.client;

/**
 * @description 监控常量定义
 * @author fancl
 * @date 2019/9/17
 * @param 
 * @return 
 */
public interface MonitorClientConstant {
    //监控队列名称
    String queueName = "monitor-queue";
    //http的类型
    String typeHttp = "http";
    //ls进程
    String typeLs = "ls";

    int code200 = 200;
    //一级分隔符
    String splitOneLevel = "\\;";
    //二级分隔符
    String splitSecondLevel = "\\,";
    //用于执行shell命令
    String shHead = "/bin/bash";

    String shC = "-c";

    String shPs = "ps -ef|grep ";

    String httpHead = "Accept";

    String httpFormCode = "application/json;charset=UTF-8";

    String processOk = "ip:%s,进程:%s 正常";

    String processBad = "ip:%s,进程:%s 不正常";

    String errMessageHttp = "http检查异常:%s:%s";

    String errMessageWhenSend = "发送消息异常:%s";

    String errMessageCmd = "ip:%s,进程:%s检测异常";

    String ip = "本机IP:%s";

    String hostName = "本机主机名:%s";

    String sendContent ="发送的状态信息:%s";

}
