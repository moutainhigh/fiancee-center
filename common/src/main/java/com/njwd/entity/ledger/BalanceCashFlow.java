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
public class BalanceCashFlow implements Serializable {
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
    * 现金流量项目id
    */
    private Long itemId;

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
    * 本期发生额
    */
    private BigDecimal occurAmount;

    /**
    * 本年累计
    */
    private BigDecimal totalAmount;

    /**
    * 已过账本期发生额
    */
    private BigDecimal postOccurAmount;

    /**
    * 已过账本年累计
    */
    private BigDecimal postTotalAmount;

    private static final long serialVersionUID = 1L;
}