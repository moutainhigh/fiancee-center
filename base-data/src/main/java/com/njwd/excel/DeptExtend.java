package com.njwd.excel;

import com.njwd.annotation.ExcelExtend;
import com.njwd.basedata.service.DeptService;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.BusinessUnit;
import com.njwd.entity.basedata.ReferenceResult;
import com.njwd.entity.basedata.dto.BusinessUnitDto;
import com.njwd.entity.basedata.dto.CompanyDto;
import com.njwd.entity.basedata.dto.DeptDto;
import com.njwd.entity.basedata.vo.CompanyVo;
import com.njwd.entity.basedata.vo.DeptVo;
import com.njwd.entity.platform.dto.SysAuxDataDto;
import com.njwd.exception.ResultCode;
import com.njwd.fileexcel.check.CheckContext;
import com.njwd.fileexcel.check.CheckResult;
import com.njwd.fileexcel.extend.AddExtend;
import com.njwd.fileexcel.extend.CheckExtend;
import com.njwd.fileexcel.extend.CheckHandler;
import com.njwd.financeback.service.BusinessUnitService;
import com.njwd.financeback.service.CompanyService;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.ShiroUtils;
import com.njwd.utils.StringUtil;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jds
 * @Description 部门导入
 * @create 2019/7/5 15:42
 */
@Component
@ExcelExtend(type = Constant.Reference.DEPT)
public class DeptExtend implements AddExtend<DeptDto>, CheckExtend {
    @Resource
    private CompanyService companyService;

    @Resource
    private DeptService deptService;

    @Resource
    private BusinessUnitService businessUnitService;

    @Resource
    private ReferenceRelationService referenceRelationService;

    /**
     * @Description 导入
     * @Author jds
     * @Date 2019/7/5 16:14
     * @Param [data]
     * @return int
     **/
    @Override
    public int addBatch(List<DeptDto> data) {
        //添加
        List<DeptDto> deptList = new ArrayList<>();
        int result=0;
        deptList.addAll(data);
        result=deptService.addDeptBatch(deptList);
        return result;
    }

    /**
     * @Description 导入
     * @Author jds
     * @Date 2019/7/5 16:13
     * @Param [data]
     * @return int
     **/
    @Override
    public int add(DeptDto deptDto) {
        //添加
        //getDeptDto(deptDto);
        return deptService.add(deptDto);
    }

    /**
     * @Description 校验
     * @Author jds
     * @Date 2019/7/5 18:10
     * @Param [checkContext]
     * @return void
     **/
    @Override
    public void check(CheckContext checkContext) {
        List<DeptDto> deptDtoList=new ArrayList<>();
        Byte isEnterpriseAdmin = checkContext.getByteValue("isEnterpriseAdmin");
        checkContext.addSheetHandler("companyCode", getCompanyCodeCheckHandler(isEnterpriseAdmin))
                    .addSheetHandler("attrBusinessUnitCode", getAttrBusinessUnitCodeCheckHandler())
                    .addSheetHandler("prarentCode", getPrarentCodeCheckHandler())
                    .addSheetHandler("name", getNameCheckHandler(deptDtoList));
    }


    /**
     * @Description 校验创建公司编码
     * @Author jds
     * @Date 2019/7/5 16:38
     * @Param []
     * @return com.njwd.financeback.service.fileexcel.extend.CheckHandler<com.njwd.basedata.entity.dto.DeptDto>
     **/

    private CheckHandler<DeptDto> getCompanyCodeCheckHandler(Byte isEnterpriseAdmin){
        return data -> {
            Long enterpriseId = UserUtils.getUserVo().getRootEnterpriseId();
            CompanyDto companyDto = new CompanyDto();
            companyDto.setCode(data.getCompanyCode());
            companyDto.setRootEnterpriseId(enterpriseId);
            CompanyVo company = companyService.findCompanyByIdOrCodeOrName(companyDto);
            if(company==null || company.getId()==null ) {
                return CheckResult.error(ResultCode.COMPANY_NOT_EXIST_OR_DISABLE.message);
            }
            data.setCompanyId(company.getId());
            data.setRootEnterpriseId(enterpriseId);
            //权限判断
            if(Constant.Is.NO.equals(isEnterpriseAdmin)){
                if (!ShiroUtils.hasPerm(Constant.MenuDefine.DEPT_IMPORT, data.getCompanyId())) {
                    return CheckResult.error(ResultCode.PERMISSION_NOT.message);
                }
            }
            return CheckResult.ok();
        };
    }


