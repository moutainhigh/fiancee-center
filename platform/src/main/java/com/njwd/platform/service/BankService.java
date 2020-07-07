package com.njwd.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.BankDto;
import com.njwd.entity.platform.vo.BankVo;

/**
 * 银行service
 *
 * @author 周鹏
 * @date 2019/11/18
 */
public interface BankService {
    /**
     * 查询分页列表
     *
     * @param bankDto
     * @return
     */
    Page<BankVo> findBankPage(BankDto bankDto);

    /**
     * 根据id查询银行信息
     *
     * @param bankDto
     * @return
     */
    BankVo findBankById(BankDto bankDto);
}
