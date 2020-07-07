package com.njwd.entity.ledger.vo;

import com.njwd.entity.ledger.MultiColumnSchemeItem;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @Author liuxiang
 * @Description 多栏账方案明细
 * @Date:11:54 2019/7/29
 **/
@Getter
@Setter
public class MultiColumnSchemeItemVo extends MultiColumnSchemeItem {
    private static final long serialVersionUID = -5497128154588053040L;

    /**
     * 核算主体ID
     **/
    private Long accountBookEntityId;

    /**
     * 会计科目ID
     **/
    private Long accountSubjectId;

    /**
     * 核算主体名称
     **/
    private String accountBookEntityName;

    /**
     * 凭证ID
     **/
    private Long voucherId;

    /**
     * 制单日期
     **/
    private Date voucherDate;

    /**
     * 凭证字号
     **/
    private String voucherWordNum;

    /**
     * 摘要
     **/
    private  String abstractContent;

    /**
     * 期初余额
     **/
    private BigDecimal openingBalance;

    /**
     * 借方
     **/
    private BigDecimal debitAmount;

    /**
     * 贷方
     **/
    private  BigDecimal creditAmount;

    /**
     * 合计借方
     **/
    private  BigDecimal totalDebitAmount;

    /**
     * 合计贷方
     **/
    private  BigDecimal totalCreditAmount;

    /**
     * 方向 0：借方、1：贷方
     **/
    private Byte balanceDirection;

    /**
     * 合计余额
     **/
    private BigDecimal totalBalance;

    /**
     * 明细项方向
     **/
    private String schemeDirection;

    /**
     * 明细项发生额
     **/
    private  BigDecimal amount;

    /**
     * 期初余额期间年
     **/
    private Integer periodYear;

    /**
     * 期初余额期间号
     **/
    private Byte periodNum;

    /**
     * 明细账期间年
     **/
    private Integer postingPeriodYear;

    /**
     * 明细账期间号
     **/
    private Byte postingPeriodNum;

    /**
     * 明细发生额
     **/
    private List<MultiColumnSchemeItemVo> itemAccountList;
}