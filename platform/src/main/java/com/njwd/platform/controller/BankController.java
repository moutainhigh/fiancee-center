package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.BankDto;
import com.njwd.entity.platform.vo.BankVo;
import com.njwd.platform.service.BankService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 银行controller
 *
 * @author 周鹏
 * @date 2019/11/18
 */
@RestController
@RequestMapping("bank")
public class BankController extends BaseController {
    @Resource
    private BankService bankService;

    /**
     * 查询分页列表
     *
     * @return Result
     * @author 周鹏
     * @date 2019/11/18
     */
    @PostMapping("findBankPage")
    public Result<Page<BankVo>> findBankPage(@RequestBody BankDto bankDto) {
        return ok(bankService.findBankPage(bankDto));
    }

    /**
     * 根据id查询银行信息
     *
     * @return Result
     * @author 周鹏
     * @date 2019/11/18
     */
    @PostMapping("findBankById")
    public Result<BankVo> findBankById(@RequestBody BankDto bankDto) {
        FastUtils.checkParams(bankDto.getId());
        return ok(bankService.findBankById(bankDto));
    }
}
