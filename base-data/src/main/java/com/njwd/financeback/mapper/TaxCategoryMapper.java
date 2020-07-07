package com.njwd.financeback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.platform.TaxCategory;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/13 9:32
 */
public interface TaxCategoryMapper extends BaseMapper<TaxCategory> {
    void addBatch(List<? extends TaxCategory> taxCategoryList);
}
