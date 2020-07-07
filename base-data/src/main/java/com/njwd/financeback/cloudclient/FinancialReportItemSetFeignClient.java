package com.njwd.financeback.cloudclient;

import com.njwd.platform.api.FinancialReportItemSetApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 *
 * @description:财务报告明细设置
 *
 * @author: Zhuzs
 * @create: 2019-09-02 10:58
 */
@FeignClient(name = "platform", contextId = "FinancialReportItemSetFeignClient")
public interface FinancialReportItemSetFeignClient extends FinancialReportItemSetApi {
}
