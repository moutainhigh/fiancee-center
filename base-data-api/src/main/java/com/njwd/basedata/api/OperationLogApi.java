package com.njwd.basedata.api;

import com.njwd.entity.base.query.LogQueryDto;
import com.njwd.entity.base.vo.SysLogVo;
import com.njwd.entity.basedata.dto.query.OperationLogQueryDto;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;

/**
 * @description: 操作日志控制类
 * @author: fancl
 * @create: 2019-06-10
 */
@RequestMapping("log")
public interface OperationLogApi {
    @RequestMapping("findOperationLogPage")
    Result findOperationLogPage(LogQueryDto logQueryDto);

    @RequestMapping("exportExcel")
    void exportExcel(LogQueryDto logQueryDto, HttpServletResponse response);

    @RequestMapping("findById")
    Result findById(OperationLogQueryDto logQueryDto);


    @RequestMapping("updateById")
    Result updateById(SysLogVo sysLogVo);

    @RequestMapping("deleteById")
    Result deleteById(OperationLogQueryDto logQueryDto);

}
