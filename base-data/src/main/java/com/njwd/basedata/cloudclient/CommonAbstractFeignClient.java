package com.njwd.basedata.cloudclient;

import com.njwd.platform.api.CommonAbstractApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/14 9:58
 */
@FeignClient(name="platform",contextId = "CommonAbstractFeignClient")
public interface CommonAbstractFeignClient extends CommonAbstractApi {



}
