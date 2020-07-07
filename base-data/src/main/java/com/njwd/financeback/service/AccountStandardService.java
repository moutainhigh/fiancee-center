package com.njwd.financeback.service;

import com.njwd.entity.basedata.AccountStandard;
import com.njwd.entity.platform.dto.AccountBookCategoryDto;

import java.util.List;

/**
 * 根据 账簿类型ID 和 租户ID 获取会计准则（含税制、记账本位币信息）
 *
 * @author zhuzs
 * @date 2019-07-08 10:31
 */
public interface AccountStandardService {
    /**
     * 获取会计准则
     */
    List<AccountStandard> accountStandardList(AccountBookCategoryDto accountBookCategoryDto);
}
