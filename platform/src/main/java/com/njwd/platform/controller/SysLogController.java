package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.SysLogDto;
import com.njwd.entity.platform.vo.SysLogVo;
import com.njwd.platform.service.SysLogService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/20 15:42
 */
@RestController
@RequestMapping("sysLog")
public class SysLogController extends BaseController {

    @Resource
    private SysLogService sysLogService;

    @RequestMapping("findSysLogPage")
    public Result<Page<SysLogVo>> findSysLogPage(@RequestBody SysLogDto sysLogDto){
        return ok(sysLogService.findSysLogPage(sysLogDto));
    }

    @RequestMapping("exportExcel")
    public void exportExcel(@RequestBody SysLogDto sysLogDto, HttpServletResponse response){
        sysLogService.exportExcel(sysLogDto,response);
    }


}

