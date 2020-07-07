package com.njwd.platform.logger;

import com.njwd.entity.base.SysLogCommon;
import com.njwd.logger.SenderService;
import com.njwd.platform.service.SysLogService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/11 15:57
 */
@Component
public class SenderServiceImpl implements SenderService {

    @Resource
    SysLogService sysLogService;

    @Override
    public void sendLog(SysLogCommon sysLogCommon) {
        sysLogService.addSysLog(sysLogCommon);
    }
}
