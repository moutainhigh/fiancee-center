package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.FinancialReport;
import com.njwd.entity.platform.dto.FinancialReportDto;
import com.njwd.entity.platform.vo.FinancialReportVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 财务报告
 * @Date:13:43 2019/6/25
 **/
public interface FinancialReportMapper extends BaseMapper<FinancialReport> {

    /**
     * 根据ID查询财务报表
     * @Author lj
     * @Date:16:45 2019/11/18
     * @param financialReportDto
     * @return com.njwd.entity.platform.vo.FinancialReportVo
     **/
    FinancialReportVo findFinancialReportById(FinancialReportDto financialReportDto);

    /**
     * 查询财务报表状态列表
     * @Author lj
     * @Date:15:07 2019/11/18
     * @param financialReportDto
     * @return java.util.List<com.njwd.entity.platform.vo.FinancialReportVo>
     **/
    List<FinancialReportVo> findFinancialReportListStatus(FinancialReportDto financialReportDto);

    /**
     * 查询财务报表分页
     * @Author lj
     * @Date:9:29 2019/11/18
     * @param page, financialReportDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.FinancialReportVo>
     **/
    Page<FinancialReportVo> findFinancialReportListPage(Page<FinancialReportVo> page, @Param("financialReportDto") FinancialReportDto financialReportDto);

    /**
     * @Description 查询资产负债表
     * @Author liuxiang
     * @Date:15:32 2019/7/2
     * @Param [financialReportVo]
     * @return java.util.List<com.njwd.platform.entity.vo.FinancialReportVo>
     **/
    List<FinancialReportVo> findAssetListByAccStandardId(FinancialReportDto financialReportDto);

    /**
     * @Description 查询利润表
     * @Author liuxiang
     * @Date:15:32 2019/7/2
     * @Param [financialReportVo]
     * @return java.util.List<com.njwd.platform.entity.vo.FinancialReportVo>
     **/
    List<FinancialReportVo> findProfitListByAccStandardId(FinancialReportDto financialReportDto);

    /**
     * @Description 查询现金流量表下拉框
     * @Author liuxiang
     * @Date:15:32 2019/7/2
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
     * @Description 财务报告利润表查询
     * @Author liuxiang
     * @Date:17:38 2019/7/26
     * @Param []
     * @return java.util.List<com.njwd.platform.entity.vo.FinancialReportVo>
     **/
    List<FinancialReportVo> findCashFlowList();

}