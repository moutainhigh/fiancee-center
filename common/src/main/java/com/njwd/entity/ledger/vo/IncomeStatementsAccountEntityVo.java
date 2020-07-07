package com.njwd.entity.ledger.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
* @description: 核算主体信息
* @author LuoY
* @date 2019/7/29 9:47
*/
@Getter
@Setter
public class IncomeStatementsAccountEntityVo {

    /**
     * 核算主体ID
     */
    private Long accountBookEntityId;

    /**
     * 核算主体名称
     */
    private String accountBookEntityName;

    /**
     * 利润表详细数据
     */
    private List<IncomeStatementsTableVo> incomeStatementsTablesListVos;

    /**
     * 现金流量表表详细数据
     */
    private List<CashFlowReportTabletVo> cashFlowReportTabletVos;

}
