package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.basedata.ProjectUseCompany;
import com.njwd.entity.basedata.dto.ProjectUseCompanyDto;
import com.njwd.entity.basedata.vo.ProjectUseCompanyVo;

import java.util.List;

/**
* @description: 项目分配使用公司mapper
* @author LuoY
* @date 2019/8/19 14:25
*/
public interface ProjectUseCompanyMapper extends BaseMapper<ProjectUseCompany> {
    /**
    * @description: 根据项目ids查询项目的使用公司列表
    * @Param [projectUseCompanyDto]
    * @return java.util.List<com.njwd.entity.basedata.ProjectUseCompany>
    * @author LuoY
    * @date 2019/8/19 14:33
    */
    List<ProjectUseCompanyVo> findProjectUseCompanysByProjectIds(ProjectUseCompanyDto projectUseCompanyDto);

    /**
    * @description: 根据项目ids查询项目使用公司列表
    * @Param [projectUseCompanyDto]
    * @return java.util.List<com.njwd.entity.basedata.vo.ProjectUseCompanyVo>
    * @author LuoY
    * @date 2019/9/17 9:57
    */
    List<ProjectUseCompanyVo> findProjectUseCompanysByProjectId2(ProjectUseCompanyDto projectUseCompanyDto);

    /**
    * @description: 根据项目id查询指定项目分配的使用公司列表
    * @Param [projectUseCompanyDto]
    * @return java.util.List<com.njwd.entity.basedata.vo.ProjectUseCompanyVo>
    * @author LuoY
    * @date 2019/8/20 11:44
    */
    ProjectUseCompanyVo findProjectUseCompanysByProjectId(ProjectUseCompanyDto projectUseCompanyDto);

    /**
    * @description: 根据使用公司查询包含指定使用公司的项目id
    * @Param [projectUseCompanyDto]
    * @return java.util.List<com.njwd.entity.basedata.vo.ProjectUseCompanyVo>
    * @author LuoY
    * @date 2019/8/20 16:09
    */
    List<Long> findProjectIdByUseCompanyId(ProjectUseCompanyDto projectUseCompanyDto);

    /**
     * @Author Libao
     * @Description 删除子表信息
     * @Date  2019/11/20 16:58
     * @Param [projectUseCompanyDto]
     * @return int
     */
    int deleteProjectUserCompany(ProjectUseCompanyDto projectUseCompanyDto);

}
