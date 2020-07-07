package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.basedata.SysMenuOptionTable;
import com.njwd.entity.platform.SysMenuOption;
import com.njwd.entity.platform.dto.SysMenuOptionDto;
import com.njwd.entity.platform.vo.SysMenuOptionVo;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 菜单选项
 * @Date:17:42 2019/6/13
 **/
public interface SysMenuOptionMapper extends BaseMapper<SysMenuOption> {

    /**
     * @Description 查询菜单选项列表
     * @Author liuxiang
     * @Date:15:34 2019/7/2
     * @Param [sysMenuOptionVo]
     * @return java.util.List<com.njwd.platform.entity.vo.SysMenuOptionVo>
     **/
    List<SysMenuOptionVo> findSysMenuOptionList(SysMenuOptionDto sysMenuOptionDto);

    /**
     * @Description 根据ID查询菜单选项
     * @Author liuxiang
     * @Date:15:34 2019/7/2
     * @Param [sysMenuOptionVo]
     * @return com.njwd.platform.entity.vo.SysMenuOptionVo
     **/
    SysMenuOptionVo findSysMenuOptionById(SysMenuOptionDto sysMenuOptionDto);

    /**
     * @description: 菜单选项数据表
     * @param: [menuCode]
     * @return: com.njwd.entity.basedata.SysMenuOptionTable 
     * @author: xdy        
     * @create: 2019-09-09 16:50 
     */
    SysMenuOptionTable findOptionTable(String menuCode);

}