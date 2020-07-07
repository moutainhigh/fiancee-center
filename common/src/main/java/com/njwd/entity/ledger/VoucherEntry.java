package com.njwd.entity.ledger;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/7/24
 */
@Getter
@Setter
@TableName("wd_voucher_entry_%s")
public class VoucherEntry implements Serializable {
    /**
    * 主键 默认自动递增
    */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
    * 凭证ID 【wd_voucher】表ID
    */
    private Long voucherId;

    /**
    * 序号
    */
    private Integer rowNum;

    /**
    * 摘要
    */
    private String abstractContent;

    /**
    * 科目ID
    */
    private Long accountSubjectId;

    /**
    * 本位币 借方金额 = 原币借方金额*汇率
    */
    private BigDecimal debitAmount;

    /**
    * 本位币 贷方金额 = 原币贷方金额*汇率
    */
    private BigDecimal creditAmount;

    /**
    * 原币币种 默认账簿本位币
    */
    private Long originalCoin;

    /**
    * 汇率
    */
    private BigDecimal exchangeRate;

    /**
    * 原币借方金额
    */
    private BigDecimal originalDebitAmount;

    /**
    * 原币贷方金额
    */
    private BigDecimal originalCreditAmount;

    /**
    * 现金流量类型 0不需要指定现金流量 1未指定 2已指定
    */
    private Byte cashFlowType;

    /**
    * 内部往来类型 0不需要生成协同凭证 1未生成 2已生成
    */
    private Byte interiorType;

    private static final long serialVersionUID = 1L;
}