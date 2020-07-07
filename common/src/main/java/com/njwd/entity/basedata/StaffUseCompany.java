package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.TableField;
import com.njwd.entity.base.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description
 * @Author: ZhuHC
 * @Date: 2019/8/20 11:13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StaffUseCompany extends BaseModel {

    /**
     * 员工
     */
    private Long staffId;
    /**
     * 部门
     */
    private Long deptId;

    /**
     * 使用公司
     */
    private Long useCompanyId;

    /**
     * 业务单元
     */
    private Long businessUnitId;

    /**
     * 排除BaseModel中的版本号
     */
    @TableField(exist = false)
    private Integer version;

    private static final long serialVersionUID = 1L;
}
