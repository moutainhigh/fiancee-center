package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author liuxiang
 * @Description 财务报告
 * @Date:15:55 2019/6/25
 **/
@Data
public class FinancialReport implements Serializable {
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
     * 类型 【财务报告类型】表ID
     */
    @TableField(value = "report_type_id")
        private Long reportTypeId;

    /**
     * 年度版本
     */
    @TableField(value = "year")
        private Integer year;

    /**
     * 会计准则 【会计准则】表ID
     */
    @TableField(value = "acc_standard_id")
        private Long accStandardId;

    /**
     * 默认 0：否、1：是
     */
    @TableField(value = "is_default")
        private Byte isDefault;

    /**
     * 数据状态 0：未生效、1：已生效
     */
    @TableField(value = "status")
        private Byte status;

    /**
     * 发布状态 0：未发布、1：已发布
     */
    @TableField(value = "is_released")
        private Byte isReleased;

    /**
     * 审核状态 0未审核 1已审核
     */
    @TableField(value = "is_approved")
    private Byte isApproved;

    /**
     * 是否删除 0：未删除、1：删除
     */
    @TableField(value = "is_del")
    private Byte isDel;

    /**
     * 启用标识 0：禁用、1：启用
     */
    @TableField(value = "is_enable")
        private Byte isEnable;

    /**
     * 描述
     */
    @TableField(value = "description")
    private String description;

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

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 管理信息:禁用人等信息
     */
    @TableField(exist = false)
    private ManagerInfo manageInfo;

}