package com.njwd.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.BankCategoryDto;
import com.njwd.entity.platform.vo.BankCategoryVo;

/**
 * 银行账号service
 *
 * @author 周鹏
 * @date 2019/11/18
 */
public interface BankCategoryService {
    /**
     * 查询分页列表
     *
     * @param bankCategoryDto
     * @return
     */
    Page<BankCategoryVo> findBankCategoryPage(BankCategoryDto bankCategoryDto);

    /**
     * 根据id查询银行信息
     *
     * @param bankCategoryDto
     * @return
     */
    BankCategoryVo findBankCategoryById(BankCategoryDto bankCategoryDto);
}
