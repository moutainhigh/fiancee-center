package com.njwd.basedata.cloudclient;

import com.njwd.platform.api.BankApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 银行
 *
 * @author 郑勇浩
 * @date 2019-07-02 13:27
 */
@FeignClient(name = "platform", contextId = "BankFeignClient")
public interface BankFeignClient extends BankApi {
}
