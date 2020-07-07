package com.njwd.excel;

import com.njwd.annotation.ExcelExtend;
import com.njwd.basedata.mapper.ProjectMapper;
import com.njwd.basedata.mapper.ProjectUseCompanyMapper;
import com.njwd.basedata.service.DeptService;
import com.njwd.basedata.service.ProjectService;
import com.njwd.basedata.service.StaffService;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.Company;
import com.njwd.entity.basedata.Dept;
import com.njwd.entity.basedata.Project;
import com.njwd.entity.basedata.dto.CompanyDto;
import com.njwd.entity.basedata.dto.ProjectDto;
import com.njwd.entity.basedata.dto.ProjectUseCompanyDto;
import com.njwd.entity.basedata.dto.StaffDto;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.fileexcel.check.CheckContext;
import com.njwd.fileexcel.check.CheckResult;
import com.njwd.fileexcel.extend.AddExtend;
import com.njwd.fileexcel.extend.CheckExtend;
import com.njwd.fileexcel.extend.CheckHandler;
import com.njwd.financeback.service.CompanyService;
import com.njwd.utils.FastUtils;
import com.njwd.utils.ShiroUtils;
import com.njwd.utils.StringUtil;
import com.njwd.utils.UserUtils;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author LuoY
 * date 2019-06-28
 * @description
 */
@Component
@Log
@ExcelExtend(type = Constant.TemplateType.PROJECT)
public class ProjectExtend implements AddExtend<ProjectDto>, CheckExtend {
    @Resource
    private ProjectMapper projectMapper;

    @Resource
    private ProjectUseCompanyMapper projectUseCompanyMapper;

    @Resource
    private ProjectService projectService;

    @Resource
    private DeptService deptService;

    @Resource
    private StaffService staffService;

    @Resource
    private CompanyService companyService;


    /**
     * @return int
     * @Description Excel导入批量导入
     * @Author LuoY
     * @Date 2019/7/2 15:09
     * @Param [data]
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int addBatch(List<ProjectDto> data) {
        int result = 0;
        //批量添加
        try {
            for (ProjectDto projectDto : data) {
                Project project = new Project();
                FastUtils.copyProperties(projectDto, project);
                //设置新增操作默认值
                projectService.setProjectDefaultInfo(project, Constant.Project.PROJECT_CRUD_METHOD_ADDPROJECT);
                //设置流水号
                projectService.setProjectSysCode(project);
                //excel导入,codeType默认为0,系统自定义
                project.setCodeType(Constant.CodeType.SYSTEMCODE);
                projectMapper.insert(project);
                if (Constant.dataType.PRIVATE.equals(projectDto.getDataType())) {
                    //如果为私有型，添加一条同创建公司的使用公司
                    insertIntoProjectUseCompany(project);
                }
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new ServiceException(ResultCode.OPERATION_FAILURE);
        }

        return result;
    }

    /**
     * @return int
     * @Description Excel导入单条导入
     * @Author LuoY
     * @Date 2019/7/2 15:10
     * @Param [data]
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int add(ProjectDto data) {
        Project project = new Project();
        FastUtils.copyProperties(data, project);
        //设置新增操作默认值
        projectService.setProjectDefaultInfo(project, "addProject");
        //设置流水号
        projectService.setProjectSysCode(project);
        //excel导入,codeType默认为0,系统自定义
        int result = 0;
        try {
            projectMapper.insert(project);
            if (Constant.dataType.PRIVATE.equals(data.getDataType())) {
                //如果为私有型，添加一条同创建公司的使用公司
                result = insertIntoProjectUseCompany(project);
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            //抛出异常，让事务捕获回滚
            throw new ServiceException(ResultCode.OPERATION_FAILURE);
        }
        return result;
    }

    /**
     * @return void
     * @Description 自定义校验
     * @Author LuoY
     * @Date 2019/7/2 15:10
     * @Param [checkContext]
     */
    @Override
    public void check(CheckContext checkContext) {
        Byte isEnterpriseAdmin = checkContext.getByteValue("isEnterpriseAdmin");
        //校验
        checkContext.addSheetHandler("createCompanyCode", findCreateCompanyCode(isEnterpriseAdmin)).
                addSheetHandler("departmentCode", findDepartmentCodeCheckHandler()).
                addSheetHandler("personInChargeCode", findPersonInCharge()).
                addSheetHandler("phoneNumber", findPhoneCheckHandler()).
                addSheetHandler("dataType", findDataTypeCheckHandler()).
                addSheetHandler("name", findProjectNameUnique());
    }

