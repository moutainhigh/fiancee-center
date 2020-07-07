package com.njwd.basedata.cloudclient;

import com.njwd.platform.api.FinancialReportApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 资产负债表
 *
 * @author zhuzs
 * @date 2019-07-02 13:27
 */
@FeignClient(name = "platform",contextId = "FinancialReportFeignClient")
public interface FinancialReportFeignClient extends FinancialReportApi {
}
