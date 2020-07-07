package com.njwd.basedata.cloudclient;

import com.njwd.platform.api.CostItemApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/19 17:54
 */
@FeignClient(name="platform",contextId = "CostItemFeignClient")
public interface CostItemFeignClient extends CostItemApi {
}
