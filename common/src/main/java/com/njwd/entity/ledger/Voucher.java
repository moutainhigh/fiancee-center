package com.njwd.entity.ledger;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.njwd.utils.DateUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/7/24
 */
@Getter
@Setter
@TableName("wd_voucher_%s")
public class Voucher implements Serializable {
    /**
    * 主键 默认自动递增
    */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
    * 租户ID
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
    * 核算主体ID
    */
    private Long accountBookEntityId;

    /**
    * 核算主体
    */
    private String accountBookEntityName;

    /**
    * 制单日期
    */
    @JsonFormat(pattern = DateUtils.PATTERN_DAY,timezone = "GMT+8")
    private Date voucherDate;

    /**
    * 单据张数
    */
    private Byte billNum;

    /**
    * 记账期间年
    */
    private Integer postingPeriodYear;

    /**
    * 记账期间号
    */
    private Byte postingPeriodNum;

    /**
     * 记账期间年号
     **/
    private Integer periodYearNum;

    /**
    * 凭证字类型 1：记 、2：收、3：付、4：转
    */
    private Byte credentialWord;

    /**
    * 凭证主号
    */
    private Integer mainCode;

    /**
    * 凭证子号
    */
    private Integer childCode;

    /**
    * 来源方式 0：手工、1：协同、2：损益结转、3：冲销、4：业务系统 5：公司间协同
    */
    private Byte sourceType;

    /**
    * 来源系统 总账
    */
    private String sourceSystem;

    /**
    * 来源单号
    */
    private String sourceCode;

    /**
    * 第一行摘要
    */
    private String firstAbstract;

    /**
    * 借方金额
    */
    private BigDecimal debitAmount;

    /**
    * 贷方金额
    */
    private BigDecimal creditAmount;

    /**
    * 现金流量检查类型: -1 非现金类凭证 0 未检查 1 已检查
    */
    private Byte cashCheckType;

    /**
    * 现金流量净发生额
    */
    private BigDecimal cashFlowAmount;

    /**
    * 凭证状态 -1：草稿、0：待审核、1：已审核、2：已过账
    */
    private Byte status;

    /**
    * 内部往来类型 0不需要生成协同凭证 1未生成 2已生成
    */
    private Byte interiorType;
    /**
     * 是否已冲销 1是 0否
     **/
    private Byte isOffset;
    /**
    * 是否删除 0：否、1：是
    */
    private Byte isDel;

    /**
    * 版本号 并发版本号
    */
    private Integer version;

    /**
    * 创建时间
    */
    private Date createTime;

    /**
    * 制单人ID
    */
    private Long creatorId;

    /**
    * 制单人
    */
    private String creatorName;

    /**
    * 修改时间
    */
    private Date updateTime;

    /**
    * 修改人ID
    */
    private Long updatorId;

    /**
    * 修改人
    */
    private String updatorName;

    /**
    * 审核状态 0：未审核、1：已审核
    */
    private Byte approveStatus;

    /**
    * 审核时间
    */
    private Date approveTime;

    /**
    * 审核人ID
    */
    private Long approverId;

    /**
    * 审核人
    */
    private String approverName;

    /**
    * 复核状态 0：未复核、1：已复核
    */
    private Byte reviewStatus;

    /**
    * 复核时间
    */
    private Date reviewTime;

    /**
    * 复核出纳ID
    */
    private Long reviewerId;

    /**
    * 复核出纳
    */
    private String reviewerName;

    /**
    * 过账状态 0：未过账、1：已过账
    */
    private Byte postingStatus;

    /**
    * 过账时间
    */
    private Date postingTime;

    /**
    * 过账人ID
    */
    private Long postingUserId;

    /**
    * 过账人
    */
    private String postingUserName;

    private static final long serialVersionUID = 1L;
}
