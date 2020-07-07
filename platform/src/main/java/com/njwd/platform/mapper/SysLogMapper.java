package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.base.SysLogCommon;
import com.njwd.entity.platform.SysLog;
import com.njwd.entity.platform.dto.SysLogDto;
import com.njwd.entity.platform.vo.SysLogVo;
import org.apache.ibatis.annotations.Param;

public interface SysLogMapper extends BaseMapper<SysLogCommon> {

    Page<SysLogVo> findSysLogPage(Page<SysLog> page, @Param("sysLogDto") SysLogDto sysLogDto);
}