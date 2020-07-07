package com.njwd.ledger.cloudclient;

import com.njwd.basedata.api.BankAccountApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author lj
 * @Description 银行账户
 * @Date:10:43 2019/8/20
 **/
@FeignClient(name = "base-data", contextId = "BankAccountFeignClient")
public interface BankAccountFeignClient extends BankAccountApi {
}
