package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.basedata.MenuCodingRules;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/15 14:16
 */
public interface MenuCodingRulesMapper extends BaseMapper<MenuCodingRules> {

    int addBatch(List<MenuCodingRules> menuCodingRulesList);

}
