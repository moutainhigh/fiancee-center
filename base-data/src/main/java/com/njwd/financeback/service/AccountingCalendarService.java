package com.njwd.financeback.service;

import com.njwd.entity.basedata.AccountingCalendar;
import com.njwd.entity.platform.dto.AccountingCalendarDto;
import com.njwd.entity.platform.vo.AccountingCalendarVo;

import java.util.List;

/**
 * 会计日历
 *
 * @author zhuzs
 * @date 2019-07-02 13:45
 */
public interface AccountingCalendarService {
    List<AccountingCalendarVo> findAccCaListByAccTypeAndStand(AccountingCalendarDto platFormAccountingCalendarDto);
}
