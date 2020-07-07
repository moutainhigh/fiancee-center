package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.njwd.annotation.ExcelCell;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class Project extends BaseModel {
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
     * 名称
     */
    @ExcelCell(index = 3)
    private String name;

    /**
     * 创建公司 【公司】表ID
     */
    @ExcelCell(index = 1)
    private Long companyId;

    /**
     * 使用公司 【公司】表ID
     */
    @ExcelCell(index = 1)
    private Long useCompanyId;

    /**
     * 负责部门 【部门】表ID
     */
    private Long departmentId;

    /**
     * 负责人
     */
    private Long personInCharge;

    /**
     * 手机号
     */
    @ExcelCell(index = 8)
    private String mobile;

    /**
     * 开始日期
     */
    @ExcelCell(index = 9)
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern="yyyy-MM-dd",timezone="GMT+8")
    private Date startDate;

    /**
     * 验收日期
     */
    @ExcelCell(index = 10)
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern="yyyy-MM-dd",timezone="GMT+8")
    private Date inspectionDate;

    /**
     * 资料类型 1.共享型,2.分配型,3.私有型
     */
    @ExcelCell(index = 2)
    private Byte dataType;

    /**
     * 备注
     */
    @ExcelCell(index = 11)
    private String remark;

    /**
     * 启用标识 0：禁用、1：启用
     */
    private Byte isEnable;

    /**
     * 删除标识 0：未删除、1： 删除
     */
    private Byte isDel;

    /**
     * 创建公司id
     */
    private Long createCompanyId;

    /**
     * 企业id
     */
    private Long rootEnterpriseId;

    /**
     * 版本号
     */
    @Version
    private Integer version;

    /**
     * 管理信息
     */
    private ManagerInfo manageInfo;
}