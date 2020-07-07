package com.njwd.ledger.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.njwd.entity.ledger.BalanceCashFlow;
import com.njwd.entity.ledger.dto.BalanceCashFlowDto;
import com.njwd.entity.ledger.dto.VoucherDto;
import com.njwd.entity.ledger.vo.BalanceCashFlowVo;

import java.util.Collection;
import java.util.List;

/**
* @description:  现金流量项目余额
* @author LuoY
* @date 2019/8/9 14:25
*/
public interface BalanceCashFlowService extends IService<BalanceCashFlow> {

    /**
    * @description: 根据核算主体查询指定期间现金流量项目余额
    * @param balanceCashFlowDto
    * @return java.util.List<com.njwd.entity.ledger.vo.BalanceCashFlowVo>
    * @author LuoY
    * @date 2019/8/9 14:28
    */
    List<BalanceCashFlowVo> findBalanceCashFlowByCondition(BalanceCashFlowDto balanceCashFlowDto);

    /**
     * 更新发生额
     *
     * @param balanceCashFlows balanceCashFlows
     * @param voucherDto       voucherDto
     * @param updateType
     * @return int
     * @author xyyxhcj@qq.com
     * @date 2019/8/13 20:03
     **/
    int updateBatch(Collection<BalanceCashFlowDto> balanceCashFlows, VoucherDto voucherDto, byte updateType);
}
