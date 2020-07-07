package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.BankCategoryDto;
import com.njwd.entity.platform.vo.BankCategoryVo;
import com.njwd.platform.service.BankCategoryService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 银行账号controller
 *
 * @author 周鹏
 * @date 2019/11/18
 */
@RestController
@RequestMapping("bankCategory")
public class BankCategoryController extends BaseController {
    @Resource
    private BankCategoryService bankCategoryService;

    /**
     * 查询分页列表
     *
     * @return Result
     * @author 周鹏
     * @date 2019/11/18
     */
    @PostMapping("findBankCategoryPage")
    public Result<Page<BankCategoryVo>> findBankCategoryPage(@RequestBody BankCategoryDto bankCategoryDto) {
        return ok(bankCategoryService.findBankCategoryPage(bankCategoryDto));
    }

    /**
     * 根据id查询银行类别信息
     *
     * @return Result
     * @author 周鹏
     * @date 2019/11/18
     */
    @PostMapping("findBankCategoryById")
    public Result<BankCategoryVo> findBankCategoryById(@RequestBody BankCategoryDto bankCategoryDto) {
        FastUtils.checkParams(bankCategoryDto.getId());
        return ok(bankCategoryService.findBankCategoryById(bankCategoryDto));
    }
}
