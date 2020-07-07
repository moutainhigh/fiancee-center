package com.njwd.basedata.cloudclient;

import com.njwd.platform.api.ParameterSetApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/14 11:24
 */
@FeignClient(name = "platform",contextId = "ParameterSetPlatformFeignClient")
public interface ParameterSetPlatformFeignClient extends ParameterSetApi {
}
