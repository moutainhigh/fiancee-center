package com.njwd.platform.api;

import com.njwd.entity.basedata.MenuCodingRules;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/16 9:06
 */
@RequestMapping("platform/menuCodingRules")
public interface MenuCodingRulesApi {

    @RequestMapping("findMenuCodingRulesList")
    Result<List<MenuCodingRules>> findMenuCodingRulesList();

}
