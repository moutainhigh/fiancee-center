package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class AccountBookType implements Serializable {
    private static final long serialVersionUID = 6429549732350905795L;
    /**
     * 主键 默认自动递增
     */
     @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 编码
     */
    @TableField(value = "code")
    private String code;

    /**
     * 名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 用途
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 启用标识 0：禁用、1：启用
     */
    @TableField(value = "is_enable")
    private Byte isEnable;

    /**
     * 是否删除 0：未删除、1：删除
     */
    @TableField(value = "is_del")
    private Byte isDel;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 创建者ID
     */
    @TableField(value = "creator_id")
    private Long creatorId;

    /**
     * 创建者
     */
    @TableField(value = "creator_name")
    private String creatorName;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 更新者ID
     */
    @TableField(value = "updator_id")
    private Long updatorId;

    /**
     * 更新者
     */
    @TableField(value = "updator_name")
    private String updatorName;

}