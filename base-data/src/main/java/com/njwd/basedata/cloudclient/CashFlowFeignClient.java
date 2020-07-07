package com.njwd.basedata.cloudclient;

import com.njwd.platform.api.CashFlowApi;
import org.springframework.cloud.openfeign.FeignClient;


/**
 * 现金流量项目表
 */
@FeignClient(name="platform",contextId = "CashFlowFeignClient")
public interface CashFlowFeignClient  extends CashFlowApi {
}
