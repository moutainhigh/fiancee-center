package com.njwd.entity.basedata;

import com.njwd.entity.base.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description 部门使用公司记录
 * @Author jds
 * @Date 2019/6/21 9:57
 * @Param
 * @return
 **/

@Data
@EqualsAndHashCode(callSuper = true)
public class DeptUseCompany extends BaseModel {

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
     * 使用公司名称
     */
    private String useCompanyName;

    /**
     * 业务单元名称
     */
    private String businessUnitName;



    private static final long serialVersionUID = 1L;
}