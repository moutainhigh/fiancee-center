package com.njwd.entity.ledger.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.njwd.entity.ledger.VoucherEntry;
import com.njwd.utils.DateUtils;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/07/24
 */
@Getter
@Setter
public class VoucherEntryVo extends VoucherEntry {
    private static final long serialVersionUID = -8191660983372551153L;
    /**
     * 制单日期
     */
    @JsonFormat(pattern = DateUtils.PATTERN_DAY,timezone = "GMT+8")
    private Date voucherDate;
    /**
     * 凭证字号
     */
    private String credentialWordCode;

    /**
     * 凭证字
     */
    private Byte credentialWord;

    /**
     * 凭证字号
     */
    private Integer mainCode;

    /**
     * 期间
     */
    private int periodYearNum;

    /**
     * 本位币金额
     */
    private BigDecimal currencyAmount;

    /**
     * 现金流量编码
     */
    private String code;

    /**
     * 现金流量名称
     */
    private String name;
    /**
     * 科目编码
     */
    private String subjectCode;

    /**
     * 科目名称
     */
    private String subjectName;

    /**
     * 科目名称
     */
    private String subjectFullName;

    /**
     * 会计期间
     */
    private String postingPeriod;

    /**
     * 现金流量项目Id
     */
    private Long cashFlowItemId;

    /**
     * 账簿Id
     */
    private Long accountBookId;


    /**
     * 账簿名称
     */
    private String accountBookName;

    /**
     * 核算主体Id
     */
    private Long accountBookEntityId;


    /**
     * 核算主体名称
     */
    private String accountBookEntityName;

    /**
     * 过账状态
     */
    private byte postingStatus;

    /**
     * 现金流量项目Id
     */
    private Long cashFlowId;

    /**
     * 0：流出、1：流入
     */
    private Byte cashFlowDirection;

    /**
     * 对应辅助核算 信息
     */
    private List<VoucherEntryAuxiliaryVo> voucherEntryAuxiliaryVos = new ArrayList<>();

    /**
     * 对应辅助核算名称
     */
    private String auxiliaryNames;

    /**
     * 对应现金流量 信息
     */
    private List<VoucherEntryCashFlowVo> voucherEntryCashFlowVos = new ArrayList<>();

    /**
     * 关联的协同凭证分录id
     **/
    private Long interiorEntryId;

    /**
     * 关联的协同凭证分录
     **/
    private VoucherEntryVo interiorEntry;

    /**
     * 分类code
     */
    private String accountCategory;

    /**
     * 对方 凭证ID
     */
    private Long oppositeVoucherId;
    /**
     * 制单人
     */
    private String creatorName;
    /**
     * 审核人
     */
    private String approverName;
    /**
     * 复核出纳
     */
    private String reviewerName;
    /**
     * 过账人
     */
    private String postingUserName;
    /**
     * 来源方式 0：手工、1：协同、2：损益结转、3：冲销、4：业务系统
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
     * 账簿编码
     */
    private String accountBookCode;
    /**
     * 来源 凭证字类型 1：记 、2：收、3：付、4：转
     */
    private Byte oppositeCredentialWord;

    /**
     * 来源 凭证主号
     */
    private Integer oppositeMainCode;
    /**
     * 版本号 并发版本号
     */
    private Integer version;
    /**
     * 记账期间年
     */
    private Integer periodYear;

    /**
     * 记账期间号
     */
    private Byte periodNum;
    /**
     * 凭证状态 -1：草稿、0：待审核、1：已审核、2：已过账
     */
    private Byte status;
    /**
     * 是否已冲销 1是 0否
     **/
    private Byte isOffset;
    /**
     * 复核状态 0：未复核、1：已复核
     */
    private Byte reviewStatus;
    /**
     * 现金流量检查类型: -1 非现金类凭证 0 未检查 1 已检查
     */
    private Byte cashCheckType;
    /**
     * 用于导出存放会计科目
     */
    private String accountingSubjects;
    /**
     * 用于导出存放辅助核算
     */
    private String auxiliaryAccounting;



}
