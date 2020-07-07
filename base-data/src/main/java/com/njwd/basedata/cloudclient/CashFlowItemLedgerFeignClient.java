package com.njwd.basedata.cloudclient;

import com.njwd.ledger.api.CashFlowItemLedgerApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author Libao
 * @Description 现金流量项目总账Client
 * @Date  2019/9/5 15:45
 * @Param
 * @return
 */
@FeignClient(name = "ledger", contextId = "CashFlowItemLedgerFeignClient")
public interface CashFlowItemLedgerFeignClient extends CashFlowItemLedgerApi {

}
