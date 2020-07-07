package com.njwd.financeback.service.impl;

import com.njwd.entity.basedata.AccountingSubject;
import com.njwd.financeback.mapper.AccountingSubjectMapper;
import com.njwd.financeback.service.AccountingSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 科目表
 *
 * @author zhuzs
 * @date 2019-07-02 13:05
 */
@Service
public class AccountingSubjectServiceImpl implements AccountingSubjectService {
    @Autowired
    private AccountingSubjectMapper accountingSubjectMapper;

    @Override
    public AccountingSubject findSubject(AccountingSubject accountingSubject) {
        return accountingSubjectMapper.selectByAccStandardId(accountingSubject);
    }
}

