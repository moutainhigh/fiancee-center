package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.ledger.BalanceSubjectInitAuxiliary;
import com.njwd.entity.ledger.BalanceSubjectInitAuxiliaryItem;
import com.njwd.entity.ledger.dto.BalanceInitDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/7/24
 */
public interface BalanceSubjectInitAuxiliaryItemMapper extends BaseMapper<BalanceSubjectInitAuxiliaryItem> {

    /**
     * 插入辅助核算数据
     * @Author lj
     * @Date:14:14 2019/8/29
     * @param balSubInitAuxItemList
     * @return void
     **/
    int addBalanceSubInitAuxItemBatch(@Param("balSubInitAuxItemList") List<BalanceSubjectInitAuxiliaryItem> balSubInitAuxItemList);
    /**
     * 查询辅助核算列表
     * @Author lj
     * @Date:9:31 2019/10/24
     * @param balanceInitDto
     * @return java.util.List<com.njwd.entity.ledger.BalanceSubjectInitAuxiliaryItem>
     **/
    List<BalanceSubjectInitAuxiliaryItem> selectBalSubInitAuxItemList(BalanceInitDto balanceInitDto);
}