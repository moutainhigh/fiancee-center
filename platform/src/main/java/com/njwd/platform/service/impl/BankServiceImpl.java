package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.BankDto;
import com.njwd.entity.platform.vo.BankVo;
import com.njwd.platform.mapper.BankMapper;
import com.njwd.platform.service.BankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 银行service实现类
 *
 * @author 周鹏
 * @date 2019/11/18
 */
@Service
public class BankServiceImpl implements BankService {
    @Autowired
    private BankMapper bankMapper;

    /**
     * 查询分页列表
     *
     * @param bankDto
     * @return
     */
    @Override
    public Page<BankVo> findBankPage(BankDto bankDto) {
        Page<BankVo> page = bankDto.getPage();
        page = bankMapper.findBankPage(page, bankDto);
        return page;
    }

    /**
     * 根据id查询银行信息
     *
     * @param bankDto
     * @return
     */
    @Override
    public BankVo findBankById(BankDto bankDto) {
        return bankMapper.findBankById(bankDto);
    }
}
