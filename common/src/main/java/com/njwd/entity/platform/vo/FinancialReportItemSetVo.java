package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.FinancialReportItemSet;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author liuxiang
 * @Description 财务报告项目明细设置
 * @Date:14:19 2019/7/18
 **/
@Getter
@Setter
public class FinancialReportItemSetVo extends FinancialReportItemSet {
    private static final long serialVersionUID = 8896545967146347438L;

    /**
     * 主键 默认自动递增
     **/
    private Long id;

    /**
     * 财务报告项目明细设置编码
     **/
    private String code;

    /**
     * 财务报告项目明细设置名称
     **/
    private String name;

    /**
     * 财务报告名称
     **/
    private String reportName;

    /**
     * 报表类型名称
     **/
    private String reportTypeName;

    /**
     * 报表类型ID
     **/
    private Long reportTypeId;

    /**
     * 报表项目编码
     **/
    private String reportItemCode;

    /**
     * 报表项目名称
     **/
    private String reportItemName;

    /**
     * 财务报告编码
     **/
    private String reportCode;

    /**
     * 级次 0：标题、1：一级、2：二级、3：三级、4：小计、5：合计、6：总计
     **/
    private Byte level;

    /**
     * 项目属性 1、资产负债、2：利润表、3：现金流量表
     **/
    private Byte itemType;

    /**
     * 增减标识 0：减、1：加
     **/
    private Byte isAdd;

    /**
     * 包含标识 0：否、1：是
     **/
    private Byte isContain;

    /**
     * 流动标识 0：否、1：是
     **/
    private Byte isFlow;

    /**
     * @Description 财务报告项目明细公式列表
     **/
    private List<FinancialReportItemFormulaVo> financialReportItemFormulaVoList;

    /**
     * @Description 财务报告重分类公式列表
     **/
    private List<FinancialReportRearrangeFormulaVo> financialReportRearrangeFormulaVoList;

    /**
     * 期末余额
     */
    private BigDecimal closingBalance;

    /**
     * 年初余额
     */
    private BigDecimal initialBalance;

    /**
     * 期末余额合计
     */
    private BigDecimal totalClosingBalance;

    /**
     * 年初余额合计
     */
    private BigDecimal totalInitialBalance;
}