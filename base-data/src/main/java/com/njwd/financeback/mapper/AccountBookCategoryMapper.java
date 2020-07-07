package com.njwd.financeback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.platform.AccountBookCategory;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/13 10:19
 */
public interface AccountBookCategoryMapper extends BaseMapper<AccountBookCategory> {
    void addBatch(List<? extends AccountBookCategory> accountBookCategoryList);
}
