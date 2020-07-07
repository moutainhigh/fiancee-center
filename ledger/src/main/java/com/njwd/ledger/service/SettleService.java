package com.njwd.ledger.service;

import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.ledger.vo.SettleResult;

import javax.annotation.Nullable;

/**
 * @author xyyxhcj@qq.com
 * @since 2019-09-20
 */

public interface SettleService {
	/**
	 * 结账
	 *
	 * @param accountBookPeriodDto accountBookPeriodDto
	 * @param nextPeriod           nextPeriod
	 * @return com.njwd.entity.ledger.vo.SettleResult
	 * @author xyyxhcj@qq.com
	 * @date 2019/9/20 9:36
	 **/
	SettleResult settle(AccountBookPeriodDto accountBookPeriodDto, @Nullable AccountBookPeriod nextPeriod);

	/**
	 * 反结账
	 *
	 * @param accountBookPeriodDto accountBookPeriodDto
	 * @return com.njwd.entity.ledger.AccountBookPeriod
	 * @author xyyxhcj@qq.com
	 * @date 2019/9/27 19:58
	 **/
	AccountBookPeriod cancelSettle(AccountBookPeriodDto accountBookPeriodDto);
}
