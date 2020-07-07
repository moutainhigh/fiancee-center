package com.njwd.entity.basedata;


import com.njwd.annotation.ExcelCell;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description 自定义核算项目大区值
 * @Date 2019/6/26 13:57
 * @Author 薛永利
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountingItemValue extends BaseModel {
    private static final long serialVersionUID = 1L;

    /**
     * 归属公司 【公司】表ID
     */
    private Long companyId;

    /**
     * 使用公司 【公司】表ID
     */
    protected Long useCompanyId;

    /**
     * 项目ID
     */
    private Long itemId;

    /**
     * 名称
     */
    @ExcelCell(index = 5)
    private String name;

    /**
     * 编码类型 0：系统、1：自定义
     */
    private Byte codeType;

    /**
     * 编码
     */
    private String code;

    /**
     * 启用标识 0：禁用、1：启用
     */
    private Byte isEnable;

    /**
     * 企业id
     */
    private Long rootEnterpriseId;
    /**
     * 描述
     */
    @ExcelCell(index = 6)
    private String remark;
    /**
     * 资料类型 1.共享型,2.分配型,3.私有型
     */
    @ExcelCell(index = 2)
    private Byte dataType;
    /**
     * 操作信息:禁用人,禁用时间等
     */
    private ManagerInfo manageInfo;
}