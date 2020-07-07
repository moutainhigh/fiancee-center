package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.njwd.entity.basedata.MenuControlStrategy;
import com.njwd.platform.mapper.MenuControlStrategyMapper;
import com.njwd.platform.service.MenuControlStrategyService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/15 14:18
 */
@Service
public class MenuControlStrategyServiceImpl implements MenuControlStrategyService {

    @Resource
    private MenuControlStrategyMapper menuControlStrategyMapper;

    @Override
    public List<MenuControlStrategy> findMenuControlStrategyList() {
        return menuControlStrategyMapper.selectList(Wrappers.emptyWrapper());
    }

}
