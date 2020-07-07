package com.njwd.ledger.cloudclient;

import com.njwd.basedata.api.SysUserApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 权限
 *
 * @author xyyxhcj@qq.com
 * @since 2019-08-03
 */
@FeignClient(name = "base-data", contextId = "SysUserFeignClient")
public interface SysUserFeignClient extends SysUserApi {
}
