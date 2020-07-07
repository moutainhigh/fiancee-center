package com.njwd.basedata.service;

import com.njwd.entity.basedata.MenuCodingRules;

/**
 * @Author ZhuHC
 * @Date  2019/9/6 13:43
 * @Description
 */
public interface MenuCodingRulesService {

    /**
     * @Author ZhuHC
     * @Date  2019/9/6 14:11
     * @Param [menuCodingRules]
     * @return com.njwd.support.Result<com.njwd.entity.basedata.MenuCodingRules>
     * @Description 根据菜单编码 查询菜单编码规则
     */
    MenuCodingRules findMenuCodingRules(MenuCodingRules menuCodingRules);
}
