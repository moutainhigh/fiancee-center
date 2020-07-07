package com.njwd.entity.basedata.vo;

import com.njwd.annotation.ExcelCell;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.BankAccount;
import com.njwd.entity.basedata.BankAccountCurrency;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;
import java.util.List;

/**
 * @Description 银行账户 vo.
 * @Date 2019-06-12 11:21
 * @Author 郑勇浩
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BankAccountVo extends BankAccount {

	/**
	 * 核算主体名称 对应 auxiliaryId
	 */
	@ExcelCell(index = 3)
	private String businessUnitName;
	/**
	 * 归属公司名称 对应 companyId
	 */
	@ExcelCell(index = 1)
	private String companyName;
	/**
	 * 使用公司名称 对应 useCompanyId
	 */
	private String useCompanyName;
	/**
	 * 状态 中文(0：已失效、1：已生效) 对应isEnable
	 */
	private String isEnableStr;

	/**
	 * 上一个id
	 */
	private Long previousId;
	/**
	 * 下一个id
	 */
	private Long nextId;
	/**
	 * 是否被引用
	 */
	private Boolean reference;

	/**
	 * 银行币种 list
	 */
	private List<BankAccountCurrency> bankAccountCurrencyList;

	/**
	 * code 对应 name 导出用
	 */
	private String code;

	/**
	 * code 对应 name 导出用
	 */
	private String disabledUserName;

	/**
	 * code 对应 name 导出用
	 */
	private Date disabledTime;
	/**
	 * code 对应 name 导出用
	 */
	private String enabledUserName;

	/**
	 * code 对应 name 导出用
	 */
	private Date enabledTime;

	@Override
	public String getCode() {
		return super.getAccount();
	}

	public String getDisabledUserName() {
		if (super.getManageInfo() == null) {
			return null;
		}
		return super.getManageInfo().getDisabledUserName();
	}

	public Date getDisabledTime() {
		if (super.getManageInfo() == null) {
			return null;
		}
		return super.getManageInfo().getDisabledTime();
	}

	public String getEnabledUserName() {
		if (super.getManageInfo() == null) {
			return null;
		}
		return super.getManageInfo().getEnabledUserName();
	}

	public Date getEnabledTime() {
		if (super.getManageInfo() == null) {
			return null;
		}
		return super.getManageInfo().getEnabledTime();
	}

	public String getIsEnableStr() {
		return this.getIsEnable() != null && Constant.Is.YES.equals(this.getIsEnable()) ? "已生效" : "已失效";
	}
}
