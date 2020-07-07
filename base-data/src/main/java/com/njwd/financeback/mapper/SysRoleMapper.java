package com.njwd.financeback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.CommParams;
import com.njwd.entity.basedata.SysRole;
import com.njwd.entity.basedata.dto.SysRoleDto;
import com.njwd.entity.basedata.vo.SysRoleVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/5/21
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {

	/**
	 * 查询用户的角色
	 *
	 * @param userId           userId
	 * @param rootEnterpriseId rootEnterpriseId
	 * @return list
	 */
	List<SysRoleVo> findList(@Param("userId") Long userId, @Param("rootEnterpriseId") Long rootEnterpriseId);

	/**
	 * 校验角色名和角色定义是否重复
	 *
	 * @param sysRole sysRole
	 * @return int
	 */
	int checkDuplication(@Param("sysRole") SysRole sysRole);

	/**
	 * 分页
	 *
	 * @param sysRoleDto sysRoleDto
	 * @param page       page
	 * @param commParams commParams
	 * @return list
	 */
	List<SysRoleVo> findPage(@Param("sysRoleDto") SysRoleDto sysRoleDto, @Param("page") Page<SysRoleVo> page, @Param("commParams") CommParams commParams);

	/**
	 * 查询当前企业可用岗位/角色列表
	 *
	 * @param sysRoleDto
	 * @param page
	 * @return
	 */
	List<SysRoleVo> selectEnableList(@Param("sysRoleDto") SysRoleDto sysRoleDto, @Param("page") Page<SysRoleVo> page);
}