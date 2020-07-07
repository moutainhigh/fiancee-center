package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author liuxiang
 * @Description 会计准则
 * @Date:15:52 2019/6/25
 **/
@Data
public class AccountingStandard implements Serializable {
    private static final long serialVersionUID = -3778517507775829562L;
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
     * 地区
     */
    private String area;

    /**
     * 默认记账本位币 【币种】表ID
     */
    private Long currencyId;

    /**
     * 税制 【税收制度】表ID
     */
    private Long taxSystemId;

    /**
     * 是否删除 0：未删除、1：删除
     */
    private Byte isDel;

    private Byte isApproved;

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
     * 更新者ID
     */
    private Long updatorId;

    /**
     * 更新者
     */
    private String updatorName;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private ManagerInfo manageInfo;
}
