package com.njwd.platform.controller;

import com.njwd.entity.platform.dto.AccountingPeriodDto;
import com.njwd.entity.platform.vo.AccountingPeriodVo;
import com.njwd.platform.service.AccountingPeriodService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 会计期间
 * @Date:14:33 2019/6/26
 **/
@RestController
@RequestMapping("accountingperiod")
public class AccountingPeriodController extends BaseController {

    @Autowired
    private AccountingPeriodService accountingPeriodService;

    /**
     * @Description 根据是否调整期和会计日历ID查询会计期间
     * @Author liuxiang
     * @Date:15:04 2019/7/2
     * @Param [accountingPeriodVo]
     * @return java.lang.String
     **/
    @PostMapping("findAccPerByIsAdjAndAccCal")
    public Result<List<AccountingPeriodVo>> findAccPerByIsAdjAndAccCal(@RequestBody AccountingPeriodDto accountingPeriodDto) {
        return ok(accountingPeriodService.findAccPerByIsAdjAndAccCal(accountingPeriodDto));
    }
}
