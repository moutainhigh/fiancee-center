package com.njwd.entity.ledger.vo;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
* @description: 财务总账-财务报告-基础数据类
* @author LuoY
* @date 2019/7/26 17:26
*/
@Getter
@Setter
public class IncomeStatementsVo {
    /**
     * 账簿名称
     */
    private String accountBookName;

    /**
     * 核算主体名称
     */
    private String accountBookEntityName;

    /**
     * 科目id
     */
    private Long AccountSubjectId;

    /**
     * 科目UpCode
     */
    private String AccountSubjectUpCode;

    /**
     * 科目code
     */
    private String AccountSubjectCode;

    /**
     * 科目name
     */
    private String AccountSubjectName;

    /**
     * 科目FullName
     */
    private String AccountSubjectFullName;

    /**
     * 本期金额
     */
    private BigDecimal currentMoney;

    /**
     * 已过账本期金额
     */
    private BigDecimal postCurrentMoney;

    /**
     * 本年累计
     */
    private BigDecimal totalCurrentMoney;

    /**
     * 已过账本年累计
     */
    private BigDecimal postTotalCurrentMoney;

    /**
     * 余额方向 0：借方、1：贷方
     */
    private Byte balanceDirection;

}
