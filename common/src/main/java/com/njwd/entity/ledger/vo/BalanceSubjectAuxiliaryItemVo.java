package com.njwd.entity.ledger.vo;

import com.njwd.entity.ledger.BalanceSubjectAuxiliary;
import com.njwd.entity.ledger.BalanceSubjectAuxiliaryItem;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author 周鹏
 * @since 2019/8/15
 */
@Getter
@Setter
public class BalanceSubjectAuxiliaryItemVo extends BalanceSubjectAuxiliaryItem {
	/**
	 * 总账账簿名称
	 */
	private String accountBookName;

	/**
	 * 核算主体名称
	 */
	private String accountBookEntityName;

	/**
	 * 核算项来源表拼接
	 */
	private String sourceTables;

	/**
	 * 核算值ID拼接
	 */
	private String itemValueIds;

	/**
	 * 辅助核算余额明细ID拼接
	 */
	private String balanceAuxiliaryIds;

	/**
	 * 辅助核算余额明细ID集合
	 */
	private List<String> auxiliaryIds;

	/**
	 * 辅助核算来源表集合
	 */
	private List<String> sourceTableList;

	/**
	 * 辅助核算值来源集合
	 */
	private List<String> itemValueIdList;

	/**
	 * 辅助核算余额表
	 */
	private BalanceSubjectAuxiliary balanceSubjectAuxiliary;

	/**
	 * 账簿启用期间
	 */
	private Integer startPeriod;

}