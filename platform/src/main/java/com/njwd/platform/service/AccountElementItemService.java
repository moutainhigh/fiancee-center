package com.njwd.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.AccountElementItem;
import com.njwd.entity.platform.dto.AccountElementItemDto;
import com.njwd.entity.platform.vo.AccountElementItemVo;

import java.util.List;

/**
 * @Author lj
 * @Description 会计要素项
 * @Date:11:24 2019/8/22
 **/
public interface AccountElementItemService {
    /**
     * 根据EleId查询会计要素项列表
     * @Author lj
     * @Date:11:28 2019/8/22
     * @param accountElementItemDto
     * @return java.util.List<com.njwd.entity.platform.vo.AccountElementItemVo>
     **/
    List<AccountElementItemVo> findListByEleId(AccountElementItemDto accountElementItemDto);
    /** 刘遵通
     * 查询页面 （分页）
     * @param accountElementItemDto
     * @return
     */
    Page<AccountElementItemVo> findPage(AccountElementItemDto accountElementItemDto);

    /**
     * 刘遵通
     * 会计要素表 分页查询
     */
    Page<AccountElementItemVo> findAccountElementPage(AccountElementItemDto accountElementItemDto);
    /**
     * 查详情
     * @param accountElementItemDto
     * @return
     */
    AccountElementItem selectById(AccountElementItemDto accountElementItemDto);

}
