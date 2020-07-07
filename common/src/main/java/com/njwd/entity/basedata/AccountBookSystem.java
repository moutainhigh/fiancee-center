package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.TableField;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 账簿启用子系统记录
 *
 * @Author: Zhuzs
 * @Date: 2019-06-21 11:12
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountBookSystem extends BaseModel {
    private static final long serialVersionUID = 6454475022758310452L;

    /**
     * 企业 ID
     */
    @NotNull
    private Long rootEnterpriseId;
    /**
     * 核算账簿ID
     */
    @NotNull
    private Long accountBookId;
    /**
     * 核算账簿名称
     */
    @NotNull
    private String accountBookName;
    /**
     * 子系统名称
     */
    @NotNull
    private String systemName;
    /**
     * 子系统标识 总账：ledger ，资产：asset，应收：receive
     */
    @NotNull
    private String systemSign;
    /**
     * 状态 0:未启用;1:已启用
     */
    @NotNull
    private Byte status;
    /**
     * 初始化状态：0: 未初始化；1:已初始化
     */
    private Byte isInitalized;
    /**
     * 启用期间年度
     */
    @NotNull
    private Integer periodYear;
    /**
     * 启用期间号
     */
    @NotNull
    private Byte periodNum;
    /**
     * 启用人ID
     */
    @NotNull
    private Long operatorId;
    /**
     * 启用人
     */
    @NotNull
    private String operatorName;
    /**
     * 启用时间
     */
    @NotNull
    private Date operateTime;

    /**
     * 现金流量启用标识 0:否；1:是
     */
    private Byte cashFlowEnableStatus;

    /**
     * 删除标识 0：未删除、1：删除
     */
    @TableField(exist = false)
    private Byte isDel;

    /**
     * 创建时间
     */
    @TableField(exist = false)
    private Date createTime;

    /**
     * 创建者ID
     */
    @TableField(exist = false)
    private Long creatorId;

    /**
     * 创建者
     */
    @TableField(exist = false)
    private String creatorName;

    /**
     * 更新时间
     */
    @TableField(exist = false)
    private Date updateTime;

    /**
     * 更新者ID
     */
    @TableField(exist = false)
    private Long updatorId;

    /**
     * 更新者
     */
    @TableField(exist = false)
    private String updatorName;

    /**
     * 操作信息:禁用人,禁用时间等
     */
    @TableField(exist = false)
    private ManagerInfo manageInfo;
}
