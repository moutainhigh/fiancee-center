package com.njwd.basedata.cloudclient;

import com.njwd.platform.api.SysSytemApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 子系统状态
 *
 * @Author: Zhuzs
 * @Date: 2019-06-26 09:42
 */
@FeignClient(name = "platform",contextId = "SysSystemFeignClient")
public interface SysSystemFeignClient extends SysSytemApi {
}
