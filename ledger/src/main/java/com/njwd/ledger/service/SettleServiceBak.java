package com.njwd.ledger.service;

import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.support.CheckVoucherResult;

/**
 * @Description 结账\反结账处理
 * @Author 朱小明
 * @Date 2019/8/13 19:24
 **/
public interface SettleServiceBak {


    /**
    * @Description 结账处理
    * @Author 朱小明
    * @Date 2019/8/22
    * @param accountBookPeriod
    * @return com.njwd.support.CheckVoucherResult
    **/
    CheckVoucherResult settle(AccountBookPeriodDto accountBookPeriod);

    /**
    * @Description 反结账处理
    * @Author 朱小明
    * @Date 2019/8/27
    * @param accountBookPeriod
    * @return void
    **/
    AccountBookPeriod cancelSettle(AccountBookPeriodDto accountBookPeriod);

    /**
    * @Description 根据当期账簿打开下X期账簿期间
    * @Author 朱小明
    * @Date 2019/9/18
    * @param accountBookPeriodDto
    * @return void
    **/

    void openAbPeriod(AccountBookPeriodDto accountBookPeriodDto);
}
