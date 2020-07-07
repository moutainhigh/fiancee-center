package com.njwd.platform.service;


import com.njwd.entity.basedata.SysMenuOptionTable;
import com.njwd.entity.platform.dto.SysMenuOptionDto;
import com.njwd.entity.platform.vo.SysMenuOptionVo;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 菜单选项
 * @Date:9:20 2019/6/14
 **/
public interface SysMenuOptionService  {

    /**
     * @Description 查询菜单选项列表
     * @Author liuxiang
     * @Date:16:53 2019/7/2
     * @Param [sysMenuOptionDto]
     * @return java.util.List<com.njwd.platform.entity.vo.SysMenuOptionVo>
     **/
    List<SysMenuOptionVo> findSysMenuOptionList(SysMenuOptionDto sysMenuOptionDto);

    /**
     * @Description 根据ID查询菜单选项
     * @Author liuxiang
     * @Date:16:53 2019/7/2
     * @Param [sysMenuOptionDto]
     * @return com.njwd.platform.entity.vo.SysMenuOptionVo
     **/
    SysMenuOptionVo findSysMenuOptionById(SysMenuOptionDto sysMenuOptionDto);
    
    /**
     * @description: 
     * @param: [sysMenuOptionDto]
     * @return: com.njwd.entity.basedata.SysMenuOptionTable 
     * @author: xdy        
     * @create: 2019-09-09 16:53
     */
    SysMenuOptionTable findOptionTable(SysMenuOptionDto sysMenuOptionDto);

}
