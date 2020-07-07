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
 * @Description 会计期间
 * @Date:11:02 2019/6/28
 **/
@Data
public class AccountingPeriod implements Serializable {
    private static final long serialVersionUID = -7935251649527412244L;
    /**
     * 主键 默认自动递增
     */
     @TableId(value = "id", type = IdType.AUTO)
        private Long id;

    /**
     * 会计年度
     */
    @TableField(value = "period_year")
        private Integer periodYear;

    /**
     * 期间号
     */
    @TableField(value = "period_num")
        private Byte periodNum;

    /**
     * 开始日期
     */
    @TableField(value = "start_date")
        private Date startDate;

    /**
     * 结束日期
     */
    @TableField(value = "end_date")
        private Date endDate;

    /**
     * 是否调整期 0：否、1：是
     */
    @TableField(value = "is_adjustment")
        private Byte isAdjustment;

    /**
     * 会计日历 【会计日历】表ID
     */
    @TableField(value = "acc_calendar_id")
        private Long accCalendarId;

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

}