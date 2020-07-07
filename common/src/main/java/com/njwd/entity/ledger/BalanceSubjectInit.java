package com.njwd.entity.ledger;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.njwd.annotation.ExcelCell;
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
public class BalanceSubjectInit implements Serializable {

    private static final long serialVersionUID = 6454475022758310446L;

    /**
    * 主键 默认自动递增
    */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
    * 子系统启用记录ID
    */
    private Long accountBookSystemId;

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
    @ExcelCell(index = 4)
    private BigDecimal openingBalance;

    /**
    * 本年借方
    */
    @ExcelCell(index = 5)
    private BigDecimal thisYearDebitAmount;

    /**
    * 本年贷方
    */
    @ExcelCell(index = 6)
    private BigDecimal thisYearCreditAmount;

    /**
    * 年初余额
    */
    private BigDecimal yearOpeningBalance;
}