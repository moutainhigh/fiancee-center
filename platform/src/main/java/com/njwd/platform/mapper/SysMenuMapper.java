package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.SysMenu;
import com.njwd.entity.platform.dto.SysMenuDto;
import com.njwd.entity.platform.dto.SysUserDto;
import com.njwd.entity.platform.vo.SysMenuVo;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedList;
import java.util.Set;

/**
 * 菜单权限
 *
 * @author: zhuzs
 * @date: 2019-11-12
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 获取用户所有权限信息
     *
     * @param: [sysUserDto]
     * @return: java.util.Set<java.lang.String>
     * @author: zhuzs
     * @date: 2019-11-12
     */
    Set<String> findPermissionDefinitionsByUserId(@Param("sysUserDto") SysUserDto sysUserDto);

    /**
     * 权限树
     *
     * @param: [sysRoleDto]
     * @return: java.util.List<com.njwd.entity.basedata.vo.SysMenuVo>
     * @author: zhuzs
     * @date: 2019-11-12
     */
    LinkedList<SysMenuVo> findList(@Param("sysUserDto") SysUserDto sysUserDto);

    /**
     * 根据类型查询权限 分页
     *
     * @param: [sysMenuDto, page]
     * @return: java.util.LinkedList<com.njwd.entity.platform.vo.SysMenuVo>
     * @author: zhuzs
     * @date: 2019-11-21
     */
    Page<SysMenuVo> findPage(@Param("page") Page<SysMenuVo> page, @Param("sysMenuDto") SysMenuDto sysMenuDto);
}