    /**
     * @return com.njwd.fileexcel.extend.CheckHandler<com.njwd.entity.basedata.dto.ProjectDto>
     * @description: 校验项目名称是否重复，公司内唯一
     * @Param []
     * @author LuoY
     * @date 2019/8/23 11:38
     */
    public CheckHandler<ProjectDto> findProjectNameUnique() {
        return data -> {
            String message = findProjectCodeCheckHandler(data);
            if (StringUtil.isNotEmpty(message)) {
                return CheckResult.error(message);
            }
            return CheckResult.ok();
        };
    }

    /**
     * @return com.njwd.fileexcel.extend.CheckHandler<com.njwd.entity.basedata.dto.ProjectDto>
     * @description: 校验创建公司
     * @Param []
     * @author LuoY
     * @date 2019/8/22 13:38
     */
    public CheckHandler<ProjectDto> findCreateCompanyCode(Byte isAdmin) {
        return data -> {
            String message = findCreateCompanyCode(data,isAdmin);
            if (StringUtil.isNotEmpty(message)) {
                return CheckResult.error(message);
            }
            return CheckResult.ok();
        };
    }

    /**
     * @return com.njwd.financeback.service.fileexcel.extend.CheckHandler<com.njwd.basedata.entity.dto.ProjectDto>
     * @Description 校验负责部门
     * @Author LuoY
     * @Date 2019/7/1 11:27
     * @Param []
     */
    public CheckHandler<ProjectDto> findDepartmentCodeCheckHandler() {
        return (data) -> {
            String message = findDepartmentCodeCheckHandler(data);
            if (StringUtil.isNotEmpty(message)) {
                return CheckResult.error(message);
            }
            return CheckResult.ok();
        };
    }

    /**
     * @return com.njwd.financeback.service.fileexcel.extend.CheckHandler<com.njwd.basedata.entity.dto.ProjectDto>
     * @Description 项目负责人校验
     * @Author LuoY
     * @Date 2019/7/8 17:19
     * @Param []
     */
    public CheckHandler<ProjectDto> findPersonInCharge() {
        return data -> {
            String message = checkPersonInCharge(data);
            if (StringUtil.isNotEmpty(message)) {
                return CheckResult.error(message);
            }
            return CheckResult.ok();
        };
    }

    /**
     * @return com.njwd.financeback.service.fileexcel.extend.CheckHandler<com.njwd.basedata.entity.dto.ProjectDto>
     * @Description 项目电话号码校验
     * @Author LuoY
     * @Date 2019/7/10 17:36
     * @Param []
     */
    public CheckHandler<ProjectDto> findPhoneCheckHandler() {
        return data -> {
            String message = checkPhoneNum(data);
            if (StringUtil.isNotEmpty(message)) {
                return CheckResult.error(message);
            }
            return CheckResult.ok();
        };
    }

    /**
     * @param
     * @return com.njwd.fileexcel.extend.CheckHandler<com.njwd.entity.basedata.dto.ProjectDto>
     * @description: 校验资料类型
     * @author LuoY
     * @date 2019/8/15 11:59
     */
    public CheckHandler<ProjectDto> findDataTypeCheckHandler() {
        return data -> {
            String message = checkDataType(data);
            if (StringUtil.isNotEmpty(message)) {
                return CheckResult.error(message);
            }
            return CheckResult.ok();
        };
    }


