package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @Author liuxiang
 * @Description 菜单
 * @Date:15:56 2019/6/25
 **/
@Data
public class SysMenu extends BaseEntity {
    private static final long serialVersionUID = 2268088838845274220L;
    /**
     * 主键 默认自动递增
     */
    @TableId(value = "menu_id", type = IdType.AUTO)
    private Long menuId;

    /**
     * 类型: 1 一级菜单 2二级菜单 3三级菜单 4 通用权限 5 业务操作
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
     * 是否子权限 0：否、1：是
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
     * 是否菜单默认勾选 0：否、1：是
     */
    private Byte isDefaultSelect;

    /**
     * 删除标识 0：未删除、1：删除
     */
    private Byte isDel;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 创建人
     */
    private String creatorName;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 修改人ID
     */
    private Long updatorId;

    /**
     * 修改人
     */
    private String updatorName;

}