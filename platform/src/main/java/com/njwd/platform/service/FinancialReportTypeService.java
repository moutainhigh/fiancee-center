package com.njwd.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.FinancialReportTypeDto;
import com.njwd.entity.platform.vo.FinancialReportTypeVo;

/**
 * @Author liuxiang
 * @Description 财务报告类型
 * @Date:10:34 2019/6/25
 **/
public interface FinancialReportTypeService{

    /**
     * 查询报表类型分页
     * @Author lj
     * @Date:10:29 2019/11/21
     * @param financialReportTypeDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.FinancialReportTypeVo>
     **/
    Page<FinancialReportTypeVo> findFinancialReportTypePage(FinancialReportTypeDto financialReportTypeDto);
}
