package com.njwd.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.njwd.basedata.mapper.MenuCodingRulesMapper;
import com.njwd.basedata.service.MenuCodingRulesService;
import com.njwd.entity.basedata.MenuCodingRules;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Description
 * @Author: ZhuHC
 * @Date: 2019/9/6 13:46
 */
@Service
public class MenuCodingRulesServiceImpl implements MenuCodingRulesService {

    @Resource
    private MenuCodingRulesMapper menuCodingRulesMapper;

    /**
     * @Author ZhuHC
     * @Date  2019/9/6 14:11
     * @Param [menuCodingRules]
     * @return com.njwd.support.Result<com.njwd.entity.basedata.MenuCodingRules>
     * @Description 根据菜单编码 查询菜单编码规则
     */
    @Override
    public MenuCodingRules findMenuCodingRules(MenuCodingRules menuCodingRules) {
        return menuCodingRulesMapper.selectOne(new LambdaQueryWrapper<MenuCodingRules>().eq(MenuCodingRules::getRootEnterpriseId,UserUtils.getUserVo().getRootEnterpriseId())
                                                                                        .eq(MenuCodingRules::getMenuCode,menuCodingRules.getMenuCode()));
    }
}
