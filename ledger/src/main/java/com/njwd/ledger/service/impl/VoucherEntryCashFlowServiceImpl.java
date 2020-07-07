package com.njwd.ledger.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.VoucherEntryCashFlow;
import com.njwd.entity.ledger.dto.VoucherEntryCashFlowDto;
import com.njwd.entity.ledger.vo.PostPeriodBalanceVo;
import com.njwd.ledger.mapper.BalanceCashFlowMapper;
import com.njwd.ledger.mapper.VoucherEntryCashFlowMapper;
import com.njwd.ledger.service.VoucherEntryCashFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/08/09
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class VoucherEntryCashFlowServiceImpl extends ServiceImpl<VoucherEntryCashFlowMapper, VoucherEntryCashFlow> implements VoucherEntryCashFlowService {

    @Autowired
    BalanceCashFlowMapper balanceCashFlowMapper;
    @Resource
    private VoucherEntryCashFlowMapper voucherEntryCashFlowMapper;


    /**
     * 过账时 修改现金流量过账金额
     * @param cashFlowBalanceList
     * @return
     */
    @Override
    public int updateCashFlowBalanceForPostPeriod(List<PostPeriodBalanceVo> cashFlowBalanceList) {
        return balanceCashFlowMapper.updateCashFlowBalanceForPostPeriod(cashFlowBalanceList);

    }

    @Override
    public int insertBatch(List<VoucherEntryCashFlowDto> entryCashFlowList, Long voucherId) {
        return voucherEntryCashFlowMapper.insertBatch(entryCashFlowList, voucherId);
    }

    @Override
    public List<VoucherEntryCashFlowDto> findList(Collection<Long> voucherIds) {
        return voucherEntryCashFlowMapper.findList(voucherIds);
    }

    @Override
    public List<PostPeriodBalanceVo> findCashFlowBalanceBeforeUpdateForPostPeriod(AccountBookPeriod accountBookPeriod) {
        return balanceCashFlowMapper.findCashFlowBalanceBeforeUpdateForPostPeriod(accountBookPeriod);
    }
}
