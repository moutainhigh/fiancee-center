package com.njwd.financeback.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.SysRole;
import com.njwd.entity.basedata.dto.SysRoleDto;
import com.njwd.entity.basedata.dto.SysUserDto;
import com.njwd.entity.basedata.dto.UserRoleDto;
import com.njwd.entity.basedata.vo.SysRoleVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.basedata.vo.UserRoleVo;
import com.njwd.support.BatchResult;

import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019-05-21
 */

public interface SysRoleService {
	/**
	 * add
	 * @param sysRole sysRole
	 * @return id
	 */
	Long add(SysRole sysRole);

	/**
	 * 创建租户岗位同时分配权限
	 * @param sysRoleDto sysRoleDto
	 * @param operator operator
	 * @return id
	 */
	Long addEnterpriseWithPerm(SysRoleDto sysRoleDto, SysUserVo operator);

	/**
	 * 校验编码是否存在
	 * @param sysRole sysRole
	 */
	void checkCode(SysRole sysRole);

	/**
	 * update
	 * @param sysRole sysRole
	 * @return int
	 */
	Long update(SysRole sysRole);

	/**
	 * 分页
	 * @param sysRoleDto params
	 * @return page
	 */
	Page<SysRoleVo> findPage(SysRoleDto sysRoleDto);

	/**
	 * 查询用户的角色
	 * @param sysUserDto userId
	 * @return list
	 */
	List<SysRoleVo> findList(SysUserDto sysUserDto);

	/**
	 * 为用户赋予角色
	 * @param sysUserDto sysUserDto
	 * @param operator 操作人
	 */
	void assign(SysUserDto sysUserDto, SysUserVo operator);

	/**
	 * 批量设置用户的公司岗位权限
	 * @param sysUserDto sysUserDto
	 * @param operator operator
	 * @return 修改的userIds
	 */
	List<Long> assignBatchAdd(SysUserDto sysUserDto, SysUserVo operator);

	/**
	 * 去除用户的公司岗位权限与账簿主体关联关系
	 * @param sysUserDto sysUserDto
	 * @param operator operator
	 */
	void assignDelete(SysUserDto sysUserDto, SysUserVo operator);

	/**
	 * 根据id查详情
	 * @param sysRoleDto sysRoleDto
	 * @return SysRoleVo
	 */
	SysRoleVo findById(SysRoleDto sysRoleDto);

	/**
	 * 批量禁用/反禁用
	 * @param sysRoleDto sysRoleDto
	 * @param operator operator
	 * @return int
	 */
	int updateBatch(SysRoleDto sysRoleDto, SysUserVo operator);

	/**
	 * 分配业务管理员岗位
	 * @param sysUserDto sysUserDto
	 * @param operator operator
	 */
	void assignBusinessAdmin(SysUserDto sysUserDto, SysUserVo operator);

	/**
	 * 校验是否被引用
	 * @param roleId roleIds
	 * @return true 未被引用 /false 被引用
	 */
	boolean checkRefer(Long roleId);

	/**
	 * 用户权限列表分页
	 * @param userRoleDto
	 * @return
	 */
	Page<UserRoleVo> findUserRolePage(UserRoleDto userRoleDto);

	/**
	 * 查询可用岗位/角色列表 分页
	 * @param sysRoleDto
	 * @return
	 */
	Page<SysRoleVo> findEnableList(SysRoleDto sysRoleDto);

	/**
	 * 批量取消已分配的岗位
	 * @param sysUserDto sysUserDto
	 * @param rootEnterpriseId 租户id,无则使用sysUserDto中的rootEnterpriseId
	 * @param userIds 用户ids,无则使用sysUserDto中的userIds
	 */
	void assignBatchDelete(SysUserDto sysUserDto, Long rootEnterpriseId, List<Long> userIds);

	/**
	 * 批量删除
	 * @param sysRoleDto sysRoleDto
	 * @return BatchResult
	 */
	BatchResult updateBatchDelete(SysRoleDto sysRoleDto);

	/**
	 * 批量禁用/反禁用
	 * @param sysRoleDto sysRoleDto
	 * @return BatchResult
	 */
	BatchResult updateBatch(SysRoleDto sysRoleDto);
}
