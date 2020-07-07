package com.njwd.entity.basedata.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.annotation.ExcelCell;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.BankAccountCurrency;
import com.njwd.entity.basedata.vo.BankAccountVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Description 银行账户 bto.
 * @Date 2019-06-12 11:21
 * @Author 郑勇浩
 */
@Getter
@Setter
public class BankAccountDto extends BankAccountVo {

	private Page<BankAccountVo> page = new Page<>();

	/**
	 * 是否降序
	 */
	private boolean desc = false;

	/**
	 * 批量银行账号id list
	 */
	private List<Long> idList;

	/**
	 * 版本号 列表
	 */
	private List<Integer> versionList;

	/**
	 * 查询归属公司id list
	 */
	private List<Long> companyIdList;

	/**
	 * 模糊查询银行账号或开户银行
	 */
	private String accountOrBankName;

	/**
	 * 币种数组
	 */
	private BankAccountCurrency[] bankAccountCurrencies;

	/**
	 * 公司编码
	 */
	@ExcelCell(index = 0)
	private String companyCode;

	/**
	 * 核算主体编码
	 */
	@ExcelCell(index = 2)
	private String businessUnitCode;

	/**
	 * 公司是否启用分账(导入用)
	 */
	private Byte hasSubAccount;
	/**
	 * 是否管理员 0:否 1:是
	 */
	private Byte isEnterpriseAdmin = Constant.Is.NO;

}
