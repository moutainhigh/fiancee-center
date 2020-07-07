package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.AccountingPeriod;
import com.njwd.entity.platform.dto.AccountingPeriodDto;
import com.njwd.entity.platform.dto.AccountingPeriodTypeDto;
import com.njwd.entity.platform.vo.AccountingPeriodTypeVo;
import com.njwd.entity.platform.vo.AccountingPeriodVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description 会计期间
 * @Date:11:08 2019/6/28
 * @Author liuxiang
 **/
public interface AccountingPeriodMapper extends BaseMapper<AccountingPeriod> {

    /**
     * @Description 根据是否调整期和会计日历ID查询会计期间
     * @Author liuxiang
     * @Date:15:24 2019/7/2
     * @Param [accountingPeriodVo]
     * @return java.util.List<com.njwd.platform.entity.vo.AccountingPeriodVo>
     **/
    List<AccountingPeriodVo> findAccPerByIsAdjAndAccCal(AccountingPeriodDto accountingPeriodDto);

    /**
     * 刘遵通
     * 期间类型 分页查询
     * @param accountingPeriodTypeDto
     * @return
     */
    Page<AccountingPeriodTypeVo> findAccountingPeriodTypePage(@Param("page") Page<AccountingPeriodTypeVo> page, @Param("accountingPeriodTypeDto") AccountingPeriodTypeDto accountingPeriodTypeDto);

}