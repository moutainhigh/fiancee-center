package com.njwd.basedata.cloudclient;

import com.njwd.platform.api.MenuCodingRulesApi;
import com.njwd.platform.api.MenuControlStrategyApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/16 9:51
 */
@FeignClient(name = "platform",contextId = "MenuControlStrategyFeignClient")
public interface MenuControlStrategyFeignClient extends MenuControlStrategyApi {


}
