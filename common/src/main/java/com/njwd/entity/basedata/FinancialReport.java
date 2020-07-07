package com.njwd.entity.basedata;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 财务报告
 *
 * @author zhuzs
 * @date 2019-07-01 16:09
 */
@Data
public class FinancialReport implements Serializable {
    private static final long serialVersionUID = 6703249406612707943L;

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
     * 类型 【财务报告类型】表ID
     */
    private Long reportTypeId;

    /**
     * 年度版本
     */
    private Integer year;

    /**
     * 会计准则 【会计准则】表ID
     */
    private Long accStandardId;

    /**
     * 默认 0：否、1：是
     */
    private Byte isDefault;

    /**
     * 数据状态 0：未生效、1：已生效
     */
    private Byte status;

    /**
     * 发布状态 0：未发布、1：已发布
     */
    private Byte ssuedStatus;

    /**
     * 启用标识 0：禁用、1：启用
     */
    private Byte isEnable;

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

