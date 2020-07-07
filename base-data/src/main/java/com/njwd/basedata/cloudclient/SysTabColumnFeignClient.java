package com.njwd.basedata.cloudclient;


import com.njwd.platform.api.SysTabColumnApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author fancl
 * @desc 表格配置Api,调用运营平台接口
 */
@FeignClient(name = "platform",contextId = "SysTabColumnFeignClient")
public interface SysTabColumnFeignClient extends SysTabColumnApi {



}
