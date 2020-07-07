package com.njwd.financeback.service.impl;

import com.njwd.basedata.cloudclient.AccountingCalendarFeignClient;
import com.njwd.entity.basedata.AccountingCalendar;
import com.njwd.entity.platform.dto.AccountingCalendarDto;
import com.njwd.entity.platform.vo.AccountingCalendarVo;
import com.njwd.financeback.service.AccountingCalendarService;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会计日历
 *
 * @author zhuzs
 * @date 2019-07-02 13:46
 */
@Service
public class AccountingCalendarServiceImpl implements AccountingCalendarService {
    @Autowired
    private AccountingCalendarFeignClient accountingCalendarFeignClient;

    /**
     * 查询 会计日历列表
     *
     * @param platFormAccountingCalendarDto
     * @return
     */
    @Override
    public List<AccountingCalendarVo> findAccCaListByAccTypeAndStand(AccountingCalendarDto platFormAccountingCalendarDto){
        List<AccountingCalendarVo> accountingCalendarVoList = accountingCalendarFeignClient.findAccCaListByAccTypeAndStand(platFormAccountingCalendarDto).getData();
        // 非空校验
        FastUtils.checkNull(accountingCalendarVoList);
        return accountingCalendarVoList;
    }
}
