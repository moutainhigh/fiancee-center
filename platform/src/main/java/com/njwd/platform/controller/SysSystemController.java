package com.njwd.platform.controller;

import com.njwd.entity.platform.dto.SysSystemDto;
import com.njwd.entity.platform.vo.SysSystemVo;
import com.njwd.platform.service.SysSystemService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author liuxiang
 * @Description 子系统说明
 * @Date:9:13 2019/6/14
 **/
@RestController
@RequestMapping("sysSystem")
public class SysSystemController extends BaseController {

    @Resource
    private SysSystemService sysSystemService;

    /**
     * @Description 查询子系统说明列表
     * @Author liuxiang
     * @Date:15:19 2019/7/2
     * @Param [sysSystemDto]
     * @return java.lang.String
     **/
    @PostMapping("findSysSystemList")
    public Result<List<SysSystemVo>> findSysSystemList(@RequestBody SysSystemDto sysSystemDto){
        List<SysSystemVo> sysSystemVoList=sysSystemService.findSysSystemList(sysSystemDto);
        return ok(sysSystemVoList);
    }

    /**
     * @Description 根据ID查询子系统说明
     * @Author liuxiang
     * @Date:15:19 2019/7/2
     * @Param [sysSystemDto]
     * @return java.lang.String
     **/
    @PostMapping("findSysSystemById")
    public Result<SysSystemVo> findSysSystemById(@RequestBody SysSystemDto sysSystemDto){
        return ok(sysSystemService.findSysSystemById(sysSystemDto));
    }
    
    /**
     * @description: 初始化数据
     * @param: []
     * @return: com.njwd.support.Result 
     * @author: xdy        
     * @create: 2019-09-05 15:53 
     */
    @RequestMapping("initData")
    public Result initData(@RequestBody SysSystemDto sysSystemDto){
        return ok(sysSystemService.initData(sysSystemDto));
    }

}
