package com.njwd.platform.service;



import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.FinancialReportDto;
import com.njwd.entity.platform.vo.FinancialReportVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author liuxiang
 * @Description 财务报告
 * @Date:9:55 2019/6/25
 **/
public interface FinancialReportService{

    /**
     * 新增财务报表
     * @Author lj
     * @Date:10:29 2019/11/18
     * @param financialReportDto
     * @return int
     **/
    Long addFinancialReport(FinancialReportDto financialReportDto);

    /**
     * 修改财务报表
     * @Author lj
     * @Date:14:29 2019/11/18
     * @param financialReportDto
     * @return int
     **/
    Long updateFinancialReport(FinancialReportDto financialReportDto);

    /**
     * 删除财务报表
     * @Author lj
     * @Date:14:57 2019/11/18
     * @param financialReportDto
     * @return com.njwd.support.BatchResult
     **/
    BatchResult delFinancialReportBatch(FinancialReportDto financialReportDto);

    /**
     * 审核财务报表
     * @Author lj
     * @Date:16:00 2019/11/18
     * @param financialReportDto
     * @return com.njwd.support.BatchResult
     **/
    BatchResult approveFinancialReportBatch(FinancialReportDto financialReportDto);

    /**
     * 反审核财务报表
     * @Author lj
     * @Date:16:00 2019/11/18
     * @param financialReportDto
     * @return com.njwd.support.BatchResult
     **/
    BatchResult disApproveFinancialReportBatch(FinancialReportDto financialReportDto);

    /**
     * 发布财务报表
     * @Author lj
     * @Date:16:02 2019/11/18
     * @param financialReportDto
     * @return com.njwd.support.BatchResult
     **/
    BatchResult releaseFinancialReportBatch(FinancialReportDto financialReportDto);

    /**
     * 根据ID查询财务报表
     * @Author lj
     * @Date:16:45 2019/11/18
     * @param financialReportDto
     * @return com.njwd.entity.platform.vo.FinancialReportVo
     **/
    FinancialReportVo findFinancialReportById(FinancialReportDto financialReportDto);

    /**
     * 查询财务报表分页
     * @Author lj
     * @Date:9:58 2019/11/18
     * @param financialReportDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.FinancialReportVo>
     **/
    Page<FinancialReportVo> findFinancialReportListPage(FinancialReportDto financialReportDto);

    /**
     * 导出
     * @param financialReportDto
     * @param response
     */
    void exportExcel(FinancialReportDto financialReportDto, HttpServletResponse response);

    /**
     * @Description 根据会计准则ID查询资产负债表
     * @Author lj
     * @Date:10:30
     * @Param [financialReportVo]
     * @return java.util.List<com.njwd.platform.entity.vo.FinancialReportVo>
     **/
    List<FinancialReportVo> findAssetListByAccStandardId(FinancialReportDto financialReportDto);

    /**
     * @Description 根据会计准则ID查询利润表
     * @Author lj
     * @Date:10:30
     * @Param [financialReportVo]
     * @return java.util.List<com.njwd.platform.entity.vo.FinancialReportVo>
     **/
    List<FinancialReportVo> findProfitListByAccStandardId(FinancialReportDto financialReportDto);

    /**
     * @Description 查询现金流量表下拉框
     * @Author lj
     * @Date:10:30
     * @Param [financialReportVo]
     * @return java.util.List<com.njwd.platform.entity.vo.FinancialReportVo>
     **/
    List<FinancialReportVo> findCashFlowListByAccStandardId(FinancialReportDto financialReportDto);

    /**
     * @Description 财务报告利润表查询
     * @Author liuxiang
     * @Date:17:38 2019/7/26
     * @Param []
     * @return java.util.List<com.njwd.platform.entity.vo.FinancialReportVo>
     **/
    List<FinancialReportVo> findProfitList();

    /**
     * @Description 财务报告资产负债表查询
     * @Author liuxiang
     * @Date:17:38 2019/7/26
     * @Param []
     * @return java.util.List<com.njwd.platform.entity.vo.FinancialReportVo>
     **/
    List<FinancialReportVo> findAssetList();

    /**
     * @Description 财务报告现金流量表查询
     * @Author liuxiang
     * @Date:17:38 2019/7/26
     * @Param []
     * @return java.util.List<com.njwd.platform.entity.vo.FinancialReportVo>
     **/
    List<FinancialReportVo> findCashFlowList();
}
