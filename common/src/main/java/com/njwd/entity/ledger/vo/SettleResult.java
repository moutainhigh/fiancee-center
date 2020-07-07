package com.njwd.entity.ledger.vo;

import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.Voucher;
import lombok.Data;

import java.util.Collections;
import java.util.List;
/**
 *
 * 结账返回vo
 * @author xyyxhcj@qq.com
 * @since 2019/9/20
 */
@Data
public class SettleResult {

    /**
     * 账簿期间建账状态 0：未建账 1：已建账
     */
    private Byte openStatus;

    /**
     * 本年利润科目和利润分配科目必须为末级
     */
    private Byte isFinal;

    /**
     * 断号凭证
     */
    private List<Voucher> cutOffList = Collections.emptyList();

    /**
     * 未过账凭证
     */
    private List<Voucher> notPostingList = Collections.emptyList();

    /**
     * 现金流量未分析凭证
     */
    private List<Voucher> notAnalysisList = Collections.emptyList();

    /**
     * 添加的损益凭证号
     */
    private List<Voucher> lossProfitList = Collections.emptyList();

    /**
     * 检查结果  true:未通过 false:通过
     */
    private Boolean checkFlag;

    /**
     * 核算主体内部往来平衡状态:-1 未启用分账核算 0:不平衡  1:平衡
     */
    private Byte notBalancedStatus;

    /**
     * 损益结转设置参数是否设置完成 0:否 1：是 -1:配置有误
     */
    private Byte parameterSetStatus;

    /**
     * 是否通过现金流量检查 -1:未启用现金流量 0:通过（或警告） 1:未通过
     */
    private Byte needAnalysisStatus;

    /**
     * 更新后账簿信息
     */
    private AccountBookPeriod accountBookPeriod;

    /**
     * 生成的损益结转凭证的ids
     **/
    private List<Long> lossProfitListVoucherIds;
}
