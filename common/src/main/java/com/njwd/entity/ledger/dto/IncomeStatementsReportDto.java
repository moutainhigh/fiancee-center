package com.njwd.entity.ledger.dto;

import com.njwd.entity.ledger.vo.IncomeStatementsAccountBookVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
* @description: 财务报告利润表
* @author LuoY
* @date 2019/8/6 13:58
*/
@Getter
@Setter
public class IncomeStatementsReportDto extends QueryFinancialReportDto {

    /**
     * 是否本年累计:0,否，1,是
     */
    private byte yearCumulative ;

    /**
     * 是否同比:0,否，1,是
     */
    private byte yearOnYear;

    /**
     * 是否环比:0,否，1,是
     */
    private byte monthOnMonth;

    /**
     * 是否增长率:0,否，1,是
     */
    private byte growRate;

    /**
     * 是否占收入比:0,否，1,是
     */
    private byte incomeRatio;

    /**
     * 是否科目明细:0,否，1,是
     */
    private byte subjectDetail;
}
