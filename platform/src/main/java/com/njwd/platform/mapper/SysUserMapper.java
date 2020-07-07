package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.SysUser;
import com.njwd.entity.platform.SysUserEnterprise;
import com.njwd.entity.platform.dto.SysMenuDto;
import com.njwd.entity.platform.dto.SysUserDto;
import com.njwd.entity.platform.vo.SysMenuVo;
import com.njwd.entity.platform.vo.SysRoleMenuVo;
import com.njwd.entity.platform.vo.SysUserVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author zhuzs
 * @date 2019-11-12 15:23
 */
public interface SysUserMapper extends BaseMapper<SysUser> {
    // —————————————————————————— 用户 ——————————————————————————
    // —————————————————————————— 用户—菜单/权限 ——————————————————————————

    /**
     * 新增 用户—菜单/权限
     *
     * @param: [sysUserMenuDtoList]
     * @return: java.lang.Integer
     * @author: zhuzs
     * @date: 2019-11-15
     */
    Integer addSysUserMenu(@Param("sysUserDto") SysUserDto sysUserDto, @Param("operator") SysUser operator);

    /**
     *
     *
     * @param: [sysUserDto, operator]
     * @return: java.lang.Integer
     * @author: zhuzs
     * @date: 2019-11-25
     */
    Integer addSysUserMenuList(@Param("sysUserDto")SysUserDto sysUserDto, @Param("operator")SysUserVo operator);

    /**
     * 根据 userId 删除用户已分配权限信息
     *
     * @param: [sysUserDto]
     * @return: void
     * @author: zhuzs
     * @date: 2019-11-18
     */
    void deleteUserMenuByUserId(@Param("sysUserDto") SysUserDto sysUserDto);

    /**
     * 已配置 菜单/权限 列表
     *
     * @param: [sysUserDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.SysUserMenuVo>
     * @author: zhuzs
     * @date: 2019-11-19
     */
    List<SysMenuVo> findAssedMenuList(@Param("sysUserDto") SysUserDto sysUserDto);

    /**
     * 已配置 菜单/权限 树
     *
     * @param: [sysUserDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.SysMenuVo>
     * @author: zhuzs
     * @date: 2019-11-22
     */
    List<SysMenuVo> findAssedMenuTree(@Param("sysUserDto") SysUserDto sysUserDto);

    // —————————————————————————— 角色-菜单/权限 ——————————————————————————

    /**
     * 返回 实施、运营、产品、管理员 拥有的角色权限列表
     *
     * @param: []
     * @return: java.util.List<com.njwd.entity.platform.vo.SysRoleVo>
     * @author: zhuzs
     * @date: 2019-11-18
     */
    List<SysRoleMenuVo> findRoleMenuList();

    // —————————————————————————— 用户—租户 ——————————————————————————

    /**
     * 分配租户
     *
     * @param: [sysUserDto]
     * @return: void
     * @author: zhuzs
     * @date: 2019-11-19
     */
    Integer assEnterprises(@Param("sysUserDto") SysUserDto sysUserDto, @Param("operator") SysUser operator);

    /**
     * 根据租户ID 删除用户-租户关联信息
     *
     * @param: []
     * @return: void
     * @author: zhuzs
     * @date: 2019-11-19
     */
    Integer delAssignedEnterpriseByEnterpriseIds(@Param("sysUserDto") SysUserDto sysUserDto);

    /**
     * 获取已分配租户信息
     *
     * @param: [sysUserDto]
     * @return: java.util.List<com.njwd.entity.platform.SysUserEnterprise>
     * @author: zhuzs
     * @date: 2019-11-18
     */
    List<SysUserEnterprise> findAssnedEnterList(@Param("sysUserDto") SysUserDto sysUserDto);

    /**
     * 根据 用户idList ，查询已分配租户的用户
     *
     * @param: [sysUserDto]
     * @return: java.util.List<java.lang.Long>
     * @author: zhuzs
     * @date: 2019-11-25
     */
    List<Long> findUserIds(@Param("sysUserDto")SysUserDto sysUserDto);

    /**
     * 用户权限列表
     *
     * @param sysMenuDto
     * @param page
     * @return
     */
    List<SysMenuVo> findUserRoleMenuList(@Param("page") Page<SysMenuVo> page, @Param("sysMenuDto") SysMenuDto sysMenuDto);

    // —————————————————————————— 用户—角色 ——————————————————————————

    /**
     * 新增用户角色
     *
     * @param: [sysUserDto]
     * @return: java.lang.Integer
     * @author: zhuzs
     * @date: 2019-11-29
     */
    Integer addSysUserRole(@Param("sysUserDto") SysUserDto sysUserDto,@Param("operator") SysUser operator);

    /**
     * 删除用户角色
     *
     * @param: [toDelUserIdList]
     * @return: java.lang.Integer
     * @author: zhuzs
     * @date: 2019-11-29
     */
    Integer delUserRoleByUserIds(@Param("sysUserDto") SysUserDto sysUserDto);

    /**
     * 查询用户已分配角色
     *
     * @param: []
     * @return: java.util.List<java.lang.Long>
     * @author: zhuzs
     * @date: 2019-11-29
     */
    List<Long> findAssedRoleIdList(@Param("sysUserDto") SysUserDto sysUserDto);
}

