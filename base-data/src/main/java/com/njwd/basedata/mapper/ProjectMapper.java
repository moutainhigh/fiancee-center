package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.Project;
import com.njwd.entity.basedata.dto.ProjectDto;
import com.njwd.entity.basedata.vo.ProjectVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description projectMapper
 * @Author LuoY
 * @Date 2019/6/25 16:43
 */
public interface ProjectMapper extends BaseMapper<Project> {

    /**
     *批量新增项目
     * @param projectList
     * @return
     */
    int addBatchProject(List<Project> projectList);

    /**
     *更新项目信息
     * @param project
     * @return
     */
    int updateProjectById(Project project);

    /**
     * 根据条件分页查询项目
     * @param projectDto
     * @return
     */
    Page<ProjectVo> findProjectsByCondition(@Param("page") Page<ProjectVo> page, @Param("projectDto") ProjectDto projectDto);

    /**
     * 根据项目id查询项目
     * @param projectDto
     * @return
     */
    ProjectVo findProjectById(@Param("projectDto") ProjectDto projectDto);

    /**
     * 根据项目ids查询项目
     * @param projectDto
     * @return
     */
    Page<ProjectVo> findProjectByIds(@Param("page") Page<ProjectVo> page,@Param("projectDto") ProjectDto projectDto);

    /**
     *查询系统生成的流水号项目是否存在
     * @param project
     * @return
     */
    int selectCountByCode(@Param("project") Project project);

    /**
    * @description: 根据创建公司ids查询项目ids
    * @Param [project]
    * @return java.util.List<java.lang.Long>
    * @author LuoY
    * @date 2019/9/11 10:22
    */
    List<Long> findProjectIdsByCompanyIds(@Param("projectDto") ProjectDto projectDto);

    /**
     * @description: 根据创建公司ids查询项目ids,不包括共享型数据
     * @Param [project]
     * @return java.util.List<java.lang.Long>
     * @author LuoY
     * @date 2019/9/11 10:22
     */
    List<Long> findProjectIdsForCheck(@Param("projectDto") ProjectDto projectDto);

    /**
    * @description: 项目修改版本号校验
    * @Param [projectDto]
    * @return int
    * @author LuoY
    * @date 2019/9/11 15:53
    */
    int selectProjectVersionById(ProjectDto projectDto);

    /**
    * @description: 根据项目id查询不包含创建公司的使用公司数量
    * @Param [projectDto]
    * @return int
    * @author LuoY
    * @date 2019/10/8 11:07
    */
    int checkProjectUseCompanyByProjectId(@Param("id") Long id);
}