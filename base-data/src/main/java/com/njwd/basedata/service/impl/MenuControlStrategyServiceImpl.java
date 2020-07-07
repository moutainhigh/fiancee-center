package com.njwd.basedata.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.njwd.basedata.mapper.MenuControlStrategyMapper;
import com.njwd.basedata.service.MenuControlStrategyService;
import com.njwd.entity.basedata.MenuControlStrategy;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/28 9:24
 */
@Service
public class MenuControlStrategyServiceImpl implements MenuControlStrategyService {

    @Resource
    private MenuControlStrategyMapper menuControlStrategyMapper;

    /**
     * @description: 获取租户控制策略
     * @param: [menuCode]
     * @return: com.njwd.entity.basedata.MenuControlStrategy
     * @author: xdy
     * @create: 2019-08-28 09-32
     */
    @Override
    public MenuControlStrategy findMenuControlStrategy(String menuCode) {
        SysUserVo userVo = UserUtils.getUserVo();
        return menuControlStrategyMapper.selectOne(Wrappers.<MenuControlStrategy>lambdaQuery()
                .eq(MenuControlStrategy::getRootEnterpriseId,userVo.getRootEnterpriseId()).eq(MenuControlStrategy::getMenuCode,menuCode));
    }
}