    /***************************************************************/
    /***********************校验方法*********************************/
    /***************************************************************/

    /**
     * @return java.lang.String
     * @description: 项目名称重复性校验
     * @Param [projectDto]
     * @author LuoY
     * @date 2019/8/28 14:51
     */
    private String findProjectCodeCheckHandler(ProjectDto projectDto) {
        String message = null;
        if (!StringUtil.isEmpty(projectDto.getName())) {
            //校验项目名称是否重复，公司内唯一
            int rows = projectService.findProjectByName(projectDto);
            if (rows > Constant.Number.ZERO) {
                message = ResultCode.PROJECT_NAME_EXIST.message;
            }
        }
        return message;
    }

    /**
     * @return java.lang.String
     * @Description 校验负责部门
     * @Author LuoY
     * @Date 2019/7/1 11:45
     * @Param [projectDto]
     */
    private String findDepartmentCodeCheckHandler(ProjectDto projectDto) {
        String message = null;
        Dept dept = null;
        //部门编码不为空且公司编码不为0000(集团)
        if (!StringUtil.isBlank(projectDto.getDepartmentCode())&&
                !Constant.BlocInfo.BLOCCODE.equals(projectDto.getCreateCompanyCode())) {
            //根据归属公司id和部门id查询当前id是否存在
            List<Dept> deptList = deptService.findDeptListByCompanyId(projectDto.getCompanyId());
            for (Dept depts : deptList) {
                if (projectDto.getDepartmentCode().equals(depts.getCode())) {
                    dept = depts;
                    break;
                }
            }
            if (dept == null) {
                message = ResultCode.PROJECT_DEPARTMENT_NOEXIST.message;
                return message;
            }
            projectDto.setDepartmentId(dept.getId());

        }
        return message;
    }

    /**
     * @return java.lang.String
     * @description: 校验创建公司
     * @Param [projectDto]
     * @author LuoY
     * @date 2019/8/22 13:59
     */
    private String findCreateCompanyCode(ProjectDto projectDto,Byte isAdmin) {
        SysUserVo sysUserVo = UserUtils.getUserVo();
        String message = null;
        if (StringUtil.isNotEmpty(projectDto.getCreateCompanyCode())) {
            //根据公司编码加企业id查询创建公司是否存在
            CompanyDto companyDto = new CompanyDto();
            companyDto.setCode(projectDto.getCreateCompanyCode());
            companyDto.setRootEnterpriseId(sysUserVo.getRootEnterpriseId());
            Company company = null;
            try {
                if (Constant.BlocInfo.BLOCCODE.equals(companyDto.getCode())){
                    company = new Company();
                    company.setName(Constant.BlocInfo.BLOCNAME);
                    company.setId(Constant.BlocInfo.BLOCID);
                    company.setCode(Constant.BlocInfo.BLOCCODE);
                }else{
                    company = companyService.findCompanyByIdOrCodeOrName(companyDto);
                }
                if (company == null) {
                    message = ResultCode.PROJECT_COMPANY_NOEXIST.message;
                    return message;
                }
            } catch (Exception e) {
                message = ResultCode.PROJECT_COMPANY_NOEXIST.message;
                return message;
            }
            //策略控制校验
            Project project = new Project();
            project.setCompanyId(company.getId());
            ResultCode resultCode = projectService.projectMenuControl(project, Constant.Project.PROJECT_CRUD_METHOD_ADDPROJECT);
            if (resultCode != null) {
                message = resultCode.getMessage();
                return message;
            }

            //导入权限校验
            if(Constant.Is.NO.equals(isAdmin)){
                if (!ShiroUtils.hasPerm(Constant.MenuDefine.PROJECT_IMPORT,company.getId())){
                    message = ResultCode.PROJECT_COMPANY_USESHIRO.message;
                    return message;
                }
            }

            projectDto.setCompanyId(company.getId());
        }
        return message;
    }


