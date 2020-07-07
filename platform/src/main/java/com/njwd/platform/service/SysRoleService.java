package com.njwd.platform.service;

import com.njwd.entity.platform.vo.SysRoleVo;

import java.util.List;

/**
 * 岗位/角色
 *
 * @author zhuzs
 * @date 2019-11-13 15:27
 */
public interface SysRoleService {

    /**
     * 获取 实施、运营、产品、管理员 拥有的角色权限列表
     * @return
     */
    List<SysRoleVo> findRoleMenuList();
}
