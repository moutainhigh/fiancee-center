package com.njwd.entity.ledger.dto;

import com.njwd.entity.base.query.BaseLedgerQueryDto;
import com.njwd.entity.ledger.vo.BalanceCashFlowVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @ClassName BalanceCashFlowDto
 * @Description 现金流量项目余额Dto
 * @Author libao
 * @Date 2019/8/5 11:45
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BalanceCashFlowQueryDto extends BaseLedgerQueryDto {
	/**
	 * 时间查询范围
	 */
	private byte voucherDateOperator;

	/**
	 * 时间查询范围
	 */
	private List<String> voucherDate;



	/**
	 * 是否包含本年累计(0:不包含 1:包含)
	 */
	private Byte isIncludeTotalAmount;


}