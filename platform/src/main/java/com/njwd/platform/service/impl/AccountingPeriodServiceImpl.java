package com.njwd.platform.service.impl;

import com.njwd.entity.platform.dto.AccountingPeriodDto;
import com.njwd.entity.platform.vo.AccountingPeriodVo;
import com.njwd.platform.mapper.AccountingPeriodMapper;
import com.njwd.platform.service.AccountingPeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 会计期间
 * @Date:11:10 2019/6/28
 **/
@Service
public class AccountingPeriodServiceImpl implements AccountingPeriodService {

    @Autowired
    private AccountingPeriodMapper accountingPeriodMapper;

    /**
     * @return java.util.List<com.njwd.platform.entity.vo.AccountingPeriodVo>
     * @Description 根据是否调整期和会计日历ID查询会计期间
     * @Author liuxiang
     * @Date:15:45 2019/7/2
     * @Param [accountingPeriodVo]
     **/
    @Override
    @Cacheable(value = "accPerByIsAdjAndAccCal", key = "#accountingPeriodDto.isAdjustment+'-'+#accountingPeriodDto.accCalendarId+'-'+#accountingPeriodDto.periodYear+'-'+#accountingPeriodDto.periodNum+'-'+#accountingPeriodDto.customPeriodYear+'-'+#accountingPeriodDto.customPeriodNum")
    public List<AccountingPeriodVo> findAccPerByIsAdjAndAccCal(AccountingPeriodDto accountingPeriodDto) {
        return accountingPeriodMapper.findAccPerByIsAdjAndAccCal(accountingPeriodDto);
    }
}
