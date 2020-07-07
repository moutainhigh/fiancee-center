package com.njwd.basedata.api;

import com.njwd.entity.basedata.dto.SysUserDto;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Description 用户 前端控制器
 * @Author 朱小明
 * @Date 2019/8/1 9:48
 **/
@RequestMapping("financeback/sysUser")
public interface SysUserApi {

	/**
	 * 新增租户下的用户
	 */
	@PostMapping("addUser")
	String addUser(SysUserDto sysUserDto);

	/**
	 * 批量引入
	 */
	@PostMapping("addUserBatch")
	String addUserBatch(SysUserDto sysUserDto);

	/**
	 * http 批量引入
	 */
	@PostMapping("httpAddUserBatch")
	String httpAddUserBatch(SysUserDto sysUserDto);

	/**
	 * 禁用
	 */
	@PostMapping("disableBatch")
	String disableBatch(SysUserDto sysUserDto);

	/**
	 * 反禁用
	 */
	@PostMapping("enableBatch")
	String enableBatch(SysUserDto sysUserDto);

	/**
	 * 删除
	 */
	@PostMapping("deleteBatch")
	String deleteBatch(SysUserDto sysUserDto);

	/**
	 * 修改默认公司
	 */
	@PostMapping("updateBySelf")
	String updateBySelf(SysUserDto sysUserDto);

	/**
	 * 根据userId+rootEnterpriseId查详情
	 */
	@PostMapping("findDetail")
	Result<SysUserVo> findDetail(SysUserDto sysUserDto);

	/**
	 * 获取用户实时Vo
	 *
	 * @param sysUserDto sysUserDto
	 * @return java.lang.String
	 * @author xyyxhcj@qq.com
	 * @date 2019/9/6 11:27
	 **/
	@PostMapping("findUserVo")
	Result<SysUserVo> findUserVo(SysUserDto sysUserDto);

	@PostMapping("findPage")
	String findPage(SysUserDto sysUserDto);

	/**
	 * 查询未引入的用户分页
	 */
	@PostMapping("findNotImportPage")
	String findNotImportPage(SysUserDto sysUserDto);

	/**
	 * 查询当前租户下有权限的用户分页
	 */
	@PostMapping("findPermPage")
	String findPermPage(SysUserDto sysUserDto);

	/**
	 * 查询公司下用户关联的账簿主体，以userId为key
	 */
	@PostMapping("findAccountBookEntityByCompany")
	String findAccountBookEntityByCompany(SysUserDto sysUserDto);

	/**
	 * 根据公司下的岗位反查用户
	 */
	@PostMapping("findRoleUserPage")
	String findRoleUserPage(SysUserDto sysUserDto);

	/**
	 * 登出
	 */
	@RequestMapping("loginOut")
	String loginOut();

	/**
	 * 用户分配公司角色
	 */
	@PostMapping("assign")
	String assign(SysUserDto sysUserDto);

	/**
	 * 批量设置用户的公司岗位权限
	 */
	@PostMapping("assignBatchAdd")
	String assignBatchAdd(SysUserDto sysUserDto);

	/**
	 * 去除用户的公司岗位权限及账簿主体
	 */
	@PostMapping("assignDelete")
	String assignDelete(SysUserDto sysUserDto);

	/**
	 * 设置是否为业务管理员
	 */
	@PostMapping("assignBusinessAdmin")
	String assignBusinessAdmin(SysUserDto sysUserDto);

	/**
	 * 查询当前企业可选用户列表 分页
	 * @param sysUserDto
	 * @return
	 */
	@PostMapping("findEnableUserList")
	String findEnableUserList(SysUserDto sysUserDto);
}
