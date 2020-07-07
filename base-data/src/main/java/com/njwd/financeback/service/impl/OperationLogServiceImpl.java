package com.njwd.financeback.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.MenuCodeConstant;
import com.njwd.entity.base.query.LogQueryDto;
import com.njwd.entity.base.vo.SysLogVo;
import com.njwd.financeback.mapper.SysLogMapper;
import com.njwd.financeback.service.OperationLogService;
import com.njwd.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @description: 日志管理实现类
 * @author: fancl
 * @create: 2019-06-06
 */
@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    SysLogMapper sysLogMapper;
    @Resource
    FileService fileService;

    /**
     * 操作日志查询 分页
     *
     * @param logQueryDto
     * @return
     */
    @Override
    public Page<SysLogVo> findOperationLogPage(LogQueryDto logQueryDto) {
        Page<SysLogVo> page = logQueryDto.getPage();
        logQueryDto.getCommParams().setOrColumn(Constant.OrMatchColumn.OPERATION_LOG_MATCH);
        List<SysLogVo> operationLogList = sysLogMapper.findOperationLogPage(logQueryDto, page,logQueryDto.getCommParams());
        page.setRecords(operationLogList);
        return page;
    }

    @Override
    public int tableIsExists(String tableName) {
        return sysLogMapper.isTableExists(tableName);
    }

    /**
     * 导出
     * @param logQueryDto
     * @param response
     */
    @Override
    public void exportExcel(LogQueryDto logQueryDto, HttpServletResponse response) {
        Page<SysLogVo> page = logQueryDto.getPage();
        fileService.resetPage(page);
        logQueryDto.getCommParams().setOrColumn(Constant.OrMatchColumn.OPERATION_LOG_MATCH);
        List<SysLogVo> operationLogList = sysLogMapper.findOperationLogPage(logQueryDto, page,logQueryDto.getCommParams());
        fileService.exportExcel(response,operationLogList, MenuCodeConstant.SYS_LOG);
    }


    /**
     * 根据id查询
     * @param sysLogVo
     * @return
     */
    @Override
    @Cacheable(value = "operationLog" ,key = "#sysLogVo.id")
    public SysLogVo findById(SysLogVo sysLogVo) {
        return sysLogMapper.findById(sysLogVo);
    }


    /**
     * 单个更新
     * @param sysLogVo
     * @return
     */
    @Override
    @CacheEvict(value="operationLog",key="#sysLogVo.id")
    public int updateById(SysLogVo sysLogVo) {
        return sysLogMapper.updateById(sysLogVo);
    }


    /**
     * 批量
     * @param sysLogVo
     * @return
     */
    @Override
    @CacheEvict(value="operationLog",allEntries = true)
    public int deleteBatch(SysLogVo sysLogVo) {
        return sysLogMapper.deleteById(sysLogVo);
    }
}
