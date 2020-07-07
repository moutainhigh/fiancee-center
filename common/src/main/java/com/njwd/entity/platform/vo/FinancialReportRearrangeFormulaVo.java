package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.FinancialReportRearrangeFormula;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FinancialReportRearrangeFormulaVo extends FinancialReportRearrangeFormula {

    /**
     * 公式科目编码 科目名称
     */
    private String cFormulaItemName;
    /**
     * 对方公式科目编码 对方科目名称
     */
    private String othersideFormulaItemName;

    /**
     * 公式科目编码 科目方向
     */
    private Byte cFormulaItemDirection;
    /**
     * 对方公式科目编码 对方科目方向
     */
    private Byte othersideFormulaItemDirection;

}