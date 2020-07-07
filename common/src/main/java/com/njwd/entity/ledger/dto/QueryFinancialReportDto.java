package com.njwd.entity.ledger.dto;

import com.njwd.entity.ledger.vo.IncomeStatementsAccountBookVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
* @description:  财务报告查询-公共dto
* @author LuoY
* @date 2019/8/9 11:05
*/
@Getter
@Setter
public class QueryFinancialReportDto {
    /**
     * 账簿信息
     */
    private List<IncomeStatementsAccountBookVo> incomeStatementsAccountBookVoList;
    /**
     * 启用期间年度
     */
    private Integer periodYear;

    /**
     * 启用期间号
     */
    private Byte periodNum;

    /**
     * 是否过账: 0,未过账，1,已过账
     */
    private byte posting;
}
