package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 会计期间类型
 * </p>
 *
 * @author lzt
 * @since 2019-11-20 09:51
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("wd_accounting_period_type")
public class AccountingPeriodType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 默认自动递增
     */
    @TableId(value = "id", type = IdType.AUTO)
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
     * 类型代号
     */
    private String typeCode;

    /**
     * 年度期间数
     */
    private Integer periodNo;

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
    private LocalDateTime createTime;

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
    private LocalDateTime updateTime;

    /**
     * 更新者ID
     */
    private Long updatorId;

    /**
     * 更新者
     */
    private String updatorName;


}
