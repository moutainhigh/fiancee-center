package com.njwd.entity.basedata;

import com.njwd.annotation.ExcelCell;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BankAccount extends BaseModel {

	/**
	 * 上级ID
	 */
	private Long upId;
	/**
	 * 企业ID
	 */
	private Long rootEnterpriseId;
	/**
	 * 归属公司 【公司】表ID
	 */
	private Long companyId;

	/**
	 * 使用公司 【公司】表ID
	 */
	private Long useCompanyId;

	/**
	 * 业务单元ID 【业务单元】表ID
	 */
	private Long businessUnitId;

	/**
	 * 开户银行 【银行】表ID
	 */
	private Long depositBankId;

	/**
	 * 开户银行名称
	 */
	@ExcelCell(index = 5)
	private String depositBankName;

	/**
	 * 账号编码
	 */
	private String code;

	/**
	 * 银行账号
	 */
	@ExcelCell(index = 4)
	private String account;

	/**
	 * 账户名称
	 */
	@ExcelCell(index = 7)
	private String name;

	/**
	 * 账户类型 【辅助资料】表ID
	 */
	@ExcelCell(index = 6)
	private Long accType;

	/**
	 * 账户类型名称 【辅助资料】表NAME
	 */
	@ExcelCell(index = 6, redundancy = true)
	private String accTypeName;

	/**
	 * 账户用途 【辅助资料】表ID
	 */
	@ExcelCell(index = 8)
	private Long accUsage;

	/**
	 * 账户用途名称 【辅助资料】表NAME
	 */
	@ExcelCell(index = 8, redundancy = true)
	private String accUsageName;

	/**
	 * 启用标识 0：禁用、1：启用
	 */
	private Byte isEnable;

	/**
	 * 管理信息
	 */
	private ManagerInfo manageInfo;

	/**
	 * 创建公司
	 */
	private Long createCompanyId;
	private static final long serialVersionUID = 1L;
}
