package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.FinancialReportItemFormula;
import com.njwd.entity.platform.FinancialReportItemSet;
import com.njwd.entity.platform.dto.FinancialReportItemSetDto;
import com.njwd.entity.platform.vo.FinancialReportItemSetVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 财务报告项目明细设置列表
 * @Date:17:44 2019/8/1
 **/
public interface FinancialReportItemSetMapper extends BaseMapper<FinancialReportItemSet> {

    /**
     * 删除
     * @Author lj
     * @Date:18:12 2019/11/19
     * @param itemSetId
     * @return int
     **/
    int deleteItemFormulaByItemId(Long itemSetId);

    /**
     * 批量插入公式
     * @Author lj
     * @Date:17:05 2019/11/19
     * @param financialReportItemFormulaList
     * @return int
     **/
    int insertItemFormulaBatch(@Param("financialReportItemFormulaList")List<FinancialReportItemFormula> financialReportItemFormulaList);

    /**
     * 根据报告项目ID查询财务报告项目明细
     * @Author lj
     * @Date:10:50 2019/11/19
     * @param dto
     * @return com.njwd.entity.platform.vo.FinancialReportItemSetVo
     **/
    FinancialReportItemSetVo findReportItemSetById(FinancialReportItemSetDto dto);

    /**
     * 根据报告ID查询财务报告项目明细列表分页
     * @Author lj
     * @Date:14:47 2019/11/19
     * @param page
     * @param dto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.FinancialReportItemSetVo>
     **/
    Page<FinancialReportItemSetVo> findReportItemSetListPage(Page<FinancialReportItemSetVo> page, @Param("dto")FinancialReportItemSetDto dto);

    /**
     * 根据报告ID查询财务报告项目明细
     * @Author lj
     * @Date:9:31 2019/11/19
     * @param dto
     * @return java.util.List<com.njwd.entity.platform.vo.FinancialReportItemSetVo>
     **/
    List<FinancialReportItemSetVo> findReportItemSetList(FinancialReportItemSetDto dto);

    /**
     * @Description 根据报告库ID查询财务报告项目明细设置列表
     * @Author liuxiang
     * @Date:17:43 2019/8/1
     * @Param [dto]
     * @return java.util.List<com.njwd.platform.entity.vo.FinancialReportItemSetVo>
     **/
    List<FinancialReportItemSetVo> findFinancialReportItemSetList(FinancialReportItemSetDto dto);
}