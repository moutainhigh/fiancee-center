package com.njwd.excel;

import com.njwd.annotation.ExcelExtend;
import com.njwd.basedata.service.DeptService;
import com.njwd.basedata.service.SequenceService;
import com.njwd.basedata.service.StaffService;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.dto.CompanyDto;
import com.njwd.entity.basedata.dto.DeptDto;
import com.njwd.entity.basedata.dto.StaffDto;
import com.njwd.entity.basedata.vo.CompanyVo;
import com.njwd.entity.basedata.vo.DeptVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.fileexcel.check.CheckContext;
import com.njwd.fileexcel.check.CheckResult;
import com.njwd.fileexcel.extend.AddExtend;
import com.njwd.fileexcel.extend.CheckExtend;
import com.njwd.fileexcel.extend.CheckHandler;
import com.njwd.financeback.service.CompanyService;
import com.njwd.utils.ShiroUtils;
import com.njwd.utils.StringUtil;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Description
 * @Author: ZhuHC
 * @Date: 2019/7/2 9:33
 */
@Component
@ExcelExtend(type = "staff")
public class StaffExtend implements AddExtend<StaffDto>, CheckExtend {

    @Resource
    private CompanyService companyService;

    @Resource
    private DeptService deptService;

    @Resource
    private StaffService staffService;

    @Resource
    private SequenceService sequenceService;

