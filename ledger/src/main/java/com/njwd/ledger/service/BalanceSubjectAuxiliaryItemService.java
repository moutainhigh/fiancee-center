package com.njwd.ledger.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.njwd.entity.ledger.BalanceSubject;
import com.njwd.entity.ledger.BalanceSubjectAuxiliary;
import com.njwd.entity.ledger.BalanceSubjectAuxiliaryItem;
import com.njwd.entity.ledger.dto.BalanceSubjectAuxiliaryItemQueryDto;
import com.njwd.entity.ledger.vo.BalanceSubjectAuxiliaryItemVo;
import com.njwd.entity.ledger.vo.BalanceSubjectAuxiliaryVo;

import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019-08-13
 */

public interface BalanceSubjectAuxiliaryItemService extends IService<BalanceSubjectAuxiliaryItem> {

    /**
    * @description: 根据核算账簿id,项目id和来源表查询科目期初辅助核算明细
    * @Param [balanceSubjectAuxiliaryItemQueryDto]
    * @return com.njwd.entity.ledger.BalanceSubjectAuxiliaryItem
    * @author LuoY
    * @date 2019/8/23 16:25
    */
    BalanceSubjectAuxiliaryItem findByAccountBookIdAndItemId(BalanceSubjectAuxiliaryItemQueryDto balanceSubjectAuxiliaryItemQueryDto);
}
