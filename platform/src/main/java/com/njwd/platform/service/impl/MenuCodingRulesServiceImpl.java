package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.njwd.entity.basedata.MenuCodingRules;
import com.njwd.platform.mapper.MenuCodingRulesMapper;
import com.njwd.platform.service.MenuCodingRulesService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/15 14:19
 */
@Service
public class MenuCodingRulesServiceImpl implements MenuCodingRulesService {

    @Resource
    private MenuCodingRulesMapper menuCodingRulesMapper;

    @Override
    public List<MenuCodingRules> findMenuCodingRulesList() {
        return menuCodingRulesMapper.selectList(Wrappers.emptyWrapper());
    }

}
