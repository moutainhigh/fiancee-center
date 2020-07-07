package com.njwd.entity.ledger.vo;

import com.njwd.entity.ledger.BalanceSubject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @Author 周鹏
 * @Description 财务报表-科目余额表
 * @Date:14:03 2019/8/2
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BalanceSubjectVo extends BalanceSubject {
    /**
     * 总账账簿名称
     */
    private String accountBookName;

    /**
     * 核算主体名称
     */
    private String accountBookEntityName;

    /**
     * 科目表id
     */
    private Long subjectId;

    /**
     * 科目编码
     */
    private String code;

    /**
     * 科目名称
     */
    private String name;

    /**
     * 方向  0：借方、1：贷方
     */
    private Byte balanceDirection;

    /**
     * 期初余额方向
     */
    private String openingDirectionName;

    /**
     * 期末余额方向
     */
    private String closingDirectionName;

    /**
     * 科目状态  0：禁用、1：启用
     */
    private Byte isEnable;

    /**
     * 是否末级  0：否、1：是
     */
    private Byte isFinal;

    /**
     * 已过账合计
     */
    private BigDecimal postAmount;

    /**
     * 辅助核算表名
     */
    private String sourceTable;

    /**
     * 辅助核算id拼接
     */
    private String itemValueIds;

    /**
     * 辅助核算编码
     */
    private String auxiliaryCode;

    /**
     * 辅助核算名称
     */
    private String auxiliaryName;

    /**
     * 开始期间号
     */
    private Byte beginNumber;

    /**
     * 结束期间号
     */
    private Byte endNumber;

    /**
     * 开始期间
     */
    private Integer beginPeriod;

    /**
     * 结束期间
     */
    private Integer endPeriod;

    /**
     * 结束期间前最近的已结账期间
     */
    private Integer endSettledPeriod;

    /**
     * 开始期间后最近的已结账期间
     */
    private AccountBookPeriodVo beginSettledPeriodVo;

    /**
     * 结束期间前最近的已结账期间
     */
    private AccountBookPeriodVo endSettledPeriodVo;
    /**
     * 明细项目ID
     */
    private Long itemSetId;

    /**
     * 运算标识
     */
    private Byte operator;

    /**
     * 是否为列表中的第一级 0：否、1：是
     */
    private Byte isFirst;

    /**
     * 本期借方发生额累计
     */
    private BigDecimal debitAmountTotal;

    /**
     * 本期贷方发生额累计
     */
    private BigDecimal creditAmountTotal;

    /**
     * 已过账本期借方发生额累计
     */
    private BigDecimal postDebitAmountTotal;

    /**
     * 已过账本期贷方发生额累计
     */
    private BigDecimal postCreditAmountTotal;

    /**
     * 结束期间前最近的已结账期间的期初和期末
     */
    private BalanceSubjectVo endSubjectVo;

    /**
     * 账簿启用期间
     */
    private Integer startPeriod;

    /**
     * 账簿启用期间金额信息
     */
    private BalanceSubjectVo startPeriodBalanceVo;

    /**
     * 开始期间后最近的已结账期间年
     */
    private Integer beginPeriodYear;

    /**
     * 开始期间后最近的已结账期间号
     */
    private Byte beginPeriodNumber;

    /**
     * 结束期间前最近的已结账期间年
     */
    private Integer endPeriodYear;

    /**
     * 结束期间前最近的已结账期间号
     */
    private Byte endPeriodNumber;
}
