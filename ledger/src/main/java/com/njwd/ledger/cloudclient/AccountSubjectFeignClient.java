package com.njwd.ledger.cloudclient;

import com.njwd.basedata.api.AccountSubjectApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 会计科目
 *
 * @author xyyxhcj@qq.com
 * @since 2019-08-03
 */
@FeignClient(name = "base-data", contextId = "AccountSubjectFeignClient")
public interface AccountSubjectFeignClient extends AccountSubjectApi {
}
