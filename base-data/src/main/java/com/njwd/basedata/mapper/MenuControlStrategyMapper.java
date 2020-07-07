package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.basedata.MenuControlStrategy;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/15 14:14
 */
public interface MenuControlStrategyMapper extends BaseMapper<MenuControlStrategy> {

    int addBatch(List<MenuControlStrategy> menuControlStrategyList);

}
