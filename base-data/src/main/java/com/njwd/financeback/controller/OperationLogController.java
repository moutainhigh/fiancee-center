package com.njwd.financeback.controller;

import com.njwd.entity.base.query.LogQueryDto;
import com.njwd.entity.base.vo.SysLogVo;
import com.njwd.entity.basedata.dto.query.OperationLogQueryDto;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.financeback.service.OperationLogService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * @description: 操作日志控制类
 * @author: fancl
 * @create: 2019-06-10
 */
@RestController
@RequestMapping("log")
public class OperationLogController extends BaseController {

    @Autowired
    OperationLogService operationLogService;
    @RequestMapping("findOperationLogPage")
    public Result findOperationLogPage(@RequestBody LogQueryDto logQueryDto){
        //判断当前年月表是否存在
        int size = operationLogService.tableIsExists("wd_sys_log_"+logQueryDto.getCurrentYearMonth());
        if(size==0){
            throw new ServiceException(ResultCode.CURRENT_MONTH_NOT_EXISTS);
        }
        return ok(operationLogService.findOperationLogPage(logQueryDto));
    }

    @RequestMapping("exportExcel")
    public void exportExcel(@RequestBody LogQueryDto logQueryDto, HttpServletResponse response){
        //判断当前年月表是否存在
        int size = operationLogService.tableIsExists("wd_sys_log_"+logQueryDto.getCurrentYearMonth());
        if(size==0){
            throw new ServiceException(ResultCode.CURRENT_MONTH_NOT_EXISTS);
        }
        operationLogService.exportExcel(logQueryDto,response);
    }

    @RequestMapping("findById")
    //@Cacheable(value = "logCache" ,key = "#logQueryDto.id")
    public Result findById(@RequestBody OperationLogQueryDto logQueryDto){
        SysLogVo vo = new SysLogVo();
        vo.setId(logQueryDto.getId());
        SysLogVo sysLogVo = operationLogService.findById(vo);
        return ok(sysLogVo);
    }


    @RequestMapping("updateById")
    //@CachePut(value = "logCache" ,key = "#logQueryDto.id")
    public Result updateById(@RequestBody SysLogVo sysLogVo){
        int i = operationLogService.updateById(sysLogVo);
        return ok("");
    }

    @RequestMapping("deleteById")
    public Result deleteById(@RequestBody OperationLogQueryDto logQueryDto){
        SysLogVo vo = new SysLogVo();
        vo.setId(logQueryDto.getId());
        int i = operationLogService.deleteBatch(vo);
        return ok("");
    }

}
