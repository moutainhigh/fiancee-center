package com.njwd.entity.ledger;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.njwd.entity.base.BaseModel;
import com.njwd.utils.DateUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 账簿期间表
 *
 * @author zhuzs
 * @date 2019-07-02 19:20
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountBookPeriod extends BaseModel {

    /**
     * 企业ID
     */
    private Long rootEnterpriseId;

    /**
     * 账簿ID
     */
    private Long accountBookId;

    /**
     * 账簿名称
     */
    private String accountBookName;

    /**
     * 账簿编码
     */
    private String accountBookCode;

    /**
     *  账簿启用子系统记录表ID
     */
    private Long accountBookSystemId;

    /**
     * 子系统名称
     */
    private String systemName;

    /**
     * 子系统标识
     */
    private String systemSign;

    /**
     * 启用期间年度
     */
    private Integer periodYear;
    /**
     * 启用期间号
     */
    private Byte periodNum;

    /**
     * 记账期间年号
     */
    private Integer periodYearNum;

    /**
     * 期间开始日期
     **/
    @JsonFormat(pattern = DateUtils.PATTERN_DAY,timezone = "GMT+8")
    private Date startDate;

    /**
     * 期间结束日期
     **/
    @JsonFormat(pattern = DateUtils.PATTERN_DAY,timezone = "GMT+8")
    private Date endDate;

    /**
     * 状态 0:未启用;1:已启用
     */
    private Byte status;

    /**
     * 是否调整期 0：否；1：是
     */
    private Byte isRevisePeriod;

    /**
     * 凭证整理时间
     */
    @JsonFormat(pattern = DateUtils.PATTERN_SECOND,timezone = "GMT+8")
    private Date adjustTime;

    /**
     * 是否有断号报告 0 ：否、1：是，默认值 0
     */
    private Byte isMakeReport;

    /**
     * 最后整理人ID
     */
    private Long adjustUserId;

    /**
     * 最后整理人
     */
    private String adjustUserName;

    /**
     * 是否已结账 0 ：否、1：是，默认值 0
     */
    private Byte isSettle;

    /**
     * 结账时间
     */
    private Date settleTime;

    /**
     * 结账人ID
     */
    private Long settleUserId;

    /**
     * 结账人
     */
    private String settleUserName;

    /**
     * 反结账时间
     */
    private Date cancelSettleTime;

    /**
     * 反结账人ID
     */
    private Long cancelSettleUserId;

    /**
     * 反结账人
     */
    private String cancelSettleUserName;

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
     * 排除BaseModel中的版本号
     */
    @TableField(exist = false)
    private Integer version;
}

