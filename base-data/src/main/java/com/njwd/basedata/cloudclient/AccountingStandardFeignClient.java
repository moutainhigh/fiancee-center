package com.njwd.basedata.cloudclient;


import com.njwd.platform.api.AccountingStandardApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 会计准则
 *
 * @author 周鹏
 * @since 2019/6/26
 */
@FeignClient(name = "platform", contextId = "AccountingStandardFeignClient")
public interface AccountingStandardFeignClient extends AccountingStandardApi {


}
