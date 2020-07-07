package com.njwd.basedata.cloudclient;

import com.njwd.ledger.api.ParameterSetApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/14 11:24
 */
@FeignClient(name = "ledger",contextId = "ParameterSetLedgerFeignClient")
public interface ParameterSetLedgerFeignClient extends ParameterSetApi {
}
