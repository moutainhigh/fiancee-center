package com.njwd.basedata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.service.ProjectService;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.basedata.ReferenceResult;
import com.njwd.entity.basedata.dto.ProjectDto;
import com.njwd.entity.basedata.vo.ProjectVo;
import com.njwd.exception.ResultCode;
import com.njwd.logger.SenderService;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
import com.njwd.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * 基础资料的项目
 *
 * @Author: LuoY
 * @Date: 2019/6/11
 */
@RestController
@RequestMapping("project")
public class ProjectController extends BaseController {
    @Resource
    private ProjectService projectService;

    @Autowired
    private SenderService senderService;

    @Resource
    private ReferenceRelationService referenceRelationService;

    /**
     * @Description 新增项目信息
     * @Author LuoY
     * @Date 2019/6/21 10:01
     * @Param [projectDto]
     * @return java.lang.String
     */
    @RequestMapping("addProject")
    public Result<ProjectVo> addProject(@RequestBody ProjectDto projectDto) {
        ProjectVo projectVo = projectService.addProject(projectDto, Constant.Project.PROJECT_CRUD_METHOD_ADDPROJECT);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys, LogConstant.menuName.project,
                LogConstant.operation.add, LogConstant.operation.add_type, projectVo.getId().toString()));
        return ok(projectVo);
    }

    /**
     * @Description 批量删除项目
     * @Author LuoY
     * @Date 2019/6/21 10:01
     * @Param [projectDto]
     * @return java.lang.String
     */
    @RequestMapping("deleteBatchProject")
    public Result<BatchResult> deleteBatchProject(@RequestBody ProjectDto projectDto) {
        //删除ids非空校验
        if(FastUtils.checkNullOrEmpty(projectDto.getIds())){
            return error(ResultCode.PARAMS_NOT);
        }
        BatchResult batchResult;
        if (projectDto.getIds().size()>1){
            //批量删除
            batchResult = projectService.deleteBatchProject(projectDto, Constant.Project.PROJECT_CRUD_METHOD_DELETEPROJECT);
        }else{
            //单条删除
            batchResult = projectService.deleteProject(projectDto,Constant.Project.PROJECT_CRUD_METHOD_DELETEPROJECT);
        }
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys, LogConstant.menuName.project,
                LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, batchResult.getSuccessList().toString()));
        return ok(batchResult);
    }

    /**
     * @Description 修改项目信息
     * @Author LuoY
     * @Date 2019/7/11 11:07
     * @Param [projectDto]
     * @return java.lang.String
     */
    @RequestMapping("updateProjectById")
    public Result<BatchResult> updateProjectById(@RequestBody ProjectDto projectDto){
        //修改项目信息id非空校验
        FastUtils.checkParams(projectDto.getId());
        BatchResult batchResult = projectService.updateProject(projectDto,Constant.Project.PROJECT_CRUD_METHOD_UPDATEPROJECTINFO);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys, LogConstant.menuName.project,
                LogConstant.operation.update, LogConstant.operation.update_type,projectDto.getId().toString()));
        return ok(batchResult);
    }

    /**
     * @Description 批量禁用项目有效性
     * @Author LuoY
     * @Date 2019/6/21 10:02
     * @Param [projectDtoList]
     * @return java.lang.String
     */
    @RequestMapping("updateBatchProjectDisable")
    public Result<BatchResult> updateBatchProjectDisable(@RequestBody ProjectDto projectDto) {
        //批量禁用ids非空校验
        if(FastUtils.checkNullOrEmpty(projectDto.getIds())){
            return error(ResultCode.PARAMS_NOT);
        }
        BatchResult batchResult;
        if(projectDto.getIds().size()>Constant.Number.ONE){
            //批量禁用
            batchResult = projectService.updateBatchProject(projectDto, Constant.Project.PROJECT_CRUD_METHOD_DISABLEPROJECT);
        }else{
            //单条禁用
            batchResult = projectService.updateProject(projectDto,Constant.Project.PROJECT_CRUD_METHOD_DISABLEPROJECT);
        }

        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys, LogConstant.menuName.project,
                LogConstant.operation.forbiddenBatch, LogConstant.operation.forbiddenBatch_type, batchResult.getSuccessList().toString()));
        return ok(batchResult);
    }

    /**
     * @description: 批量分配项目使用公司
     * @Param [projectDto]
     * @return com.njwd.support.Result<com.njwd.support.BatchResult>
     * @author LuoY
     * @date 2019/8/14 17:02
     */
    @PostMapping("updateBatchProjectUseCompany")
    public Result<BatchResult> updateProjectUseCompany(@RequestBody ProjectDto projectDto){
        //修改项目信息id非空校验
        FastUtils.checkParams(projectDto.getIds());
        BatchResult batchResult;
        if(projectDto.getIds().size()>Constant.Number.ONE){
            //批量
            batchResult = projectService.updateBatchProjectUseCompany(projectDto, Constant.Project.PROJECT_CRUD_METHOD_UPDATEPROJECTUSECOMPANY);
        }else{
            //单条
            batchResult = projectService.updateProjectUseCompany(projectDto,Constant.Project.PROJECT_CRUD_METHOD_UPDATEPROJECTUSECOMPANY);
        }
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys, LogConstant.menuName.project,
                LogConstant.operation.update, LogConstant.operation.update_type,batchResult.getSuccessList().toString()));
        return ok(batchResult);
    }

    /**
    * @description: 批量取消分配项目
    * @Param [projectDto]
    * @return com.njwd.support.Result<com.njwd.support.BatchResult>
    * @author LuoY
    * @date 2019/8/20 14:30
    */
    @PostMapping("updateDistributionBatchProjectUseCompanyInfo")
    public Result<BatchResult> updateProjectUseCompanyInfo(@RequestBody ProjectDto projectDto){
        //修改项目信息id非空校验
        FastUtils.checkParams(projectDto.getId());
        BatchResult batchResult = projectService.updateProjectUseCompanyInfo(projectDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys, LogConstant.menuName.project,
                LogConstant.operation.update, LogConstant.operation.update_type,batchResult.getSuccessList().toString()));
        return ok(batchResult);
    }

    /**
     * @Description 批量反禁用项目有效性
     * @Author LuoY
     * @Date 2019/6/21 10:02
     * @Param [projectDtoList]
     * @return java.lang.String
     */
    @RequestMapping("updateBatchProjectEnable")
    public Result<BatchResult> updateBatchProjectEnable(@RequestBody ProjectDto projectDto) {
        //批量反禁用id非空校验
        if(FastUtils.checkNullOrEmpty(projectDto.getIds())){
            return error(ResultCode.PARAMS_NOT);
        }
        BatchResult batchResult;
        if(projectDto.getIds().size()>Constant.Number.ONE){
            //批量反禁用
            batchResult = projectService.updateBatchProject(projectDto, Constant.Project.PROJECT_CRUD_METHOD_ENABLEPROJECT);
        }else{
            //单条反禁用
            batchResult = projectService.updateProject(projectDto,Constant.Project.PROJECT_CRUD_METHOD_ENABLEPROJECT);
        }
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys, LogConstant.menuName.project,
                LogConstant.operation.antiForbiddenBatch, LogConstant.operation.antiForbiddenBatch_type, batchResult.getSuccessList().toString()));
        return ok(batchResult);
    }


    /**
     * @Description 根据条件查询项目列表分页
     * @Author LuoY
     * @Date 2019/6/21 10:01
     * @Param [projectDto]
     * @return java.lang.String
     */
    @RequestMapping("findProjectByCondition")
    public Result<Page<ProjectVo>> findProjectByCondition(@RequestBody ProjectDto projectDto) {
    	FastUtils.checkParams(projectDto.getIsEnterpriseAdmin());
        Page<ProjectVo> page = projectService.findProjectByCondition(projectDto);
        return ok(page);
    }


    /**
     * @Description 根据id查询项目信息
     * @Author LuoY
     * @Date 2019/6/21 10:02
     * @Param [projectDto]
     * @return java.lang.String
     */
    @RequestMapping("findProjectById")
    public Result<ProjectVo> findProjectById(@RequestBody ProjectDto projectDto) {
        FastUtils.checkParams(projectDto.getId());
        ProjectVo projectVo = projectService.findProjectById(projectDto);
        //判断项目是否被引用
        ReferenceResult referenceResult = referenceRelationService.isReference(Constant.Reference.PROJECT, projectDto.getId());
        if (referenceResult != null && referenceResult.isReference()){
            projectVo.setIsCited(Constant.Is.YES);
        }
        return ok(projectVo);
    }

    /**
     * @Description 基础资料项目数据导出
     * @Author LuoY
     * @Date 2019/6/21 10:01
     * @Param [projectDto, response]
     * @return void
     */
    @RequestMapping("exportProjectData")
    public void exportProjectData(@RequestBody ProjectDto projectDto, HttpServletResponse response) {
        projectService.exportProjectData(projectDto, response);
    }

    /**
    * @description: 升级资料类型
    * @Param [projectDto]
    * @return com.njwd.support.Result<java.lang.Integer>
    * @author LuoY
    * @date 2019/9/10 9:18
    */
    @RequestMapping("upgradeDataType")
    public Result<BatchResult> upgradeDataType(@RequestBody ProjectDto projectDto){
        FastUtils.checkParams(projectDto.getId());
//        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
//                LogConstant.sysName.FinanceBackSys, LogConstant.menuName.project,
//                LogConstant.operation.antiForbiddenBatch, LogConstant.operation.antiForbiddenBatch_type, projectDto.getId().toString()));
        return ok(projectService.upgradeDataType(projectDto,Constant.Project.PROJECT_CRUD_METHOD_UPGRADEPROJECTUSECOMPANY));
    }
}
