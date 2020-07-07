package com.njwd.platform.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.FinancialReportItemDto;
import com.njwd.entity.platform.dto.FinancialReportItemSetDto;
import com.njwd.entity.platform.vo.FinancialReportItemSetVo;
import com.njwd.entity.platform.vo.FinancialReportItemVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author liuxiang
 * @Description 财务报告项目明细设置
 * @Date:9:55 2019/6/25
 **/
public interface FinancialReportItemSetService {

    /**
     * 新增报表项目库
     * @Author lj
     * @Date:16:23 2019/11/15
     * @param financialReportItemDto
     * @return int
     **/
    Long addFinancialReportItem(FinancialReportItemDto financialReportItemDto);

    /**
     * 删除报表项目库
     * @Author lj
     * @Date:17:26 2019/11/15
     * @param financialReportItemDto
     * @return com.njwd.support.BatchResult
     **/
    BatchResult delFinancialReportItemBatch(FinancialReportItemDto financialReportItemDto);

    /**
     * 修改报表项目库
     * @Author lj
     * @Date:16:56 2019/11/15
     * @param financialReportItemDto
     * @return int
     **/
    Long updateFinancialReportItem(FinancialReportItemDto financialReportItemDto);

    /**
     * 审核报表项目库
     * @Author lj
     * @Date:17:48 2019/11/15
     * @param financialReportItemDto
     * @return com.njwd.support.BatchResult
     **/
    BatchResult approveFinancialReportItemBatch(FinancialReportItemDto financialReportItemDto);

    /**
     * 反审核报表项目库
     * @Author lj
     * @Date:17:50 2019/11/15
     * @param financialReportItemDto
     * @return com.njwd.support.BatchResult
     **/
    BatchResult disApproveFinancialReportItemBatch(FinancialReportItemDto financialReportItemDto);

    /**
     * 根据ID查询报表项目库
     * @Author lj
     * @Date:18:10 2019/11/15
     * @param dto
     * @return com.njwd.entity.platform.vo.FinancialReportItemVo
     **/
    FinancialReportItemVo findReportItemById(FinancialReportItemDto dto);

    /**
     * 查询报表项目库分页
     * @Author lj
     * @Date:15:18 2019/11/15
     * @param financialReportItemDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.FinancialReportItemVo>
     **/
    Page<FinancialReportItemVo> findReportItemListPage(FinancialReportItemDto financialReportItemDto);

    /**
     * 导出
     * @param financialReportItemDto
     * @param response
     */
    void exportExcel(FinancialReportItemDto financialReportItemDto, HttpServletResponse response);

    /**
     * 添加财务报告项目明细
     * @Author lj
     * @Date:15:20 2019/11/19
     * @param financialReportItemSetDto
     * @return int
     **/
    Long addFinancialReportItemSet(FinancialReportItemSetDto financialReportItemSetDto);

    /**
     * 删除财务报告项目明细
     * @Author lj
     * @Date:9:41 2019/11/20
     * @param financialReportItemSetDto
     * @return int
     **/
    BatchResult delFinancialReportItemSetBatch(FinancialReportItemSetDto financialReportItemSetDto);

    /**
     * 修改财务报告项目明细
     * @Author lj
     * @Date:17:58 2019/11/19
     * @param financialReportItemSetDto
     * @return int
     **/
    Long updateFinancialReportItemSet(FinancialReportItemSetDto financialReportItemSetDto);

    /**
     * 清空表达式
     * @Author lj
     * @Date:17:58 2019/11/19
     * @param financialReportItemSetDto
     * @return int
     **/
    int clear(FinancialReportItemSetDto financialReportItemSetDto);

    /**
     * 根据报告ID查询财务报告项目明细
     * @Author lj
     * @Date:9:30 2019/11/19
     * @param financialReportItemSetDto
     * @return java.util.List<com.njwd.entity.platform.vo.FinancialReportItemSetVo>
     **/
    List<FinancialReportItemSetVo> findReportItemSetList(FinancialReportItemSetDto financialReportItemSetDto);

    /**
     * 根据报告ID查询财务报告项目明细列表分页
     * @Author lj
     * @Date:14:41 2019/11/19
     * @param financialReportItemSetDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.FinancialReportItemSetVo>
     **/
    Page<FinancialReportItemSetVo> findReportItemSetListPage(FinancialReportItemSetDto financialReportItemSetDto);

    /**
     * 根据报告项目ID查询财务报告项目明细
     * @Author lj
     * @Date:10:49 2019/11/19
     * @param financialReportItemSetDto
     * @return com.njwd.entity.platform.vo.FinancialReportItemSetVo
     **/
    FinancialReportItemSetVo findReportItemSetById(FinancialReportItemSetDto financialReportItemSetDto);

    /**
     * @Description 根据报告库ID查询财务报告项目明细设置列表
     * @Author liuxiang
     * @Date:17:43 2019/8/1
     * @Param [dto]
     * @return java.util.List<com.njwd.platform.entity.vo.FinancialReportItemSetVo>
     **/
    List<FinancialReportItemSetVo> findFinancialReportItemSetList(FinancialReportItemSetDto dto);

}
