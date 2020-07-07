package com.njwd.entity.basedata;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/5/22
 */
@Data
public class SysRoleMenu implements Serializable {
    /**
    * 岗位/角色id
    */
    private Long roleId;

    /**
    * 菜单id
    */
    private Long menuId;

    /**
     * 是否半选: 1半选 0全选
     */
    private Byte isHalf;

    /**
    * 创建人编码
    */
    private Long creatorId;
    private Date createTime;

    private static final long serialVersionUID = 1L;
}