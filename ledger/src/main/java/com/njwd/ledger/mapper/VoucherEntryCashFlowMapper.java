package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.ledger.VoucherEntryCashFlow;
import com.njwd.entity.ledger.dto.VoucherEntryCashFlowDto;
import com.njwd.entity.ledger.vo.VoucherEntryCashFlowVo;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/7/24
 */
public interface VoucherEntryCashFlowMapper extends BaseMapper<VoucherEntryCashFlow> {
    /**
     * 批量插入
     *
     * @param editCashFlowList editCashFlowList
     * @param voucherId        voucherId
     * @return int
     * @author xyyxhcj@qq.com
     * @date 2019/8/5 16:13
     **/
    int insertBatch(@Param("editCashFlowList") List<VoucherEntryCashFlowDto> editCashFlowList, @Param("voucherId") Long voucherId);

    /**
     * 查询现金流量明细
     *
     * @param voucherIds voucherIds
     * @return java.util.LinkedList<com.njwd.entity.ledger.dto.VoucherEntryCashFlowDto>
     * @author xyyxhcj@qq.com
     * @date 2019/8/20 9:49
     **/
    LinkedList<VoucherEntryCashFlowDto> findList(@Param("voucherIds") Collection<Long> voucherIds);

    /**
     * @Author Libao
     * @Description 查询现金流量项目Id
     * @Date  2019/9/5 15:05
     * @Param []
     * @return java.util.List<com.njwd.entity.ledger.vo.VoucherEntryCashFlowVo>
     */
    List<VoucherEntryCashFlowVo> findCashFlowItemIdFromVoucher();
}