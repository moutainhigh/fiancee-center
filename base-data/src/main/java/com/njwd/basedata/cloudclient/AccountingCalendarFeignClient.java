package com.njwd.basedata.cloudclient;

import com.njwd.platform.api.AccountingCalendarApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 会计日历
 *
 * @author zhuzs
 * @date 2019-07-02 13:44
 */
@FeignClient(name = "platform",contextId = "AccountingCalendarFeignClient")
public interface AccountingCalendarFeignClient extends AccountingCalendarApi {
}

