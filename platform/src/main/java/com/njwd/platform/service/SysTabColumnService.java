package com.njwd.platform.service;


import com.njwd.entity.platform.dto.SysTabColumnDto;
import com.njwd.entity.platform.vo.SysTabColumnVo;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 表格配置
 * @Date:9:25 2019/6/14
 **/
public interface SysTabColumnService{


    /**
     * @Description 查询表格配置列表
     * @Author liuxiang
     * @Date:17:03 2019/7/2
     * @Param [sysTabColumnVo]
     * @return java.util.List<com.njwd.platform.entity.vo.SysTabColumnVo>
     **/
    List<SysTabColumnVo> findSysTabColumnList(SysTabColumnDto sysTabColumnDto);

    /**
     * @Description 根据ID查询表格配置
     * @Author liuxiang
     * @Date:17:03 2019/7/2
     * @Param [sysTabColumnVo]
     * @return com.njwd.platform.entity.vo.SysTabColumnVo
     **/
    SysTabColumnVo findSysTabColumnById(SysTabColumnDto sysTabColumnDto);
}
