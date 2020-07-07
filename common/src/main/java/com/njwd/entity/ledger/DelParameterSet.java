package com.njwd.entity.ledger;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author liuxiang
 * @Description 总账参数设置
 * @Date:9:27 2019/7/25
 **/
@Getter
@Setter
public class DelParameterSet implements Serializable {
    private static final long serialVersionUID = 500116988030771078L;
    /**
     * 主键 默认自动递增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    @TableField(value = "root_enterprise_id")
    private Long rootEnterpriseId;

    /**
     * 本年利润科目ID
     */
    @TableField(value = "lr_acc_subject_id")
    private Long lrAccSubjectId;

    /**
     * 利润分配科目ID
     */
    @TableField(value = "fp_acc_subject_id")
    private Long fpAccSubjectId;

    /**
     * 损益调整科目ID
     */
    @TableField(value = "sy_acc_subject_id")
    private Long syAccSubjectId;

    /**
     * 凭证分单方式 0：合并、1：分开
     */
    @TableField(value = "credential_type")
    private Byte credentialType;

    /**
     * 凭证字类型 0：记 、1：收付转
     */
    @TableField(value = "credential_word_type")
    private Byte credentialWordType;

    /**
     * 未来期间数 值范围：1、2
     */
    @TableField(value = "future_period_num")
    private Byte futurePeriodNum;

    /**
     * 制单审核同一人  0：否、1：是
     */
    @TableField(value = "is_add_approve_same")
    private Byte isAddApproveSame;

    /**
     * 审核反审核不是同一人 0：否、1：是
     */
    @TableField(value = "is_approve_not_same")
    private Byte isApproveNotSame;

    /**
     * 现金银行类凭证是否出纳复核 0：否、1：是
     */
    @TableField(value = "is_cashier_review")
    private Byte isCashierReview;

    /**
     * 出纳复核是否在审核之前 0：否、1：是
     */
    @TableField(value = "is_review_before_approve")
    private Byte isReviewBeforeApprove;

    /**
     * 出纳复核取消复核不是同一人 0：否、1：是
     */
    @TableField(value = "is_review_not_same")
    private Byte isReviewNotSame;

    /**
     * 允许修改他人凭证 0：否、1：是
     */
    @TableField(value = "is_can_update_other")
    private Byte isCanUpdateOther;

    /**
     * 允许反结账 0：否、1：是
     */
    @TableField(value = "is_open_accounts")
    private Byte isOpenAccounts;


    /**
     * 结账时检查现金流量 0：否、1：是
     */
    @TableField(value = "is_check_cash_flow")
    private Byte isCheckCashFlow;

    /**
     * 凭证保存须指定现金流量项目 0：否、1：是
     */
    @TableField(value = "is_must_set_cash_flow")
    private Byte isMustSetCashFlow;

    /**
     * 结账后允许修改现金流量 0：否、1：是
     */
    @TableField(value = "is_update_cash")
    private Byte isUpdateCash;

    /**
     * 打印模板 FP：发票、A41：A4一版、A42：A4二版、A43：A4三版、A5：A5
     */
    @TableField(value = "print_model")
    private String printModel;

    /**
     * 打印科目格式 0：编码本级、1：编码全级
     */
    @TableField(value = "print_subject_type")
    private Byte printSubjectType;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 创建人ID
     */
    @TableField(value = "creator_id")
    private Long creatorId;

    /**
     * 创建人
     */
    @TableField(value = "creator_name")
    private String creatorName;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 修改人ID
     */
    @TableField(value = "updator_id")
    private Long updatorId;

    /**
     * 修改人
     */
    @TableField(value = "updator_name")
    private String updatorName;

}
