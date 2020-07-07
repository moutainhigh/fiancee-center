package com.njwd.basedata.service.impl;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.cloudclient.BalanceSubjectAuxiliaryItemFeignClient;
import com.njwd.basedata.cloudclient.VoucherFeignClient;
import com.njwd.basedata.mapper.ProjectMapper;
import com.njwd.basedata.mapper.ProjectUseCompanyMapper;
import com.njwd.basedata.service.BaseCustomService;
import com.njwd.basedata.service.MenuControlStrategyService;
import com.njwd.basedata.service.ProjectService;
import com.njwd.basedata.service.SequenceService;
import com.njwd.common.Constant;
import com.njwd.common.LedgerConstant;
import com.njwd.common.MenuCodeConstant;
import com.njwd.entity.basedata.*;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.CompanyDto;
import com.njwd.entity.basedata.dto.ProjectDto;
import com.njwd.entity.basedata.dto.ProjectUseCompanyDto;
import com.njwd.entity.basedata.vo.AccountBookVo;
import com.njwd.entity.basedata.vo.ProjectUseCompanyVo;
import com.njwd.entity.basedata.vo.ProjectVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.BalanceSubjectAuxiliaryItem;
import com.njwd.entity.ledger.dto.BalanceSubjectAuxiliaryItemQueryDto;
import com.njwd.entity.ledger.dto.VoucherDto;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.financeback.service.AccountBookService;
import com.njwd.financeback.service.CompanyService;
import com.njwd.service.FileService;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BatchResult;
import com.njwd.utils.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 项目service实现层
 *
 * @author LuoY
 * @date 2019-06-19
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    @Resource
    private ProjectMapper projectMapper;

    @Resource
    private ProjectUseCompanyMapper projectUseCompanyMapper;

    @Resource
    private FileService fileService;

    @Resource
    private CompanyService companyService;

    @Resource
    private SequenceService sequenceService;

    @Resource
    private ReferenceRelationService referenceRelationService;

    @Resource
    private BaseCustomService baseCustomService;

    @Resource
    private AccountBookService accountBookService;

    @Resource
    private MenuControlStrategyService menuControlStrategyService;

    @Resource
    private BalanceSubjectAuxiliaryItemFeignClient balanceSubjectAuxiliaryItemFeignClient;

    @Resource
    private VoucherFeignClient voucherFeignClient;

    /**
     * @return int
     * @Description 添加项目信息
     * @Author LuoY
     * @Date 2019/6/21 9:28
     * @Param [projectDto]
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectVo addProject(ProjectDto projectDto, String type) {
        SysUserVo sysUserVo = UserUtils.getUserVo();
        //校验添加的项目信息名称是否存在，如果存在则提示错误
        checkProjectNameUnique(projectDto);
        Project project = new Project();
        //将projectdto的值赋值给project
        FastUtils.copyProperties(projectDto, project);
        //设置project类其它的基础信息
        setProjectDefaultInfo(project, type);
        //如果codeType==0，需要系统生成项目编号
        if (project.getCodeType().equals(Constant.CodeType.SYSTEMCODE)) {
            setProjectSysCode(project);
            projectDto.setCode(project.getCode());
        }
        //校验添加的项目信息编码是否存在，如果存在则提示错误
        checkProjectCodeUnique(projectDto);
        //控制策略校验
        ResultCode resultData = projectMenuControl(project, type);
        if (resultData != null) {
            throw new ServiceException(resultData);
        }

        //user新增按钮操作权限校验
        if (Constant.Is.NO.equals(projectDto.getIsEnterpriseAdmin())) {
            ShiroUtils.checkPerm(Constant.MenuDefine.PROJECT_EDIT, projectDto.getCompanyId());
        }

        ProjectVo projectVo = new ProjectVo();
        //添加项目
        projectMapper.insert(project);
        //如果当前项目资料类型为私有型,则添加一条同创建公司的使用公司到使用公司表
        if (Constant.dataType.PRIVATE.equals(projectDto.getDataType())) {
            ProjectUseCompanyDto projectUseCompanyDto = new ProjectUseCompanyDto();
            projectUseCompanyDto.setUseCompanyId(projectDto.getCompanyId());
            projectUseCompanyDto.setProjectId(project.getId());
            projectUseCompanyDto.setIsDel(Constant.Is.NO);
            projectUseCompanyDto.setCreateTime(new Date());
            projectUseCompanyDto.setCreatorId(sysUserVo.getUserId());
            projectUseCompanyDto.setCreatorName(sysUserVo.getName());
            projectUseCompanyMapper.insert(projectUseCompanyDto);
        }
        //新增成功，将插入的数据id传给前台用于跳转查看页面
        projectVo.setId(project.getId());
        return projectVo;
    }

    /**
     * @return int
     * @Description 批量新增项目信息
     * @Author LuoY
     * @Date 2019/6/28 17:44
     * @Param [projectList]
     */
    @Override
    public int addBatchProjectInfo(List<Project> projectList) {
        return projectMapper.addBatchProject(projectList);
    }

    /**
     * @return com.njwd.support.BatchResult
     * @Description 删除项目
     * @Author LuoY
     * @Date 2019/7/9 16:04
     * @Param [projectDto, type]
     */
    @Override
    @CacheEvict(value = Constant.RedisCache.PROJECT, key = "#projectDto.getIds().get(0)", condition = "#projectDto.getIds().size() == 1")
    public BatchResult deleteProject(ProjectDto projectDto, String type) {
        //执行删除
        BatchResult batchResult = batchDataHandle(projectDto, type);
        return batchResult;
    }

    /**
     * @return com.njwd.entity.basedata.vo.ProjectVo
     * @Description 批量删除
     * @Author LuoY
     * @Date 2019/7/4 11:04
     * @Param [projectDtoList, type]
     */
    @Override
    public BatchResult deleteBatchProject(ProjectDto projectDtoList, String type) {
        //清楚缓存
        RedisUtils.removeBatch(Constant.RedisCache.PROJECT, projectDtoList.getIds());
        BatchResult batchResult = batchDataHandle(projectDtoList, type);
        return batchResult;
    }

    /**
     * @return com.njwd.support.BatchResult
     * @Description 修改项目信息
     * @Author LuoY
     * @Date 2019/7/9 15:28
     * @Param [projectDto, type]
     */
    @Override
    @CacheEvict(value = Constant.RedisCache.PROJECT, key = "#projectDto.getIds().get(0)", condition = "#projectDto.getIds().size() == 1")
    public BatchResult updateProject(ProjectDto projectDto, String type) {
        //清楚缓存
        RedisUtils.remove(Constant.RedisCache.PROJECT, projectDto.getId());
        BatchResult batchResult = batchDataHandle(projectDto, type);

        return batchResult;
    }

    /**
     * @return int
     * @Description 批量修改项目有效性
     * @Author LuoY
     * @Date 2019/6/21 9:28
     * @Param [projectDtoList, type]
     */
    @Override
    public BatchResult updateBatchProject(ProjectDto projectDto, String type) {
        //清楚缓存
        RedisUtils.removeBatch(Constant.RedisCache.PROJECT, projectDto.getIds());
        BatchResult batchResult = batchDataHandle(projectDto, type);
        return batchResult;
    }

    /**
     * @param projectDto
     * @param type
     * @return com.njwd.support.BatchResult
     * @description: 分配项目
     * @Param [projectDto, type]
     * @author LuoY
     * @date 2019/8/14 15:42
     */
    @Override
    @CacheEvict(value = Constant.RedisCache.PROJECT, key = "#projectDto.getIds().get(0)", condition = "#projectDto.getIds().size() == 1")
    public BatchResult updateProjectUseCompany(ProjectDto projectDto, String type) {
        BatchResult batchResult = batchDataHandle(projectDto, type);
        return batchResult;
    }

    /**
     * @param projectDto
     * @param type
     * @return com.njwd.support.BatchResult
     * @description: 批量分配项目
     * @Param [projectDto, type]
     * @author LuoY
     * @date 2019/8/14 15:42
     */
    @Override
    public BatchResult updateBatchProjectUseCompany(ProjectDto projectDto, String type) {
        //清楚缓存
        RedisUtils.removeBatch(Constant.RedisCache.PROJECT, projectDto.getIds());
        BatchResult batchResult = batchDataHandle(projectDto, type);
        return batchResult;
    }

    /**
     * @param projectDto
     * @return com.njwd.support.BatchResult
     * @description: 取消分配项目
     * @Param [projectDto, type]
     * @author LuoY
     * @date 2019/8/20 14:32
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = Constant.RedisCache.PROJECT, key = "#projectDto.getId()")
    public BatchResult updateProjectUseCompanyInfo(ProjectDto projectDto) {
        SysUserVo sysUserVo = UserUtils.getUserVo();
        BatchResult batchResult = new BatchResult();
        List<Long> successList = new ArrayList<>();
        List<ReferenceDescription> failList = new ArrayList<>();
        projectDto.setRootEnterpriseId(sysUserVo.getRootEnterpriseId());
        Project project = projectMapper.findProjectById(projectDto);
        //项目状态校验
        if (Constant.Is.YES.equals(project.getIsDel())) {
            //项目状态为删除
            throw new ServiceException(ResultCode.PROJECT_ISDELTE_HASDELETE);
        }
        if (Constant.Is.NO.equals(project.getIsEnable())) {
            //项目状态为禁用
            throw new ServiceException(ResultCode.PROJECT_ISENABLE_HASDISABLE);
        }

        //操作权限校验
        if (Constant.Is.NO.equals(projectDto.getIsEnterpriseAdmin())) {
            ShiroUtils.checkPerm(Constant.MenuDefine.PROJECT_EDIT, project.getCompanyId());
        }
        //version校验
        if (projectMapper.selectProjectVersionById(projectDto) > Constant.Number.ZERO) {
            throw new ServiceException(ResultCode.IS_CHANGE);
        }

        Project project1 = new Project();
        FastUtils.copyProperties(projectDto, project1);
        project1.setVersion(projectDto.getVersion() + Constant.Number.ONE);
        project1.setUpdateTime(new Date());
        project1.setUpdatorId(sysUserVo.getUserId());
        project1.setUpdatorName(sysUserVo.getName());
        projectMapper.updateProjectById(project1);
        //对取消分配的使用公司进行校验
        if (projectDto.getUseCompanyIdArr() != null) {
            for (Long companyId : projectDto.getUseCompanyIdArr()) {
                //数据引用校验
                if (!checkDataReferenceForDistribution(Constant.TableName.PROJECT, failList, companyId, projectDto.getId())) {
                    //如果数据未被引用,取消分配的使用公司
                    ProjectUseCompany projectUseCompany = new ProjectUseCompany();
                    projectUseCompany.setIsDel(Constant.Is.YES);
                    projectUseCompanyMapper.update(projectUseCompany, new LambdaQueryWrapper<ProjectUseCompany>().
                            eq(ProjectUseCompany::getProjectId, projectDto.getId()).
                            eq(ProjectUseCompany::getUseCompanyId, companyId));
                    successList.add(companyId);
                }
            }
            batchResult.setFailList(failList);
            batchResult.setSuccessList(successList);
        }
        return batchResult;
    }

    /**
     * @param projectDto
     * @return int
     * @description: 升级资料类型
     * @Param [projectDto]
     * @author LuoY
     * @date 2019/9/10 9:19
     */
    @Override
    @CacheEvict(value = Constant.RedisCache.PROJECT, key = "#projectDto.getId()")
    public BatchResult upgradeDataType(ProjectDto projectDto, String type) {
        BatchResult batchResult = batchDataHandle(projectDto, type);
        return batchResult;
    }

    /**
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.ProjectVo>
     * @Description 根据条件查询项目信息
     * @Author LuoY
     * @Date 2019/6/21 9:26
     * @Param [projectDto]
     */
    @Override
    public Page<ProjectVo> findProjectByCondition(ProjectDto projectDto) {
        Page<ProjectVo> projectVoPage = null;
        SysUserVo sysUserVo = UserUtils.getUserVo();
        if((Constant.Is.NO).equals(projectDto.getIsEnterpriseAdmin())){
            projectDto.setUserId(sysUserVo.getUserId());
        }
        if (FastUtils.checkNullOrEmpty(projectDto.getUseCompanyIds())) {
            //不根据使用公司查询
            projectDto.setRootEnterpriseId(sysUserVo.getRootEnterpriseId());
            projectVoPage = projectMapper.findProjectsByCondition(projectDto.getPage(), projectDto);
            List<Long> ids = new ArrayList<>();
            //获取项目ids
            for (ProjectVo projectVo : projectVoPage.getRecords()) {
                ids.add(projectVo.getId());
            }
            //查询使用公司
            ProjectUseCompanyDto projectUseCompanyDto = new ProjectUseCompanyDto();
            projectUseCompanyDto.setProjectIds(ids);
            List<ProjectUseCompanyVo> projectUseCompanyVos = projectUseCompanyMapper.findProjectUseCompanysByProjectIds(projectUseCompanyDto);
            mergeProjectData(projectVoPage.getRecords(), projectUseCompanyVos);
        } else {
            //user会根据使用公司来查询
            //先根据使用公司查询出来包含使用公司的项目id
            ProjectUseCompanyDto projectUseCompanyDto = new ProjectUseCompanyDto();
            projectUseCompanyDto.setUseCompanyIds(projectDto.getUseCompanyIds());
            projectUseCompanyDto.setMenuCode(projectDto.getMenuCode());
            projectUseCompanyDto.setUserId(projectDto.getUserId());
            //先根据使用id查询对应的项目id
            List<Long> projectIds = projectUseCompanyMapper.findProjectIdByUseCompanyId(projectUseCompanyDto);
            //根据创建公司查询对应的项目id
            projectDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
            List<Long> projectIdsTest = ShiroUtils.filterPerm(Constant.MenuDefine.PROJECT_FIND,projectDto.getUseCompanyIds());
            //如果没有公司权限则集团数据也不展示
            if(CollectionUtils.isEmpty(projectIdsTest)){
                return null;
            }
            List<Long> projectIds1 = projectMapper.findProjectIdsByCompanyIds(projectDto);
            //两个list合并去重
            projectIds.removeAll(projectIds1);
            projectIds.addAll(projectIds1);
            if (!FastUtils.checkNullOrEmpty(projectIds)) {
                projectDto.setIds(projectIds);
                projectDto.setDataType(Constant.dataType.SHRETYPE);
                projectDto.setIsDel(Constant.Is.NO);
                //查询项目
                projectVoPage = projectMapper.findProjectByIds(projectDto.getPage(), projectDto);
                projectUseCompanyDto.setProjectIds(projectDto.getIds());
                List<ProjectUseCompanyVo> projectUseCompanyVos = projectUseCompanyMapper.findProjectUseCompanysByProjectIds(projectUseCompanyDto);
                mergeProjectData(projectVoPage.getRecords(), projectUseCompanyVos);
            }
        }
        return projectVoPage;
    }

    /**
     * @return int
     * @Description 根据项目名称查询项目是否重复
     * @Author LuoY
     * @Date 2019/6/21 9:26
     * @Param [projectDto]
     */
    @Override
    public int findProjectByName(ProjectDto projectDto) {
        SysUserVo sysUserVo = UserUtils.getUserVo();
        //项目名称校验，公司内不重复
        List<Project> projectList = projectMapper.selectList(new LambdaQueryWrapper<Project>().
                eq(Project::getName, projectDto.getName()).
                eq(Project::getIsDel, Constant.Is.NO).
                eq(Project::getCompanyId, projectDto.getCompanyId()).
                eq(Project::getRootEnterpriseId, sysUserVo.getRootEnterpriseId()));
        int flag = Constant.Number.ONE;
        if (FastUtils.checkNullOrEmpty(projectList)) {
            flag = Constant.Number.ZERO;
        } else if (projectList.size() == Constant.Number.ONE && !StringUtil.isNull(projectDto.getId())) {
            if (projectList.get(Constant.Number.ZERO).getId().equals(projectDto.getId())) {
                flag = Constant.Number.ZERO;
            }
        }
        return flag;
    }

    /**
     * @return int
     * @Description 根据项目编号查找项目
     * @Author LuoY
     * @Date 2019/6/21 9:27
     * @Param [projectDto]
     */
    @Override
    public int findProjectByCode(ProjectDto projectDto) {
        if (StringUtil.isNotEmpty(projectDto.getCode())) {
            return projectMapper.selectCount(new LambdaQueryWrapper<Project>().
                    eq(Project::getCode, projectDto.getCode()).
                    eq(Project::getCompanyId, projectDto.getCompanyId()).
                    eq(Project::getIsDel, Constant.Is.NO));
        } else {
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
    }

    /**
     * @return com.njwd.entity.basedata.vo.ProjectVo
     * @Description 根据项目id查询项目
     * @Author LuoY
     * @Date 2019/6/21 9:29
     * @Param [projectDto]
     */
    @Override
    @Cacheable(value = Constant.RedisCache.PROJECT, key = "#projectDto.id", unless = "#result==null")
    public ProjectVo findProjectById(ProjectDto projectDto) {
        SysUserVo sysUserVo = UserUtils.getUserVo();
        FastUtils.checkParams(projectDto.getId());
        projectDto.setRootEnterpriseId(sysUserVo.getRootEnterpriseId());
        ProjectVo projectVo = projectMapper.findProjectById(projectDto);
        ProjectUseCompanyDto projectUseCompanyDto = new ProjectUseCompanyDto();
        projectUseCompanyDto.setProjectId(projectVo.getId());
        ProjectUseCompanyVo projectUseCompanyVos = projectUseCompanyMapper.findProjectUseCompanysByProjectId(projectUseCompanyDto);
        if (projectUseCompanyVos != null) {
            projectVo.setUseCompanyNames(projectUseCompanyVos.getUseCompanyName());
            projectVo.setUseCompanyIdString(projectUseCompanyVos.getUseCompanyIdString());
        } else if (Constant.dataType.SHRETYPE.equals(projectVo.getDataType())) {
            projectVo.setUseCompanyNames(LedgerConstant.FinancialString.AllUseCompany);
        }
        return projectVo;
    }

    /**
     * @return void
     * @Description Excel项目导出
     * @Author LuoY
     * @Date 2019/6/21 9:26
     * @Param [projectDto, response]
     */
    @Override
    public void exportProjectData(ProjectDto projectDto, HttpServletResponse response) {
        Page<ProjectVo> page = projectDto.getPage();
        fileService.resetPage(page);
        Page<ProjectVo> projectVoList = findProjectByCondition(projectDto);
        fileService.exportExcel(response, projectVoList.getRecords(), MenuCodeConstant.PROJECT, projectDto.getIsEnterpriseAdmin());
    }

    /**
     * @return void
     * @Description 设置project数据操作是的默认信息, 如创建人，创建时间等信息
     * @Author LuoY
     * @Date 2019/6/21 9:31
     * @Param [object, method]
     */
    @Override
    public void setProjectDefaultInfo(Object object, String method) {
        SysUserVo userVo = UserUtils.getUserVo();
        Project project = (Project) object;
        switch (method) {
            case "addProject":
                //新增项目
                project.setIsEnable(Constant.Is.YES);
                project.setIsDel(Constant.Is.NO);
                project.setCreateTime(new Date());
                project.setCreatorId(userVo.getUserId());
                project.setCreatorName(userVo.getName());
                project.setRootEnterpriseId(userVo.getRootEnterpriseId());
                break;
            case "updateProjectEnable":
                //项目有效性反禁用
                project.setIsEnable(Constant.Is.YES);
                project.setUpdateTime(new Date());
                break;
            case "updateProjectDisable":
                //项目有效性禁用
                project.setIsEnable(Constant.Is.NO);
                project.setUpdateTime(new Date());
                break;
            case "deleteProject":
                //删除项目
                project.setIsDel(Constant.Is.YES);
                project.setUpdateTime(new Date());
                break;
            case "updateProjectInfo":
                //修改项目明细
                //项目编号,编号类型
                project.setCode(null);
                project.setCodeType(null);
                project.setUpdateTime(new Date());
                break;
            default:
                project.setUpdateTime(new Date());
        }
    }

    /**
     * @return void
     * @Description 系统生成流水号判断
     * @Author LuoY
     * @Date 2019/6/25 16:58
     * @Param [Project]
     */
    @Override
    public void setProjectSysCode(Project project) {
        //根据公司companyid取companycode
        CompanyDto companyDto = new CompanyDto();
        Company company = new Company();

        if (Constant.BlocInfo.BLOCID.equals(project.getCompanyId())) {
            //集团
            company.setId(Constant.BlocInfo.BLOCID);
            company.setCode(Constant.BlocInfo.BLOCCODE);
        } else {
            companyDto.setId(project.getCompanyId());
            company = companyService.findCompanyByIdOrCodeOrName(companyDto);
        }

        //判断系统流水号是否已被用
        while (true) {
            project.setCode(sequenceService.getCode(Constant.BaseCodeRule.PROJECT, Constant.BaseCodeRule.LENGTH_FOUR,
                    project.getCompanyId(), company.getCode(), Constant.BaseCodeRule.COMPANY));
            int flag = projectMapper.selectCountByCode(project);
            if (Constant.Number.ZERO.equals(flag)) {
                break;
            }
        }
    }

    /**
     * @return void
     * @Description 校验项目名称是否唯一
     * @Author LuoY
     * @Date 2019/6/21 9:31
     * @Param [projectDto]
     */
    public void checkProjectNameUnique(ProjectDto projectDto) {
        Integer rows = findProjectByName(projectDto);
        if (rows > Constant.Number.ZERO) {
            throw new ServiceException(ResultCode.PROJECT_NAME_EXIST);
        }
    }

    /**
     * @return void
     * @Description 校验项目编码是否唯一
     * @Author LuoY
     * @Date 2019/6/21 9:31
     * @Param [projectDto]
     */
    public void checkProjectCodeUnique(ProjectDto projectDto) {
        int rows = findProjectByCode(projectDto);
        if (rows > 0) {
            throw new ServiceException(ResultCode.CODE_EXIST);
        }
    }

    /**
     * @return void
     * @Description 校验项目数据是否被引用
     * @Author LuoY
     * @Date 2019/6/21 16:13
     * @Param [projectDto]
     */
    public ReferenceContext checkDeleteUseData(ProjectDto projectDto) {
        //校验数据是否被引用
        ReferenceContext referenceContext = referenceRelationService.isReference(Constant.Reference.PROJECT, projectDto.getIds());
        return referenceContext;
    }

    /**
     * @return com.njwd.support.BatchResult
     * @Description (批量)修改删除处理
     * @Author LuoY
     * @Date 2019/7/4 10:56
     * @Param [projectDto, type]
     */
    public BatchResult batchDataHandle(ProjectDto projectDto, String type) {
        SysUserVo sysUserVo = UserUtils.getUserVo();
        List<ReferenceDescription> successDetailsList = new ArrayList<>();
        if (FastUtils.checkNullOrEmpty(projectDto.getIds())) {
            List<Long> ids = new ArrayList<>();
            List<Integer> versions = new ArrayList<>();
            ids.add(projectDto.getId());
            versions.add(projectDto.getVersion());
            projectDto.setIds(ids);
            projectDto.setVersionIds(versions);
        }

        //获取数据操作的控制策略
        MenuControlStrategy menuControlStrategy = menuControlStrategyService.findMenuControlStrategy(MenuCodeConstant.PROJECT);
        //查询数据状态
        BatchResult batchResult = new BatchResult();
        List<ReferenceDescription> failList = new ArrayList<>();
        List<ProjectVo> projectList = null;
        //项目信息修改使用
        if (Constant.Project.PROJECT_CRUD_METHOD_UPDATEPROJECTINFO.equals(type)) {
            if (FastUtils.checkNullOrEmpty(projectDto.getIds()) && projectDto.getId() > 0) {
                List<Long> idList = new ArrayList<>();
                idList.add(projectDto.getId());
                projectDto.setIds(idList);
            }
        }
        //version校验（CAS）
        FastUtils.filterVersionIds(projectMapper, new QueryWrapper<>(), Constant.ColumnName.ID, projectDto.getIds(), projectDto.getVersionIds(), failList);
        ReferenceDescription rfd = new ReferenceDescription();
        //校验项目数据是否被引用
        if (Constant.Project.PROJECT_CRUD_METHOD_DELETEPROJECT.equals(type)){
            //删除项目的时候校验数据引用
            ReferenceContext referenceContext = checkDeleteUseData(projectDto);
            if (!referenceContext.getReferences().isEmpty()) {
                //有引用数据
                failList.addAll(referenceContext.getReferences());
                for (ReferenceDescription referenceDescription : failList) {
                    projectDto.getIds().remove(referenceDescription.getBusinessId());
                }
            }
        }

        //员工批量操作校验数据操作权限
        if (Constant.Is.NO.equals(projectDto.getIsEnterpriseAdmin()) && !Constant.Project.PROJECT_CRUD_METHOD_UPDATEPROJECTINFO.equals(type)) {
            FastUtils.filterIdsByGroupId(projectMapper, projectDto.getIds(), failList);
        }

        //数据校验
        if (!FastUtils.checkNullOrEmpty(projectDto.getIds())) {
            Page<ProjectVo> page = projectMapper.findProjectByIds(projectDto.getPage(), projectDto);
            projectList = page.getRecords();
            checkData(projectList, rfd, failList, projectDto, type, menuControlStrategy);
        }

        //权限操作校验
        if (Constant.Is.NO.equals(projectDto.getIsEnterpriseAdmin()) && !FastUtils.checkNullOrEmpty(projectDto.getIds())) {
            BatchResult batchResult1 = projectShiroCheck(projectDto, type);
            List<Long> failsIds = new ArrayList<>();
            batchResult1.getFailList().forEach(data -> {
                projectDto.getIds().forEach(idData -> {
                    if (data.getBusinessId().equals(idData)) {
                        failsIds.add(idData);
                    }
                });
            });
            projectDto.getIds().removeAll(failsIds);
            failList.addAll(batchResult1.getFailList());
        }


        //设置校验结果
        batchResult.setFailList(failList);
        Project project = new Project();
        //对象值转换
        FastUtils.copyProperties(projectDto, project);


        //修改项目信息时，如果有异常直接抛出异常
        if (Constant.Project.PROJECT_CRUD_METHOD_UPDATEPROJECTINFO.equals(type) || Constant.Project.PROJECT_CRUD_METHOD_UPGRADEPROJECTUSECOMPANY.equals(type)) {
            if ((!StringUtil.isNull(rfd.getReferenceDescription()) || !FastUtils.checkNullOrEmpty(failList))) {
                if (FastUtils.checkNullOrEmpty(failList)) {
                    throw new ServiceException(rfd.getReferenceDescription(), ResultCode.UNAUTHORIZED);
                } else {
                    throw new ServiceException(failList.get(Constant.Number.ZERO).getReferenceDescription(), ResultCode.UNAUTHORIZED);
                }
            }
        }
        //数据操作
        if (projectDto.getIds().size() > Constant.Number.ZERO) {
            //设置基础信息
            setProjectDefaultInfo(project, type);
            if (Constant.Project.PROJECT_CRUD_METHOD_DISABLEPROJECT.equals(type)) {
                project.setBatchIds(projectDto.getIds());
                baseCustomService.batchEnable(project, Constant.IsEnable.DISABLE, projectMapper, null);
            } else if (Constant.Project.PROJECT_CRUD_METHOD_ENABLEPROJECT.equals(type)) {
                project.setBatchIds(projectDto.getIds());
                baseCustomService.batchEnable(project, Constant.IsEnable.ENABLE, projectMapper, null);
            } else {
                FastUtils.updateBatch(projectMapper, project, Constant.ColumnName.ID, projectDto.getIds(), null);
                //单条跟新
                if(projectDto.getIds().size() == Constant.Number.ONE){
                    //跟新子表数据
                    ProjectUseCompanyDto projectUseCompanyDto = new ProjectUseCompanyDto();
                    projectUseCompanyDto.setUseCompanyId(projectDto.getCompanyId());
                    projectUseCompanyDto.setProjectId(project.getId());
                    projectUseCompanyDto.setIsDel(Constant.Is.NO);
                    projectUseCompanyDto.setCreateTime(new Date());
                    projectUseCompanyDto.setCreatorId(sysUserVo.getUserId());
                    projectUseCompanyDto.setCreatorName(sysUserVo.getName());
                    //原分配类型为共享型或分配型（未分配）
                    if (Constant.dataType.SHRETYPE.equals(projectDto.getOldDataType()) || Constant.dataType.DISTRIBUTION.equals(projectDto.getOldDataType()) ) {
                        if (Constant.dataType.PRIVATE.equals(projectDto.getDataType())){
                            projectUseCompanyMapper.insert(projectUseCompanyDto);
                        }
                    }else {
                        //原分配类型为私有型
                         //线分配类型为共享型或分配型（未分配）
                        if (Constant.dataType.SHRETYPE.equals(projectDto.getDataType()) || Constant.dataType.DISTRIBUTION.equals(projectDto.getDataType())){
                            projectUseCompanyMapper.deleteProjectUserCompany(projectUseCompanyDto);
                        }else{
                            projectUseCompanyMapper.update(projectUseCompanyDto,new LambdaQueryWrapper<ProjectUseCompany>().eq(ProjectUseCompany::getProjectId, projectUseCompanyDto.getProjectId()));
                        }
                    }
                }
            }
        }

        if (Constant.Project.PROJECT_CRUD_METHOD_UPDATEPROJECTUSECOMPANY.equals(type)) {
            //分配
            distributionProject(successDetailsList,failList,projectList, projectDto, sysUserVo);
            batchResult.setFailList(failList);
            batchResult.setSuccessDetailsList(successDetailsList);
            return batchResult;
        }

        batchResult.setSuccessList(projectDto.getIds());
        return batchResult;
    }

    /**
     * @return void
     * @description: 项目分配
     * @Param [projectList, projectDto, sysUserVo]
     * @author LuoY
     * @date 2019/8/19 17:03
     */
    @Transactional(rollbackFor = Exception.class)
    public void distributionProject(List<ReferenceDescription> successList,List<ReferenceDescription> failList,List<ProjectVo> projectList, ProjectDto projectDto, SysUserVo sysUserVo) {
        if (!FastUtils.checkNullOrEmpty(projectList)) {
            //分配,往项目分配使用公司表插入分配公司数据
            for (Project project1 : projectList) {
                //先查询使用公司表里面是否包含分配公司里面的数据
                ProjectUseCompanyDto projectUseCompanyDto = new ProjectUseCompanyDto();
                projectUseCompanyDto.setProjectId(project1.getId());
                List<ProjectUseCompanyVo> projectUseCompanys = projectUseCompanyMapper.findProjectUseCompanysByProjectId2(projectUseCompanyDto);
                List<Long> hasCompanyId = new ArrayList<>();
                //查询已经存在的分配公司
                ReferenceDescription referenceDescription;
                for (ProjectUseCompany projectUseCompany : projectUseCompanys) {
                    for (Long companyId : projectDto.getUseCompanyIdArr()) {
                        if (companyId.equals(projectUseCompany.getUseCompanyId())) {
                            referenceDescription = new ReferenceDescription();
                            referenceDescription.setBusinessId(project1.getId());
                            referenceDescription.setCompanyId(companyId);
                            referenceDescription.setReferenceDescription(ResultCode.DATA_IS_DISTRIBUTE.message);
                            failList.add(referenceDescription);
                            hasCompanyId.add(companyId);
                        }
                    }
                }
                //从添加的公司里面里面删除已经存在的公司id
                List<Long> addCompanyId = new ArrayList<>();
                for (Long id : projectDto.getUseCompanyIdArr()){
                    addCompanyId.add(id);
                }
                addCompanyId.removeAll(hasCompanyId);
                //更新项目表的更新时间，更新人，更新着id，和项目版本号
                project1.setUpdateTime(new Date());
                project1.setUpdatorId(sysUserVo.getUserId());
                project1.setUpdatorName(sysUserVo.getName());
                projectMapper.updateProjectById(project1);
                if (!FastUtils.checkNullOrEmpty(addCompanyId)) {
                    //添加新未分配的使用公司
                    for (Long company : addCompanyId) {
                        ProjectUseCompany projectUseCompany1 = new ProjectUseCompany();
                        projectUseCompany1.setProjectId(project1.getId());
                        projectUseCompany1.setUseCompanyId(company);
                        projectUseCompany1.setCreateTime(new Date());
                        projectUseCompany1.setCreatorId(sysUserVo.getUserId());
                        projectUseCompany1.setCreatorName(sysUserVo.getName());
                        projectUseCompanyMapper.insert(projectUseCompany1);
                        referenceDescription = new ReferenceDescription();
                        referenceDescription.setBusinessId(project1.getId());
                        referenceDescription.setCompanyId(company);
                        successList.add(referenceDescription);
                    }
                }
            }

        }
    }

    /**
     * @return void
     * @description: 数据校验
     * @Param [projectList, rfd, failList, projectDto, type]
     * @author LuoY
     * @date 2019/8/19 17:03
     */
    private void checkData(List<ProjectVo> projectList, ReferenceDescription rfd, List<ReferenceDescription> failList, ProjectDto projectDto, String type, MenuControlStrategy menuControlStrategy) {
        List<Project> projects = new ArrayList<>();
        if (!FastUtils.checkNullOrEmpty(projectList)) {
            for (Project project : projectList) {
                    rfd = new ReferenceDescription();
                    //校验数据是否已删除
                    if (Constant.Is.YES.equals(project.getIsDel())) {
                        for (Long companyId : projectDto.getUseCompanyIdArr()) {
                            //如果数据已被删除，设置结果集无法删除数据的id和无法删除的原因
                            rfd.setBusinessId(project.getId());
                            rfd.setCompanyId(companyId);
                            rfd.setReferenceDescription(ResultCode.PROJECT_ISDELTE_HASDELETE.message);
                            //删除的项目不允许修改项目信息
                            failList.add(rfd);

                        }
                        projectDto.getIds().remove(project.getId());
                        projects.add(project);
                        continue;
                    }
                    if (Constant.Project.PROJECT_CRUD_METHOD_UPDATEPROJECTINFO.equals(type)) {
                        //修改项目信息的时候需要对项目名称进行校验
                        checkProjectNameUnique(projectDto);
                    }
                    if (Constant.Project.PROJECT_CRUD_METHOD_ENABLEPROJECT.equals(type)) {
                        if (Constant.Is.YES.equals(project.getIsEnable())) {
                            //如果数据已被启用，设置结果集无法启用数据的id和无法启用的原因
                                rfd.setBusinessId(project.getId());
                                rfd.setReferenceDescription(ResultCode.PROJECT_ISENABLE_HASENABLE.message);
                                failList.add(rfd);
                            projectDto.getIds().remove(project.getId());
                            projects.add(project);
                        }
                    } else if (Constant.Project.PROJECT_CRUD_METHOD_DISABLEPROJECT.equals(type)
                            || Constant.Project.PROJECT_CRUD_METHOD_UPDATEPROJECTINFO.equals(type)
                            || Constant.Project.PROJECT_CRUD_METHOD_UPDATEPROJECTUSECOMPANY.equals(type)
                    ) {
                        if (Constant.Is.NO.equals(project.getIsEnable())) {
                            //如果数据已被禁用，设置结果集无法禁用数据的id和无法禁用的原因
                            for (Long companyId : projectDto.getUseCompanyIdArr()) {
                                rfd.setBusinessId(project.getId());
                                rfd.setCompanyId(companyId);
                                rfd.setReferenceDescription(ResultCode.PROJECT_ISENABLE_HASDISABLE.message);
                                //禁用的项目不允许修改项目信息
                                failList.add(rfd);
                            }
                            projectDto.getIds().remove(project.getId());
                            projects.add(project);
                        }
                    }
                    if (Constant.Project.PROJECT_CRUD_METHOD_UPDATEPROJECTUSECOMPANY.equals(type)) {
                        if (!Constant.dataType.DISTRIBUTION.equals(project.getDataType())) {
                            //如果不是分配型,不允许分配
                            for (Long companyId : projectDto.getUseCompanyIdArr()) {
                                rfd.setBusinessId(project.getId());
                                rfd.setCompanyId(companyId);
                                rfd.setReferenceDescription(ResultCode.PROJECT_USECOMPANY_DATATYPEDISTRIBUTION.message);
                                //不是分配型项目不允许分配
                                failList.add(rfd);
                            }
                            projectDto.getIds().remove(project.getId());
                            projects.add(project);
                        }
                    }

                if(Constant.Project.PROJECT_CRUD_METHOD_DELETEPROJECT.equals(type)){
                    //删除项目,校验项目使用公司,如果使用公司列表包含不是创建公司的使用公司,不给删除,只能取消分配使用公司后才能删除
                    int result = projectMapper.checkProjectUseCompanyByProjectId(project.getId());
                    if (result>Constant.Number.ZERO){
                        rfd.setBusinessId(project.getId());
                        rfd.setReferenceDescription(ResultCode.PROJECT_DELETE_DELETEPROJECTUSED.message);
                        //不是分配型项目不允许分配
                        failList.add(rfd);
                        projectDto.getIds().remove(project.getId());
                        projects.add(project);
                    }
                }
            }
        }
        if (!FastUtils.checkNullOrEmpty(projects)) {
            projectList.removeAll(projects);
        }
    }

    /**
     * @return void
     * @description: projectVo数据合并
     * @Param [projectVos, projectUseCompanyVos]
     * @author LuoY
     * @date 2019/8/22 19:09
     */
    private void mergeProjectData(List<ProjectVo> projectVos, List<ProjectUseCompanyVo> projectUseCompanyVos) {
        for (ProjectVo projectVo : projectVos) {
            for (ProjectUseCompanyVo projectUseCompanyVo : projectUseCompanyVos) {
                if (projectUseCompanyVo.getProjectId().equals(projectVo.getId())) {
                    projectVo.setUseCompanyNames(projectUseCompanyVo.getUseCompanyName());
                    projectVo.setUseCompanyIdString(projectUseCompanyVo.getUseCompanyIdString());
                } else if (Constant.dataType.SHRETYPE.equals(projectVo.getDataType())) {
                    projectVo.setUseCompanyNames(LedgerConstant.FinancialString.AllUseCompany);
                }
            }
        }
    }

    /**
     * @return com.njwd.support.BatchResult
     * @description: 判断已选需要取消分配的公司中有没有发生这个资料作为辅助核算项目使用后的相关的期初数据：
     * @Param [projectDto]
     * @author LuoY
     * @date 2019/8/23 11:58
     */
    @Override
    public Boolean checkDataReferenceForDistribution(String sourceTable, List<ReferenceDescription> referenceDescriptions,
                                                     Long companyId, Long id) {

        Boolean isHas = false;
        //根据公司id查询对应的核算账簿
        AccountBookDto accountBookDto = new AccountBookDto();
        accountBookDto.setCompanyId(companyId);
        AccountBookVo accountBookVo = accountBookService.findByCompanyId(accountBookDto);
        if (accountBookVo != null) {
            //根据核算账簿id+数据来源表+项目id查询期初余额表里面是否引用
            BalanceSubjectAuxiliaryItemQueryDto balanceSubjectAuxiliaryDto = new BalanceSubjectAuxiliaryItemQueryDto();
            balanceSubjectAuxiliaryDto.setAccountBookId(accountBookVo.getId());
            balanceSubjectAuxiliaryDto.setSourceTables(sourceTable);
            balanceSubjectAuxiliaryDto.setItemValueId(id);
            BalanceSubjectAuxiliaryItem balanceSubjectAuxiliary = balanceSubjectAuxiliaryItemFeignClient.findByAccountBookIdAndItemValueId(balanceSubjectAuxiliaryDto).getData();
            if (balanceSubjectAuxiliary != null) {
                //有查询结果,表示数据已被引用
                isHas = true;
            } else {
                //根据核算账簿id+数据来源表+项目id查询凭证分录辅助核算表中是否引用
                VoucherDto voucherDto = new VoucherDto();
                voucherDto.setAccountBookId(accountBookVo.getId());
                voucherDto.setItemValueId(id);
                voucherDto.setSourceTable(sourceTable);
                Integer count = voucherFeignClient.findVoucherEntryAuxiliaryByItemValueId(voucherDto).getData();
                if (count>Constant.Number.ZERO) {
                    //有查询结果表示数据已被引用
                    isHas = true;
                }
            }
        }
        if (isHas) {
            //如果数据已被引用添加校验失败信息
            ReferenceDescription referenceDescription = new ReferenceDescription();
            referenceDescription.setBusinessId(companyId);
            referenceDescription.setReferenceDescription(ResultCode.IS_CITED.message);
            referenceDescriptions.add(referenceDescription);
        }
        return isHas;
    }

    /**
     * @return ResultCode
     * @description: 项目控制策略
     * @Param [projectDto, type]
     * @author LuoY
     * @date 2019/8/28 13:57
     */
    @Override
    public ResultCode projectMenuControl(Project project, String type) {
        ResultCode resultMessage = null;
        //查询控制策略
        MenuControlStrategy menuControlStrategy = menuControlStrategyService.findMenuControlStrategy(MenuCodeConstant.PROJECT);
        if (Constant.Project.PROJECT_CRUD_METHOD_ADDPROJECT.equals(type)) {
            //如果集团创建为否
            if (Constant.Is.NO.equals(menuControlStrategy.getGroupCreate())) {
                //如果项目创建公司id为0(集团ID为0)
                if (Constant.Number.ZEROL.equals(project.getCompanyId())) {
                    resultMessage = ResultCode.PROJECT_ADD_PROJECTINFOBYADMIN;
                    return resultMessage;
                }
            }
            //如果公司创建为否
            if (Constant.Is.NO.equals(menuControlStrategy.getCompanyCreate())) {
                //如果项目创建公司id不为0(集团ID为0)
                if (!Constant.Number.ZEROL.equals(project.getCompanyId())) {
                    resultMessage = ResultCode.PROJECT_ADD_PROJECTINFOBYADMIN;
                    return resultMessage;
                }
            }
            //如果共享为否
            if (Constant.Is.NO.equals(menuControlStrategy.getIsShare())) {
                if (Constant.dataType.SHRETYPE.equals(project.getDataType())) {
                    resultMessage = ResultCode.PROJECT_ADD_PROJECTINFOBYSHARE;
                    return resultMessage;
                }

            } else if (Constant.Is.YES.equals(menuControlStrategy.getIsShare())) {
                //如果共享为是
                if (!Constant.Number.ZEROL.equals(project.getCompanyId())) {
                    if (Constant.dataType.SHRETYPE.equals(project.getDataType())) {
                        resultMessage = ResultCode.PROJECT_USECOMPANY_DATATYPEUSER;
                        return resultMessage;
                    }
                }
            }
            //如果分配为否
            if (Constant.Is.NO.equals(menuControlStrategy.getIsDistribute())) {
                if (Constant.dataType.DISTRIBUTION.equals(project.getDataType())) {
                    resultMessage = ResultCode.PROJECT_ADD_PROJECTINFOBYDISTRIBUTION;
                    return resultMessage;
                }
            } else if (Constant.Is.YES.equals(menuControlStrategy.getIsDistribute())) {
                //如果分配为是
                if (!Constant.Number.ZEROL.equals(project.getCompanyId())) {
                    if (Constant.dataType.DISTRIBUTION.equals(project.getDataType())) {
                        resultMessage = ResultCode.PROJECT_USECOMPANY_DATATYPEUSER;
                        return resultMessage;
                    }
                }
            }
            //如果私有为否
            if (Constant.Is.NO.equals(menuControlStrategy.getIsPrivate())) {
                if (Constant.dataType.PRIVATE.equals(project.getDataType())) {
                    resultMessage = ResultCode.PROJECT_ADD_PROJECTINFOBYPRIVATE;
                    return resultMessage;
                }
            } else if (Constant.Is.NO.equals(menuControlStrategy.getIsPrivate())) {
                if (Constant.Number.ZEROL.equals(project.getCompanyId())) {
                    if (Constant.dataType.PRIVATE.equals(project.getDataType())) {
                        resultMessage = ResultCode.PROJECT_USECOMPANY_DATATYPEADMIN;
                        return resultMessage;
                    }
                }
            }
        }
        //项目类型升级
        if (Constant.Project.PROJECT_CRUD_METHOD_UPGRADEPROJECTUSECOMPANY.equals(type)) {
            //如果控制策略升级为否
            if (Constant.Is.NO.equals(menuControlStrategy.getIsChangeToDistribute())) {
                resultMessage = ResultCode.PROJECT_USECOMPANY_DATATYPEPRIVATEOTHER;
                return resultMessage;
            }
        }
        return resultMessage;
    }


    /**
     * @return com.njwd.support.BatchResult
     * @description: 批量操作权限校验
     * @Param [projectDto, type]
     * @author LuoY
     * @date 2019/9/23 10:50
     */
    public BatchResult projectShiroCheck(ProjectDto projectDto, String type) {
        String menu = null;
        if (Constant.Project.PROJECT_CRUD_METHOD_UPDATEPROJECTINFO.equals(type)) {
            //修改权限
            menu = Constant.MenuDefine.PROJECT_EDIT;
        } else if (Constant.Project.PROJECT_CRUD_METHOD_DELETEPROJECT.equals(type)) {
            //删除权限
            menu = Constant.MenuDefine.PROJECT_DELETE;
        }
        if (Constant.Project.PROJECT_CRUD_METHOD_DISABLEPROJECT.equals(type)) {
            //禁用
            menu = Constant.MenuDefine.PROJECT_DISABLE;
        }
        if (Constant.Project.PROJECT_CRUD_METHOD_ENABLEPROJECT.equals(type)) {
            //反禁用
            menu = Constant.MenuDefine.PROJECT_ENABLE;
        }

        //根据ids查询project
        List<ProjectVo> projects = projectMapper.findProjectByIds(projectDto.getPage(), projectDto).getRecords();
        //权限校验
        BatchResult batchResult = ShiroUtils.filterNotPermData(projects, menu, new ShiroUtils.CheckPermSupport<ProjectVo>() {
            @Override
            public Long getBusinessId(ProjectVo projectVo) {
                return projectVo.getId();
            }

            @Override
            public Long getCompanyId(ProjectVo projectVo) {
                return projectVo.getCompanyId();
            }
        });
        return batchResult;
    }
}

