package com.njwd.entity.ledger.vo;

import com.njwd.annotation.ExcelCell;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.ledger.BalanceSubjectInit;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Author lj
 * @Description 期初余额-科目期初
 * @Date:10:01 2019/7/29
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class BalanceSubjectInitVo extends BalanceSubjectInit {

    /**
     * 核算账簿
     */
    private String accountBookName;

    /**
     * 是否分账核算
     */
    private Byte hasSubAccount;

    /**
     * 科目表ID
     */
    private Long subjectId;

    /**
     * 科目表
     */
    private String subjectName;

    /**
     * 科目编码
     */
    @ExcelCell(index = 0)
    private String accountSubjectCode;

    /**
     * 核算主体NAME
     */
    private String accountBookEntityName;

    /**
     * 核算主体CODE
     */
    private String accountBookEntityCode;

    /**
     * 核算账簿下的核算主体
     **/
    private List<AccountBookEntityVo> entityList;

    /**
     * 辅助核算列表
     **/
    private List<LinkedHashMap> auxList;

    /**
     * 辅助核算列表头部信息
     **/
    private List<BalanceSubjectInitExtVo> titleInfo;

    /**
     * 资产期初余额之和
     **/
    private BigDecimal assetsOpeningBalanceSum;

    /**
     * 资产本年借方
     **/
    private BigDecimal assetsThisYearDebitAmountSum;

    /**
     * 资产本年贷方
     **/
    private BigDecimal assetsThisYearCreditAmountSum;

    /**
     * 资产方向
     **/
    private String assetsDirection;

    /**
     * 成本期初余额之和
     **/
    private BigDecimal costOpeningBalanceSum;

    /**
     * 成本本年借方
     **/
    private BigDecimal costThisYearDebitAmountSum;

    /**
     * 成本本年贷方
     **/
    private BigDecimal costThisYearCreditAmountSum;

    /**
     * 成本方向
     **/
    private String costDirection;

    /**
     * 共同期初余额之和
     **/
    private BigDecimal commonOpeningBalanceSum;

    /**
     * 共同本年借方
     **/
    private BigDecimal commonThisYearDebitAmountSum;

    /**
     * 共同本年贷方
     **/
    private BigDecimal commonThisYearCreditAmountSum;

    /**
     * 共同方向
     **/
    private String commonDirection;

    /**
     * 借方期初余额合计
     **/
    private BigDecimal debitOpeningBalanceTotal;

    /**
     * 借方本年借方合计
     **/
    private BigDecimal debitThisYearDebitAmountTotal;

    /**
     * 借方本年贷方合计
     **/
    private BigDecimal debitThisYearCreditAmountTotal;

    /**
     * 借方合计方向
     **/
    private String debitTotalDirection;

    /**
     * 负债期初余额之和
     **/
    private BigDecimal debtOpeningBalanceSum;

    /**
     * 负债本年借方
     **/
    private BigDecimal debtThisYearDebitAmountSum;

    /**
     * 负债本年贷方
     **/
    private BigDecimal debtThisYearCreditAmountSum;

    /**
     * 负债方向
     **/
    private String debtDirection;

    /**
     * 权益期初余额之和
     **/
    private BigDecimal rightOpeningBalanceSum;

    /**
     * 权益本年借方
     **/
    private BigDecimal rightThisYearDebitAmountSum;

    /**
     * 权益本年贷方
     **/
    private BigDecimal rightThisYearCreditAmountSum;

    /**
     * 权益方向
     **/
    private String rightDirection;

    /**
     * 损益期初余额之和
     **/
    private BigDecimal profitOpeningBalanceSum;

    /**
     * 损益本年借方
     **/
    private BigDecimal profitThisYearDebitAmountSum;

    /**
     * 损益本年贷方
     **/
    private BigDecimal profitThisYearCreditAmountSum;

    /**
     * 损益方向
     **/
    private String profitDirection;

    /**
     * 贷方期初余额合计
     **/
    private BigDecimal creditOpeningBalanceTotal;

    /**
     * 贷方本年借方合计
     **/
    private BigDecimal creditThisYearDebitAmountTotal;

    /**
     * 贷方本年贷方合计
     **/
    private BigDecimal creditThisYearCreditAmountTotal;

    /**
     * 贷方合计方向
     **/
    private String creditTotalDirection;

    /**
     * 借贷余额合计差额
     **/
    private BigDecimal differenceOpeningBalance;

    /**
     * 借贷发生额合计差额
     **/
    private BigDecimal differenceAmount;
}
