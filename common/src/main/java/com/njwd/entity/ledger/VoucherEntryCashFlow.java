package com.njwd.entity.ledger;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/7/24
 */
@Getter
@Setter
@TableName("wd_voucher_entry_cash_flow_%s")
public class VoucherEntryCashFlow implements Serializable {
	/**
	 * 主键 默认自动递增
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 凭证ID
	 */
	private Long voucherId;

	/**
	 * 凭证分录id 【wd_voucher_entry】表ID
	 */
	private Long entryId;

	/**
	 * 序号
	 */
	private Integer rowNum;

	/**
	 * 对方分录id
	 */
	private Long oppositeEntryId;

	/**
	 * 主表项目ID
	 */
	private Long cashFlowItemId;

	/**
	 * 本位币金额
	 */
	private BigDecimal currencyAmount;

	private static final long serialVersionUID = 1L;
}