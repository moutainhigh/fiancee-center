package com.njwd.ledger.service;

import com.njwd.entity.ledger.dto.BalanceDto;
import com.njwd.entity.ledger.dto.CashFlowReportDto;
import com.njwd.entity.ledger.dto.IncomeStatementsReportDto;
import com.njwd.entity.ledger.vo.IncomeStatementsAccountBookVo;
import com.njwd.entity.platform.vo.FinancialReportItemSetVo;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
* @description: 财务总账-财务报告-利润表,现金流量表
* @author LuoY
* @date 2019/7/26 13:46
*/
public interface FinancialReportService {
    /**
    * @description: 根据条件查询指定利润表
    * @param incomeStatementsTableDto
    * @return java.util.List<com.njwd.ledger.entity.vo.IncomeStatementsAccountEntityVo>
    * @author LuoY
    * @date 2019/7/26 13:48
    */
    List<IncomeStatementsAccountBookVo> findCurrentIssueProfit(@NotNull IncomeStatementsReportDto incomeStatementsTableDto);

    /**
    * @description: 根据条件查询指定现金流量表
    * @param cashFlowReportDto
    * @return java.util.List<com.njwd.entity.ledger.vo.IncomeStatementsAccountBookVo>
    * @author LuoY
    * @date 2019/8/9 11:33
    */
    List<IncomeStatementsAccountBookVo> findCashFlowReport(@NotNull CashFlowReportDto cashFlowReportDto);

    /**
    * @description: 财务报告计算公式
    * @param financialReportItemSetVos 财务报告, balanceSubject 科目余额,cashFlowItemMap 现金流量公式code方向
    * @return java.util.Map<java.lang.String,java.math.BigDecimal>
    * @author LuoY
    * @date 2019/8/6 14:44
    */
    Map<String, BigDecimal> financialReportCalculationResult(@NotNull List<FinancialReportItemSetVo> financialReportItemSetVos, @NotNull Map<String,String> balanceSubject,@NotNull BalanceDto balanceDto,Map<String,Byte> cashFlowItemMap);

    /**
    * @description: 导出利润表
    * @Param [response, incomeStatementsTableDto]
    * @return void
    * @author LuoY
    * @date 2019/8/30 9:20
    */
    void exportIssueProfit(HttpServletResponse response,IncomeStatementsReportDto incomeStatementsReportDto);

    /**
     * @description: 导出利润表
     * @Param [response, incomeStatementsTableDto]
     * @return void
     * @author LuoY
     * @date 2019/8/30 9:20
     */
    void exportCashFlowReport(HttpServletResponse response,CashFlowReportDto cashFlowReportDto);
}
