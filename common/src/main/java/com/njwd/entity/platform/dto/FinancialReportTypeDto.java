package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.FinancialReportTypeVo;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author lj
 * @Description 财务报告类型
 * @Date:10:23 2019/11/21
 **/
@Getter
@Setter
public class FinancialReportTypeDto extends FinancialReportTypeVo {
    private Page<FinancialReportTypeVo> page = new Page<>();

    /**
     *编码或名称
     **/
    private String codeOrName;
}
