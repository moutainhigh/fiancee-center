package com.njwd.basedata.cloudclient;

import com.njwd.ledger.api.SysInitDataApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/12 14:55
 */
@FeignClient(name = "ledger",contextId = "SysInitDataFeignClient")
public interface SysInitDataFeignClient extends SysInitDataApi {

}
