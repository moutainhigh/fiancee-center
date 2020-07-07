package com.njwd.ledger.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.njwd.entity.ledger.BalanceCashFlow;
import com.njwd.entity.ledger.dto.BalanceCashFlowDto;
import com.njwd.entity.ledger.dto.VoucherDto;
import com.njwd.entity.ledger.vo.BalanceCashFlowVo;
import com.njwd.ledger.mapper.BalanceCashFlowMapper;
import com.njwd.ledger.service.BalanceCashFlowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
* @description: 现金流量项目余额
* @author LuoY
* @date 2019/8/9 15:33
*/
@Service
@Transactional(rollbackFor = Exception.class)
public class BalanceCashFlowServiceImpl extends ServiceImpl<BalanceCashFlowMapper, BalanceCashFlow> implements BalanceCashFlowService {

    @Resource
    private BalanceCashFlowMapper balanceCashFlowMapper;

    /**
     * @param balanceCashFlowDto
     * @return java.util.List<com.njwd.entity.ledger.vo.BalanceCashFlowVo>
     * @description: 根据核算主体查询指定期间现金流量项目余额
     * @author LuoY
     * @date 2019/8/9 14:28
     */
    @Override
    public List<BalanceCashFlowVo> findBalanceCashFlowByCondition(BalanceCashFlowDto balanceCashFlowDto) {
        return balanceCashFlowMapper.findBalanceCashFlowByCondition(balanceCashFlowDto);
    }

    @Override
    public int updateBatch(Collection<BalanceCashFlowDto> balanceCashFlows, VoucherDto voucherDto, byte updateType) {
        return balanceCashFlowMapper.updateBatch(balanceCashFlows, voucherDto, updateType);
    }

}
