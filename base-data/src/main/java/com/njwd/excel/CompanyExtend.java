package com.njwd.excel;

import com.njwd.annotation.ExcelExtend;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.*;
import com.njwd.entity.basedata.dto.CompanyDto;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.dto.AccountBookCategoryDto;
import com.njwd.fileexcel.check.CheckContext;
import com.njwd.fileexcel.check.CheckResult;
import com.njwd.fileexcel.extend.AddExtend;
import com.njwd.fileexcel.extend.CheckExtend;
import com.njwd.fileexcel.extend.CheckHandler;
import com.njwd.financeback.mapper.BusinessUnitMapper;
import com.njwd.financeback.mapper.CompanyMapper;
import com.njwd.financeback.service.AccountStandardService;
import com.njwd.financeback.service.BusinessUnitService;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/11 15:10
 */
@Component
@ExcelExtend(type = "company")
public class CompanyExtend implements AddExtend<CompanyDto>, CheckExtend {

    @Resource
    private CompanyMapper companyMapper;
    @Resource
    private BusinessUnitMapper businessUnitMapper;
    @Resource
    private BusinessUnitService businessUnitService;
    @Resource
    private AccountStandardService accountStandardService;

    @Override
    public int addBatch(List<CompanyDto> datas) {
        SysUserVo sysUserVo = UserUtils.getUserVo();
        for(CompanyDto companyDto:datas){
            companyDto.setCreatorId(sysUserVo.getUserId());
            companyDto.setCreatorName(sysUserVo.getName());
            companyDto.setRootEnterpriseId(sysUserVo.getRootEnterpriseId());
        }
        companyMapper.addBatch(datas);
        List<BusinessUnit> businessUnits = new ArrayList<>();
        for(CompanyDto companyDto:datas){
            businessUnits.add(getBusinessUnit(companyDto));
        }
        return businessUnitMapper.addBatch(businessUnits);
    }

    @Override
    public int add(CompanyDto data) {
        companyMapper.insert(data);
        return businessUnitMapper.insert(getBusinessUnit(data));
    }

    private BusinessUnit getBusinessUnit(CompanyDto company){
        BusinessUnit businessUnit = new BusinessUnit();
        businessUnit.setCompanyId(company.getId());
        businessUnitService.generateCode(businessUnit,company.getCode());
        businessUnit.setName(company.getName());
        businessUnit.setUnitFormId(company.getCompanyFormId());
        businessUnit.setUnitFormName(company.getCompanyFormName());
        businessUnit.setCreatorId(company.getCreatorId());
        businessUnit.setCreatorName(company.getCreatorName());
        businessUnit.setRootEnterpriseId(company.getRootEnterpriseId());

        if(Constant.Is.YES.equals(company.getHasSubAccount())){
            businessUnit.setIsAccountEntity(Constant.Is.YES);
        }else{
            businessUnit.setIsAccountEntity(Constant.Is.NO);
        }
        return businessUnit;
    }

    @Override
    public void check(CheckContext checkContext) {
        SysUserVo sysUserVo = UserUtils.getUserVo();
        AccountBookCategoryDto accountBookCategoryDto = new AccountBookCategoryDto();
        accountBookCategoryDto.setAccountTypeId(Constant.AccountBookType.ACCOUNT_BOOK_ID);
        accountBookCategoryDto.setRootEnterpriseId(sysUserVo.getRootEnterpriseId());
        List<AccountStandard> accountStandardList = accountStandardService.accountStandardList(accountBookCategoryDto);
        checkContext.addSheetHandler("accountingStandardName",getAccountingStandardName(accountStandardList));
        checkContext.addSheetHandler("taxSystemName",getTaxSystemName(accountStandardList));
        checkContext.addSheetHandler("accountingCurrencyName",getAccountingCurrencyName(accountStandardList));
    }

    private CheckHandler<CompanyDto> getAccountingStandardName(List<AccountStandard> accountStandardList){
        return data -> {
            if(accountStandardList==null||accountStandardList.isEmpty())
                return CheckResult.error("租户会计准则不存在");
            boolean isExist = false;
            for(AccountStandard accountStandard:accountStandardList){
                if(accountStandard.getAccStandardName().equals(data.getAccountingStandardName())){
                    data.setAccountingStandardId(accountStandard.getAccStandardId());
                    isExist = true;
                    break;
                }
            }
            if(!isExist)
                return CheckResult.error("会计准则不在取值范围内");
            return CheckResult.ok();
        };
    }

    private CheckHandler<CompanyDto> getTaxSystemName(List<AccountStandard> accountStandardList){
        return data -> {
            if(accountStandardList==null||accountStandardList.isEmpty())
                return CheckResult.error("租户会计准则不存在");
            boolean isExist = false;
            for(AccountStandard accountStandard:accountStandardList){
                if(accountStandard.getAccStandardName().equals(data.getAccountingStandardName())){
                    if(accountStandard.getTaxSystems()==null||accountStandard.getTaxSystems().isEmpty()){
                        return CheckResult.error("租户税制不存在");
                    }
                    for(TaxSystem taxSystem:accountStandard.getTaxSystems()){
                        if(taxSystem.getTaxSystemName().equals(data.getTaxSystemName())){
                            data.setTaxSystemId(taxSystem.getTaxSystemId());
                            isExist = true;
                            break;
                        }
                    }
                }
            }
            if(!isExist)
                return CheckResult.error("税制不在取值范围内");
            return CheckResult.ok();
        };
    }

    private CheckHandler<CompanyDto> getAccountingCurrencyName(List<AccountStandard> accountStandardList){
        return data -> {
            if(accountStandardList==null||accountStandardList.isEmpty())
                return CheckResult.error("租户会计准则不存在");
            boolean isExist = false;
            for(AccountStandard accountStandard:accountStandardList){
                if(accountStandard.getAccStandardName().equals(data.getAccountingStandardName())){
                    if(accountStandard.getCurrencys()==null||accountStandard.getCurrencys().isEmpty()){
                        return CheckResult.error("租户记账本位币不存在");
                    }
                    for(Currency currency:accountStandard.getCurrencys()){
                        if(currency.getCurrencyName().equals(data.getAccountingCurrencyName())){
                            data.setAccountingCurrencyId(currency.getCurrencyId());
                            isExist = true;
                            break;
                        }
                    }
                }
            }
            if(!isExist)
                return CheckResult.error("税制不在取值范围内");
            return CheckResult.ok();
        };
    }


}
