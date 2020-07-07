package com.njwd.basedata.cloudclient;

import com.njwd.platform.api.SysMenuOptionApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/21 14:17
 */
@FeignClient(name = "platform",contextId = "SysMenuOptionFeignClient")
public interface SysMenuOptionFeignClient extends SysMenuOptionApi {

}
