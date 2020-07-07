package com.njwd.ledger.cloudclient;

import com.njwd.basedata.api.AccountBookSystemApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author lj
 * @Description 账簿启用子系统记录
 * @Date:15:19 2019/8/6
 **/
@FeignClient(name = "base-data", contextId = "AccountBookSystemFeignClient")
public interface AccountBookSystemFeignClient extends AccountBookSystemApi {
}
