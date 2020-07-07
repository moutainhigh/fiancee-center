package com.njwd.basedata.cloudclient;

import com.njwd.platform.api.UnitApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/15 16:25
 */
@FeignClient(name = "platform",contextId = "UnitFeignClient")
public interface UnitFeignClient extends UnitApi {
}
