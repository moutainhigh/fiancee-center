package com.njwd.basedata.cloudclient;

import com.njwd.platform.api.TaxCategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/18 9:55
 */
@FeignClient(name = "platform",contextId = "TaxCategoryFeignClient")
public interface TaxCategoryFeignClient extends TaxCategoryApi {
}
