package com.njwd.financeback.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.SysMenu;
import com.njwd.entity.basedata.SysUserRole;
import com.njwd.entity.basedata.dto.SysMenuDto;
import com.njwd.entity.basedata.dto.SysRoleDto;
import com.njwd.entity.basedata.vo.SysMenuVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.financeback.service.SysMenuService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.RedisUtils;
import com.njwd.utils.ShiroUtils;
import com.njwd.utils.UserUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/5/20
 */
@RestController
@RequestMapping("sysMenu")
public class SysMenuController extends BaseController {
	@Resource
	private SysMenuService sysMenuService;

	@PostMapping("add")
	public Result<Long> add(@RequestBody SysMenu sysMenu) {
		ShiroUtils.checkRole(Constant.ShiroAdminDefi.SYS_ADMIN, null);
		SysUserVo operator = UserUtils.getUserVo();
		sysMenu.setCreatorId(operator.getUserId());
		sysMenu.setCreatorName(operator.getName());
		return syncCheckCode(sysMenu, () -> sysMenuService.add(sysMenu));
	}

	@PostMapping("update")
	public Result<Long> update(@RequestBody SysMenu sysMenu) {
		ShiroUtils.checkRole(Constant.ShiroAdminDefi.SYS_ADMIN, null);
		SysUserVo operator = UserUtils.getUserVo();
		sysMenu.setUpdatorId(operator.getUserId());
		sysMenu.setUpdatorName(operator.getName());
		sysMenu.setIsDel(null);
		return syncCheckCode(sysMenu, () -> sysMenuService.update(sysMenu));
	}

	/**
	 * 使用锁校验code
	 */
	private Result<Long> syncCheckCode(SysMenu sysMenu, RedisUtils.LockProcess<Long> lockProcess) {
		Long id;
		if (StringUtils.isEmpty(sysMenu.getCode())) {
			id = lockProcess.execute();
		} else {
			id = RedisUtils.lock(String.format(Constant.LockKey.SYS_MENU_CODE, sysMenu.getCode()), Constant.SysConfig.REDIS_LOCK_TIMEOUT, lockProcess);
		}
		return ok(id);
	}

	@PostMapping("deleteBatch")
	public Result deleteBatch(@RequestBody SysMenuDto sysMenuDto) {
		ShiroUtils.checkRole(Constant.ShiroAdminDefi.SYS_ADMIN, null);
		sysMenuDto.setIsDel(Constant.Is.YES);
		return confirm(sysMenuService.updateBatch(sysMenuDto, UserUtils.getUserVo()));
	}

	@PostMapping("findPage")
	public Result<Page<SysMenuVo>> findPage(@RequestBody SysMenuDto sysMenuDto) {
		ShiroUtils.checkRole(Constant.ShiroAdminDefi.SYS_ADMIN, null);
		return ok(sysMenuService.findPage(sysMenuDto));
	}

	/**
	 * 查询roleId对应的权限,必有roleId
	 */
	@PostMapping("findList")
	public Result<List<SysMenuVo>> findList(@RequestBody SysRoleDto sysRoleDto) {
		FastUtils.checkParams(sysRoleDto.getRoleId());
		return ok(sysMenuService.findList(sysRoleDto));
	}

	/**
	 * 查询所有权限树,缓存整棵权限树
	 */
	@PostMapping("findTree")
	public Result<List<SysMenuVo>> findTree(@RequestBody SysRoleDto sysRoleDto) {
		return ok(sysMenuService.findList(sysRoleDto));
	}

	/**
	 * 查询用户+公司id下的权限列表
	 */
	@PostMapping("findListByCompanyAndUser")
	public Result<List<Long>> findListByCompanyAndUser(@RequestBody SysUserRole sysUserRole) {
		sysUserRole.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
		return ok(sysMenuService.findListByCompanyAndUser(sysUserRole));
	}

	/**
	 * 查询当前企业可选择子系统列表
	 * @param sysMenuDto
	 * @return
	 */
	@PostMapping("findEnableSysList")
	public Result<Page<SysMenuVo>> findEnableSysList(@RequestBody SysMenuDto sysMenuDto) {
		SysUserVo operator = UserUtils.getUserVo();
		ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
		sysMenuDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
		return ok(sysMenuService.findEnableSysList(sysMenuDto));
	}

	/**
	 * 查询当前企业可选择菜单列表
	 * @param sysMenuDto
	 * @return
	 */
	@PostMapping("findEnableMenuList")
	public Result<Page<SysMenuVo>> findEnableMenuList(@RequestBody SysMenuDto sysMenuDto) {
		SysUserVo operator = UserUtils.getUserVo();
		ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
		sysMenuDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
		return ok(sysMenuService.findEnableMenuList(sysMenuDto));
	}

	/**
	 * 查询用户有权的权限标识
	 **/
	@PostMapping("getPermissionsByUserId")
	public Result<Set<String>> getPermissionsByUserId(@RequestBody SysUserVo sysUserVo) {
		return ok(sysMenuService.getPermissionDefinitionsByUserId(sysUserVo));
	}
}
