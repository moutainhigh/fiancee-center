package com.njwd.basedata.cloudclient;

import com.njwd.platform.api.MenuCodingRulesApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/16 9:51
 */
@FeignClient(name = "platform",contextId = "MenuCodingRulesFeignClient")
public interface MenuCodingRulesFeignClient extends MenuCodingRulesApi {



}
