package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.basedata.BankAccountCurrency;
import org.apache.ibatis.annotations.Param;

/**
 * @Description 银行账户币种 Mapper.
 * @Date 2019-06-11 12:00
 * @Author 郑勇浩
 */
public interface BankAccountCurrencyMapper extends BaseMapper<BankAccountCurrency> {

	/**
	 * @Description 批量新增币种
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:32
	 * @Param [bankAccountId, currencies]
	 * @return int
	 */
	int insertBatch(@Param("bankAccountId") Long bankAccountId, @Param("currencies") BankAccountCurrency[] currencies);
}