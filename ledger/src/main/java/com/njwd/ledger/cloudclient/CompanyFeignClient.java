package com.njwd.ledger.cloudclient;

import com.njwd.basedata.api.CompanyApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 公司API
 *
 * @author zhuzs
 * @date 2019-08-07 14:15
 */
@FeignClient(name = "base-data",contextId = "CompanyFeignClient")
public interface CompanyFeignClient extends CompanyApi {
}
