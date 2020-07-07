package com.njwd.financeback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.SysUserRole;
import com.njwd.entity.basedata.dto.CompanyDto;
import com.njwd.entity.basedata.dto.SysUserDto;
import com.njwd.entity.basedata.dto.UserRoleDto;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.basedata.vo.UserRoleVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/5/22
 */
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
	/**
	 * 批量插入用户的公司角色
	 *
	 * @param companyId  公司id
	 * @param roleIds    角色ids
	 * @param sysUserDto sysUserDto
	 * @param operator   操作人
	 * @return int
	 */
	int insertBatch(@Param("companyId") Long companyId, @Param("roleIds") Long[] roleIds, @Param("sysUserDto") SysUserDto sysUserDto, @Param("operator") SysUserVo operator);

	/**
	 * 批量设置用户的公司岗位权限
	 *
	 * @param userDto userDto
	 * @param companyIds companyIds
	 * @param operator   operator
	 */
	void assignBatchAdd(@Param("userDto") SysUserDto userDto, @Param("companyIds") Long[] companyIds, @Param("operator") SysUserVo operator);

	/**
	 * 查询有无用户权限关联当前公司
	 *
	 * @param companyDto
	 * @return
	 */
	List<UserRoleVo> selectUserRoleList(CompanyDto companyDto);

	/**
	 * 用户权限列表
	 *
	 * @param userRoleDto
	 * @param page
	 * @return
	 */
	List<UserRoleVo> selectUserRolePage(@Param("userRoleDto") UserRoleDto userRoleDto, @Param("page") Page<UserRoleVo> page);
}