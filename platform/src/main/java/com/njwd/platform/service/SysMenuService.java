package com.njwd.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.SysMenuDto;
import com.njwd.entity.platform.dto.SysUserDto;
import com.njwd.entity.platform.vo.SysMenuVo;
import com.njwd.entity.platform.vo.SysRoleMenuVo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;

/**
 * 菜单权限
 *
 * @author: zhuzs
 * @date: 2019-11-12
 */
public interface SysMenuService {

    /**
     * 获取用户所有权限信息
     *
     * @param: [sysUserDto]
     * @return: java.util.Set<java.lang.String>
     * @author: zhuzs
     * @date: 2019-11-12
     */
    Set<String> findPermissionDefinitionsByUserId(SysUserDto sysUserDto);

    /**
     * 权限树
     *
     * @param: [sysRoleDto]
     * @return: java.util.List<com.njwd.entity.basedata.vo.SysMenuVo>
     * @author: zhuzs
     * @date: 2019-11-12
     */
    List<SysMenuVo> findList();

    /**
     * 根据类型 获取菜单列表
     *
     * @param: [sysMenuDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.SysMenuVo>
     * @author: zhuzs
     * @date: 2019-11-20
     */
    Page<SysMenuVo> findMenuPageByType(SysMenuDto sysMenuDto);

    /**
     * 返回 实施、运营、产品、管理员 拥有的角色权限列表
     *
     * @param: []
     * @return: com.njwd.entity.platform.vo.SysRoleMenuVo
     * @author: zhuzs
     * @date: 2019-11-21
     */
    SysRoleMenuVo findUserMenuList();

    /**
     * 用户权限表
     *
     * @param: [sysMenuDto]
     * @return: java.util.List
     * @author: zhuzs
     * @date: 2019-11-19
     */
    Page<SysMenuVo> findUserRoleMenuList(SysMenuDto sysMenuDto);

    /**
     * 数据重构
     *
     * @param: [sysUserMenuVos]
     * @return: java.util.List<com.njwd.entity.platform.vo.SysMenuVo>
     * @author: zhuzs
     * @date: 2019-11-21
     */
    List<SysMenuVo> rebuildData(List<SysMenuVo> sysUserMenuVos);

    /**
     * 用户权限表 导出
     *
     * @param: [sysMenuDto, response]
     * @return: void
     * @author: zhuzs
     * @date: 2019-11-26
     */
    void userRoleMenuListExport(SysMenuDto sysMenuDto, HttpServletResponse response);
}
