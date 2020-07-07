package com.njwd.entity.basedata.vo;

import com.njwd.annotation.ExcelCell;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.Project;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description 项目VO
 * @Author LuoY
 * @date 2019/6/21 15:36
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectVo extends Project {

    /**
     * 创建公司名称 --> companyId
     */
    @ExcelCell(index = 1)
    private String companyName;

    /**
     * 负责部门名称 --> departmentId
     */
    @ExcelCell(index = 5)
    private String departmentName;

    /**
     * 负责人名称 --> person_in_charge
     */
    @ExcelCell(index = 7)
    private String personInChargeName;

    /**
     * 项目使用公司
     */
    private String useCompanyNames;

    /**
     * 项目使用公司ids
     */
    private String useCompanyIdString;

    public String getCompanyName(){
        if (Constant.BlocInfo.BLOCID.equals(this.getCompanyId())){
            return Constant.BlocInfo.BLOCNAME;
        }
        return companyName;
    }

    /**
     * 是否被引用（0：否 1：是）
     */
    private Byte isCited;
}
