package com.njwd.platform.api;

import com.njwd.entity.platform.dto.FinancialReportItemSetDto;
import com.njwd.entity.platform.vo.FinancialReportItemSetVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 财务报告项目明细设置
 * @Date:17:11 2019/7/12
 **/
@RequestMapping("platform/financialReportItemSet")
public interface FinancialReportItemSetApi {
    @PostMapping("findFinancialReportItemSetList")
    Result<List<FinancialReportItemSetVo>> findFinancialReportItemSetList(@RequestBody FinancialReportItemSetDto platformFinancialReportItemSetDto);
}
