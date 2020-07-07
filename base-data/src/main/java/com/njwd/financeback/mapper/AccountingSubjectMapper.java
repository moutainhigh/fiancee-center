package com.njwd.financeback.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.basedata.AccountingSubject;

/**
 * 科目表
 *
 * @author zhuzs
 * @date 2019-07-11 16:44
 */
public interface AccountingSubjectMapper extends BaseMapper<AccountingSubject> {
    AccountingSubject selectByAccStandardId(AccountingSubject accountingSubject);
}

