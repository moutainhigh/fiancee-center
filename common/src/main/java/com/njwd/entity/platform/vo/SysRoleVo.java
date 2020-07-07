package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.SysRole;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 角色/岗位
 *
 * @author: zhuzs
 * @date: 2019-11-12
 */
@Data
public class SysRoleVo extends SysRole {
    private static final long serialVersionUID = -7484058898942714956L;

    /**
     * 实施、运营、产品、管理员 拥有的角色权限列表
     */
    List<SysMenuVo> sysMenuVoList;
}