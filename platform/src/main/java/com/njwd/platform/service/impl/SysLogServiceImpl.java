package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.base.SysLogCommon;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.platform.SysLog;
import com.njwd.entity.platform.dto.SysLogDto;
import com.njwd.entity.platform.vo.SysLogVo;
import com.njwd.platform.mapper.SysLogMapper;
import com.njwd.platform.service.SysLogService;
import com.njwd.service.FileService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/11 16:02
 */
@Service
public class SysLogServiceImpl implements SysLogService {

    @Resource
    SysLogMapper sysLogMapper;

    @Resource
    FileService fileService;

    @Override
    public void addSysLog(SysLogCommon sysLogCommon) {
        sysLogMapper.insert(sysLogCommon);
    }

    @Override
    public Page<SysLogVo> findSysLogPage(SysLogDto sysLogDto) {
        if(sysLogDto.getEndTime()!=null)
            sysLogDto.setEndTime(sysLogDto.getEndTime()+" 23:59:59");
        return sysLogMapper.findSysLogPage(sysLogDto.getPage(),sysLogDto);
    }

    @Override
    public void exportExcel(SysLogDto sysLogDto, HttpServletResponse response) {
        Page<SysLog> page = sysLogDto.getPage();
        fileService.resetPage(page);
        fileService.exportExcel(response,findSysLogPage(sysLogDto).getRecords()
                ,new ExcelColumn("sysName","功能模块")
                ,new ExcelColumn("menuName","功能菜单")
                ,new ExcelColumn("operation","操作按钮")
                ,new ExcelColumn("creatorName","员工姓名")
                ,new ExcelColumn("mobile","手机号码")
                ,new ExcelColumn("createTime","操作时间")
                ,new ExcelColumn("ipAddress","IP地址"));
    }

}
