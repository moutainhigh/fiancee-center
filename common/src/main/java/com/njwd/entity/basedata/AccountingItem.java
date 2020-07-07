package com.njwd.entity.basedata;

import com.njwd.annotation.ExcelCell;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description 自定义核算项目
 * @Date 2019/6/26 13:57
 * @Author 薛永利
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountingItem extends BaseModel {
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @ExcelCell(index = 0)
    private String name;

    /**
     * 编码
     */
    private String code;

    /**
     * 编码类型 0：系统、1：自定义
     */
    private Byte codeType;

    /**
     * 资料类型 1.共享型,2.分配型,3.私有型
     */
    private Byte dataType;

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
    @ExcelCell(index = 1)
    private String remark;
    /**
     * 操作信息:禁用人,禁用时间等
     */
    private ManagerInfo manageInfo;
}