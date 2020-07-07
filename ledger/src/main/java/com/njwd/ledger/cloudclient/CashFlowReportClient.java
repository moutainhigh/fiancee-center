package com.njwd.ledger.cloudclient;

import com.njwd.basedata.api.CashFlowItemApi;
import org.springframework.cloud.openfeign.FeignClient;
/**
 * 现金流量项目表
 */
@FeignClient(name="base-data",contextId = "CashFlowReportClient")
public interface CashFlowReportClient extends CashFlowItemApi {
}
