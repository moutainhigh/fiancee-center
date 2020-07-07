package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.TableField;
import com.njwd.entity.base.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
/**
 * @Author zhuzs
 * @Description 会计期间
 * @Date: 2019/7/2
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountingPeriod extends BaseModel {
    private static final long serialVersionUID = -7935251649527412244L;


    /**
     * 会计年度
     */
    private Integer periodYear;

    /**
     * 期间号
     */
    private Integer periodNum;

    /**
     * 开始日期
     */
    private Date startDate;

    /**
     * 结束日期
     */
    private Date endDate;

    /**
     * 是否调整期 0：否、1：是
     */
    private Byte isAdjustment;

    /**
     * 会计日历 【会计日历】表ID
     */
    private Long accCalendarId;

    /**
     * 启用标识 0：禁用、1：启用
     */
    private Byte isEnable;

    /**
     * 排除BaseModel中的版本号
     */
    @TableField(exist = false)
    private Integer version;
}