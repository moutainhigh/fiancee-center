package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.FinancialReportItem;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author lj
 * @Description 报表项目库
 * @Date:14:16 2019/11/15
 **/
@Getter
@Setter
public class FinancialReportItemVo extends FinancialReportItem {
    /**
     * 报表类型名称
     **/
    private String reportTypeName;

    /**
     * 报表类型编码
     **/
    private String reportTypeCode;
}
