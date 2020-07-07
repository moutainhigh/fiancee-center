package com.njwd.platform.service;


import com.njwd.entity.platform.dto.AccountingPeriodDto;
import com.njwd.entity.platform.vo.AccountingPeriodVo;

import java.util.List;
/**
 * @Author liuxiang
 * @Description 会计期间
 * @Date:11:08 2019/6/28
 **/
public interface AccountingPeriodService{

    /**
     * @Description 根据是否调整期和会计日历ID查询会计期间
     * @Author lj
     * @Date:10:25
     * @Param [accountingPeriodVo]
     * @return java.util.List<com.njwd.platform.entity.vo.AccountingPeriodVo>
     **/
    List<AccountingPeriodVo> findAccPerByIsAdjAndAccCal(AccountingPeriodDto accountingPeriodDto);

}
