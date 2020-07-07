package com.njwd.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.base.SysLogCommon;
import com.njwd.entity.platform.dto.SysLogDto;
import com.njwd.entity.platform.vo.SysLogVo;
import javax.servlet.http.HttpServletResponse;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/11 15:55
 */
public interface SysLogService {

    void addSysLog(SysLogCommon sysLogCommon);

    Page<SysLogVo> findSysLogPage(SysLogDto sysLogDto);

    void exportExcel(SysLogDto sysLogDto, HttpServletResponse response);
}
