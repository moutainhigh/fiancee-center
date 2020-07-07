package com.njwd.platform.service.impl;


import com.njwd.entity.basedata.SysMenuOptionTable;
import com.njwd.entity.platform.dto.SysMenuOptionDto;
import com.njwd.entity.platform.vo.SysMenuOptionVo;
import com.njwd.platform.mapper.SysMenuOptionMapper;
import com.njwd.platform.service.SysMenuOptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 菜单选项
 * @Date:16:53 2019/7/2
 **/
@Service
public class SysMenuOptionServiceImpl implements SysMenuOptionService {

    @Autowired
    private SysMenuOptionMapper sysMenuOptionMapper;

    /**
     * @Description 查询菜单选项列表
     * @Author liuxiang
     * @Date:16:53 2019/7/2
     * @Param [sysMenuOptionVo]
     * @return java.util.List<com.njwd.platform.entity.vo.SysMenuOptionVo>
     **/
    @Override
    @Cacheable(value = "sysMenuOptionList", key = "#sysMenuOptionDto.menuCode+''")
    public List<SysMenuOptionVo> findSysMenuOptionList(SysMenuOptionDto sysMenuOptionDto) {
        return sysMenuOptionMapper.findSysMenuOptionList(sysMenuOptionDto);
    }

    /**
     * @Description 根据ID查询菜单选项
     * @Author liuxiang
     * @Date:16:53 2019/7/2
     * @Param [sysMenuOptionVo]
     * @return com.njwd.platform.entity.vo.SysMenuOptionVo
     **/
    @Override
    @Cacheable(value = "sysMenuOptionById", key = "#sysMenuOptionDto.id+''",unless="#result == null")
    public SysMenuOptionVo findSysMenuOptionById(SysMenuOptionDto sysMenuOptionDto) {
        return sysMenuOptionMapper.findSysMenuOptionById(sysMenuOptionDto);
    }
    
    /**
     * @description: 
     * @param: [sysMenuOptionDto]
     * @return: com.njwd.entity.basedata.SysMenuOptionTable 
     * @author: xdy        
     * @create: 2019-09-09 16:53 
     */
    @Override
    public SysMenuOptionTable findOptionTable(SysMenuOptionDto sysMenuOptionDto) {
        return sysMenuOptionMapper.findOptionTable(sysMenuOptionDto.getMenuCode());
    }
}
