package com.njwd.ledger.cloudclient;

import com.njwd.platform.api.FinancialReportItemSetApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
* @description:  财务报告明细项
* @author LuoY
* @date 2019/8/2 11:40
*/
@FeignClient(name = "platform",contextId = "FinancialReportItemSetFeignClient")
public interface FinancialReportItemSetFeignClient extends FinancialReportItemSetApi {
}
