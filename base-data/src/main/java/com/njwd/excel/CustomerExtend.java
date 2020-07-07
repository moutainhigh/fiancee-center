package com.njwd.excel;

import com.njwd.annotation.ExcelExtend;
import com.njwd.basedata.service.CustomerSupplierService;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.Company;
import com.njwd.entity.basedata.dto.CompanyDto;
import com.njwd.entity.basedata.dto.CustomerSupplierDto;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.fileexcel.check.CheckContext;
import com.njwd.fileexcel.check.CheckResult;
import com.njwd.fileexcel.extend.AddExtend;
import com.njwd.fileexcel.extend.CheckExtend;
import com.njwd.fileexcel.extend.CheckHandler;
import com.njwd.financeback.service.CompanyService;
import com.njwd.utils.StringUtil;
import com.njwd.utils.UserUtils;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Description 导入处理实现类
 * @Date 2019/7/16 9:32
 * @Author 朱小明
 */
@Component
@Log
@ExcelExtend(type = "customer")
public class CustomerExtend implements AddExtend<CustomerSupplierDto>, CheckExtend {

    @Resource
    private CustomerSupplierService customerSupplierService;
    @Resource
    private CompanyService companyService;

    /**
     * 批量录入
     *
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int addBatch(List<CustomerSupplierDto> customerSupplierDtos) {
        int result = 0;
        try {
            for (CustomerSupplierDto dto : customerSupplierDtos) {
                setCommand(dto);
                Long id = customerSupplierService.addCustomerSupplier(dto);
                if (id != null) {
                    result++;
                }
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new ServiceException(ResultCode.OPERATION_FAILURE);
        }
        return result;
    }

    /**
     * 逐条录入
     *
     * @param data
     * @return
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int add(CustomerSupplierDto data) {
        setCommand(data);
        Long id = customerSupplierService.addCustomerSupplier(data);
        if (id == null) {
            return 0;
        }
        return 1;
    }

    /**
     * @return void
     * @Description 补足参数
     * @Author 朱小明
     * @Date 2019/7/17 9:41
     * @Param [customerSupplierDto]
     **/
    private void setCommand(CustomerSupplierDto customerSupplierDto) {
        customerSupplierDto.setDataType(Constant.CustomerSupplier.CUSTOMER);
        customerSupplierDto.setIsInternalCustomer(Constant.CustomerSupplier.IS_INTERNAL_NO.toString());
        customerSupplierDto.setIsEnterpriseAdmin(Constant.CustomerSupplier.PERSONAL);
    }

    @Override
    public void check(CheckContext checkContext) {
        //校验
        Byte isEnterpriseAdmin = checkContext.getByteValue("isEnterpriseAdmin");
        checkContext.addSheetHandler("companyName", getCompanyNameHandler(isEnterpriseAdmin)).
                addSheetHandler("customerType", getTypeHandler()).
                addSheetHandler("dataTypes", getDataTypesHandler()).
                addSheetHandler("unifiedSocialCreditCode", getCreditCodeHandler()).
                addSheetHandler("idCardNum", getIdCardNumHandler()).
                addSheetHandler("contactNumber", getContactNumberHandler());
    }

    /**
     * 校验公司名称
     *
     * @return
     */
    private CheckHandler<CustomerSupplierDto> getCompanyNameHandler(Byte isEnterpriseAdmin) {
        return (data) -> {
            String message = checkCompanyByName(data, isEnterpriseAdmin);
            if (StringUtil.isNotEmpty(message)) {
                return CheckResult.error(message);
            }
            return CheckResult.ok();
        };
    }

    /**
     * 根据类型校验字段
     *
     * @return
     */
    private CheckHandler<CustomerSupplierDto> getTypeHandler() {
        return (data) -> {
            if (Constant.CustomerSupplier.ENTERPRISE.toString().equals(data.getCustomerType())) {
                data.setIdCardNum(null);
            } else if (Constant.CustomerSupplier.PERSONAL.toString().equals(data.getCustomerType())) {
                data.setUnifiedSocialCreditCode(null);
            }
            return CheckResult.ok();
        };
    }

