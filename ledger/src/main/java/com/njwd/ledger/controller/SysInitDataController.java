package com.njwd.ledger.controller;

import com.njwd.ledger.service.SysInitDataService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/12 14:39
 */
@RestController
@RequestMapping("initApi")
public class SysInitDataController extends BaseController {

    @Resource
    private SysInitDataService sysInitDataService;

    @RequestMapping("createTable")
    public Result<Boolean> createTable(){
        return ok(sysInitDataService.createTable());
    }

}
