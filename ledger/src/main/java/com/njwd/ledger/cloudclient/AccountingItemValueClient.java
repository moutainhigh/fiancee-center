package com.njwd.ledger.cloudclient;

import com.njwd.basedata.api.AccountingItemValueApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author wuweiming
 * @Description 自定义核算API
 * @Date:09:49 2019/08/07
 **/
@FeignClient(name = "base-data", contextId = "AccountingItemValueClient")
public interface AccountingItemValueClient extends AccountingItemValueApi {
}
