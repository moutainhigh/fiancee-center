package com.njwd.basedata.api;

import com.njwd.entity.basedata.SysMenu;
import com.njwd.entity.basedata.SysUserRole;
import com.njwd.entity.basedata.dto.SysMenuDto;
import com.njwd.entity.basedata.dto.SysRoleDto;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Set;

/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/5/20
 */
@RequestMapping("financeback/sysMenu")
public interface SysMenuApi {

	@PostMapping("add")
	Result add(SysMenu sysMenu);

	@PostMapping("update")
	Result update(SysMenu sysMenu);


	@PostMapping("deleteBatch")
	Result deleteBatch(SysMenuDto sysMenuDto);

	@PostMapping("findPage")
	Result findPage(SysMenuDto sysMenuDto);

	/**
	 * 查询roleId对应的权限,必有roleId
	 */
	@PostMapping("findList")
	Result findList(SysRoleDto sysRoleDto);

	/**
	 * 查询所有权限树,缓存整棵权限树
	 */
	@PostMapping("findTree")
	Result findTree(SysRoleDto sysRoleDto);

	/**
	 * 查询用户+公司id下的权限列表
	 */
	@PostMapping("findListByCompanyAndUser")
	Result findListByCompanyAndUser(SysUserRole sysUserRole);

	/**
	 * 查询当前企业可选择子系统列表
	 * @param sysMenuDto
	 * @return
	 */
	@PostMapping("findEnableSysList")
	Result findEnableSysList(SysMenuDto sysMenuDto);

	/**
	 * 查询当前企业可选择菜单列表
	 * @param sysMenuDto
	 * @return
	 */
	@PostMapping("findEnableMenuList")
	Result findEnableMenuList(SysMenuDto sysMenuDto);

	/**
	 * 查询用户有权的权限标识
	 *
	 * @param sysUserVo sysUserVo
	 * @return com.njwd.support.Result<java.util.Set < java.lang.String>>
	 * @author xyyxhcj@qq.com
	 * @date 2019/8/3 9:15
	 **/
	@PostMapping("getPermissionsByUserId")
	Result<Set<String>> getPermissionsByUserId(SysUserVo sysUserVo);
}
