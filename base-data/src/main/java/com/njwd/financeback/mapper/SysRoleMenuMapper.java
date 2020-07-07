package com.njwd.financeback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.basedata.SysRoleMenu;
import com.njwd.entity.basedata.dto.SysRoleDto;
import com.njwd.entity.basedata.vo.SysUserVo;
import org.apache.ibatis.annotations.Param;
/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/5/22
 */
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {
	/**
	 * 批量插入角色的权限
	 * @param sysRoleDto sysRoleDto
	 * @param operator operator
	 * @return int
	 */
	int insertBatch(@Param("sysRoleDto") SysRoleDto sysRoleDto, @Param("operator") SysUserVo operator);
}