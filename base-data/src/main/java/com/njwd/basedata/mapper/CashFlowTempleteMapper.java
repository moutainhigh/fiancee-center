package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.platform.CashFlow;
import com.njwd.entity.platform.dto.CashFlowItemDto;
import org.apache.ibatis.annotations.Param;

/**
 * @Author Libao
 * @Description CashFlowItemMapper
 * @Date 2019/6/12 9:14
 */
public interface CashFlowTempleteMapper extends BaseMapper<CashFlow> {
/**
 * @Author Libao
 * @Description 跟新现金流量表模板数据
 * @Date  2019/6/25 11:49
 * @Param []
 * @return int
 */
int updateCashFlowTemplate(@Param("cashFlow") CashFlow cashFlow);

	CashFlow findCashFlowTemplate(@Param("cashFlow") CashFlow cashFlow);

	CashFlow findCashFlowItemTemplateId(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

	CashFlow findCashFlowItemTemplateIdByParam(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

}