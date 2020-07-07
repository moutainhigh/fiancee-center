package com.njwd.basedata.cloudclient;


import com.njwd.platform.api.AccountElementItemApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 会计要素项
 *
 * @author 周鹏
 * @since 2019/8/22
 */
@FeignClient(name = "platform", contextId = "AccountElementItemFeignClient")
public interface AccountElementItemFeignClient extends AccountElementItemApi {


}
