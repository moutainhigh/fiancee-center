package com.njwd.basedata.controller;

import com.njwd.basedata.service.MenuCodingRulesService;
import com.njwd.entity.basedata.MenuCodingRules;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Description
 * @Author: ZhuHC
 * @Date: 2019/9/6 13:40
 */
@RestController
@RequestMapping("menuCodingRules")
public class MenuCodingRulesController extends BaseController{

    @Resource
    private MenuCodingRulesService menuCodingRulesService;

    /**
     * @Author ZhuHC
     * @Date  2019/9/6 14:11
     * @Param [menuCodingRules]
     * @return com.njwd.support.Result<com.njwd.entity.basedata.MenuCodingRules>
     * @Description 根据菜单编码 查询菜单编码规则
     */
    @RequestMapping("findMenuCodingRules")
    public Result<MenuCodingRules> findMenuCodingRules(@RequestBody MenuCodingRules menuCodingRules){
        FastUtils.checkParams(menuCodingRules.getMenuCode());
        return ok(menuCodingRulesService.findMenuCodingRules(menuCodingRules));
    }
}
