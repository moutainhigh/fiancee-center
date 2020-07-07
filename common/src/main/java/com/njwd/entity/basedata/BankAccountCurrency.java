package com.njwd.entity.basedata;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class BankAccountCurrency implements Serializable {
	/**
	 * 主键 默认自动递增
	 */
	private Long id;

	/**
	 * 银行账号ID 【银行账号】表ID
	 */
	private Long bankAccountId;

	/**
	 * 币种ID 【币种】表ID
	 */
	private Long currencyId;

	/**
	 * 币种名称
	 */
	private String currencyName;

	/**
	 * 创建时间
	 */
	private Date createTime;

	private static final long serialVersionUID = 1L;
}