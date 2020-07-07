package com.njwd.entity.basedata.vo;

import com.njwd.entity.basedata.AccountBook;
import com.njwd.entity.basedata.AccountCashFlow;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import com.njwd.entity.platform.vo.FinancialReportVo;
import com.njwd.entity.platform.vo.SysSystemVo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * 核算账簿
 *
 * @Author: Zhuzs
 * @Date: 2019-05-16 17:18
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AccountBookVo extends AccountBook {
    private static final long serialVersionUID = -3292071320132183401L;
    /**
     * 公司编码
     */
    private String companyCode;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 会计准则ID
     */
    private Long accountingStandardId;

    /**
     * 会计准则
     */
    private String accountingStandardName;

    /**
     * 记账本位币ID
     */
    private Long accountingCurrencyId;

    /**
     * 记账本位币
     */
    private String accountingCurrencyName;

    /**
     * 是否分账核算
     */
    private Byte hasSubAccount;

    /**
     * 账簿启用子系统记录 列表
     */
    List<SysSystemVo> sysSystemVos;

    /**
     * 账簿启用子系统记录 MAP
     */
    Map<String,SysSystemVo> accountBookSystemMap;

    /**
     * 现金流量项目表
     */
    AccountCashFlow accountCashFlow;

    /**
     * 资产负债
     */
    List<FinancialReportVo> assetList;

    /**
     * 现金流量表
     */
    List<FinancialReportVo> cashFlowList;

    /**
     * 资产负债
     */
    List<FinancialReportVo> profitList;

    /**
     * 核算主体ID(主键)
     */
    private Long entityId;

    /**
     * 核算主体
     */
    private String entityName;

    /**
     * 核算主体编码
     */
    private String entityCode;

    /**
     * 是否是默认核算主体 0：否，1：是
     */
    private Byte isDefault;

    /**
     * 默认 核算主体列表
     */
    AccountBookEntityVo defaultAccountBookEntityVo;

    /**
     * 总帐启用状态 0:未启用;1:已启用
     */
    Byte ledgerStatus;

    /**
     * 已打开账簿期间
     */
    List<AccountBookPeriodVo> accountBookPeriodVoList;

    /**
     * 最近已结账期间
     */
    private AccountBookPeriodVo lastPostingPeriod;

    /**
     * 现金流量启用标识 0:否；1:是
     */
    private Byte cashFlowEnableStatus;

}
