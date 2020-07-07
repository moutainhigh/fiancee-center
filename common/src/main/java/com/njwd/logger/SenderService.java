package com.njwd.logger;


import com.njwd.entity.base.SysLogCommon;

/**
 * 异步接口
 */
public interface SenderService {
    /**
     * 日志发送
     *
     * @param sysLogCommon 日志对象字符串
     */
    void sendLog(SysLogCommon sysLogCommon);
}
