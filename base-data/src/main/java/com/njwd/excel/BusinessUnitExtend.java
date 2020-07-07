package com.njwd.excel;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.njwd.annotation.ExcelExtend;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.BusinessUnit;
import com.njwd.entity.basedata.dto.BusinessUnitDto;
import com.njwd.entity.basedata.dto.CompanyDto;
import com.njwd.entity.basedata.vo.CompanyVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.fileexcel.check.CheckContext;
import com.njwd.fileexcel.check.CheckResult;
import com.njwd.fileexcel.extend.AddExtend;
import com.njwd.fileexcel.extend.CheckExtend;
import com.njwd.fileexcel.extend.CheckHandler;
import com.njwd.financeback.mapper.BusinessUnitMapper;
import com.njwd.financeback.service.BusinessUnitService;
import com.njwd.financeback.service.CompanyService;
import com.njwd.utils.FastUtils;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/11 10:44
 */
@Component
@ExcelExtend(type = "business_unit")
public class BusinessUnitExtend implements AddExtend<BusinessUnitDto>, CheckExtend {

    @Resource
    private BusinessUnitMapper businessUnitMapper;
    @Resource
    private BusinessUnitService businessUnitService;
    @Resource
    private CompanyService companyService;


    @Override
    public void check(CheckContext checkContext) {
        Long userId = checkContext.getLongValue("userId");
        checkContext.addSheetHandler("name",findNameCheckHandler(userId));
    }

    /**
     * 扩展名称的校验
     * @return
     */
    private CheckHandler<BusinessUnitDto> findNameCheckHandler(Long userId){
        return (data)->{
            //System.err.println("userId:"+userId);
            SysUserVo sysUserVo = UserUtils.getUserVo();
            Integer count = businessUnitMapper.selectCount(new LambdaQueryWrapper<BusinessUnit>().
                    eq(BusinessUnit::getName,data.getName()).
                    eq(BusinessUnit::getCompanyId,data.getCompanyId()).
                    eq(BusinessUnit::getRootEnterpriseId,sysUserVo.getRootEnterpriseId()).
                    eq(BusinessUnit::getIsDel, Constant.Number.ZERO).
                    eq(BusinessUnit::getIsEnable,Constant.Number.ONE));
            if(count!=null&&count>0) {
                return CheckResult.error("数据有重复");
            }
            return CheckResult.ok();
        };
    }

    @Override
    public int addBatch(List<BusinessUnitDto> datas) {
        SysUserVo sysUserVo = UserUtils.getUserVo();
        List<BusinessUnit> businessUnits = new ArrayList<>();
        for(BusinessUnitDto businessUnitDto:datas){
            businessUnitDto.setCreatorId(sysUserVo.getUserId());
            businessUnitDto.setCreatorName(sysUserVo.getName());
            businessUnitDto.setRootEnterpriseId(sysUserVo.getRootEnterpriseId());
            CompanyDto companyDto= new CompanyDto();
            companyDto.setId(businessUnitDto.getCompanyId());
            CompanyVo companyVo = companyService.findCompanyByIdOrCodeOrName(companyDto);
            BusinessUnit businessUnit = businessUnitService.generateCode(businessUnitDto,companyVo.getCode());
            if(Constant.Is.YES.equals(companyVo.getHasSubAccount())){
                businessUnitDto.setIsAccountEntity(Constant.Is.YES);
            }
            businessUnitDto.setCode(businessUnit.getCode());
            FastUtils.copyProperties(businessUnitDto,businessUnit);
            businessUnits.add(businessUnit);
        }
        return businessUnitMapper.addBatch(businessUnits);
    }

    @Override
    public int add(BusinessUnitDto data) {
        return businessUnitMapper.insert(data);
    }
}
