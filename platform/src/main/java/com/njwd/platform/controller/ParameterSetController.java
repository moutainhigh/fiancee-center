package com.njwd.platform.controller;

import com.njwd.entity.ledger.vo.ParameterSetVo;
import com.njwd.platform.service.ParameterSetService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/14 11:07
 */
@RestController
@RequestMapping("parameterSet")
public class ParameterSetController extends BaseController {

    @Resource
    private ParameterSetService parameterSetService;

    @RequestMapping("findParameterSet")
    public Result<List<ParameterSetVo>> findParameterSet(){
        return ok(parameterSetService.findParameterSet());
    }


}
