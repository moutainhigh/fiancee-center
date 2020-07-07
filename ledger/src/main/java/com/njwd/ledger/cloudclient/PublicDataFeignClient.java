package com.njwd.ledger.cloudclient;

import com.njwd.basedata.api.PublicDataApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 基础资料的客户和供应商控制层
 *
 * @author xyyxhcj@qq.com
 * @since 2019-08-03
 */
@FeignClient(name = "base-data", contextId = "PublicDataFeignClient")
public interface PublicDataFeignClient extends PublicDataApi {
}
