package com.njwd.entity.basedata;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 会计日历
 *
 * @Author zhuzs
 * @Description 会计日历
 * @Date: 2019/7/2
 **/
@Data
public class AccountingCalendar implements Serializable {
    private static final long serialVersionUID = -4468982107922291462L;
    /**
     * 主键 默认自动递增
     */
    private Long id;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 期间类型 【期间类型】表ID
     */
    private Long typeId;

    /**
     * 起始会计年度
     */
    private String startYear;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 会计准则 【会计准则】表ID
     */
    private Long accStandardId;

    /**
     * 启用标识 0：禁用、1：启用
     */
    private Byte isEnable;

    /**
     * 删除标识 0：未删除、1： 删除
     */
    private Byte isDel;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 创建者
     */
    private String creatorName;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 更新者ID
     */
    private Long updatorId;

    /**
     * 更新者
     */
    private String updatorName;

}