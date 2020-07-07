package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.FinancialReportTypeDto;
import com.njwd.entity.platform.vo.FinancialReportTypeVo;
import com.njwd.platform.service.FinancialReportTypeService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author lj
 * @Description 财务报告类型
 * @Date:10:39 2019/11/21
 **/
@RestController
@RequestMapping("financialReportType")
public class FinancialReportTypeController extends BaseController {

    @Autowired
    private FinancialReportTypeService financialReportTypeService;

    /**
     * 查询报表类型分页
     * @Author lj
     * @Date:16:48 2019/11/18
     * @param financialReportTypeDto
     * @return com.njwd.support.Result<com.njwd.entity.platform.vo.FinancialReportVo>
     **/
    @PostMapping("findFinancialReportTypePage")
    public Result<Page<FinancialReportTypeVo>> findFinancialReportTypePage(@RequestBody FinancialReportTypeDto financialReportTypeDto){
        return ok(financialReportTypeService.findFinancialReportTypePage(financialReportTypeDto));
    }
}
