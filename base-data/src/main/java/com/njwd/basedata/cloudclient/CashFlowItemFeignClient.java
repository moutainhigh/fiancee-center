package com.njwd.basedata.cloudclient;

import com.njwd.platform.api.CashFlowItemApi;
import org.springframework.cloud.openfeign.FeignClient;
/**
 * 现金流量项目
 */
@FeignClient(name="platform",contextId = "CashFlowItemFeignClient")
public interface CashFlowItemFeignClient extends CashFlowItemApi {
}
