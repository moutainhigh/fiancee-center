package com.njwd.financeback.service;

import com.njwd.entity.basedata.FinancialReport;
import com.njwd.entity.platform.dto.FinancialReportDto;
import com.njwd.entity.platform.vo.FinancialReportVo;

import java.util.List;

/**
 * 财务报告表
 *
 * @author zhuzs
 * @date 2019-07-02 13:29
 */
public interface FinancialReportService {
    /**
     * 资产负债表
     *
     * @return
     */
    List<FinancialReportVo> findAssetList();

    /**
     * 资产负债表
     *
     * @param platformFinancialReportDto
     * @return
     */
    List<FinancialReportVo> findAssetListByAccStandardId(FinancialReportDto platformFinancialReportDto);

    /**
     * 现金流量表
     *
     * @return
     */
    List<FinancialReportVo> findCashFlowList();

    /**
     * 现金流量表
     *
     * @param platformFinancialReportDto
     * @return
     */
    List<FinancialReportVo> findCashFlowListByAccStandardId(FinancialReportDto platformFinancialReportDto);

    /**
     * 利润表
     *
     * @return
     */
    List<FinancialReportVo> findProfitList();

    /**
     * 利润表
     *
     * @param platformFinancialReportDto
     * @return
     */
    List<FinancialReportVo> findProfitListByAccStandardId(FinancialReportDto platformFinancialReportDto);
}

