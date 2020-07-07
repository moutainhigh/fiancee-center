package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 核算账簿
 *
 * @Author: Zhuzs
 * @Date: 2019-05-21 15:22
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountBook extends BaseModel {
    private static final long serialVersionUID = 4980677619406669342L;

    /**
    * 企业ID
    */
    private Long rootEnterpriseId;

    /**
    * 公司ID
    */
    private Long companyId;

    /**
    * 账簿编码
    */
    private String code;

    /**
    * 账簿名称：公司名称+核算账簿
    */
    private String name;

    /**
     * 账簿类型ID
     */
    private Long accountBookTypeId;

    /**
     * 账簿类型
     */
    private String accountBookTypeName;

    /**
     * 会计日历ID
     */
    private Long accountingCalendarId;

    /**
     * 会计日历
     */
    private String accountingCalendarName;

    /**
     * 科目表ID
     */
    private Long subjectId;

    /**
     * 科目表
     */
    private String subjectName;

    /**
     * 资产负债表ID
     */
    private Long balanceSheetId;

    /**
     * 利润表ID
     */
    private Long incomeStatementId;

    /**
     * 现金流量表ID
     */
    private Long cashFlowId;

    /**
     * 数据状态 0：已失效；1：已生效
     */
    private Byte status;

    /**
     * 现金流量项目表ID
     */
    private Long cashFlowItemId;

    /**
     * 操作信息:禁用人,禁用时间等
     */
    @TableField(exist = false)
    private ManagerInfo manageInfo;

}