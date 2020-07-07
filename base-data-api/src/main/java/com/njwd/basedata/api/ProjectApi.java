package com.njwd.basedata.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.dto.ProjectDto;
import com.njwd.entity.basedata.vo.ProjectVo;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;

/**
 * 基础资料的项目
 *
 * @Author: LuoY
 * @Date: 2019/6/11
 */
@RequestMapping("project")
public interface ProjectApi {

    /**
     * @Description 新增项目信息
     * @Author LuoY
     * @Date 2019/6/21 10:01
     * @Param [projectDto]
     * @return java.lang.String
     */
    @RequestMapping("addProject")
    Result<ProjectVo> addProject(ProjectDto projectDto);

    /**
     * @Description 批量删除项目
     * @Author LuoY
     * @Date 2019/6/21 10:01
     * @Param [projectDto]
     * @return java.lang.String
     */
    @RequestMapping("deleteBatchProject")
    Result<BatchResult> deleteBatchProject(ProjectDto projectDto);

    /**
     * @Description 修改项目信息
     * @Author LuoY
     * @Date 2019/7/11 11:07
     * @Param [projectDto]
     * @return java.lang.String
     */
    @RequestMapping("updateProjectById")
    Result<BatchResult> updateProjectById(ProjectDto projectDto);

    /**
     * @Description 批量禁用项目有效性
     * @Author LuoY
     * @Date 2019/6/21 10:02
     * @Param [projectDtoList]
     * @return java.lang.String
     */
    @RequestMapping("updateBatchProjectDisable")
    Result<BatchResult> updateBatchProjectDisable(ProjectDto projectDto);

    /**
     * @Description 批量反禁用项目有效性
     * @Author LuoY
     * @Date 2019/6/21 10:02
     * @Param [projectDtoList]
     * @return java.lang.String
     */
    @RequestMapping("updateBatchProjectEnable")
    Result<BatchResult> updateBatchProjectEnable(ProjectDto projectDto);


    /**
     * @Description 根据条件查询项目列表分页
     * @Author LuoY
     * @Date 2019/6/21 10:01
     * @Param [projectDto]
     * @return java.lang.String
     */
    @RequestMapping("findProjectByCondition")
    Result<Page<ProjectVo>> findProjectByCondition(ProjectDto projectDto);


    /**
     * @Description 根据id查询项目信息
     * @Author LuoY
     * @Date 2019/6/21 10:02
     * @Param [projectDto]
     * @return java.lang.String
     */
    @RequestMapping("findProjectById")
    Result<ProjectVo> findProjectById(ProjectDto projectDto);

    /**
     * @Description 基础资料项目数据导出
     * @Author LuoY
     * @Date 2019/6/21 10:01
     * @Param [projectDto, response]
     * @return void
     */
    @RequestMapping("exportProjectData")
    void exportProjectData(ProjectDto projectDto, HttpServletResponse response);
}
