package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.SysAuxDataDto;
import com.njwd.entity.platform.vo.SysAuxDataVo;
import com.njwd.platform.service.SysAuxDataService;
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
 * @Description 辅助资料
 * @Date:9:10 2019/6/14
 **/
@RestController
@RequestMapping("sysAuxData")
public class SysAuxDataController extends BaseController {

    @Resource
    private SysAuxDataService sysAuxDataService;

    /**
     * @Description 根据Name和Type查询辅助资料列表
     * @Author liuxiang
     * @Date:15:16 2019/7/2
     * @Param [sysAuxDataVo]
     * @return java.lang.String
     **/
    @PostMapping("findSysAuxDataListByNameType")
    public Result<List<SysAuxDataVo>> findSysAuxDataListByNameType(@RequestBody SysAuxDataDto sysAuxDataDto){
        List<SysAuxDataVo> sysAuxDataVoList=sysAuxDataService.findSysAuxDataListByNameType(sysAuxDataDto);
        return ok(sysAuxDataVoList);
    }

    /**
     * @Description 根据NameOrCode和Type查询辅助资料分页
     * @Author liuxiang
     * @Date:15:16 2019/7/2
     * @Param [sysAuxDataDto]
     * @return java.lang.String
     **/
    @PostMapping("findAuxDataPageByNameCodeType")
    public Result<Page<SysAuxDataVo>> findAuxDataPageByNameCodeType(@RequestBody SysAuxDataDto sysAuxDataDto){
        return ok(sysAuxDataService.findAuxDataPageByNameCodeType(sysAuxDataDto));
    }

    /**
     * @Description 根据Type查询辅助资料列表
     * @Author liuxiang
     * @Date:15:16 2019/7/2
     * @Param [sysAuxDataVo]
     * @return java.lang.String
     **/
    @PostMapping("findSysAuxDataListByType")
    public Result<List<SysAuxDataVo>> findSysAuxDataListByType(@RequestBody SysAuxDataDto sysAuxDataDto){
        List<SysAuxDataVo> sysAuxDataVoList=sysAuxDataService.findSysAuxDataListByType(sysAuxDataDto);
        return ok(sysAuxDataVoList);
    }

    /**
     * @Description 根据Names查询辅助资料列表
     * @Author liuxiang
     * @Date:15:17 2019/7/2
     * @Param [sysAuxDataVo]
     * @return java.lang.String
     **/
    @PostMapping("findSysAuxDataListByNames")
    public Result<List<SysAuxDataVo>> findSysAuxDataListByNames(@RequestBody SysAuxDataDto sysAuxDataDto){
        List<SysAuxDataVo> sysAuxDataVoList=sysAuxDataService.findSysAuxDataListByNames(sysAuxDataDto);
        return ok(sysAuxDataVoList);
    }

    /**
     * @Description 根据ID查询辅助资料
     * @Author liuxiang
     * @Date:15:17 2019/7/2
     * @Param [sysAuxDataDto]
     * @return java.lang.String
     **/
    @PostMapping("findSysAuxDataById")
    public Result<SysAuxDataVo> findSysAuxDataById(@RequestBody SysAuxDataDto sysAuxDataDto){
        return ok(sysAuxDataService.findSysAuxDataById(sysAuxDataDto));
    }
}
