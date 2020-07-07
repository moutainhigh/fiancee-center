package com.njwd.platform.service.impl;

import com.njwd.entity.platform.dto.SysTabColumnDto;
import com.njwd.entity.platform.vo.SysTabColumnVo;
import com.njwd.platform.mapper.SysTabColumnMapper;
import com.njwd.platform.service.SysTabColumnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * @Author liuxiang
 * @Description 表格配置
 * @Date:17:02 2019/7/2
 **/
@Service
public class SysTabColumnServiceImpl implements SysTabColumnService {

    @Autowired
    private SysTabColumnMapper sysTabColumnMapper;


    /**
     * @Description 查询表格配置列表
     * @Author liuxiang
     * @Date:17:02 2019/7/2
     * @Param [sysTabColumnVo]
     * @return java.util.List<com.njwd.platform.entity.vo.SysTabColumnVo>
     **/
    @Override
    //@Cacheable(value = "sysTabColumnList", key = "#sysTabColumnDto.menuCode+'-'+#sysTabColumnDto.isEnterpriseAdmin")
    public List<SysTabColumnVo> findSysTabColumnList(SysTabColumnDto sysTabColumnDto) {
        return sysTabColumnMapper.findSysTabColumnList(sysTabColumnDto);
    }

    /**
     * @Description 根据ID查询表格配置
     * @Author liuxiang
     * @Date:17:03 2019/7/2
     * @Param [sysTabColumnVo]
     * @return com.njwd.platform.entity.vo.SysTabColumnVo
     **/
    @Override
    @Cacheable(value = "sysTabColumnById", key = "#sysTabColumnDto.id+''",unless="#result == null")
    public SysTabColumnVo findSysTabColumnById(SysTabColumnDto sysTabColumnDto) {
        return sysTabColumnMapper.findSysTabColumnById(sysTabColumnDto);
    }
}
