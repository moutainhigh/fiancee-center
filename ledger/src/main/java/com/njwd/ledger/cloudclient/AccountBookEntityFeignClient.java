package com.njwd.ledger.cloudclient;

import com.njwd.basedata.api.AccountBookEntityApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author lj
 * @Description 账簿API
 * @Date:14:46 2019/8/6
 **/
@FeignClient(name = "base-data", contextId = "AccountBookEntityFeignClient")
public interface AccountBookEntityFeignClient extends AccountBookEntityApi {
}
