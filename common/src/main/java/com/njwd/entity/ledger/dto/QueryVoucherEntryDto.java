package com.njwd.entity.ledger.dto;

import com.njwd.entity.base.query.BaseLedgerQueryDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/07/24
 */
@Getter
@Setter
public class QueryVoucherEntryDto extends BaseLedgerQueryDto {
	private static final long serialVersionUID = -1603424478695763559L;
	/**
	 * 分录是否修改 1是 0否
	 */
	private byte isModify;



	/**
	 * 制单日期
	 */
	private List<String> voucherDate;

	/**
	 * 制单日期查询类型
	 */
	private byte voucherDateOperator;

	/**
	 * 现金流量查询类型
	 */
	private byte cashFlowItemOperator;

	/**
	 * 现金流量Id集合
	 */
	private List<Long> cashFlowItemIds;
	/**
	 * 是否展示科目全名(0：不展示， 1：展示  默认0)
	 */
	private Byte isShowFullName;




}