    @Override
    public void check(CheckContext checkContext) {
        Byte isEnterpriseAdmin = checkContext.getByteValue("isEnterpriseAdmin");
        //通用规则校验通过后，会校验自定义的规则
        checkContext.addSheetHandler("companyCode", getCompanyCodeCheckHandler(isEnterpriseAdmin))
                    .addSheetHandler("deptCode", getDeptCodeCheckHandler())
                    .addSheetHandler("name", getNameCheckHandler())
                    .addSheetHandler("contactNumber", getContactNumberCheckHandler())
                    .addSheetHandler("bankAccount", getBankAccountCheckHandler())
                    .addSheetHandler("email", getEmailCheckHandler())
                    .addSheetHandler("idCardNum", getIdCardNumCheckHandler());
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/21 11:11
     * @Param []
     * @return com.njwd.fileexcel.extend.CheckHandler<com.njwd.entity.basedata.dto.StaffDto>
     * @Description 检验手机号
     */
    private CheckHandler<StaffDto> getContactNumberCheckHandler() {
        return data -> {
            if (StringUtil.isNotEmpty(data.getContactNumber())) {
                //匹配手机号1开头+十位数字
                if (!Pattern.matches(Constant.Project.REGEX_PHONE_NUMBER, data.getContactNumber())) {
                    return CheckResult.error(ResultCode.INVAILD_CONTACT_NUMBER.message);
                }
                StaffDto staffDto = new StaffDto();
                staffDto.setContactNumber(data.getContactNumber());
                Integer num = staffService.findStaffInfoByNumberOrCard(staffDto);
                if(0 != num){
                    return CheckResult.error(ResultCode.EXIST_CONTACT_NUMBER.message);
                }
            }
            //校验成功
            return CheckResult.ok();
        };
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/21 11:11
     * @Param []
     * @return com.njwd.fileexcel.extend.CheckHandler<com.njwd.entity.basedata.dto.StaffDto>
     * @Description 检验银行卡号
     */
    private CheckHandler<StaffDto> getBankAccountCheckHandler() {
        return data -> {
            String bankAccount = data.getBankAccount();
            if (StringUtil.isNotEmpty(bankAccount)) {
                StaffDto staffDto = new StaffDto();
                staffDto.setBankAccount(bankAccount);
                Integer num = staffService.findStaffInfoByNumberOrCard(staffDto);
                if(0 != num){
                    return CheckResult.error(ResultCode.EXIST_BANK_ACCOUNT.message);
                }
            }
            //校验成功
            return CheckResult.ok();
        };
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/21 11:11
     * @Param []
     * @return com.njwd.fileexcel.extend.CheckHandler<com.njwd.entity.basedata.dto.StaffDto>
     * @Description 检验邮箱
     */
    private CheckHandler<StaffDto> getEmailCheckHandler() {
        return data -> {
            if (StringUtil.isNotEmpty(data.getEmail())) {
                //邮箱
                if (!Pattern.matches(Constant.Staff.REGEX_EMAIL, data.getEmail())) {
                    return CheckResult.error(ResultCode.INVAILD_EMAIL.message);
                }
            }
            //校验成功
            return CheckResult.ok();
        };
    }

    /**
     * 校验身份证号是否符合规则
     * @return
     */
    private CheckHandler<StaffDto> getIdCardNumCheckHandler() {
        return data -> {
            if (StringUtil.isNotEmpty(data.getIdCardNum())) {
                if (!Pattern.matches(Constant.Staff.REGEX_ID_CARD_NUMBER, data.getIdCardNum())) {
                    return CheckResult.error(ResultCode.INVAILD_ID_CARD_NUMBER.message);
                }
            }
            return CheckResult.ok();
        };
    }

    /**
     * 校验部门是否存在
     * @return
     */
    private CheckHandler<StaffDto> getDeptCodeCheckHandler() {
        return data -> {
            DeptVo deptVo = getDeptId(data);
            if(null == deptVo || null == deptVo.getId()) {
                return CheckResult.error(ResultCode.DEPT_NOT_EXIST.message);
            }else{
                if(Constant.Is.NO.equals(deptVo.getIsEnd())){
                    return CheckResult.error(ResultCode.DEPT_IS_NOT_END.message);
                }
            }
            //校验成功
            return CheckResult.ok();
        };
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/22 14:44
     * @Param []
     * @return com.njwd.fileexcel.extend.CheckHandler<com.njwd.entity.basedata.dto.StaffDto>
     * @Description 公司编码校验 创建公司是否存在
     */
    private CheckHandler<StaffDto> getCompanyCodeCheckHandler(Byte isEnterpriseAdmin) {
        return data -> {
            Long enterpriseId = UserUtils.getUserVo().getRootEnterpriseId();
            CompanyDto companyDto = new CompanyDto();
            companyDto.setCode(data.getCompanyCode());
            companyDto.setRootEnterpriseId(enterpriseId);
            CompanyVo company = companyService.findCompanyByIdOrCodeOrName(companyDto);
            if(null == company || null == company.getId()) {
                return CheckResult.error(ResultCode.COMPANY_NOT_EXIST.message);
            }
            //权限判断
            if(Constant.Is.NO.equals(isEnterpriseAdmin)){
                if (!ShiroUtils.hasPerm(Constant.MenuDefine.STAFF_IMPORT, company.getId())) {
                    return CheckResult.error(ResultCode.PERMISSION_NOT.message);
                }
            }
            //校验成功
            return CheckResult.ok();
        };
    }

    /**
     * 校验员工姓名
     * @return
     */
    private CheckHandler<StaffDto> getNameCheckHandler() {
        return data -> {
            if (!Pattern.matches(Constant.Staff.REGEX_STAFF_NAME, data.getName())) {
                return CheckResult.error(ResultCode.INVAILD_STAFF_NAME.message);
            }
            //校验成功
            return CheckResult.ok();
        };
    }

    @Override
    public int addBatch(List<StaffDto> staffDtoList) {
        List<StaffDto> staffList = new ArrayList<>();
        int result;
        for(StaffDto staffDto : staffDtoList)
        {
            setStaffInfo(staffDto);
            //校验 该公司员工编码唯一性 部门内唯一
            checkStaffUniqueness(staffDto);
            staffList.add(staffDto);
        }
        result = staffService.addBatchStaff(staffList);
        return result;
    }

    private void checkStaffUniqueness(StaffDto staffDto) {
        Integer row = staffService.findStaffByCode(staffDto);
        if (row != 0) {
            throw new ServiceException(ResultCode.CODE_EXIST);
        }
    }

    @Override
    public int add(StaffDto staffDto) {
        setStaffInfo(staffDto);
        checkStaffUniqueness(staffDto);
        return staffService.insertStaff(staffDto);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/7/4 11:29
     * @Param [staffDto]
     * @return void
     * @Description 设置需要插入的信息
     */
    private void setStaffInfo(StaffDto staffDto) {
        //根据公司名称和企业ID查询公司编码
        Long enterpriseId = UserUtils.getUserVo().getRootEnterpriseId();
        CompanyDto companyDto = new CompanyDto();
        companyDto.setCode(staffDto.getCompanyCode());
        companyDto.setRootEnterpriseId(enterpriseId);
        CompanyVo companyVo = companyService.findCompanyByIdOrCodeOrName(companyDto);
        DeptVo deptVo = getDeptId(staffDto);
        //放入公司ID
        staffDto.setCompanyId(companyVo.getId());
        //生成员工编码
        staffDto.setCode(sequenceService.getCode(Constant.BaseCodeRule.LENGTH_THREE,staffDto.getCompanyId(),
                deptVo.getCode(),Constant.BaseCodeRule.COMPANY));
        staffDto.setCodeType(Constant.CodeType.SYSTEMCODE);
        staffDto.setDeptId(deptVo.getId());
        SysUserVo userVo = UserUtils.getUserVo();
        staffDto.setCreatorId(userVo.getUserId());
        staffDto.setCreatorName(userVo.getName());
        staffDto.setCreateTime(new Date());
        staffDto.setRootEnterpriseId(userVo.getRootEnterpriseId());
        staffDto.setCreateCompanyId(companyVo.getId());
        staffDto.setAttrBusinessUnitId(deptVo.getAttrBusinessUnitId());
        staffDto.setUseCompanyId(deptVo.getUseCompanyId());
        staffDto.setBusinessUnitId(deptVo.getBusinessUnitId());
        staffDto.setIsDel(Constant.Is.NO);
        staffDto.setIsEnable(Constant.Is.YES);
    }



    /**
     * @Author ZhuHC
     * @Date  2019/7/4 11:29
     * @Param [staffDto]
     * @return java.lang.String
     * @Description 获得对应部门
     */
    private DeptVo getDeptId(StaffDto staffDto) {
        Long enterpriseId = UserUtils.getUserVo().getRootEnterpriseId();
        DeptDto deptDto = new DeptDto();
        deptDto.setRootEnterpriseId(enterpriseId);
        deptDto.setCode(staffDto.getDeptCode());
        return deptService.findByCode(deptDto);
    }
}
