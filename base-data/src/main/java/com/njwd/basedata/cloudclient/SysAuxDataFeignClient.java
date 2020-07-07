package com.njwd.basedata.cloudclient;


import com.njwd.platform.api.SysAuxDataApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 辅助资料
 *
 * @author 周鹏
 * @since 2019/6/24
 */
@FeignClient(name = "platform", contextId = "SysAuxDataFeignClient")
public interface SysAuxDataFeignClient extends SysAuxDataApi {


}
