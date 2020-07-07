package com.njwd.basedata.cloudclient;

import com.njwd.platform.api.CurrencyApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/15 9:25
 */
@FeignClient(name="platform",contextId = "CurrencyFeignClient")
public interface CurrencyFeignClient extends CurrencyApi {
}
