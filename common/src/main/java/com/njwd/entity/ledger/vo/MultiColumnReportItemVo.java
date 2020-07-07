package com.njwd.entity.ledger.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.njwd.utils.DateUtils;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @description: 多栏明细账
 * @author: xdy
 * @create: 2019/8/31 11:30
 */
@Getter
@Setter
public class MultiColumnReportItemVo {

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
    @JsonFormat(pattern = DateUtils.PATTERN_DAY, timezone = "GMT+8")
    private Date voucherDate;

    /**
     * 凭证字类型 1：记 、2：收、3：付、4：转
     */
    private Byte credentialWord;

    /**
     * 凭证主号
     */
    private Integer mainCode;

    /**
     * 凭证字号
     */
    private String credentialWordCode;

    /**
     * 摘要
     **/
    private  String abstractContent;

    /**
     * 合计借方
     **/
    private BigDecimal totalDebitAmount;

    /**
     * 合计贷方
     **/
    private  BigDecimal totalCreditAmount;

    /**
     * 方向 0：借方、1：贷方
     **/
    private Byte balanceDirection;

    /**
     * 方向名称
     */
    private String balanceDirectionName;

    /**
     * 合计余额
     **/
    private BigDecimal totalBalance;

    /**
     * 右侧多栏账
     */
    private List<BigDecimal> multiColumnAmount;

    /**
     * 是否合计
     */
    private Byte isTotal;

}
