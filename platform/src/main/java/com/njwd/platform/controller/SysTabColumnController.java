package com.njwd.platform.controller;

import com.njwd.entity.platform.dto.SysTabColumnDto;
import com.njwd.entity.platform.vo.SysTabColumnVo;
import com.njwd.platform.service.SysTabColumnService;
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
 * @Description 表格配置项
 * @Date:9:14 2019/6/14
 **/
@RestController
@RequestMapping("sysTabColumn")
public class SysTabColumnController extends BaseController {

    @Resource
    private SysTabColumnService sysTabColumnService;

    /**
     * @Description 查询表格配置列表
     * @Author liuxiang
     * @Date:15:20 2019/7/2
     * @Param [sysTabColumnDto]
     * @return java.lang.String
     **/
    @PostMapping("findSysTabColumnList")
    public Result<List<SysTabColumnVo>> findSysTabColumnList(@RequestBody SysTabColumnDto sysTabColumnDto){
        return ok(sysTabColumnService.findSysTabColumnList(sysTabColumnDto));
    }

    /**
     * @Description 根据ID查询表格配置
     * @Author liuxiang
     * @Date:15:20 2019/7/2
     * @Param [sysTabColumnDto]
     * @return java.lang.String
     **/
    @PostMapping("findSysTabColumnById")
    public Result<SysTabColumnVo> findSysTabColumnById(@RequestBody SysTabColumnDto sysTabColumnDto){
        return ok(sysTabColumnService.findSysTabColumnById(sysTabColumnDto));
    }
}
