package com.njwd.ledger.cloudclient;

import com.njwd.basedata.api.BusinessUnitApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @description:
 * @author: xdy
 * @create: 2019/10/17 9:25
 */
@FeignClient(name = "base-data",contextId = "BusinessUnitFeignClient")
public interface BusinessUnitFeignClient extends BusinessUnitApi {
}
