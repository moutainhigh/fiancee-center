package com.njwd.entity.ledger.vo;


import com.njwd.entity.ledger.BalanceCashFlow;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @ClassName BalanceCashFlowVo
 * @Description 现金流量余额Vo
 * @Author libao
 * @Date 2019/8/5 11:41
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BalanceCashFlowVo extends BalanceCashFlow {
	/**
	 * 现金流量项目code
	 */
	private String code;

	/**
	 * 现金流量项目名称
	 */
	private String name;

	/**
	 * 现金流量项目方向
	 */
	private byte cashFlowDirection;
	/**
	 * 现金流量Id
	 */
	private Long cashFlowId;

	/**
	 * 现金流量级次
	 */
	private byte level;

	/**
	 * 账簿名称
	 */
	private String accountBookName;

	/**
	 * 主体名称
	 */
	private String accountBookEntityName;


	/**
	 * 现金流量项目ID集合
	 */
	private List<Long> ids;
}

