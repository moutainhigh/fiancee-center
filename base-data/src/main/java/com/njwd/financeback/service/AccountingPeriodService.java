package com.njwd.financeback.service;

import com.njwd.entity.platform.dto.AccountingPeriodDto;
import com.njwd.entity.platform.vo.AccountingPeriodVo;

import java.util.List;

/**
 * 会计期间
 *
 * @author zhuzs
 * @date 2019-07-02 14:02
 */
public interface AccountingPeriodService {
    List<AccountingPeriodVo> findAccountingPeriodByIsAdjustmentAndAccCalendarId(AccountingPeriodDto platformAccountingPeriodDto);
}
