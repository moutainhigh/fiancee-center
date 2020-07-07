package com.njwd.support;

import com.njwd.common.Constant;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.Voucher;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 财务检查结果
 * @Date 2019/8/12 9:13
 * @Author 朱小明
 */
@Data
public class CheckVoucherResult {

    /**
     * 账簿期间建账状态 0：未建账 1：已建账
     */
    private Byte openStatus;

    /**
     * 检查本年利润科目和利润分配科目必须为科目
     */
    private Byte isFinal;

    /**
     * 断号凭证
     */
    private List<Voucher> cutOffList;

    /**
     * 未过账凭证
     */
    private List<Voucher> notPostingList;

    /**
     * 现金流量未分析凭证
     */
    private List<Voucher> notAnalysisList;

    /**
     * 添加的损益凭证号
     */
    private List<Voucher> lossProfitList;

    /**
     * 检查结果
     */
    private Boolean checkFlag;

    /**
     * 核算主体内部往来平衡状态:-1 未启用分账核算 0:不平衡  1:平衡
     */
    private Byte notBalancedStatus;

    /**
     * 损益结转设置参数是否设置完成 0:否 1：是
     */
    private Byte parameterSetStatus;

    /**
     * 是否需要现金流量检查 0:否 1:是
     */
    private Byte needNalysisStatus;

    /**
     * 更新后账簿信息
     */
    private AccountBookPeriod accountBookPeriod;

    public CheckVoucherResult () {
        this.checkFlag = false;
        this.openStatus = Constant.Is.NO;
        this.isFinal = Constant.Is.NO;
        this.cutOffList = new ArrayList<>();
        this.notPostingList = new ArrayList<>();
        this.notAnalysisList = new ArrayList<>();
        this.lossProfitList = new ArrayList<>();
        this.notBalancedStatus = Constant.Is.NO;
        this.parameterSetStatus = Constant.Is.NO;
        this.needNalysisStatus = Constant.Is.NO;
    }


}
