package com.njwd.platform.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.AccountingStandardDto;
import com.njwd.entity.platform.vo.AccountingStandardVo;
import com.njwd.platform.service.AccountingStandardService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author liuxiang
 * @Description 会计准则
 * @Date:17:08 2019/7/12
 **/
@RestController
@RequestMapping("accountingStandard")
public class AccountingStandardController extends BaseController {

    @Resource
    private AccountingStandardService accountingStandardService;

    /**
     * @Description 查询会计准则列表信息
     * @Author liuxiang
     * @Date:15:05 2019/7/2
     * @Param []
     * @return java.lang.String
     **/
    @PostMapping("findAccountingList")
    public Result<List<AccountingStandardVo>> findAccountingList() {
        return ok(accountingStandardService.findAccountingList());
    }

    /**
     * @Description 查询单个会计准则信息
     * @Author liuxiang
     * @Date:15:05 2019/7/2
     * @Param [accountingStandardDto]
     * @return java.lang.String
     **/
    @PostMapping("findAccountingById")
    public Result<AccountingStandardVo> findAccountingById(@RequestBody AccountingStandardDto accountingStandardDto){
        return ok(accountingStandardService.findAccountingById(accountingStandardDto));
    }

    @PostMapping("findPage")
    public Result<Page<AccountingStandardVo>> findPage(@RequestBody AccountingStandardDto accountingStandardDto) {
        return ok(accountingStandardService.findPage(accountingStandardDto));
    }

    @PostMapping("findDetail")
    public Result<AccountingStandardVo> findDetail(@RequestBody AccountingStandardDto accountingStandardDto) {
        return ok(accountingStandardService.findDetail(accountingStandardDto));
    }
}
