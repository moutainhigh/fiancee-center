package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.SysRoleMenu;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 角色/岗位——权限 关联表
 * @author xyyxhcj@qq.com
 * @since 2019/5/22
 */
@Data
public class SysRoleMenuVo extends SysRoleMenu {
    // 实施人员 拥有的默认权限
    List<SysRoleMenu> implementerMenus ;
    // 运营人员 拥有的默认权限
    List<SysRoleMenu> operatorMenus ;
    // 产品人员 拥有的默认权限
    List<SysRoleMenu> productorMenus ;
    // 产品人员 拥有的默认权限
    List<SysRoleMenu> sysManagerMenus ;

    /**
     * 功能菜单ID
     */
    private Long tLevelMenuId;
}