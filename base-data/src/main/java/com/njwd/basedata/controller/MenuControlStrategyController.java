package com.njwd.basedata.controller;

import com.njwd.basedata.service.MenuControlStrategyService;
import com.njwd.entity.basedata.MenuControlStrategy;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/28 10:06
 */
@RestController
@RequestMapping("menuControlStrategy")
public class MenuControlStrategyController extends BaseController {

    @Resource
    private MenuControlStrategyService menuControlStrategyService;

    @RequestMapping("findMenuControlStrategy")
    public Result<MenuControlStrategy> findMenuControlStrategy(@RequestBody MenuControlStrategy menuControlStrategy){
        return ok(menuControlStrategyService.findMenuControlStrategy(menuControlStrategy.getMenuCode()));
    }

}