    /**
     * 校验资料类型
     *
     * @return
     */
    private CheckHandler<CustomerSupplierDto> getDataTypesHandler() {
        return (data) -> {
            String message = "";
            if (Constant.BlocInfo.BLOCNAME.equals(data.getCompanyName())) {
                if (!Constant.dataType.SHRETYPE.equals(data.getDataTypes()) && !Constant.dataType.DISTRIBUTION.equals(data.getDataTypes())) {
                    message = ResultCode.ONLY_SHARE_AND_DISTRIBUTION.message;
                }
            } else {
                if (!Constant.dataType.PRIVATE.equals(data.getDataTypes())) {
                    message = ResultCode.ONLY_PRIVATE.message;
                }
            }
            if (StringUtil.isNotEmpty(message)) {
                return CheckResult.error(message);
            }
            return CheckResult.ok();
        };
    }

    /**
     * 统一社会信用代码规则校验
     *
     * @return
     */
    private CheckHandler<CustomerSupplierDto> getCreditCodeHandler() {
        return (data) -> {
            if (StringUtil.isNotEmpty(data.getUnifiedSocialCreditCode())) {
                Boolean flag = Pattern.matches(Constant.CustomerSupplier.REGEX_CREDIT_CODE, data.getUnifiedSocialCreditCode());
                if (!flag) {
                    return CheckResult.error(ResultCode.INVAILD_CREDIT_CODE.message);
                }
            }
            return CheckResult.ok();
        };
    }

    /**
     * 身份证号规则校验
     *
     * @return
     */
    private CheckHandler<CustomerSupplierDto> getIdCardNumHandler() {
        return (data) -> {
            if (StringUtil.isNotEmpty(data.getIdCardNum())) {
                Boolean flag = Pattern.matches(Constant.Staff.REGEX_ID_CARD_NUMBER, data.getIdCardNum());
                if (!flag) {
                    return CheckResult.error(ResultCode.INVAILD_ID_CARD_NUM.message);
                }
            }
            return CheckResult.ok();
        };
    }

    /**
     * 手机号规则校验
     *
     * @return
     */
    private CheckHandler<CustomerSupplierDto> getContactNumberHandler() {
        return (data) -> {
            if (StringUtil.isNotEmpty(data.getContactNumber())) {
                Boolean flag = Pattern.matches(Constant.Project.REGEX_PHONE_NUMBER, data.getContactNumber());
                if (!flag) {
                    return CheckResult.error(ResultCode.INVAILD_CONTACT_NUMBER.message);
                }
            }
            return CheckResult.ok();
        };
    }

    private String checkCompanyByName(CustomerSupplierDto dto, Byte isEnterpriseAdmin) {
        SysUserVo sysUserVo = UserUtils.getUserVo();
        String message = null;
        if (StringUtil.isNotEmpty(dto.getCompanyName())) {
            if (Constant.Is.NO.equals(isEnterpriseAdmin) && Constant.BlocInfo.BLOCNAME.equals(dto.getCompanyName())) {
                //user端,不能上传集团数据
                message = ResultCode.UPLOAD_BLOC_ERROR.message;
                return message;
            }
            //根据公司编码加企业id查询创建公司是否存在
            CompanyDto companyDto = new CompanyDto();
            companyDto.setName(dto.getCompanyName());
            companyDto.setRootEnterpriseId(sysUserVo.getRootEnterpriseId());
            try {
                if (Constant.BlocInfo.BLOCNAME.equals(dto.getCompanyName())) {
                    dto.setCompanyId(Constant.BlocInfo.BLOCID);
                } else {
                    Company company = companyService.findCompanyByIdOrCodeOrName(companyDto);
                    if (company.getId() == null) {
                        message = ResultCode.COMPANY_NAME_NOT_EXIST.message;
                        return message;
                    }
                    dto.setCompanyId(company.getId());
                }
            } catch (Exception e) {
                message = ResultCode.COMPANY_NAME_NOT_EXIST.message;
                return message;
            }
        }
        return message;
    }

}
