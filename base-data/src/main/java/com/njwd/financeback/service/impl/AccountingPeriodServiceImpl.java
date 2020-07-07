package com.njwd.financeback.service.impl;

import com.njwd.basedata.cloudclient.AccountingPeriodFeignClient;
import com.njwd.common.Constant;
import com.njwd.entity.platform.dto.AccountingPeriodDto;
import com.njwd.entity.platform.vo.AccountingPeriodVo;
import com.njwd.financeback.service.AccountingPeriodService;
import com.njwd.utils.FastUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;

/**
 * 获取账簿启用子系统 会计期间可选范围——平台
 *
 * @author zhuzs
 *
 * @date 2019-07-02 14:03
 */
@Service
public class AccountingPeriodServiceImpl implements AccountingPeriodService {
    @Autowired
    private AccountingPeriodFeignClient accountingPeriodFeignClient;

    /**
     * 获取账簿启用子系统 会计期间可选范围——平台
     *
     * @param platformAccountingPeriodDto
     * @return
     */
    @Override
    public List<AccountingPeriodVo> findAccountingPeriodByIsAdjustmentAndAccCalendarId(AccountingPeriodDto platformAccountingPeriodDto){
        platformAccountingPeriodDto.setIsAdjustment(Constant.Is.NO);
        List<AccountingPeriodVo> accountingPeriodVoList = accountingPeriodFeignClient.findAccPerByIsAdjAndAccCal(platformAccountingPeriodDto).getData();
        Calendar cal = Calendar.getInstance();
        AccountingPeriodVo currAccountingPeriodVo = new AccountingPeriodVo();
        currAccountingPeriodVo.setCustomPeriodYear(cal.get(Calendar.YEAR));
        currAccountingPeriodVo.setCustomPeriodNum(cal.get(Calendar.MONTH)+1);
        // 非空校验
        FastUtils.checkNull(accountingPeriodVoList);
        accountingPeriodVoList.add(0,currAccountingPeriodVo);
        return accountingPeriodVoList;
    }
}

