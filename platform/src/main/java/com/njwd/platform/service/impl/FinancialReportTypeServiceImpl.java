package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.FinancialReportTypeDto;
import com.njwd.entity.platform.vo.FinancialReportTypeVo;
import com.njwd.platform.mapper.FinancialReportTypeMapper;
import com.njwd.platform.service.FinancialReportTypeService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

/**
 * @Author lj
 * @Description 财务报告类型
 * @Date:10:43 2019/11/21
 **/
@Service
public class FinancialReportTypeServiceImpl implements FinancialReportTypeService {

    @Resource
    private FinancialReportTypeMapper financialReportTypeMapper;


    /**
     * 查询报表类型分页
     *
     * @param financialReportTypeDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.FinancialReportTypeVo>
     * @Author lj
     * @Date:10:29 2019/11/21
     **/
    @Override
    public Page<FinancialReportTypeVo> findFinancialReportTypePage(FinancialReportTypeDto financialReportTypeDto) {
        Page<FinancialReportTypeVo> page =financialReportTypeDto.getPage();
        page = financialReportTypeMapper.findFinancialReportTypePage(page,financialReportTypeDto);
        return page;
    }
}
