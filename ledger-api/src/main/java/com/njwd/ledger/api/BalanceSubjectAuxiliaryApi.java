package com.njwd.ledger.api;

import com.njwd.entity.ledger.BalanceSubjectAuxiliaryItem;
import com.njwd.entity.ledger.dto.BalanceSubjectAuxiliaryItemQueryDto;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
* @description: 科目期初辅助核算项目
* @author LuoY
* @date 2019/8/23 17:19
*/
@RequestMapping("ledger/balanceSubjectAuxiliary")
public interface BalanceSubjectAuxiliaryApi {

    /**
    * @description: 根据账簿id和值来源表和值id查询
    * @Param [queryDto]
    * @return com.njwd.support.Result<com.njwd.entity.ledger.BalanceSubjectAuxiliaryItem>
    * @author LuoY
    * @date 2019/8/23 17:22
    */
    @PostMapping("findByAccountBookIdAndItemValueId")
    Result<BalanceSubjectAuxiliaryItem> findByAccountBookIdAndItemValueId(@RequestBody BalanceSubjectAuxiliaryItemQueryDto queryDto);
}
