package com.njwd.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.njwd.basedata.mapper.BankAccountCurrencyMapper;
import com.njwd.basedata.service.BankAccountCurrencyService;
import com.njwd.entity.basedata.dto.BankAccountCurrencyDto;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Description 银行账户 service impl.
 * @Date 2019-06-11 12:00
 * @Author 郑勇浩
 */
@Service
public class BankAccountCurrencyServiceImpl implements BankAccountCurrencyService {

	@Resource
	private BankAccountCurrencyMapper bankAccountCurrencyMapper;


	@Override
	public int insertBatch(BankAccountCurrencyDto bto) {
		return bankAccountCurrencyMapper.insertBatch(bto.getBankAccountId(), bto.getBankAccountCurrencies());
	}

	@Override
	public int delete(BankAccountCurrencyDto bankAccountCurrencyDto) {
		return bankAccountCurrencyMapper.delete(new QueryWrapper<>(bankAccountCurrencyDto));
	}
}
