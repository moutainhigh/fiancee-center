package com.njwd.platform.controller;

import com.njwd.entity.basedata.MenuCodingRules;
import com.njwd.platform.service.MenuCodingRulesService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/15 14:25
 */
@RestController
@RequestMapping("menuCodingRules")
public class MenuCodingRulesController extends BaseController {

    @Resource
    private MenuCodingRulesService menuCodingRulesService;

    @RequestMapping("findMenuCodingRulesList")
    public Result<List<MenuCodingRules>> findMenuCodingRulesList(){
        return ok(menuCodingRulesService.findMenuCodingRulesList());
    }



}
