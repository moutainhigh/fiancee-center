package com.njwd.financeback.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.basedata.SysUserEnterprise;
import com.njwd.entity.basedata.dto.SysUserDto;
import com.njwd.entity.basedata.remote.resp.UserLogin;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.basedata.vo.UserAccountBookEntityVo;
import com.njwd.financeback.service.SysRoleService;
import com.njwd.financeback.service.SysUserService;
import com.njwd.handler.UserRealm;
import com.njwd.logger.SenderService;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
import com.njwd.utils.ShiroUtils;
import com.njwd.utils.UserUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 用户 前端控制器
 *
 * @author xyyxhcj@qq.com
 * @since 2019/05/22
 */
@RestController
@RequestMapping("sysUser")
public class SysUserController extends BaseController {
	@Resource
	private SysUserService sysUserService;
	@Resource
	private SysRoleService sysRoleService;
	@Resource
	private SenderService senderService;
	@Resource
	private UserRealm userRealm;

	/**
	 * 新增租户下的用户
	 */
	@PostMapping("addUser")
	public Result addUser(@RequestBody SysUserDto sysUserDto) {
		SysUserVo operator = UserUtils.getUserVo();
		ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
		FastUtils.checkParams(sysUserDto.getUserId(), sysUserDto.getName());
		sysUserDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		sysUserDto.setCreatorId(operator.getUserId());
		sysUserDto.setCreatorName(operator.getName());
		sysUserService.addUser(sysUserDto);
		senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.users, LogConstant.operation.add, LogConstant.operation.add_type, sysUserDto.getUserId().toString()));
		return ok(true);
	}

	/**
	 * http 批量引入
	 */
	@PostMapping("httpAddUserBatch")
	public Result<List<Long>> httpAddUserBatch(@RequestBody SysUserDto sysUserDto) {
		SysUserVo operator = new SysUserVo();
		operator.setUserId(sysUserDto.getCreatorId());
		operator.setName(sysUserDto.getCreatorName());
		List<Long> userIds = sysUserService.httpAddUserBatch(sysUserDto, operator);
		System.out.println(" userIds :"+userIds);
		return ok(userIds);
	}

	/**
	 * 批量引入
	 */
	@PostMapping("addUserBatch")
	public Result addUserBatch(@RequestBody SysUserDto sysUserDto) {
		SysUserVo operator = UserUtils.getUserVo();
		ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
		FastUtils.checkParams(sysUserDto.getSysUserDtoList());
		List<Long> userIds = sysUserService.addUserBatch(sysUserDto, operator);
		senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.users, LogConstant.operation.addBatch, LogConstant.operation.addBatch_type, userIds.toString()));
		return ok(true);
	}

	/**
	 * 校验平台token 自动登录 租户管理员与平台管理角色不添加,通过登录参数判断是否租户管理及平台管理,是则设置对应角色数据(不存库)
	 */
	@RequestMapping("login")
	public Result<SysUserVo> login(@RequestBody UserLogin userLogin, HttpServletRequest request) {
		return ok(sysUserService.login(userLogin, request.getSession().getId()));
	}

	/**
	 * 禁用
	 */
	@PostMapping("disableBatch")
	public Result<BatchResult> disableBatch(@RequestBody SysUserDto sysUserDto) {
		SysUserVo operator = UserUtils.getUserVo();
		Long rootEnterpriseId = operator.getRootEnterpriseId();
		ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, rootEnterpriseId);
		sysUserDto.setIsEnable(Constant.Is.NO);
		BatchResult batchResult = sysUserService.updateBatch(sysUserDto, operator);
		userRealm.clearCachedAuthList(rootEnterpriseId, batchResult.getSuccessList());
		senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.users, LogConstant.operation.forbidden, LogConstant.operation.forbidden_type, batchResult.getSuccessList().toString()));
		return ok(batchResult);


	}

	/**
	 * 反禁用
	 */
	@PostMapping("enableBatch")
	public Result<BatchResult> enableBatch(@RequestBody SysUserDto sysUserDto) {
		SysUserVo operator = UserUtils.getUserVo();
		Long rootEnterpriseId = operator.getRootEnterpriseId();
		ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, rootEnterpriseId);
		sysUserDto.setIsEnable(Constant.Is.YES);
		BatchResult batchResult = sysUserService.updateBatch(sysUserDto, operator);
		userRealm.clearCachedAuthList(rootEnterpriseId, batchResult.getSuccessList());
		senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.users, LogConstant.operation.antiForbidden, LogConstant.operation.antiForbidden_type, batchResult.getSuccessList().toString()));
		return ok(batchResult);
	}

	/**
	 * 删除
	 */
	@PostMapping("deleteBatch")
	public Result<BatchResult> deleteBatch(@RequestBody SysUserDto sysUserDto) {
		SysUserVo operator = UserUtils.getUserVo();
		ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
		sysUserDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		BatchResult batchResult = sysUserService.updateBatchDelete(sysUserDto);
		senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.users, LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, batchResult.getSuccessList().toString()));
		return ok(batchResult);
	}

	/**
	 * 修改默认公司等个人自定义数据
	 */
	@PostMapping("updateBySelf")
	public Result updateBySelf(@RequestBody SysUserDto sysUserDto) {
		@NotNull SysUserVo operator = UserUtils.getUserVo();
		Result result = confirm(sysUserService.updateBySelf(sysUserDto, operator));
		senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.users, LogConstant.operation.update, LogConstant.operation.update_type, operator.getUserId().toString()));
		return result;
	}

	/**
	 * 查个人的自定义数据
	 **/
	@PostMapping("findConfig")
	public Result findConfig() {
		SysUserVo operator = UserUtils.getUserVo();
		SysUserEnterprise sysUserEnterprise = sysUserService.findConfig(operator);
		return ok(sysUserEnterprise);
	}

	/**
	 * 根据userId+rootEnterpriseId查详情
	 */
	@PostMapping("findDetail")
	public Result<SysUserVo> findDetail(@RequestBody SysUserDto sysUserDto) {
		sysUserDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
		return ok(sysUserService.findDetail(sysUserDto));
	}
	
	/**
	 * 总账内部调用 获取用户实时Vo
	 **/
	@PostMapping("findUserVo")
	public Result<SysUserVo> findUserVo(@RequestBody SysUserDto sysUserDto) {
		return ok(sysUserService.findUserVo(sysUserDto));
	}

	@PostMapping("findPage")
	public Result<Page<SysUserVo>> findPage(@RequestBody SysUserDto sysUserDto) {
		if (sysUserDto.getRootEnterpriseId() == null) {
			sysUserDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
		}
		return ok(sysUserService.findPage(sysUserDto));
	}

	@PostMapping("exportExcel")
	public void exportExcel(@RequestBody SysUserDto sysUserDto,HttpServletResponse response) {
		if (sysUserDto.getRootEnterpriseId() == null) {
			sysUserDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
		}
		sysUserService.exportExcel(sysUserDto,response);
	}


	/**
	 * 查询未引入的用户分页
	 */
	@PostMapping("findNotImportPage")
	public Result<Page<SysUserVo>> findNotImportPage(@RequestBody SysUserDto sysUserDto) {
		return ok(sysUserService.findNotImportPage(sysUserDto));
	}

	/**
	 * 查询当前租户下有权限的用户分页
	 */
	@PostMapping("findPermPage")
	public Result<Page<SysUserVo>> findPermPage(@RequestBody SysUserDto sysUserDto) {
		if (sysUserDto.getRootEnterpriseId() == null) {
			sysUserDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
		}
		return ok(sysUserService.findPermPage(sysUserDto));
	}

	/**
	 * 查询公司下用户关联的账簿主体，以userId为key
	 */
	@PostMapping("findAccountBookEntityByCompany")
	public Result<Map<Long, List<UserAccountBookEntityVo>>> findAccountBookEntityByCompany(@RequestBody SysUserDto sysUserDto) {
		return ok(sysUserService.findAccountBookEntityByCompany(sysUserDto));
	}

	/**
	 * 根据公司下的岗位反查用户
	 */
	@PostMapping("findRoleUserPage")
	public Result<Page<SysUserVo>> findRoleUserPage(@RequestBody SysUserDto sysUserDto) {
		if (sysUserDto.getRootEnterpriseId() == null) {
			sysUserDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
		}
		return ok(sysUserService.findRoleUserPage(sysUserDto));
	}

	/**
	 * 登出
	 */
	@RequestMapping("loginOut")
	public Result loginOut() {
		SecurityUtils.getSubject().logout();
		return ok(true);
	}

	/**
	 * 用户分配公司角色
	 */
	@PostMapping("assign")
	public Result assign(@RequestBody SysUserDto sysUserDto) {
		SysUserVo operator = UserUtils.getUserVo();
		ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
		sysUserDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		sysRoleService.assign(sysUserDto, operator);
		senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.users, LogConstant.operation.auth, LogConstant.operation.auth_type, sysUserDto.getUserId().toString()));
		return ok(true);
	}

	/**
	 * 批量设置用户的公司岗位权限
	 */
	@PostMapping("assignBatchAdd")
	public Result assignBatchAdd(@RequestBody SysUserDto sysUserDto) {
		SysUserVo operator = UserUtils.getUserVo();
		ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
		List<Long> editUserIds = sysRoleService.assignBatchAdd(sysUserDto, operator);
		senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.users, LogConstant.operation.authBatch, LogConstant.operation.authBatch_type, editUserIds.toString()));
		return ok(true);
	}

	/**
	 * 去除用户的公司岗位权限及账簿主体
	 */
	@PostMapping("assignDelete")
	public Result assignDelete(@RequestBody SysUserDto sysUserDto) {
		SysUserVo operator = UserUtils.getUserVo();
		ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
		sysRoleService.assignDelete(sysUserDto, operator);
		senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.users, LogConstant.operation.unAuthBatch, LogConstant.operation.unAuthBatch_type, sysUserDto.getUserId().toString()));
		return ok(true);
	}

	/**
	 * 设置是否为业务管理员
	 */
	@PostMapping("assignBusinessAdmin")
	public Result assignBusinessAdmin(@RequestBody SysUserDto sysUserDto) {
		SysUserVo operator = UserUtils.getUserVo();
		ShiroUtils.checkRole(Constant.ShiroAdminDefi.ENTERPRISE_ADMIN, operator.getRootEnterpriseId());
		sysUserDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		sysRoleService.assignBusinessAdmin(sysUserDto, operator);
		senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.users, LogConstant.operation.authAdmin, LogConstant.operation.authAdmin_type, sysUserDto.getUserId().toString()));
		return ok(true);
	}

	/**
	 * 查询当前企业可选用户列表 分页
	 * @param sysUserDto
	 * @return
	 */
	@PostMapping("findEnableUserList")
	public Result<Page<SysUserVo>> findEnableUserList(@RequestBody SysUserDto sysUserDto){
		SysUserVo operator = UserUtils.getUserVo();
		ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
		sysUserDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		return ok(sysUserService.findEnableList(sysUserDto));
	}
}
