package com.njwd.entity.ledger.dto;

import com.njwd.entity.ledger.vo.BalanceCashFlowVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName BalanceCashFlowDto
 * @Description 现金流量项目余额Dto
 * @Author libao
 * @Date 2019/8/5 11:45
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BalanceCashFlowDto extends BalanceCashFlowVo {
	/**
	 * 是否包含未记账  0：不包含  1：包含  默认0
	 */
	private Byte isContainNotPosting;
	/**
	 * 时间方式  0：会计区间  1：制单日期
	 */
	private Byte dateType;

	/**
	 * 开始时间（期间）
	 */
	private String startTime;

	/**
	 * 结束时间（期间）
	 */
	private String endTime;

	/**
	 * 起始期间
	 */
	private Byte lastPeriod;

}