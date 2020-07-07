package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.FinancialReport;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author liuxiang
 * @Description 财务报告
 * @Date:9:52 2019/6/25
 **/
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FinancialReportVo extends FinancialReport {
    private static final long serialVersionUID = 2083036074574412802L;

    /**
     * 财务报告类型编码
     **/
    private String reportTypeCode;

    /**
     * 会计准则的流水号编码
     **/
    private String accStandardCode;

    /**
     * 财务报告类型名称
     **/
    private String reportTypeName;

    /**
     * 报告项目库项目属性
     **/
    private String reportItemType;

    /**
     * 报告项目库项名称
     **/
    private String reportItemName;

    /**
     * 报告项目库项编码
     **/
    private String reportItemCode;

    /**
     * 会计准则名称
     **/
    private String accStandardName;

}