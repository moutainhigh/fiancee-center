package com.njwd.financeback.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.SysMenu;
import com.njwd.entity.basedata.SysRole;
import com.njwd.entity.basedata.SysUserRole;
import com.njwd.entity.basedata.dto.SysMenuDto;
import com.njwd.entity.basedata.dto.SysRoleDto;
import com.njwd.entity.basedata.vo.SysMenuVo;
import com.njwd.entity.basedata.vo.SysUserVo;

import java.util.List;
import java.util.Set;

/**
 * @author xyyxhcj@qq.com
 * @since 2019-05-20
 */

public interface SysMenuService {
	/**
	 * add
	 * @param sysMenu sysMenu
	 * @return Long
	 */
	Long add(SysMenu sysMenu);

	/**
	 * update
	 * @param sysMenu sysMenu
	 * @return Long
	 */
	Long update(SysMenu sysMenu);

	/**
	 * shiro登录时获取权限列表
	 * @param sysUserVo userId
	 * @return set
	 */
	Set<String> getPermissionDefinitionsByUserId(SysUserVo sysUserVo);

	/**
	 * 分页
	 * @param sysMenuDto params
	 * @return page
	 */
	Page<SysMenuVo> findPage(SysMenuDto sysMenuDto);

	/**
	 * 提供roleId时查询所有,未提供roleId时返回权限树
	 * 提供parentId时返回该parentId为根级的权限树
	 * @param sysRoleDto sysRoleDto
	 * @return list
	 */
	List<SysMenuVo> findList(SysRoleDto sysRoleDto);

	/**
	 * 查询所有岗位的所有权限,必有roleId
	 * @param sysRole roleId
	 * @return list
	 */
	List<SysMenuVo> getList(SysRole sysRole);

	/**
	 * 为角色赋权,包含校验roleId
	 * @param sysRoleDto params
	 * @param operator 操作人
	 */
	void assign(SysRoleDto sysRoleDto, SysUserVo operator);
	/**
	 * 为角色赋权,不校验
	 * @param sysRoleDto params
	 * @param operator 操作人
	 */
	void assignPerm(SysRoleDto sysRoleDto, SysUserVo operator);
	/**
	 * 批量禁用/反禁用/删除
	 * @param sysMenuDto sysMenuDto
	 * @param operator operator
	 * @return int
	 */
	int updateBatch(SysMenuDto sysMenuDto, SysUserVo operator);

	/**
	 * 查询用户+公司id下的权限列表
	 * @param sysUserRole sysUserRole
	 * @return list
	 */
	List<Long> findListByCompanyAndUser(SysUserRole sysUserRole);

	/**
	 * 当前企业可选择子系统列表
	 * @param sysMenuDto
	 * @return
	 */
	Page<SysMenuVo> findEnableSysList(SysMenuDto sysMenuDto);

	/**
	 * 当前企业可选择菜单列表
	 * @param sysMenuDto
	 * @return
	 */
	Page<SysMenuVo> findEnableMenuList(SysMenuDto sysMenuDto);
}