    /**
     * @Description 校验创建公司业务单元编码
     * @Author jds
     * @Date 2019/7/5 16:38
     * @Param []
     * @return com.njwd.financeback.service.fileexcel.extend.CheckHandler<com.njwd.basedata.entity.dto.DeptDto>
     **/

    private CheckHandler<DeptDto> getAttrBusinessUnitCodeCheckHandler(){
        return data -> {
            BusinessUnitDto businessUnit=new BusinessUnitDto();
            BusinessUnit result=new BusinessUnit();
            businessUnit.setName(data.getAttrBusinessUnitName());
            businessUnit.setCompanyId(data.getCompanyId());
            businessUnit.setCode(data.getAttrBusinessUnitCode());
            result=businessUnitService.findBusinessByCode(businessUnit);
            if( result==null|| result.getId()==null){
                return CheckResult.error(ResultCode.ATTR_BUSINESS_UNIT_NO.message);
            }
            data.setAttrBusinessUnitId(result.getId());
            data.setBusinessUnitId(result.getId());
            data.setUseCompanyId(data.getCompanyId());
            //设置编码  默认系统编码
            data.setCodeType(Constant.Is.NO);
            return CheckResult.ok();
        };
    }

    /**
     * @Description 校验上级部门编码
     * @Author jds
     * @Date 2019/7/5 16:38
     * @Param []
     * @return com.njwd.financeback.service.fileexcel.extend.CheckHandler<com.njwd.basedata.entity.dto.DeptDto>
     **/

    private CheckHandler<DeptDto> getPrarentCodeCheckHandler(){
        return data -> {
            //根据上级编码查询上级部门
            if(data.getPrarentCode()!=null && !data.getPrarentCode().isEmpty()){
                DeptDto deptDto=new DeptDto();
                deptDto.setCode(data.getPrarentCode());
                deptDto.setRootEnterpriseId(data.getRootEnterpriseId());
                DeptVo deptVo=deptService.findByCode(deptDto);
                if( deptVo==null|| deptVo.getId()==null){
                    return CheckResult.error(ResultCode.PARENT_DEPT_NO_OR_DISABLE.message);
                }
                data.setPrarentLevel(deptVo.getDeptLevel());
                data.setPrarentId(deptVo.getId());
                //判断上级否被引用
                ReferenceResult reference = referenceRelationService.isReference(Constant.Reference.DEPT, deptVo.getId());
                if(!reference.isReference()){
                    DeptDto deptParent = new DeptDto();
                    FastUtils.copyProperties(deptVo, deptParent);
                    List<Long>pId=new ArrayList<>();
                    pId.add(deptParent.getId());
                    deptParent.setIdList(pId);
                    //查询未引用的部门变更历史数量
                    List<DeptVo>deptVos=deptService.findChangeCount(deptParent);
                    if(deptVos.get(Constant.Number.ZERO).getChangeCount()>Constant.Number.ONE){
                        //变更历史多于1  设定为已经被引用
                        reference.setReference(true);
                        reference.setReferenceDescription(ResultCode.IS_USED_BY_DEPT.message);
                    }
                }
                if(reference.isReference()){
                    return CheckResult.error(ResultCode.PARENT_DEPT_IS_USED.message);
                }
                if(Constant.Level.ONE.equals(deptVo.getDeptLevel())){
                    data.setDeptLevel(Constant.Level.TWO);
                }else if(Constant.Level.TWO.equals(deptVo.getDeptLevel())){
                    data.setDeptLevel(Constant.Level.THREE);
                }else if(Constant.Level.THREE.equals(deptVo.getDeptLevel())){
                    data.setDeptLevel(Constant.Level.FOUR);
                }else if(Constant.Level.FOUR.equals(deptVo.getDeptLevel())){
                    return CheckResult.error(ResultCode.MAX_LEVEL.message);
                }
            }else {
                data.setDeptLevel(Constant.Level.ONE);
            }
            return CheckResult.ok();
        };
    }


    /**
     * @Description 校验部门名称
     * @Author jds
     * @Date 2019/7/5 16:38
     * @Param []
     * @return com.njwd.financeback.service.fileexcel.extend.CheckHandler<com.njwd.basedata.entity.dto.DeptDto>
     **/

