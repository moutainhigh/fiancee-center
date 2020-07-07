package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.Version;
import com.njwd.annotation.ExcelCell;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Dept extends BaseModel {

    /**
     * 编码类型 0：系统、1：自定义
     */
    private Byte codeType;

    /**
     * 上级部门
     */
    private Long prarentId;

    /**
     * 部门层级
     */
    private Byte deptLevel;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    @ExcelCell(index = 4)
    private String name;

    /**
     * 归属公司
     */
    private Long companyId;

    /**
     * 归属公司 业务单元
     */
    private Long attrBusinessUnitId;

    /**
     * 使用公司id
     */
    private Long useCompanyId;

    /**
     * 使用公司 业务单元
     */
    private Long businessUnitId;


    /**
     * 属性 【辅助资料】表ID
     */
    @ExcelCell(index = 5)
    private Long deptType;

    /**
     * 属性名称 【辅助资料】名称
     */
    @ExcelCell(index = 5, redundancy = true)
    private String deptTypeName;

    /**
     * 描述
     */
    @ExcelCell(index = 8)
    private String remark;

    /**
     * 启用标识 0：禁用、1：启用
     */
    private Byte isEnable;


    /**
     * 是否是末吉
     */
    private Byte isEnd;


    /**
     * 版本号
     */
    @Version
    private Integer version;


    /**
     * 企业ID
     */
    private Long rootEnterpriseId;

    /**
     * 操作信息
     */
    @TableField(exist = true)
    private ManagerInfo manageInfo;

    private static final long serialVersionUID = 1L;
}