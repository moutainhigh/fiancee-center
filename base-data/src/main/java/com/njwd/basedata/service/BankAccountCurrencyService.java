package com.njwd.basedata.service;

import com.njwd.entity.basedata.dto.BankAccountCurrencyDto;

/**
 * @Description 银行币种 service.
 * @Date 2019-06-11 12:00
 * @Author 郑勇浩
 */
public interface BankAccountCurrencyService {

	/**
	 * @Description 批量新增币种
	 * @Author 郑勇浩
	 * @Data 2019/7/2 16:04
	 * @Param [bankAccountCurrencyDto]
	 * @return int
	 */
	int insertBatch(BankAccountCurrencyDto bankAccountCurrencyDto);

	/**
	 * @Description 删除银行账户下的币种
	 * @Author 郑勇浩
	 * @Data 2019/7/2 16:01
	 * @Param []
	 * @return int
	 */
	int delete(BankAccountCurrencyDto bankAccountCurrencyDto);
}
