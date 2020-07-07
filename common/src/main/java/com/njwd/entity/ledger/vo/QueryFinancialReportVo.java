package com.njwd.entity.ledger.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author LuoY
 * @description: 财务报告-公共dto
 * @date 2019/8/9 10:48
 */
@Getter
@Setter
public class QueryFinancialReportVo {
    /**
     * 表编码
     */
    private Integer tableCode;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 级次 0：标题、1：一级、2：二级、3：三级、4：小计、5：合计、6：总计
     **/
    private Byte level;

    /**
     * 项目属性:1、资产、2：负债、3：利润表、4：现金流量表
     **/
    private Byte itemType;

    /**
     * 增减标识 0：减、1：加
     **/
    private Byte isAdd;

    /**
     * 其中标识：0：否、1：是
     **/
    private Byte isContain;

    /**
     * 流动标识 0：否、1：是
     **/
    private Byte isFlow;

    /**
     * 上级项目编码
     */
    private String projectCode;

    /**
     * 项目编码
     */
    private String upProjectCode;

    /**
     * 本期金额
     */
    private BigDecimal currentMoney;

    /**
     * 本年累计
     */
    private BigDecimal yearCumulative;

    /**
     * 科目方向
     */
    private Byte Direction;
}
