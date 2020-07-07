package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.njwd.entity.base.ManagerInfo;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author liuxiang
 * @Description 辅助核算
 * @Date:15:54 2019/6/25
 **/
@Data
public class AuxiliaryItem implements Serializable {
    private static final long serialVersionUID = 5067847164631593802L;
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
     * 来源模块类型 0：基础资料、1：辅助资料
     */
    @TableField(value = "source_model_type")
        private Byte sourceModelType;

    /**
     * 来源模块名称
     */
    @TableField(value = "source_model")
        private String sourceModel;

    /**
     * 值来源名称
     */
    @TableField(value = "source_name")
        private String sourceName;

    /**
     * 值来源表
     */
    @TableField(value = "source_table")
        private String sourceTable;


    /**
     * 启用标识 0：禁用、1：启用
     */
    @TableField(value = "is_enable")
        private Byte isEnable;


    /**
     * 是否删除 0：未删除、1：删除
     */
    @TableField(value = "is_del")
    @TableLogic
        private Byte isDel;

    /**
     * 创建者ID
     */
    @TableField(value = "creator_id")
        private Long creatorId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
        private Date createTime;

    /**
     * 创建者
     */
    @TableField(value = "creator_name")
        private String creatorName;

    /**
     * 更新者ID
     */
    @TableField(value = "updator_id")
        private Long updatorId;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
        private Date updateTime;

    /**
     * 更新者
     */
    @TableField(value = "updator_name")
        private String updatorName;


    /**
     * 审核状态 0：未审核、1：已审核
     */
    private Byte isApproved;

    /**
     * 发布状态 0：未发布、1：已发布
     */
    private Byte isReleased;

    /**
     * 操作信息:禁用人,禁用时间等
     */
    private ManagerInfo manageInfo;

    /**
     * 来源主键
     */
    private Long sourceId;
}