package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.AccountingPeriodTypeDto;
import com.njwd.entity.platform.vo.AccountingPeriodTypeVo;
import com.njwd.platform.mapper.AccountingPeriodMapper;
import com.njwd.platform.service.AccountingPeriodTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description: 会计期间类型
 * @author: lzt
 * @create: 2019-11-20 09:51
 */
@Service
public class AccountingPeriodTypeServiceImpl implements AccountingPeriodTypeService {

    @Autowired
    private AccountingPeriodMapper accountingPeriodMapper;

    /**
     * @description: 会计期间类型分页
     * @param: [accountingPeriodTypeDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.AccountingPeriodTypeVo>
     * @author: lzt
     * @create: 2019-11-20 09:51
     */
    @Override
    public Page<AccountingPeriodTypeVo> findAccountingPeriodTypePage(AccountingPeriodTypeDto accountingPeriodTypeDto) {
        Page<AccountingPeriodTypeVo> page = accountingPeriodTypeDto.getPage();
        return accountingPeriodMapper.findAccountingPeriodTypePage(page,accountingPeriodTypeDto);
    }
}

