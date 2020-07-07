package com.njwd.entity.basedata.vo;

import com.njwd.entity.basedata.UserRole;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: Zhuzs
 * @Date: 2019-05-29 10:04
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserRoleVo extends UserRole {
    private static final long serialVersionUID = -4713544735664591653L;
    /**
     * 子系统名称
     */
    private String typeName;

    /**
     * 菜单ID
     * type=3
     */
    private Long menuId;

    /**
     * 菜单名称
     */
    private String menuName;
}
