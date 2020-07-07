package com.njwd.financeback.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.base.query.LogQueryDto;
import com.njwd.entity.base.vo.SysLogVo;

import javax.servlet.http.HttpServletResponse;

public interface OperationLogService {
    /**
     * 操作日志查询 分页
     * @param logQueryDto
     * @return
     */
    Page<SysLogVo> findOperationLogPage(LogQueryDto logQueryDto);

    /**
     * 表是否存在
     * @param tableName
     * @return
     */
    int tableIsExists(String tableName);

    void exportExcel(LogQueryDto logQueryDto, HttpServletResponse response);

    SysLogVo findById(SysLogVo sysLogVo);

    int updateById(SysLogVo sysLogVo);

    int deleteBatch(SysLogVo sysLogVo);
}
