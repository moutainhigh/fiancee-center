package com.njwd.entity.ledger.vo;

import com.njwd.entity.ledger.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/07/24
 */
@Getter
@Setter
public class VoucherVo extends Voucher {
	private static final long serialVersionUID = 7573557794432771963L;
	private List<VoucherEntry> entryList;
	/**
	 * 辅助核算字典,key为分录ID
	 */
	private Map<Long, List<VoucherEntryAuxiliary>> entryAuxiliaryMap;
	/**
	 * 现金流量字典,key为分录ID
	 **/
	private Map<Long, List<VoucherEntryCashFlow>> entryCashFlowMap;

	/**
	 * 已开启的期间
	 **/
	private List<AccountBookPeriod> accountBookPeriods;
	/**
	 * 分录数据
	 */
	private List<VoucherEntryVo> voucherEntryVos;
	/**
	 * 凭证字号
	 */
	private String credentialWordCode;
	/**
	 * 是否被切割凭证数据最后一段 0 否 1 是
	 */
	private Byte isEndFlag;
	/**
	 * 摘要
	 */
	private String abstractContent;
	/**
	 * 是否已结账，0：否、1：是
	 */
	private Byte isSettle;
	/**
	 * 凭证打印 分割后的 凭证数据
	 */
	private List<VoucherVo> voucherVoList;
	/**
	 * 打印模板
	 */
	private Byte printModel;
	/**
	 * 账簿编码
	 */
	private String accountBookCode;
    /**
     * 来源 凭证字类型 1：记 、2：收、3：付、4：转
     */
    private Byte oppositeCredentialWord;

    /**
     * 来源 凭证主号
     */
    private Integer oppositeMainCode;
}
