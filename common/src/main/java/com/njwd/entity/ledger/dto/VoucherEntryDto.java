package com.njwd.entity.ledger.dto;

import com.njwd.entity.ledger.vo.VoucherEntryVo;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/07/24
 */
@Getter
@Setter
public class VoucherEntryDto extends VoucherEntryVo {
	private static final long serialVersionUID = -1603424478695763559L;
	/**
	 * 分录是否修改 1是 0否
	 */
	private byte isModify;
	/**
	 * 新序号
	 **/
	private Integer newRowNum;

	private List<VoucherEntryAuxiliaryDto> editAuxiliaryList = new ArrayList<>();

	private List<VoucherEntryCashFlowDto> editCashFlowList = new ArrayList<>();
	/**
	 * 内部往来 对方凭证分录
	 **/
	private VoucherEntryDto editInteriorEntry;

	/**
	 * 凭证主体DTO
	 */
	private VoucherDto voucherDto;

	/**
	 * 科目对应的辅助核算余额表ID
	 **/
	private Long auxiliaryBalanceId;
}
