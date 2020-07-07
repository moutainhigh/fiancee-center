package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.FinancialReportType;
import com.njwd.entity.platform.dto.FinancialReportTypeDto;
import com.njwd.entity.platform.vo.FinancialReportTypeVo;
import org.apache.ibatis.annotations.Param;

/**
 * @Author lj
 * @Description //TODO 
 * @Date:10:42 2019/11/21
 **/
public interface FinancialReportTypeMapper extends BaseMapper<FinancialReportType> {

    /**
     * 查询报表类型分页
     * @Author lj
     * @Date:10:44 2019/11/21
     * @param page
     * @param financialReportTypeDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.FinancialReportTypeVo>
     **/
    Page<FinancialReportTypeVo> findFinancialReportTypePage(Page<FinancialReportTypeVo> page,@Param("financialReportTypeDto") FinancialReportTypeDto financialReportTypeDto);
}