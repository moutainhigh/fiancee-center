package com.njwd.ledger.cloudclient;

import com.njwd.basedata.api.AccountingItemApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author wuweiming
 * @Description 自定义核算API
 * @Date:09:49 2019/08/07
 **/
@FeignClient(name = "base-data-wwm", contextId = "AccountingItemClient")
public interface AccountingItemClient extends AccountingItemApi {
}
