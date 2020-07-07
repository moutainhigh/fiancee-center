package com.njwd.basedata.cloudclient;


import com.njwd.platform.api.AuxiliaryItemApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 辅助核算
 *
 * @author 周鹏
 * @since 2019/6/19
 */
@FeignClient(name = "platform", contextId = "AuxiliaryFeignClient")
public interface AuxiliaryFeignClient extends AuxiliaryItemApi {


}
