package com.njwd.platform.api;

import com.njwd.entity.platform.dto.AccountingPeriodDto;
import com.njwd.entity.platform.vo.AccountingPeriodVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 会计期间
 * @Date:15:21 2019/6/28
 **/
@RequestMapping("platform/accountingperiod")
public interface AccountingPeriodApi {

    /**
     * @Description 根据是否调整期和会计日历ID查询会计期间
     * @Author liuxiang
     * @Date:17:07 2019/7/12
     * @Param [platformAccountingPeriodDto]
     * @return java.lang.String
     **/
    @PostMapping("findAccPerByIsAdjAndAccCal")
    Result<List<AccountingPeriodVo>> findAccPerByIsAdjAndAccCal(AccountingPeriodDto accountingPeriodDto);

}
