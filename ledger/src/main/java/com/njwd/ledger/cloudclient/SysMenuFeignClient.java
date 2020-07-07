package com.njwd.ledger.cloudclient;

import com.njwd.basedata.api.SysMenuApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 权限
 *
 * @author xyyxhcj@qq.com
 * @since 2019-08-03
 */
@FeignClient(name = "base-data", contextId = "SysMenuFeignClient")
public interface SysMenuFeignClient extends SysMenuApi {
}
