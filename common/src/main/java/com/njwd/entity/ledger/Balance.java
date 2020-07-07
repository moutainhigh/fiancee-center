package com.njwd.entity.ledger;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author 朱小明
 * @since 2019/8/29
 */
@Data
public class Balance implements Serializable {

    private static final long serialVersionUID = -794337379730316044L;
    /**
    * 主键 默认自动递增
    */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
    * 账簿ID
    */
    private Long accountBookId;

    /**
    * 核算主体ID
    */
    private Long accountBookEntityId;

    /**
    * 科目id
    */
    private Long accountSubjectId;

    /**
    * 启用期间年度
    */
    private Integer periodYear;

    /**
    * 启用期间号
    */
    private Byte periodNum;

    /**
     * 记账期间年号
     **/
    private Integer periodYearNum;

    /**
    * 期初余额
    */
    private BigDecimal openingBalance;

    /**
    * 期末余额
    */
    private BigDecimal closingBalance;

    /**
    * 本期借方
    */
    private BigDecimal debitAmount;

    /**
    * 本期贷方
    */
    private BigDecimal creditAmount;

    /**
    * 借方累计
    */
    private BigDecimal totalDebitAmount;

    /**
    * 贷方累计
    */
    private BigDecimal totalCreditAmount;

    /**
    * 已过账本期借方
    */
    private BigDecimal postDebitAmount;

    /**
    * 已过账本期贷方
    */
    private BigDecimal postCreditAmount;

    /**
    * 已过账借方累计
    */
    private BigDecimal postTotalDebitAmount;

    /**
    * 已过账贷方累计
    */
    private BigDecimal postTotalCreditAmount;

    /**
    * 损益本期借方
    */
    private BigDecimal syDebitAmount;

    /**
    * 损益本期贷方
    */
    private BigDecimal syCreditAmount;

    /**
    * 损益借方累计
    */
    private BigDecimal syTotalDebitAmount;

    /**
    * 损益贷方累计
    */
    private BigDecimal syTotalCreditAmount;

    /**
    * 已过账损益本期借方
    */
    private BigDecimal postSyDebitAmount;

    /**
    * 已过账损益本期贷方
    */
    private BigDecimal postSyCreditAmount;

    /**
    * 已过账损益借方累计
    */
    private BigDecimal postSyTotalDebitAmount;

    /**
    * 已过账损益贷方累计
    */
    private BigDecimal postSyTotalCreditAmount;

}