package com.njwd.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.Project;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.dto.ProjectDto;
import com.njwd.entity.basedata.vo.ProjectVo;
import com.njwd.exception.ResultCode;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Description 项目service层
 * @Author LuoY
 * @Date 2019/6/21 10:04
 */
public interface ProjectService {

    /**
     * @return int
     * @Description 添加项目信息
     * @Author LuoY
     * @Date 2019/6/21 10:06
     * @Param [projectDto]
     */
    ProjectVo addProject(ProjectDto projectDto, String type);

    /**
     * @return int
     * @Description 批量添加项目信息
     * @Author LuoY
     * @Date 2019/6/28 17:42
     * @Param [projectList]
     */
    int addBatchProjectInfo(List<Project> projectList);

    /**
     * @return com.njwd.support.BatchResult
     * @Description 删除项目
     * @Author LuoY
     * @Date 2019/7/9 15:22
     * @Param [projectDto]
     */
    BatchResult deleteProject(ProjectDto projectDto, String type);

    /**
     * @return ProjectVo
     * @Description 批量删除项目
     * @Author LuoY
     * @Date 2019/6/21 10:07
     * @Param [projectDtoList]
     */
    BatchResult deleteBatchProject(ProjectDto projectDtoList, String type);

    /**
     * @return com.njwd.support.BatchResult
     * @Description 修改项目信息
     * @Author LuoY
     * @Date 2019/7/9 15:26
     * @Param [projectDto, type]
     */
    BatchResult updateProject(ProjectDto projectDto, String type);

    /**
     * @return ProjectVo
     * @Description 批量修改项目信息
     * @Author LuoY
     * @Date 2019/6/21 10:07
     * @Param [projectDtoList, type]
     */
    BatchResult updateBatchProject(ProjectDto projectDto, String type);

    /**
     * @return com.njwd.support.BatchResult
     * @description: 分配项目
     * @Param [projectDto, type]
     * @author LuoY
     * @date 2019/8/14 15:42
     */
    BatchResult updateProjectUseCompany(ProjectDto projectDto, String type);

    /**
     * @return com.njwd.support.BatchResult
     * @description: 批量分配项目
     * @Param [projectDto, type]
     * @author LuoY
     * @date 2019/8/14 15:42
     */
    BatchResult updateBatchProjectUseCompany(ProjectDto projectDto, String type);

    /**
     * @return com.njwd.support.BatchResult
     * @description: 取消分配项目
     * @Param [projectDto]
     * @author LuoY
     * @date 2019/8/20 14:32
     */
    BatchResult updateProjectUseCompanyInfo(ProjectDto projectDto);

    /**
     * @description: 升级资料类型
     * @Param [projectDto]
     * @return int
     * @author LuoY
     * @date 2019/9/10 9:19
     */
    BatchResult upgradeDataType(ProjectDto projectDto,String type);

    /**
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.ProjectVo>
     * @Description 根据条件（编号,名称,归属公司id,使用公司id,数据有效性）查询项目清单列表
     * @Author LuoY
     * @Date 2019/6/21 10:05
     * @Param [projectDto]
     */
    Page<ProjectVo> findProjectByCondition(ProjectDto projectDto);

    /**
     * @return int
     * @Description 根据项目名称查询项目是否存在
     * @Author LuoY
     * @Date 2019/6/21 10:06
     * @Param [projectDto]
     */
    int findProjectByName(ProjectDto projectDto);

    /**
     * @return int
     * @Description 根据项目编码查询项目是否存在
     * @Author LuoY
     * @Date 2019/6/21 10:06
     * @Param [projectDto]
     */
    int findProjectByCode(ProjectDto projectDto);

    /**
     * @return com.njwd.entity.basedata.vo.ProjectVo
     * @Description 根据编号查询项目信息
     * @Author LuoY
     * @Date 2019/6/21 10:07
     * @Param [projectDto]
     */
    ProjectVo findProjectById(ProjectDto projectDto);

    /**
     * @return void
     * @Description 基础资料项目数据导出
     * @Author LuoY
     * @Date 2019/6/21 10:05
     * @Param [projectDto, response]
     */
    void exportProjectData(ProjectDto projectDto, HttpServletResponse response);

    /**
     * @return void
     * @Description 判断流水号是否占用
     * @Author LuoY
     * @Date 2019/6/28 16:57
     * @Param [project]
     */
    void setProjectDefaultInfo(Object object, String method);

    /**
     * @return void
     * @Description 数据操作默认值设置
     * @Author LuoY
     * @Date 2019/6/28 16:58
     * @Param [project]
     */
    void setProjectSysCode(Project project);

    /**
     * @param sourceTable
     * @param referenceDescriptions
     * @param companyId
     * @param id
     * @return java.lang.Boolean
     * @description: 判断已选需要取消分配的公司中有没有发生这个资料作为辅助核算项目使用后的相关的期初数据：
     * @author LuoY
     * @date 2019/8/29 11:55
     */
    Boolean checkDataReferenceForDistribution(@NotNull String sourceTable, @NotNull List<ReferenceDescription> referenceDescriptions, @NotNull Long companyId, @NotNull Long id);

    /**
     * @return com.njwd.exception.ResultCode
     * @description: 控制策略
     * @Param [project, type]
     * @author LuoY
     * @date 2019/8/28 14:45
     */
    ResultCode projectMenuControl(Project project, String type);
}

