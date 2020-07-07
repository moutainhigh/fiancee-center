package com.njwd.financeback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.SysMenu;
import com.njwd.entity.basedata.SysUserRole;
import com.njwd.entity.basedata.dto.SysMenuDto;
import com.njwd.entity.basedata.vo.SysMenuVo;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/5/20
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {
	/**
	 * 校验菜单权限标识是否重复
	 *
	 * @param sysMenu params
	 * @return count
	 */
	int checkDuplication(@Param("sysMenu") SysMenu sysMenu);

	/**
	 * shiro登录时获取权限列表
	 *
	 * @param userId           userId
	 * @param rootEnterpriseId rootEnterpriseId
	 * @return set
	 */
	Set<String> getPermissionDefinitionsByUserId(@Param("userId") Long userId, @Param("rootEnterpriseId") Long rootEnterpriseId);

	/**
	 * 分页
	 *
	 * @param sysMenuDto params
	 * @param page       pageConfig
	 * @return list
	 */
	List<SysMenuVo> findPage(@Param("sysMenuDto") SysMenuDto sysMenuDto, @Param("page") Page<SysMenuVo> page);

	/**
	 * 查询角色关联的菜单列表,无角色id则查所有未停用的菜单树
	 *
	 * @param roleId   roleId
	 * @param parentId parentId
	 * @return list
	 */
	List<SysMenuVo> findList(@Param("roleId") Long roleId, @Param("parentId") Long parentId);

	/**
	 * 无userId查所有
	 *
	 * @param userId           userId
	 * @param rootEnterpriseId rootEnterpriseId
	 * @return list
	 */
	LinkedList<SysMenuVo> findListByUserId(@Param("userId") Long userId, @Param("rootEnterpriseId") Long rootEnterpriseId);

	/**
	 * 查询用户+公司id下的权限列表
	 *
	 * @param sysUserRole sysUserRole
	 * @return list
	 */
	List<Long> findListByCompanyAndUser(@Param("sysUserRole") SysUserRole sysUserRole);

	/**
	 * 根据菜单的parentId查询及目录名
	 * @param parentId parentId
	 * @return name
	 */
	String findNameByMenuParentId(@Param("parentId") Long parentId);

	/**
	 * 可选择子系统列表
	 * @param sysMenuDto
	 * @return
	 */
    List<SysMenuVo> findEnableSysList(@Param("sysMenuDto") SysMenuDto sysMenuDto, @Param("page") Page<SysMenuVo> page);

	/**
	 * 可选择菜单列表
	 * @param sysMenuDto
	 * @return
	 */
	List<SysMenuVo> findEnableMenuList(@Param("sysMenuDto") SysMenuDto sysMenuDto, @Param("page") Page<SysMenuVo> page);
}