package com.njwd.financeback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.platform.Unit;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/13 10:06
 */
public interface UnitMapper extends BaseMapper<Unit> {
    void addBatch(List<? extends Unit> unitList);
}
