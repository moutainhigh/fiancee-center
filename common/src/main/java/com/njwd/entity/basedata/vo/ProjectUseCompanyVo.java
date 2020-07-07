package com.njwd.entity.basedata.vo;

import com.njwd.entity.basedata.ProjectUseCompany;
import lombok.Getter;
import lombok.Setter;

/**
* @description: 项目分配使用公司VO
* @author LuoY
* @date 2019/8/19 14:20
*/

@Getter
@Setter
public class ProjectUseCompanyVo extends ProjectUseCompany {

    /**
    * @description: 使用公司名称
    * @author LuoY
    * @date 2019/8/19 17:40
    */
    private String useCompanyName;

    /**
     * @description: 使用公司名称
     * @author LuoY
     * @date 2019/8/19 17:40
     */
    private String useCompanyIdString;
}
