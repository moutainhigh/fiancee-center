package com.njwd.entity.basedata;

import com.njwd.annotation.ExcelCell;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description 客户供应商
 * @Author 朱小明
 * @Date 2019/7/4 18:05
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerSupplier extends BaseModel {

    /**
     * 企业ID
     */
    private Long rootEnterpriseId;

    /**
     * 创建公司
     */
    private Long companyId;

    /**
     * 全局ID
     */
    private Long globalId;

    /**
     * 编码类型 0：系统、1：自定义
     */
    private Byte codeType;

    /**
     * 资料类型 1.共享型,2.分配型,3.私有型
     */
    @ExcelCell(index = 3)
    private Byte dataTypes;

    /**
     * 编码
     */
    private String code;

    /**
     * 客户名称
     */
    @ExcelCell(index = 2)
    private String name;

    /**
     * 统一社会信用代码
     */
    @ExcelCell(index = 4)
    private String unifiedSocialCreditCode;

    /**
     * 身份证号
     */
    @ExcelCell(index = 5)
    private String idCardNum;

    /**
     * 经营地址
     */
    @ExcelCell(index = 6)
    private String businessAddress;

    /**
     * 联系人
     */
    @ExcelCell(index = 7)
    private String linkman;

    /**
     * 联系电话
     */
    @ExcelCell(index = 8)
    private String contactNumber;

    /**
     * 启用标识 0：禁用、1：启用
     */
    private Byte isEnable;

    private static final long serialVersionUID = 1L;

    private ManagerInfo manageInfo;

}