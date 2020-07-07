package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/5/22
 */
@Getter
@Setter
public class SysMenu implements Serializable {
    /**
    * 权限/菜单id
    */
    @TableId(type = IdType.AUTO)
    private Long menuId;

    /**
     * 是否管理员菜单  0：否、1：是
     */
    private Byte isAdminMenu;

    /**
    * 类型: 1目录 2模块 3菜单 4权限组 5按钮
    */
    private Byte type;

    /**
     * 目录名称
     */
    private String typeName;

    /**
    * 菜单名称
    */
    private String name;

    /**
    * 排序
    */
    private Byte sort;

    /**
    * 前端权限编码
    */
    private String code;

    /**
    * 父权限id
    */
    private Long parentId;

    /**
    * 是否子权限 1是 0否
    */
    private Byte isChild;

    /**
    * 权限标识
    */
    private String permission;

    /**
    * 备注
    */
    private String remark;
    /**
     * 是否默认选中 0：否、1：是
     */
    private Byte isDefaultSelect;

    /**
    * 创建人编码
    */
    private Long creatorId;
    private String creatorName;
    /**
    * 创建时间
    */
    private Date createTime;

    /**
    * 修改人编码
    */
    private Long updatorId;
    private String updatorName;

    /**
    * 修改时间
    */
    private Date updateTime;

    /**
    * 删除标识: 0未删除 1删除
    */
    private Byte isDel;

    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof SysMenu && menuId.equals(((SysMenu) obj).getMenuId()));
    }

    @Override
    public int hashCode() {
        return menuId.hashCode();
    }
}