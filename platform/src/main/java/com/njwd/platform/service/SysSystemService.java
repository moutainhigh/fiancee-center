package com.njwd.platform.service;


import com.njwd.entity.platform.dto.SysSystemDto;
import com.njwd.entity.platform.vo.SysSystemVo;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 子系统说明
 * @Date:9:23 2019/6/14
 **/
public interface SysSystemService  {


    /**
     * @Description 查询子系统说明列表
     * @Author liuxiang
     * @Date:16:59 2019/7/2
     * @Param [sysSystemDto]
     * @return java.util.List<com.njwd.platform.entity.vo.SysSystemVo>
     **/
    List<SysSystemVo> findSysSystemList(SysSystemDto sysSystemDto);

    /**
     * @Description 根据ID查询子系统说明
     * @Author liuxiang
     * @Date:16:59 2019/7/2
     * @Param [sysSystemDto]
     * @return com.njwd.platform.entity.vo.SysSystemVo
     **/
    SysSystemVo findSysSystemById(SysSystemDto sysSystemDto);
    
    /**
     * @description: 初始化
     * @param: [sysSystemDto]
     * @return: int 
     * @author: xdy        
     * @create: 2019-09-05 16:28 
     */
    int initData(SysSystemDto sysSystemDto);

}
