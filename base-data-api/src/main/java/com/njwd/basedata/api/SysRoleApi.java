package com.njwd.basedata.api;

import com.njwd.entity.basedata.SysRole;
import com.njwd.entity.basedata.dto.SysRoleDto;
import com.njwd.entity.basedata.dto.SysUserDto;
import com.njwd.entity.basedata.dto.UserRoleDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 岗位 前端控制器
 *
 * @author xyyxhcj@qq.com
 * @since 2019/05/21
 */
@RequestMapping("sysRole")
public interface SysRoleApi {

	/**
	 * 平台管理员创建通用角色/岗位
	 */
	@PostMapping("add")
	String add(SysRole sysRole);

	@PostMapping("addEnterprise")
	String addEnterprise(SysRole sysRole);

	/**
	 * 创建租户岗位同时分配权限
	 */
	@PostMapping("addEnterpriseWithPerm")
	String addEnterpriseWithPerm(SysRoleDto sysRoleDto);

	@PostMapping("update")
	String update(SysRole sysRole);

	@PostMapping("updateEnterprise")
	String updateEnterprise(SysRole sysRole);
	
	/**
	 * 禁用
	 */
	@PostMapping("disableEnterpriseBatch")
	String disableBatch(SysRoleDto sysRoleDto);

	/**
	 * 反禁用
	 */
	@PostMapping("enableEnterpriseBatch")
	String enableBatch(SysRoleDto sysRoleDto);

	@PostMapping("deleteEnterpriseBatch")
	String deleteEnterpriseBatch(SysRoleDto sysRoleDto);

	@PostMapping("deleteBatch")
	String deleteBatch(SysRoleDto sysRoleDto);

	@PostMapping("findById")
	String findById(SysRoleDto sysRoleDto);

	@PostMapping("findPage")
	String findPage(SysRoleDto sysRoleDto);

	/**
	 * rootEnterpriseId必有,有userId则查询对应的角色,无userId则查询租户下的所有岗位
	 */
	@PostMapping("findList")
	String findList(SysUserDto sysUserDto);

	/**
	 * 配置通用岗位的权限
	 */
	@PostMapping("assign")
	String assign(SysRoleDto sysRoleDto);

	@PostMapping("assignEnterprise")
	String assignEnterprise(SysRoleDto sysRoleDto);

	/**
	 * 查询编码是否重复
	 */
	@PostMapping("checkCode")
	String checkCode(SysRoleDto sysRoleDto);


	/**
	 * 用户权限列表 分页
	 */
	@PostMapping("findUserRolePage")
	String findUserRolePage(UserRoleDto userRoleDto);

	/**
	 * 查询当前企业可用岗位列表 分页
	 */
	@RequestMapping("findEnableRoleList")
	String findEnableRoleList(SysRoleDto sysRoleDto);

	/**
	 * 校验数据是否被引用,被引用则不允许修改
	 */
	@PostMapping("checkRefer")
	String checkRefer(SysRoleDto sysRoleDto);
}
