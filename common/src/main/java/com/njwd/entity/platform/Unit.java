package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@ToString
public class Unit {
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
    * 舍入方式 0：四舍五入、1：舍去、2：进位
    */
    private Byte roundingType;

    /**
    * 精度
    */
    @TableField("`precision`")
    private Integer precision;

    /**
    * 换算单位id
    */
    private Long conversionId;

    /**
    * 是否基准单位 0 否 1是
    */
    private Byte isBase;

    /**
    * 换算值
    */
    private BigDecimal conversionValue;

    /**
    * 审核状态
    */
    private Byte isApproved;

    /**
    * 发布状态
    */
    private Byte isReleased;

    /**
    * 启用标识 0：禁用、1：启用
    */
    private Byte isEnable;

    /**
    * 是否删除 0：未删除、1：删除
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

    /**
    * 扩展信息
    */
    private Object manageInfo;

    /**
     * 租户ID
     */
    private Long rootEnterpriseId;

    /**
     * 平台ID
     */
    private Long platformId;


}
