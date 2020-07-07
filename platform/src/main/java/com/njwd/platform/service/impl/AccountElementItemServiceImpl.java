package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.AccountElementItem;
import com.njwd.entity.platform.dto.AccountElementItemDto;
import com.njwd.entity.platform.vo.AccountElementItemVo;
import com.njwd.platform.mapper.AccountElementItemMapper;
import com.njwd.platform.service.AccountElementItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author lj
 * @Description 会计要素项
 * @Date:11:24 2019/8/22
 **/
@Service
public class AccountElementItemServiceImpl implements AccountElementItemService {
    @Autowired
    private AccountElementItemMapper accountElementItemMapper;

    /**
     * 根据EleId查询会计要素项列表
     * @Author lj
     * @Date:11:28 2019/8/22
     * @param accountElementItemDto
     * @return java.util.List<com.njwd.entity.platform.vo.AccountElementItemVo>
     **/
    @Override
   /* @Cacheable(value = "findListByEleId", key = "#accountElementItemDto.elementId+''")*/
    public List<AccountElementItemVo> findListByEleId(AccountElementItemDto accountElementItemDto) {
        return accountElementItemMapper.findListByEleId(accountElementItemDto);
    }

    /** 刘遵通
     * 查询页面 （分页）
     * @param accountElementItemDto
     * @return
     */
    @Override
    public Page<AccountElementItemVo> findPage(AccountElementItemDto accountElementItemDto) {
        return accountElementItemMapper.findPage(accountElementItemDto.getPage(),accountElementItemDto);
    }
    /**
     * 刘遵通
     * 会计要素表 分页查询
     */
    @Override
    public Page<AccountElementItemVo> findAccountElementPage(AccountElementItemDto accountElementItemDto) {
        return accountElementItemMapper.findAccountElementPage(accountElementItemDto.getPage(),accountElementItemDto);
    }

    /**
     * 查详情
     * @param accountElementItemDto
     * @return
     */
    @Override
    public AccountElementItem selectById(AccountElementItemDto accountElementItemDto) {
        AccountElementItem accountElementItem = accountElementItemMapper.selectById(accountElementItemDto.getId());
        return accountElementItem;
    }
}
