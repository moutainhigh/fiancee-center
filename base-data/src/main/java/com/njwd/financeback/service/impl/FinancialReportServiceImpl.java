package com.njwd.financeback.service.impl;

import com.njwd.basedata.cloudclient.FinancialReportFeignClient;
import com.njwd.entity.basedata.FinancialReport;
import com.njwd.entity.platform.dto.FinancialReportDto;
import com.njwd.entity.platform.vo.FinancialReportVo;
import com.njwd.financeback.service.FinancialReportService;
import com.njwd.support.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 财务报告表
 *
 * @author zhuzs
 * @date 2019-07-02 13:30
 */
@Service
public class FinancialReportServiceImpl implements FinancialReportService {
    @Autowired
    private FinancialReportFeignClient financialReportFeignClient;

    // 资产负债表
    @Override
    public List<FinancialReportVo> findAssetList(){
        Result<List<FinancialReportVo>>  result = financialReportFeignClient.findAssetList();
        return result.getData();
    }

    // 资产负债表
    @Override
    public List<FinancialReportVo> findAssetListByAccStandardId(FinancialReportDto platformFinancialReportDto){
        Result<List<FinancialReportVo>> result = financialReportFeignClient.findAssetListByAccStandardId(platformFinancialReportDto);
        return result.getData();
    }

    // 现金流量表
    @Override
    public List<FinancialReportVo> findCashFlowList(){
        Result<List<FinancialReportVo>> result = financialReportFeignClient.findCashFlowList();
        return result.getData();
    }

    // 现金流量表
    @Override
    public List<FinancialReportVo> findCashFlowListByAccStandardId(FinancialReportDto platformFinancialReportDto){
        Result<List<FinancialReportVo>>  result = financialReportFeignClient.findCashFlowListByAccStandardId(platformFinancialReportDto);
        return result.getData();
    }

    // 利润表
    @Override
    public List<FinancialReportVo> findProfitList(){
        Result<List<FinancialReportVo>> result = financialReportFeignClient.findProfitList();
        return result.getData();
    }

    // 利润表
    @Override
    public List<FinancialReportVo> findProfitListByAccStandardId(FinancialReportDto platformFinancialReportDto){
        Result<List<FinancialReportVo>> result = financialReportFeignClient.findProfitListByAccStandardId(platformFinancialReportDto);
        return result.getData();
    }
}

