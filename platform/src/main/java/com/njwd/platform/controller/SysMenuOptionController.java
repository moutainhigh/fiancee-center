package com.njwd.platform.controller;

import com.njwd.entity.basedata.SysMenuOptionTable;
import com.njwd.entity.platform.dto.SysMenuOptionDto;
import com.njwd.entity.platform.vo.SysMenuOptionVo;
import com.njwd.platform.service.SysMenuOptionService;
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
 * @Description 菜单选项
 * @Date:9:12 2019/6/14
 **/
@RestController
@RequestMapping("sysMenuOption")
public class SysMenuOptionController extends BaseController {

    @Resource
    private SysMenuOptionService sysMenuOptionService;

    /**
     * @Description 查询菜单选项列表
     * @Author liuxiang
     * @Date:15:18 2019/7/2
     * @Param [sysMenuOptionDto]
     * @return java.lang.String
     **/
    @PostMapping("findSysMenuOptionList")
    public Result<List<SysMenuOptionVo>> findSysMenuOptionList(@RequestBody SysMenuOptionDto sysMenuOptionDto){
        List<SysMenuOptionVo> sysMenuOptionVoList=sysMenuOptionService.findSysMenuOptionList(sysMenuOptionDto);
        return ok(sysMenuOptionVoList);
    }

    /**
     * @Description 根据ID查询菜单选项
     * @Author liuxiang
     * @Date:15:18 2019/7/2
     * @Param [sysMenuOptionDto]
     * @return java.lang.String
     **/
    @PostMapping("findSysMenuOptionById")
    public Result<SysMenuOptionVo> findSysMenuOptionById(@RequestBody SysMenuOptionDto sysMenuOptionDto){
        return ok(sysMenuOptionService.findSysMenuOptionById(sysMenuOptionDto));
    }

    /**
     * @description: 
     * @param: [sysMenuOptionDto]
     * @return: com.njwd.support.Result<com.njwd.entity.basedata.SysMenuOptionTable> 
     * @author: xdy        
     * @create: 2019-09-09 16:59
     */
    @PostMapping("findOptionTable")
    public Result<SysMenuOptionTable> findOptionTable(@RequestBody SysMenuOptionDto sysMenuOptionDto){
        return ok(sysMenuOptionService.findOptionTable(sysMenuOptionDto));
    }

}
