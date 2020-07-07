package com.njwd.basedata.cloudclient;

import com.njwd.platform.api.AccountBookCategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 根据 账簿类型ID 和 租户ID 获取会计准则（含税制、记账本位币信息）
 *
 * @author zhuzs
 * @date 2019-07-02 14:03
 */
@FeignClient(name = "platform",contextId = "AccountStandardFeignClient")
public interface AccountStandardFeignClient extends AccountBookCategoryApi {
}
