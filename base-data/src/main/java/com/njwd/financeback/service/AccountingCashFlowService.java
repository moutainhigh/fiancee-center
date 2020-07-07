package com.njwd.financeback.service;

import com.njwd.entity.basedata.AccountCashFlow;

/**
 * 现金流量项目表
 *
 * @author zhuzs
 * @date 2019-07-02 13:20
 */
public interface AccountingCashFlowService {
    /**
     * 查询 现金流量项目表列表
     *
     * @return
     */
    AccountCashFlow findAccountCashFlow(AccountCashFlow accountCashFlow);
}

