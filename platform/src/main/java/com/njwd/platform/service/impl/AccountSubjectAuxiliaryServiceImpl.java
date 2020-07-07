package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.njwd.entity.platform.AccountSubjectAuxiliary;
import com.njwd.platform.mapper.AccountSubjectAuxiliaryMapper;
import com.njwd.platform.service.AccountSubjectAuxiliaryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class AccountSubjectAuxiliaryServiceImpl extends ServiceImpl<AccountSubjectAuxiliaryMapper, AccountSubjectAuxiliary> implements AccountSubjectAuxiliaryService {


}
