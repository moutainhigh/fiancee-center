package com.njwd.ledger.controller;

import com.njwd.entity.ledger.dto.BalanceDto;
import com.njwd.entity.ledger.dto.CashFlowReportDto;
import com.njwd.entity.ledger.dto.IncomeStatementsReportDto;
import com.njwd.entity.ledger.vo.BalanceVo;
import com.njwd.entity.ledger.vo.IncomeStatementsAccountBookVo;
import com.njwd.ledger.service.BalanceService;
import com.njwd.ledger.service.FinancialReportService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
* @description: 财务总账-财务报告
* @author LuoY
* @date 2019/7/25 16:10
*/
@RestController
@RequestMapping("financialReport")
public class FinancialReportController extends BaseController {
    @Resource
    private FinancialReportService financialReportService;

    @Resource
    private BalanceService balanceService;

    /**
    * @description: 根据条件查询财务报告-利润表
    * @param incomeStatementsTableDto
    * @return java.util.List<com.njwd.ledger.entity.vo.IncomeStatementsAccountEntityVo>
    * @author LuoY
    * @date 2019/7/30 9:17
    */
    @RequestMapping("findProfitStatementByCondition")
    public Result<List<IncomeStatementsAccountBookVo>> findProfitStatementByCondition(@RequestBody IncomeStatementsReportDto incomeStatementsTableDto){
        return ok(financialReportService.findCurrentIssueProfit(incomeStatementsTableDto));
    }

    /**
    * @description: 根据条件查询财务报告-现金流量表
    * @param cashFlowReportDto
    * @return com.njwd.support.Result<java.util.List<com.njwd.entity.ledger.vo.IncomeStatementsAccountBookVo>>
    * @author LuoY
    * @date 2019/8/9 17:09
    */
    @RequestMapping("findCashFlowReportByCondition")
    public Result<List<IncomeStatementsAccountBookVo>> findCashFlowReportByCondition(@RequestBody CashFlowReportDto cashFlowReportDto){
        return ok(financialReportService.findCashFlowReport(cashFlowReportDto));
    }

    /**
     * 资产负债表
     *
     * @param: [balanceDtoList]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.ledger.vo.BalanceVo>>
     * @author: zhuzs
     * @date: 2019-09-16 14:38
     */
    @RequestMapping("findBalanceReport")
    public Result<List<BalanceVo>> findBalanceReport(@RequestBody List<BalanceDto> balanceDtoList){
        return ok(balanceService.getBalanceReport(balanceDtoList));
    }

    /**
     * 资产负债表——导出
     *
     * @param: [balanceDtos, response]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 14:38
     */
    @RequestMapping("balanceReportExport")
    public void balanceReportExport(@RequestBody List<BalanceDto> balanceDtos, HttpServletResponse response){
        balanceService.balanceReportExport(balanceDtos,response);
    }

    /**
    * @description: 利润表导出
    * @Param [response, incomeStatementsTableDto]
    * @return void
    * @author LuoY
    * @date 2019/9/3 15:52
    */
    @RequestMapping("exportProfitStatementReport")
    public void exportProfitStatementReport(HttpServletResponse response,@RequestBody IncomeStatementsReportDto incomeStatementsTableDto){
        financialReportService.exportIssueProfit(response,incomeStatementsTableDto);
    }

    /**
     * @description: 现金流量表导出
     * @Param [response, incomeStatementsTableDto]
     * @return void
     * @author LuoY
     * @date 2019/9/3 15:52
     */
    @RequestMapping("exportCashFlowReport")
    public void exportCashFlowReport(HttpServletResponse response,@RequestBody CashFlowReportDto cashFlowReportDto){
        financialReportService.exportCashFlowReport(response,cashFlowReportDto);
    }
}
