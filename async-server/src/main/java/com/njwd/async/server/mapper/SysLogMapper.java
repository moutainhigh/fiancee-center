package com.njwd.async.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.base.SysLog;
import com.njwd.entity.base.SysLogCommon;
import org.apache.ibatis.annotations.Param;

public interface SysLogMapper extends BaseMapper<SysLog> {

    /**
     * 表名是否存在
     * @param tableName
     * @return
     */
    int isTableExists(@Param("tableName") String tableName);
    /**
     * 新增到动态表中
     *
     * @param sysLog:     日志对象
     * @param currentYearMonth: 当前年月
     * @return
     */
    int insertDynamic(@Param("sysLog") SysLogCommon sysLog, @Param("currentYearMonth") String currentYearMonth);

    /**
     * 生成日志月表
     * @param tName 表名称
     */
    void generateMonthTable(@Param("tName") String tName);
}