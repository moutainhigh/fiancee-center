package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.ledger.BalanceCashFlowInit;
import com.njwd.entity.ledger.dto.BalanceCashFlowInitDto;
import com.njwd.entity.ledger.dto.BalanceInitDto;
import com.njwd.entity.ledger.vo.BalanceCashFlowInitVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/7/24
 */
public interface BalanceCashFlowInitMapper extends BaseMapper<BalanceCashFlowInit> {

    /**
     * 现金流量期初录入
     * @Author lj
     * @Date:10:10 2019/8/2
     * @param balanceCashFlowInits
     * @return int
     **/
    int addCashFlowInitBatch(@Param("balanceCashFlowInits") List<BalanceCashFlowInitDto> balanceCashFlowInits);

    List<BalanceCashFlowInitVo> findCashFlowItemIdFromInit();

    /**
     * 查询现金流量期初列表
     * @Author lj
     * @Date:9:52 2019/10/24
     * @param balanceInitDto
     * @return java.util.List<com.njwd.entity.ledger.BalanceCashFlowInit>
     **/
    List<BalanceCashFlowInit> selectBalanceCashFlowInitList(BalanceInitDto balanceInitDto);
}