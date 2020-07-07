package com.njwd.platform.controller;

import com.njwd.entity.basedata.MenuControlStrategy;
import com.njwd.platform.service.MenuControlStrategyService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/16 9:12
 */
@RestController
@RequestMapping("menuControlStrategy")
public class MenuControlStrategyController extends BaseController {

    @Resource
    private MenuControlStrategyService menuControlStrategyService;

    @RequestMapping("findMenuControlStrategyList")
    public Result<List<MenuControlStrategy>> findMenuControlStrategyList(){
        return ok(menuControlStrategyService.findMenuControlStrategyList());
    }

}
