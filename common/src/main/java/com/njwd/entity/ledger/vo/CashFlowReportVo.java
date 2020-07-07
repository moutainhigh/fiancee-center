package com.njwd.entity.ledger.vo;

import com.njwd.entity.ledger.BalanceCashFlow;
import lombok.Getter;
import lombok.Setter;

/**
* @description:  财务报告-现金流量表
* @author LuoY
* @date 2019/8/9 14:24
*/
@Getter
@Setter
public class CashFlowReportVo extends BalanceCashFlow {
    /**
     * 项目code
     */
    private String code;

    /**
     * 现金流量项目方向
     */
    private byte cashFlowDirection;
}
