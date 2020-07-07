package com.njwd.ledger.cloudclient;

import com.njwd.basedata.api.AccountBookApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 核算账簿
 *
 * @author xyyxhcj@qq.com
 * @since 2019-08-03
 */
@FeignClient(name = "base-data", contextId = "AccountBookFeignClient")
public interface AccountBookFeignClient extends AccountBookApi {
}
