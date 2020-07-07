package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.platform.SysRole;
import com.njwd.entity.platform.vo.SysRoleVo;

import java.util.List;

/**
 * 岗位/角色
 *
 * @author zhuzs
 * @date 2019-11-13 15:15
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {
    List<SysRoleVo> findRoleMenuList();
}
