package com.njwd.platform.service.impl;

import com.njwd.entity.platform.vo.AccountBookTypeVo;
import com.njwd.platform.mapper.AccountBookTypeMapper;
import com.njwd.platform.service.AccountBookTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 账簿类型
 * @Date:15:44 2019/7/2
 **/
@Service
public class AccountBookTypeServiceImpl implements AccountBookTypeService {

    @Autowired
    private AccountBookTypeMapper accountBookTypeMapper;

    /**
     * @return java.util.List<com.njwd.platform.entity.vo.AccountBookTypeVo>
     * @Description 查询账簿类型列表
     * @Author lj
     * @Date:17:03 2019/6/25
     * @Param []
     **/
    @Override
    @Cacheable(value = "accountBookTypeList")
    public List<AccountBookTypeVo> findAccountBookTypeList() {
        return accountBookTypeMapper.findAccountBookTypeList();
    }
}
