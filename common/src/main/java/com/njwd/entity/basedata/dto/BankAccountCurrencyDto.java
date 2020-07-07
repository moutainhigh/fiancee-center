package com.njwd.entity.basedata.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.BankAccountCurrency;
import com.njwd.entity.basedata.vo.BankAccountCurrencyVo;
import com.njwd.entity.basedata.vo.BankAccountVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description 银行账户 bto.
 * @Date 2019-06-12 11:21
 * @Author 郑勇浩
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class BankAccountCurrencyDto extends BankAccountCurrencyVo {

	private Page<BankAccountVo> page = new Page<>();

	/**
	 * 币种数组
	 */
	private BankAccountCurrency[] bankAccountCurrencies;

}