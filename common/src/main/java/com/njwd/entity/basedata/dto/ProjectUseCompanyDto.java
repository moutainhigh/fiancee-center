package com.njwd.entity.basedata.dto;

import com.njwd.entity.basedata.vo.ProjectUseCompanyVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
* @description: 项目分配使用公司dto
* @author LuoY
* @date 2019/8/19 14:21
*/
@Getter
@Setter
public class ProjectUseCompanyDto extends ProjectUseCompanyVo {

    /**
    * 项目ids
    */
    private List<Long> projectIds;

    /**
     * 使用公司ids
     */
    private List<Long> useCompanyIds;


    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 前端菜单code
     */
    private String menuCode;
}
