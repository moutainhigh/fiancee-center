package com.njwd.async.server.service;

/**
 * 异步接收服务类
 */
public interface ReceiverService {
    /**
     * 新增日志
     *
     * @param sysLogCommonStr Log对象str
     * @throws Exception
     */
    void addSysLog(String sysLogCommonStr) throws Exception;
}
