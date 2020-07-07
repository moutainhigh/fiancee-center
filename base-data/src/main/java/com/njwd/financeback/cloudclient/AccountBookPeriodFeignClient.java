package com.njwd.financeback.cloudclient;

import com.njwd.ledger.api.AccountBookPeriodApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 账簿期间
 *
 * @author zhuzs
 * @date 2019-08-05 15:22
 */
@FeignClient(name = "ledger", contextId = "AccountBookPeriodFeignClient")
public interface AccountBookPeriodFeignClient  extends AccountBookPeriodApi {
}

