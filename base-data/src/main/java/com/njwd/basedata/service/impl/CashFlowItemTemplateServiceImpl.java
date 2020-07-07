package com.njwd.basedata.service.impl;

import com.njwd.basedata.mapper.CashFlowTempleteMapper;
import com.njwd.basedata.service.CashFlowItemTemplateService;
import com.njwd.entity.platform.CashFlow;
import com.njwd.entity.platform.dto.CashFlowItemDto;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author Libao
 * @Description 现金流量项目Service层实现类
 * @Date  2019/6/11 17:40
 **/
@Service
public class CashFlowItemTemplateServiceImpl implements CashFlowItemTemplateService {
    @Resource
    private CashFlowTempleteMapper cashFlowTempleteMapper;


    @Override
    public int addCashFlowTemplate(CashFlow cashFlow) {
        return cashFlowTempleteMapper.insert(cashFlow);
    }

    /**
     * @param cashFlow
     * @return int
     * @Author Libao
     * @Description 跟新现金流量表模板数据
     * @Date 2019/7/16 14:52
     * @Param [cashFlow]
     */
    @Override
    public int updateCashFlowTemplate(CashFlow cashFlow) {
        return cashFlowTempleteMapper.updateById(cashFlow);
    }

    @Override
    public CashFlow findCashFlowTemplate(CashFlow cashFlow) {
        return cashFlowTempleteMapper.findCashFlowTemplate(cashFlow);
    }


    /**
     * @param cashFlowItemDto
     * @return list
     * @Author Libao
     * @Description 根据租户Id 和 现金流量项目表Id查询现金流量项目
     * @Date 2019/7/2 10:32
     * @ParamcashFlowItemDto
     */
    @Override
    public CashFlow findCashFlowItemTemplateId(CashFlowItemDto cashFlowItemDto) {
        return cashFlowTempleteMapper.findCashFlowItemTemplateId(cashFlowItemDto);
    }


    /**
     * @return list
     * @Author Libao
     * @Description 根据账簿和准则查询现金流量项目表Id
     * @Date 2019/7/2 10:32
     * @ParamcashFlowItemDto
     */
    @Override
    public CashFlow findCashFlowItemTemplateIdByParam(CashFlowItemDto cashFlowItemDto) {
        return cashFlowTempleteMapper.findCashFlowItemTemplateIdByParam(cashFlowItemDto);
    }

}

