package com.njwd.financeback.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.CommParams;
import com.njwd.entity.base.SysLog;
import com.njwd.entity.base.query.LogQueryDto;
import com.njwd.entity.base.vo.SysLogVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysLogMapper extends BaseMapper<SysLog> {
    /**
     * 表名是否存在
     * @param tableName
     * @return
     */
    int isTableExists(@Param("tableName") String tableName);

    SysLogVo findById(@Param("sysLog") SysLogVo sysLogVo);

    int updateById(@Param("sysLog") SysLogVo sysLogVo);

    int deleteById(@Param("sysLog") SysLogVo sysLogVo);



    /**
     * 日志查询 分页
     * @param logQueryDto
     * @param page
     * @return
     */
    List<SysLogVo> findOperationLogPage(@Param("logQueryDto") LogQueryDto logQueryDto, @Param("page") Page<SysLogVo> page, @Param("commParams") CommParams commParams);

}