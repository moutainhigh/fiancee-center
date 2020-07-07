package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.FinancialReportItemFormula;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @Author liuxiang
 * @Description 财务报告明细公式
 * @Date:17:05 2019/8/1
 **/
@Getter
@Setter
public class FinancialReportItemFormulaVo extends FinancialReportItemFormula {
    private static final long serialVersionUID = 9044433156912170958L;
    /**
     * 主键 默认自动递增
     */
    private Long id;

    /**
     * 明细项目ID 【财务报告明细表】表ID
     */
    private Long itemSetId;

    /**
     * 公式类型 0：科目或项目、1：项目行
     */
    private Byte formulaType;

    /**
     * 公式项目编码 科目、现金流量项目、报告项目
     */
    private String formulaItemCode;

    /**
     * 运算标识 0：加、1：减
     */
    private Byte operator;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 科目或项目名称
     */
    private String formulaItemName;

    /**
     * 科目方向
     */
    private Byte formulaItemDirection;
}