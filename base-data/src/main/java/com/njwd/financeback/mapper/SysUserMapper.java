package com.njwd.financeback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.CommParams;
import com.njwd.entity.basedata.SysUser;
import com.njwd.entity.basedata.dto.SysUserDto;
import com.njwd.entity.basedata.vo.SysUserVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/5/22
 */
public interface SysUserMapper extends BaseMapper<SysUser> {
	/**
	 * 查询用户
	 *
	 * @param sysUserDto sysUserDto
	 * @return sysUserVo
	 */
	SysUserVo findUserVo(@Param("sysUserDto") SysUserDto sysUserDto);

	/**
	 * 分页
	 *
	 * @param sysUserDto sysUserDto
	 * @param page       page
	 * @param commParams commParams
	 * @return list
	 */
	List<SysUserVo> findPage(@Param("sysUserDto") SysUserDto sysUserDto, @Param("page") Page<SysUserVo> page, @Param("commParams") CommParams commParams);

	/**
	 * 获取当前企业可用用户列表
	 *
	 * @param sysUserDto
	 * @param page
	 * @return
	 */
	List<SysUserVo> selectEnableList(@Param("sysUserDto") SysUserDto sysUserDto, @Param("page") Page<SysUserVo> page);

	/**
	 * 查询当前租户下有权限的用户分页
	 *
	 * @param sysUserDto sysUserDto
	 * @param page       page
	 * @return list
	 */
	List<SysUserVo> findPermPage(@Param("sysUserDto") SysUserDto sysUserDto, @Param("page") Page<SysUserVo> page);

	/**
	 * 根据公司下的岗位反查用户
	 *
	 * @param sysUserDto sysUserDto
	 * @param page       page
	 * @return list
	 */
	List<SysUserVo> findRoleUserPage(@Param("sysUserDto") SysUserDto sysUserDto, @Param("page") Page<SysUserVo> page);
}