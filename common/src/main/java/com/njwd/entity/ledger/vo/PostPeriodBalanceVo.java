package com.njwd.entity.ledger.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 *@description: 过账 计算科目金额
 *@author: fancl
 *@create: 2019-08-22 
 */
@Getter
@Setter
public class PostPeriodBalanceVo implements Serializable {
    private Long accountBookId;     //账簿
    private Long accountBookEntityId;//核算主体
    private Integer periodYearNum;  //账簿期间
    private Long accountSubjectId;  //科目id
    private Long cashFlowItemId;     //现金流量项目id
    private BigDecimal debitAmount; //借方金额
    private BigDecimal creditAmount; //贷方金额
    private BigDecimal currencyAmount;//货币金额

    //辅助核算
    private Long balanceAuxiliaryId;  //辅助核算id
    private Long entryId;  //分录id
    private String sourceTable; //值来源
    private Long itemValueId;//辅助核算值id

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("balanceAuxiliaryId:").append(balanceAuxiliaryId).append(", accountSubjectId:").append(accountSubjectId).append(",sourceTable:").append(sourceTable).append(",itemValueId:").append(itemValueId)
        .append(",debitAmount").append(debitAmount).append(",creditAmount").append(creditAmount);
        return sb.toString();
    }
}