    /**
     * @return java.lang.String
     * @Description 项目负责人校验
     * @Author LuoY
     * @Date 2019/7/5 10:57
     * @Param [projectDto]
     */
    private String checkPersonInCharge(ProjectDto projectDto) {
        String message = null;
        if (StringUtil.isNotEmpty(projectDto.getPersonInChargeCode())) {
            StaffDto staff1Dto = new StaffDto();
            staff1Dto.setCode(projectDto.getPersonInChargeCode());
            staff1Dto.setDeptId(projectDto.getDepartmentId());
            staff1Dto.setCompanyId(projectDto.getCompanyId());
            try {
                int userId = staffService.findStaffInfoByCode(staff1Dto);
                projectDto.setPersonInCharge(Long.valueOf(userId));
            } catch (Exception e) {
                message = ResultCode.PROJECT_PERSONINCHARG_NOEXIST.message;
            }
        }
        return message;
    }

    /**
     * @return java.lang.String
     * @Description 电话号码校验
     * @Author LuoY
     * @Date 2019/7/10 17:17
     * @Param [projectDto]
     */
    private String checkPhoneNum(ProjectDto projectDto) {
        String message = null;
        if (StringUtil.isNotEmpty(projectDto.getMobile())) {
            //匹配手机号1开头+十位数字
            if (!Pattern.matches(Constant.Project.REGEX_PHONE_NUMBER, projectDto.getMobile())) {
                message = ResultCode.PROJECT_ISPHONENUM_NOTPHONE.message;
            }
        }
        return message;
    }

    /**
     * @return java.lang.String
     * @description: 资料类型校验
     * @Param projectDto
     * @author LuoY
     * @date 2019/8/15 11:12
     */
    public String checkDataType(ProjectDto projectDto) {
        String message = null;
        if (!Constant.Number.ZERO.equals(projectDto.getDataType())) {
            if (Constant.Number.ZEROL.equals(projectDto.getCompanyId())) {
                //创建公司为集团的时候,创建公司为集团时，可用值为共享型和分配型
                if (Constant.dataType.PRIVATE.equals(projectDto.getDataType())) {
                    message = ResultCode.PROJECT_USECOMPANY_DATATYPEADMIN.message;
                }
            } else {
                //创建公司为公司时，可用值为私有型，默认为私有型；
                if (!Constant.dataType.PRIVATE.equals(projectDto.getDataType())) {
                    message = ResultCode.PROJECT_USECOMPANY_DATATYPEUSER.message;
                }
            }
        }
        if(StringUtil.isBlank(message)){
            //策略控制校验
            Project project = new Project();
            project.setCodeType(projectDto.getDataType());
            ResultCode resultCode = projectService.projectMenuControl(project, Constant.Project.PROJECT_CRUD_METHOD_ADDPROJECT);
            if (resultCode != null) {
                message = resultCode.getMessage();
                return message;
            }
        }

        return message;
    }

    /**
     * @return java.lang.Integer
     * @description: 添加项目使用公司
     * @Param [project]
     * @author LuoY
     * @date 2019/8/22 16:19
     */
    private Integer insertIntoProjectUseCompany(Project project) {
        SysUserVo sysUserVo = UserUtils.getUserVo();
        ProjectUseCompanyDto projectUseCompanyDto = new ProjectUseCompanyDto();
        projectUseCompanyDto.setProjectId(project.getId());
        projectUseCompanyDto.setUseCompanyId(project.getCompanyId());
        projectUseCompanyDto.setIsDel(Constant.Is.NO);
        projectUseCompanyDto.setCreateTime(new Date());
        projectUseCompanyDto.setCreatorId(sysUserVo.getUserId());
        projectUseCompanyDto.setCreatorName(sysUserVo.getName());
        return projectUseCompanyMapper.insert(projectUseCompanyDto);
    }
}
