package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.njwd.annotation.ExcelCell;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 公司
 *
 * @Author: Zhuzs
 * @Date: 2019-05-21 15:19
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Company extends BaseModel {

    private static final long serialVersionUID = 1294515783918515292L;

    /**
     * 企业ID
     */
    @NotNull
    private Long rootEnterpriseId;

    /**
     * 编码
     */
    @NotNull
    @ExcelCell(index = 0)
    private String code;

    /**
     * 名称
     */
    @NotNull
    @ExcelCell(index = 1)
    private String name;

    /**
     * 简称 是否需要默认等于名称
     */
    @ExcelCell(index = 3)
    private String simpleName;

    /**
     * 公司形态ID
     */
    @NotNull
    @ExcelCell(index = 2)
    private Long companyFormId;

    /**
     * 公司形态
     */
    @ExcelCell(index = 2,redundancy = true)
    private String companyFormName;

    /**
     * 注册地址
     */
    @ExcelCell(index = 14)
    private String registeAddress;

    /**
     * 成立日期
     */
    @JsonFormat(pattern="yyyy-MM-dd",timezone = "GMT+8")
    @ExcelCell(index = 4)
    private Date establishDate;

    /**
     * 公司类型ID
     */
    @ExcelCell(index = 5)
    private Long companyTypeId;

    /**
     * 公司类型
     */
    @ExcelCell(index = 5,redundancy = true)
    private String companyTypeName;

    /**
     * 法人代表
     */
    @ExcelCell(index = 6)
    private String legalPerson;

    /**
     * 固定电话
     */
    @ExcelCell(index = 7)
    private String fixedPhone;

    /**
     * 手机号码
     */
    @ExcelCell(index = 8)
    private String mobile;

    /**
     * 统一社会信用代码
     */
    @ExcelCell(index = 9)
    private String creditCode;

    /**
     * 纳税人识别号
     */
    @ExcelCell(index = 10)
    private String taxPayerNumber;

    /**
     * 工商注册号
     */
    @ExcelCell(index = 11)
    private String registeNumber;

    /**
     * 开户行
     */
    @ExcelCell(index = 12)
    private String bankName;

    /**
     * 银行账户
     */
    @ExcelCell(index = 13)
    private String bankNumber;

    /**
     * 会计准则ID
     */
    private Long accountingStandardId;

    /**
     * 会计准则
     */
    @ExcelCell(index = 15)
    private String accountingStandardName;

    /**
     * 税制ID
     */
    private Long taxSystemId;

    /**
     * 税制
     */
    @ExcelCell(index = 16)
    private String taxSystemName;

    /**
     * 记账本位币ID
     */
    private Long accountingCurrencyId;

    /**
     * 记账本位币
     */
    @ExcelCell(index = 17)
    private String accountingCurrencyName;

    /**
     * 纳税人资格ID
     */
    @ExcelCell(index = 18)
    private Long taxQualificationId;

    /**
     * 纳税人资格
     */
    @ExcelCell(index = 18,redundancy = true)
    private String taxQualificationName;

    /**
     * 是否建账 0：否、1：是
     */
    private Byte isAccounting;

    /**
     * 是否分账核算 0：否、1：是
     */
    @ExcelCell(index = 19)
    private Byte hasSubAccount;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 操作信息:禁用人,禁用时间等
     */
    @TableField(exist = false)
    private ManagerInfo manageInfo;

}