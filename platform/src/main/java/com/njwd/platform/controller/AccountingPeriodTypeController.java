package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.AccountingPeriodTypeDto;
import com.njwd.entity.platform.vo.AccountingPeriodTypeVo;
import com.njwd.platform.service.AccountingPeriodTypeService;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.njwd.support.BaseController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @description: 会计期间类型
 * @author: lzt
 * @create: 2019-11-20 09:51
 */
@RestController
@RequestMapping("accountingPeriodType")
public class AccountingPeriodTypeController extends BaseController {

    @Resource
    private AccountingPeriodTypeService accountingPeriodTypeService;

    /**
     * @description: 会计期间类型分页
     * @param: [accountingPeriodTypeDto]
     * @return: com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.AccountingPeriodTypeVo>>
     * @author: lzt
     * @create: 2019-11-20 09:51
     */
    @RequestMapping("findAccountingPeriodTypePage")
    public Result<Page<AccountingPeriodTypeVo>> findAccountingPeriodTypePage(@RequestBody AccountingPeriodTypeDto accountingPeriodTypeDto){
        return ok(accountingPeriodTypeService.findAccountingPeriodTypePage(accountingPeriodTypeDto));
    }
}


