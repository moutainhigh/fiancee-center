package com.njwd.basedata.cloudclient;


import com.njwd.platform.api.AccountBookTypeApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 账簿类型
 *
 * @author 周鹏
 * @since 2019/6/26
 */
@FeignClient(name = "platform", contextId = "AccountBookTypeFeignClient")
public interface AccountBookTypeFeignClient extends AccountBookTypeApi {


}
