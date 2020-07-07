package com.njwd.basedata.cloudclient;

import com.njwd.ledger.api.VoucherApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
* @description: 凭证
* @author LuoY
* @date 2019/8/26 13:44
*/
@FeignClient(name = "ledger",contextId = "VoucherFeignClient")
public interface VoucherFeignClient extends VoucherApi {
}
