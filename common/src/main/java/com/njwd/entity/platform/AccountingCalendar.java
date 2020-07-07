package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.*;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author lj
 * @Description 会计日历
 * @Date:14:03 2019/6/26
 **/
@Data
public class AccountingCalendar implements Serializable {
    private static final long serialVersionUID = 8438234482107881143L;
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
     * 期间类型 【期间类型】表ID
     */
    @TableField(value = "type_id")
        private Long typeId;

    /**
     * 起始会计年度
     */
    @TableField(value = "start_year")
        private String startYear;

    /**
     * 开始日期
     */
    @TableField(value = "start_date")
        private String startDate;

    /**
     * 会计准则 【会计准则】表ID
     */
    @TableField(value = "acc_standard_id")
        private Long accStandardId;

    /**
     * 启用标识 0：禁用、1：启用
     */
    @TableField(value = "is_enable")
        private Byte isEnable;

    /**
     * 删除标识 0：未删除、1： 删除
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
    /**
     * 调整期个数
     */
    @TableField(value = "adjust_num")
    private Integer adjustNum;
    /**
     * 发布状态 0：未发布、1：已发布
     */
    @TableField(value = "is_released")
    private Byte isReleased;
    /**
     * 审核状态 0：未审核、1：已审核
     */
    @TableField(value = "is_approved")
    private Byte isApproved;
    /**
     * 版本号 并发版本号
     */
    @Version
    private Integer version;
    /**
     * 管理信息:禁用人等信息
     */
    @TableField(exist = false)
    private ManagerInfo manageInfo;
}