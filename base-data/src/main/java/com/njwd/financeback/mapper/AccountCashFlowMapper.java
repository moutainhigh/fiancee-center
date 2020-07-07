package com.njwd.financeback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.basedata.AccountCashFlow;

/**
 * 现金流量项目表
 *
 * @author zhuzs
 * @date 2019-07-11 17:57
 */
public interface AccountCashFlowMapper extends BaseMapper<AccountCashFlow> {
    AccountCashFlow selectByAccStandardId(AccountCashFlow accountCashFlow);
}

