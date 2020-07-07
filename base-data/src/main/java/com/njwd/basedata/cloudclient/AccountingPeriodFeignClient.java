package com.njwd.basedata.cloudclient;

import com.njwd.platform.api.AccountingPeriodApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 会计期间
 *
 * @author zhuzs
 * @date 2019-07-02 14:03
 */
@FeignClient(name = "platform",contextId = "AccountingPeriodFeignClient")
public interface AccountingPeriodFeignClient extends AccountingPeriodApi {
}