    private CheckHandler<DeptDto> getNameCheckHandler(List<DeptDto> deptDtoList){
        return data -> {
            if(deptDtoList.size()>Constant.Number.ZERO){
                for (DeptDto dept:deptDtoList){
                    if(dept.getCompanyId().equals(data.getCompanyId()) && dept.getDeptLevel().equals(data.getDeptLevel())&& dept.getName().equals(data.getName()) ){
                        return CheckResult.error(ResultCode.DEPT_NAME_REPEAT.message);
                    }
                }
            }
            deptDtoList.add(data);
            //空格验证
            if (StringUtil.regMatch(data.getName(), Constant.Regex.HAS_SPACE, false)) {
                return CheckResult.error(ResultCode.NAME_FORMAL_ERROR.message);
            }
            //特殊字符验证
            if (StringUtil.regMatch(data.getName(), Constant.Regex.HAS_SPECIAL, false)) {
                return CheckResult.error(ResultCode.NAME_FORMAL_ERROR.message);
            }
            Integer na = deptService.checkName(data);
            if(!Constant.Number.ZERO.equals(na)){
                return CheckResult.error(ResultCode.NAME_EXIST.message);
            }
            return CheckResult.ok();
        };
    }



    /**
     * @Description 验证属性是否存在
     * @Author jds
     * @Date 2019/7/7 14:01
     * @Param []
     * @return com.njwd.financeback.service.fileexcel.extend.CheckHandler<com.njwd.basedata.entity.dto.DeptDto>
     **/
    private CheckHandler<DeptDto> getTypeNameCheckHandler(){
        return data -> {
            SysAuxDataDto sysAuxDataDto=new SysAuxDataDto();
            sysAuxDataDto.setName(data.getDeptTypeName());
            sysAuxDataDto.setType(Constant.PropertyName.DEPT_TYPE);
            Result<Long> type=deptService.findDpetTypeByName(sysAuxDataDto);
           if(type == null){
               return CheckResult.error("部门属性不存在！");
           }
           data.setDeptType(type.getData());
            return CheckResult.ok();
        };
    }

    /**
     * @Description 获取所有信息
     * @Author jds
     * @Date 2019/7/7 14:49
     * @Param [deptDto]
     * @return void
     **/
    public void getDeptDto(DeptDto deptDto){
        Long enterpriseId = UserUtils.getUserVo().getRootEnterpriseId();
        //设置归属公司
        CompanyDto companyDto = new CompanyDto();
        companyDto.setCode(deptDto.getCompanyName());
        companyDto.setRootEnterpriseId(enterpriseId);
        CompanyVo company = companyService.findCompanyByIdOrCodeOrName(companyDto);
        deptDto.setCompanyId(company.getId());
        //设置使用公司
        companyDto.setCode(deptDto.getUseCompanyName());
        companyDto.setRootEnterpriseId(enterpriseId);
        CompanyVo useCompany = companyService.findCompanyByIdOrCodeOrName(companyDto);
        deptDto.setUseCompanyId(useCompany.getId());
        BusinessUnitDto businessUnitParam=new BusinessUnitDto();
        businessUnitParam.setRootEnterpriseId(enterpriseId);
        //设置归属公司 业务单元
        businessUnitParam.setCompanyId(company.getId());
        businessUnitParam.setName(deptDto.getAttrBusinessUnitName());
        BusinessUnit attrBusinessUnit=businessUnitService.findBusinessUnitByName(businessUnitParam);
        deptDto.setAttrBusinessUnitId(attrBusinessUnit.getId());
        //设置使用公司 业务单元
        businessUnitParam.setCompanyId(useCompany.getId());
        businessUnitParam.setName(deptDto.getBusinessUnitName());
        BusinessUnit businessUnit=businessUnitService.findBusinessUnitByName(businessUnitParam);
        deptDto.setBusinessUnitId(businessUnit.getId());
        //设置属性
        SysAuxDataDto sysAuxDataDto=new SysAuxDataDto();
        sysAuxDataDto.setCodeOrName(deptDto.getDeptTypeName());
        sysAuxDataDto.setType(Constant.PropertyName.DEPT_TYPE);
        Result<Long> type=deptService.findDpetTypeByName(sysAuxDataDto);
        deptDto.setDeptType(type.getData());
        //设置编码  默认系统编码
        deptDto.setCodeType(Constant.Is.NO);

    }

}

