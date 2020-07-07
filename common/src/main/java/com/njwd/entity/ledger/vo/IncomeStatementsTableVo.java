package com.njwd.entity.ledger.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
* @description: 利润表
* @author LuoY
* @date 2019/8/8 11:23
*/
@Getter
@Setter
public class IncomeStatementsTableVo extends QueryFinancialReportVo {

    /**
     * 本期占收入比
     */
    private BigDecimal currentIncomeRatio;


    /**
     * 本年累计占收入比
     */
    private BigDecimal yearIncomeRatio;

    /**
     * 同比
     */
    private BigDecimal yearOnYear;

    /**
     * 同比占收入比
     */
    private BigDecimal yearCompareIncomeRatio;

    /**
     * 环比
     */
    private BigDecimal monthOnMonth;

    /**
     * 环比占收入比
     */
    private BigDecimal monthCompareIncomeRatio;

    /**
     * 增长率
     */
    private BigDecimal growRate;

    /**
     * 是否科目明细
     */
    private Byte isSubject;

    /**
     * 科目明细对应科目余额code
     */
    private String reportCode;
}
