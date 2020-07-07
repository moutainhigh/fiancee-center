package com.njwd.ledger.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.dto.PostPeriodDto;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;

/**
 * 过账接口
 */
public interface TransferItemsService {


    /**
     * @description 过账 分页
     * @author fancl
     * @date 2019/8/16
     * @param postPeriodDto 入参dto对象
     * @return
     */
    Page<AccountBookPeriodVo> doTransferItems(PostPeriodDto postPeriodDto);

    /**
     * @description 过账相关计算和update
     * @author fancl
     * @date 2019/8/16
     * @param accountBookPeriod 账簿期间对象
     * @return
     */
    AccountBookPeriodVo calcBalanceForTransfer(AccountBookPeriod accountBookPeriod);

}
