package com.njwd.ledger.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.VoucherEntryCashFlow;
import com.njwd.entity.ledger.dto.VoucherEntryCashFlowDto;
import com.njwd.entity.ledger.vo.PostPeriodBalanceVo;

import java.util.Collection;
import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019-08-09
 */

public interface VoucherEntryCashFlowService extends IService<VoucherEntryCashFlow> {

    /**
     * 过账时 修改现金流量过账金额
     *
     * @param cashFlowBalanceList
     * @return
     */
    int updateCashFlowBalanceForPostPeriod(List<PostPeriodBalanceVo> cashFlowBalanceList);

    /**
     * 批量插入现金流量分析
     *
     * @param entryCashFlowList entryCashFlowList
     * @param voucherId         voucherId
     * @return int
     * @author xyyxhcj@qq.com
     * @date 2019/8/16 10:11
     **/
    int insertBatch(List<VoucherEntryCashFlowDto> entryCashFlowList, Long voucherId);
    /**
     * 查询现金流量明细
     *
     * @param voucherIds voucherIds
     * @return java.util.List<com.njwd.entity.ledger.dto.VoucherEntryCashFlowDto>
     * @author xyyxhcj@qq.com
     * @date 2019/8/20 9:48
     **/
    List<VoucherEntryCashFlowDto> findList(Collection<Long> voucherIds);

    /**
     * @description 过账 查询现金流量金额
     * @author fancl
     * @date 2019/8/22
     * @param 
     * @return 
     */
    List<PostPeriodBalanceVo> findCashFlowBalanceBeforeUpdateForPostPeriod(AccountBookPeriod accountBookPeriod);
}
