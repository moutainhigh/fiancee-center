package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.BankCategoryDto;
import com.njwd.entity.platform.vo.BankCategoryVo;
import com.njwd.platform.mapper.BankCategoryMapper;
import com.njwd.platform.service.BankCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 银行类别service实现类
 *
 * @author 周鹏
 * @date 2019/11/18
 */
@Service
public class BankCategoryServiceImpl implements BankCategoryService {
    @Autowired
    private BankCategoryMapper bankCategoryMapper;

    /**
     * 查询分页列表
     *
     * @param bankCategoryDto
     * @return
     */
    @Override
    public Page<BankCategoryVo> findBankCategoryPage(BankCategoryDto bankCategoryDto) {
        Page<BankCategoryVo> page = bankCategoryDto.getPage();
        page = bankCategoryMapper.findBankCategoryPage(page, bankCategoryDto);
        return page;
    }

    /**
     * 根据id查询银行信息
     *
     * @param bankCategoryDto
     * @return
     */
    @Override
    public BankCategoryVo findBankCategoryById(BankCategoryDto bankCategoryDto) {
        return bankCategoryMapper.findBankCategoryById(bankCategoryDto);
    }
}
