package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import com.njwd.entity.platform.vo.FinancialReportItemFormulaVo;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author liuxiang
 * @Description 财务报告明细公式
 * @Date:17:05 2019/8/1
 **/
@Getter
@Setter
@TableName(value = "wd_financial_report_item_formula")
public class FinancialReportItemFormulaDto extends FinancialReportItemFormulaVo {
    private static final long serialVersionUID = 6027718358366895013L;
}