package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author liuxiang
 * @Description 财务报告类型
 * @Date:15:55 2019/6/25
 **/
@Data
public class FinancialReportType implements Serializable {
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
     * 审核状态 0：未审核、1：已审核
     */
    @TableField(value = "is_approved")
    private Byte isApproved;

}