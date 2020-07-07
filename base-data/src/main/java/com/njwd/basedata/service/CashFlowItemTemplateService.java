package com.njwd.basedata.service;

import com.njwd.entity.platform.CashFlow;
import com.njwd.entity.platform.dto.CashFlowItemDto;

/**
 * @Author Libao
 * @Description 现金流量项目Service类
 * @Date  2019/6/11 17:40
 **/
public interface CashFlowItemTemplateService {

    /**
     * @return int
     * @Author Libao
     * @Description 跟新现金流量表模板数据
     * @Date 2019/7/16 14:52
     * @Param [cashFlow]
     */
    int updateCashFlowTemplate(CashFlow cashFlow);

    int addCashFlowTemplate(CashFlow cashFlow);

    CashFlow findCashFlowTemplate(CashFlow cashFlow);

    /**
     * @return list
     * @Author Libao
     * @Description 根据基准Id 查询现金流量项目表Id
     * @Date 2019/7/2 10:32
     * @ParamcashFlowItemDto
     */
    CashFlow findCashFlowItemTemplateId(CashFlowItemDto cashFlowItemDto);

    /**
     * @return list
     * @Author Libao
     * @Description 根据账簿和准则查询现金流量项目表Id
     * @Date 2019/7/2 10:32
     * @ParamcashFlowItemDto
     */
    CashFlow findCashFlowItemTemplateIdByParam(CashFlowItemDto cashFlowItemDto);


}

