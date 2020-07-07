package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.njwd.annotation.ExcelCell;
import com.njwd.entity.base.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author zhuhc
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Staff extends BaseModel {
    /**
    * 主键 默认自动递增
    */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
    * 编码类型 0：系统、1：自定义
    */
    private Byte codeType;

    /**
    * 编码
    */
    private String code;

    /**
    * 姓名
    */
    @ExcelCell(index = 4)
    private String name;

    /**
    * 归属公司
    */
    private Long companyId;

    /**
    * 部门 部门表Id
    */
    private Long deptId;

    /**
    * 电子邮箱
    */
    @ExcelCell(index = 8)
    private String email;

    /**
    * 银行账号
    */
    @ExcelCell(index = 6)
    private String bankAccount;

    /**
    * 描述
    */
    @ExcelCell(index = 9)
    private String remark;

    /**
    * 身份证号
    */
    @ExcelCell(index = 7)
    private String idCardNum;

    /**
    * 联系电话
    */
    @ExcelCell(index = 5)
    private String contactNumber;

    /**
    * 启用标识 0：禁用、1：启用
    */
    @ExcelCell(index = 10)
    private Byte isEnable;

    /**
    * 删除标识 0：未删除、1：删除
    */
    @TableLogic
    private Byte isDel;

    /**
    * 创建时间
    */
    private Date createTime;

    /**
    * 创建者ID
    */
    private Long creatorId;

    /**
    * 创建者
    */
    private String creatorName;

    /**
    * 更新时间
    */
    private Date updateTime;

    /**
    * 更新者ID
    */
    private Long updatorId;

    /**
    * 更新者
    */
    private String updatorName;

    /**
     * 企业ID
     */
    private Long rootEnterpriseId;

    /**
     * 创建公司
     */
    private Long createCompanyId;

    /**
     * 创建公司（归属公司）业务单元
     */
    private Long attrBusinessUnitId;
    /**
     * 使用公司
     */
    private Long useCompanyId;
    /**
     * 使用公司业务单元
     */
    private Long businessUnitId;

    private static final long serialVersionUID = 1L;
}