package com.njwd.entity.ledger;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
public class BalanceSubjectInitAuxiliary implements Serializable {
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
    * 期初余额
    */
    private BigDecimal openingBalance;

    /**
    * 本年借方
    */
    private BigDecimal thisYearDebitAmount;

    /**
    * 本年贷方
    */
    private BigDecimal thisYearCreditAmount;

    /**
    * 年初余额
    */
    private BigDecimal yearOpeningBalance;

    private static final long serialVersionUID = 1L;
}