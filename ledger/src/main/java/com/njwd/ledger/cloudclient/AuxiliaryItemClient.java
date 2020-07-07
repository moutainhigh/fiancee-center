package com.njwd.ledger.cloudclient;

import com.njwd.platform.api.AuxiliaryItemApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author wuweiming
 * @Description 辅助核算API
 * @Date:09:41 2019/08/07
 **/
@FeignClient(name = "platform-wwm", contextId = "AuxiliaryItemClient")
public interface AuxiliaryItemClient extends AuxiliaryItemApi {
}
