package com.njwd.basedata.cloudclient;

import com.njwd.ledger.api.BalanceSubjectAuxiliaryApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
* @description: 科目期初辅助核算
* @author LuoY
* @date 2019/8/23 17:28
*/
@FeignClient(name = "ledger",contextId = "BalanceSubjectAuxiliaryItemFeignClient")
public interface BalanceSubjectAuxiliaryItemFeignClient extends BalanceSubjectAuxiliaryApi {
}
