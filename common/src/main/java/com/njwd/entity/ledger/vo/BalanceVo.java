package com.njwd.entity.ledger.vo;

import com.njwd.entity.platform.vo.FinancialReportItemSetVo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 资产负债表——账簿
 *
 * @author zhuzs
 * @date 2019-08-01 17:43
 */
@Data
public class BalanceVo implements Serializable {
    private static final long serialVersionUID = 5602969904790272128L;
    /**
     * 记账本位币ID
     */
    private Long accountingCurrencyId;

    /**
     * 记账本位币
     */
    private String accountingCurrencyName;

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
     * 核算主体名称
     */
    private String accountBookEntityName;

    /**
     * 报告明细（含ID，名称）
     */
    private List<FinancialReportItemSetVo> balanceItemList;

    /**
     * 期末余额
     */
    private Map<String,BigDecimal> closingBalanceReport;

    /**
     * 年初余额
     */
    private Map<String,BigDecimal> initialBalanceReport;

    /**
     * 核算主体项目余额 List
     */
    private List<BalanceVo> entityBalanceVoList;

}
