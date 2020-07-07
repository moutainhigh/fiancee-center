package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.FinancialReportItem;
import com.njwd.entity.platform.dto.CashFlowDto;
import com.njwd.entity.platform.dto.FinancialReportItemDto;
import com.njwd.entity.platform.vo.CashFlowVo;
import com.njwd.entity.platform.vo.FinancialReportItemVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author lj
 * @Description 报表项目库
 * @Date:14:25 2019/11/15
 **/
public interface FinancialReportItemMapper extends BaseMapper<FinancialReportItem> {

    /**
     * 批量更新
     * @Author lj
     * @Date:11:06 2019/11/13
     * @param ids
     * @param type
     * @return int
     **/
    int updateCashFlowBatch(@Param("ids") List<Long> ids,@Param("type") int type);

    /**
     * 查询报表项目库状态列表
     * @Author lj
     * @Date:17:32 2019/11/15
     * @param dto
     * @return java.util.List<com.njwd.entity.platform.vo.FinancialReportItemVo>
     **/
    List<FinancialReportItemVo> findReportItemListStatus(FinancialReportItemDto dto);

    /**
     * 根据ID查询报表项目库
     * @Author lj
     * @Date:18:07 2019/11/15
     * @param dto
     * @return com.njwd.entity.platform.vo.FinancialReportItemVo
     **/
    FinancialReportItemVo findReportItemById(FinancialReportItemDto dto);

    /**
     * @Description 查询报表项目库分页
     * @Author liuxiang
     * @Date:17:43 2019/8/1
     * @Param [dto]
     * @return java.util.List<com.njwd.platform.entity.vo.FinancialReportItemSetVo>
     **/
    Page<FinancialReportItemVo> findReportItemListPage(Page<FinancialReportItemVo> page, @Param("dto")FinancialReportItemDto dto);
}