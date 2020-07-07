package com.njwd.financeback.service.impl;

import com.njwd.entity.basedata.AccountCashFlow;
import com.njwd.financeback.mapper.AccountCashFlowMapper;
import com.njwd.financeback.service.AccountingCashFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 现金流量项目表
 *
 * @author zhuzs
 * @date 2019-07-02 13:22
 */
@Service
public class AccountingCashFlowServiceImpl implements AccountingCashFlowService {
    @Autowired
    private AccountCashFlowMapper accountCashFlowMapper;

    /**
     * 查询 现金流量项目表
     *
     * @return
     */
    @Override
    public AccountCashFlow findAccountCashFlow(AccountCashFlow accountCashFlow){
        return accountCashFlowMapper.selectByAccStandardId(accountCashFlow);
    }
}

