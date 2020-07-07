package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.njwd.annotation.ExcelCell;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 业务单元
 *
 * @Author: Zhuzs
 * @Date: 2019-05-21 15:21
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessUnit extends BaseModel {
    private static final long serialVersionUID = 8841569865721744526L;

    /**
    * 企业ID
    */
    private Long rootEnterpriseId;

    /**
    * 公司ID
    */
    @ExcelCell(index = 0)
    private Long companyId;

    /**
    * 编码
    */
    private String code;

    /**
    * 名称
    */
    @ExcelCell(index = 1)
    private String name;

    /**
    * 形态ID
    */
    @ExcelCell(index = 2)
    private Long unitFormId;

    /**
     * 形态
     */
    @ExcelCell(index = 2,redundancy = true)
    private String unitFormName;

    /**
    * 是否生效 0：否、1：是
    */
    private Byte isEnable;

    /**
    * 是否被引用 0：否、1：是
    */
    private Byte isRef;

    /**
    * 是否公司本部 0：否、1：是
    */
    private Byte isCompany;

    /**
     * 是否为核算主体
     */
    private Byte isAccountEntity;

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