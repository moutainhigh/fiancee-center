package com.njwd.platform.api;

import com.njwd.entity.platform.dto.FinancialReportDto;
import com.njwd.entity.platform.vo.FinancialReportVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 财务报告
 * @Date:17:11 2019/7/12
 **/
@RequestMapping("platform/financialReport")
public interface FinancialReportApi {

    /**
     * @Description 根据会计准则ID查询利润表
     * @Author liuxiang
     * @Date:17:12 2019/7/12
     * @Param [platformFinancialReportDto]
     * @return java.lang.String
     **/
    @PostMapping("findProfitListByAccStandardId")
    Result<List<FinancialReportVo>> findProfitListByAccStandardId(FinancialReportDto platformFinancialReportDto);

    /**
     * @Description 根据会计准则ID查询资产负债表
     * @Author liuxiang
     * @Date:17:12 2019/7/12
     * @Param [platformFinancialReportDto]
     * @return java.lang.String
     **/
    @PostMapping("findAssetListByAccStandardId")
    Result<List<FinancialReportVo>> findAssetListByAccStandardId(FinancialReportDto platformFinancialReportDto);

    /**
     * @Description 查询现金流量表下拉框
     * @Author liuxiang
     * @Date:17:12 2019/7/12
     * @Param [platformFinancialReportDto]
     * @return java.lang.String
     **/
    @PostMapping("findCashFlowListByAccStandardId")
    Result<List<FinancialReportVo>>  findCashFlowListByAccStandardId(FinancialReportDto platformFinancialReportDto);

    /**
     * @Description 查询利润表
     * @Author liuxiang
     * @Date:15:12 2019/7/2
     * @Param [financialReportVo]
     * @return java.lang.String
     **/
    @PostMapping("findProfitList")
    Result<List<FinancialReportVo>>  findProfitList();

    /**
     * @Description 查询资产负债表
     * @Author liuxiang
     * @Date:15:13 2019/7/2
     * @Param [financialReportVo]
     * @return java.lang.String
     **/
    @PostMapping("findAssetList")
    Result<List<FinancialReportVo>> findAssetList();

    /**
     * @Description 查询现金流量表下拉框
     * @Author liuxiang
     * @Date:15:13 2019/7/2
     * @Param [financialReportDto]
     * @return java.lang.String
     **/
    @PostMapping("findCashFlowList")
    Result<List<FinancialReportVo>> findCashFlowList();

    /**
     * @Description 查询财务报告列表
     * @Author liuxiang
     * @Date:17:50 2019/7/26
     * @Param []
     * @return java.lang.String
     **/
    @PostMapping("findReportList")
    Result<List<FinancialReportVo>> findReportList();

    /**
     * @Description 查询财务报告利润表
     * @Author liuxiang
     * @Date:17:48 2019/7/26
     * @Param []
     * @return java.lang.String
     **/
    @PostMapping("findPrfitList")
    Result<List<FinancialReportVo>> findPrfitList();


    /**
     * @Description 查询财务报告资产负债表
     * @Author liuxiang
     * @Date:17:48 2019/7/26
     * @Param []
     * @return java.lang.String
     **/
    @PostMapping("findAsetList")
    Result<List<FinancialReportVo>> findAsetList();

    /**
     * @Description 查询财务报告现金流量表
     * @Author liuxiang
     * @Date:17:48 2019/7/26
     * @Param []
     * @return java.lang.String
     **/
    @PostMapping("findCashFlwList")
    Result<List<FinancialReportVo>> findCashFlwList();
}
