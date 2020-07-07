package com.njwd.excel;

import com.njwd.annotation.ExcelExtend;
import com.njwd.basedata.mapper.AccountingItemValueMapper;
import com.njwd.basedata.service.AccountingItemService;
import com.njwd.basedata.service.AccountingItemValueService;
import com.njwd.basedata.service.SequenceService;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.AccountingItemValue;
import com.njwd.entity.basedata.dto.AccountingItemDto;
import com.njwd.entity.basedata.dto.AccountingItemValueDto;
import com.njwd.entity.basedata.dto.CompanyDto;
import com.njwd.entity.basedata.vo.AccountingItemVo;
import com.njwd.entity.basedata.vo.CompanyVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.fileexcel.check.CheckContext;
import com.njwd.fileexcel.check.CheckResult;
import com.njwd.fileexcel.extend.AddExtend;
import com.njwd.fileexcel.extend.CheckExtend;
import com.njwd.fileexcel.extend.CheckHandler;
import com.njwd.financeback.service.CompanyService;
import com.njwd.utils.FastUtils;
import com.njwd.utils.ShiroUtils;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description 自定义核算项目大区值导入
 * @Date 2019/7/2 17:06
 * @Author 薛永利
 */
@Component
@ExcelExtend(type = "accounting_item_value")
public class AccountingItemValueExtend implements AddExtend<AccountingItemValueDto>, CheckExtend {

    @Resource
    private CompanyService companyService;
    @Resource
    private AccountingItemService accountingItemService;
    @Resource
    private AccountingItemValueService accountingItemValueService;
    @Resource
    private SequenceService sequenceService;
    @Resource
    private AccountingItemValueMapper accountingItemValueMapper;

    /**
     * @Description 自定义核算项目大区值批量导入
     * @Author 薛永利
     * @Date 2019/7/3 11:10
     * @Param [datas]
     * @return int
     */
    @Override
    public int addBatch(List<AccountingItemValueDto> datas) {
        List<AccountingItemValue> accountingItemValueList = new ArrayList<>();
        for (AccountingItemValueDto accountingItemValueDto : datas) {
            AccountingItemValue accountingItemValue = new AccountingItemValue();
            //设置默认值
            setOperatorInfo(accountingItemValueDto);
            FastUtils.copyProperties(accountingItemValueDto, accountingItemValue);
            //设置完整流水号 Z+两位流水号+公司编码+三位流水号
            accountingItemValue.setCode( sequenceService.getCode(
                    accountingItemValueDto.getItemCode(),
                    Constant.BaseCodeRule.LENGTH_THREE,
                    accountingItemValueDto.getCompanyId(),
                    accountingItemValueDto.getCompanyCode(),
                    Constant.BaseCodeRule.COMPANY
            ));
            //excel导入,codeType默认为0,系统自定义
            accountingItemValue.setCodeType(Constant.CodeType.SYSTEMCODE);
            accountingItemValueList.add(accountingItemValue);
        }
        //批量新增项目
        return accountingItemValueService.addBatchAccountingItemValue(accountingItemValueList);
    }

    /**
     * @Description 自定义核算项目大区值单行导入
     * @Author 薛永利
     * @Date 2019/7/3 11:10
     * @Param [data]
     * @return int
     */
    @Override
    public int add(AccountingItemValueDto data) {
        AccountingItemValue accountingItemValue = new AccountingItemValue();
        //设置默认值
        setOperatorInfo(data);
        FastUtils.copyProperties(data, accountingItemValue);
        //设置完整流水号 Z+两位流水号+公司编码+三位流水号
        accountingItemValue.setCode( sequenceService.getCode(
                data.getItemCode(),
                Constant.BaseCodeRule.LENGTH_THREE,
                data.getCompanyId(),
                data.getCompanyCode(),
                Constant.BaseCodeRule.COMPANY
        ));
        //excel导入,codeType默认为0,系统自定义
        accountingItemValue.setCodeType(Constant.CodeType.SYSTEMCODE);
        return accountingItemValueMapper.insert(accountingItemValue);
    }

    @Override
    public void check(CheckContext checkContext) {
        Byte isEnterpriseAdmin = checkContext.getByteValue("isEnterpriseAdmin");
        //通用规则校验通过后，会校验自定义的规则
        checkContext.addSheetHandler("companyCode", getCompanyCheckHandler(isEnterpriseAdmin));
        checkContext.addSheetHandler("name", getNameCheckHandler());
        checkContext.addSheetHandler("dataType", getDataTypeCheckHandler());
        //checkContext.addHandler("useCompanyId", getUseCompanyCheckHandler());
        checkContext.addSheetHandler("itemName", getItemNameCheckHandler());
    }
    /**
     * 校验资料类型和创建公司是否相符：集团为共享型，公司为私有型
     * @return
     */
    private CheckHandler<AccountingItemValueDto> getDataTypeCheckHandler() {
        return data -> {
            if( Constant.Character.GROUP_CODE.equals(data.getCompanyCode()) && data.getDataType().intValue() != Constant.Number.ONE) {
                return CheckResult.error(ResultCode.PROJECT_USECOMPANY_DATATYPEADMIN.message);
            }
            if( !Constant.Character.GROUP_CODE.equals(data.getCompanyCode()) && data.getDataType().intValue() != Constant.Number.LENGTH) {
                return CheckResult.error(ResultCode.PROJECT_USECOMPANY_DATATYPEUSER.message);
            }
            //校验成功
            return CheckResult.ok();
        };
    }
    /**
     * 校验租户内值名称是否重复
     * @return
     */
    private CheckHandler<AccountingItemValueDto> getNameCheckHandler() {
        return data -> {
            Long rootEnterpriseId = UserUtils.getUserVo().getRootEnterpriseId();
            data.setRootEnterpriseId(rootEnterpriseId);
            int row = accountingItemValueService.findAccountingItemValueByName(data);
            if(row >0) {
                return CheckResult.error(ResultCode.NAME_EXIST.message);
            }
            //校验成功
            return CheckResult.ok();
        };
    }
    /**
     * 校验归属公司是否存在
     * @return
     */
    private CheckHandler<AccountingItemValueDto> getCompanyCheckHandler(Byte isEnterpriseAdmin) {
        return data -> {
            CompanyVo companyVo = getCompanyVo(data);
            if(companyVo == null) {
                return CheckResult.error(ResultCode.ACCOUNTING_ITEM_COMPANY_NOEXIST.message);
            }
            //给公司ID、编码赋值
            data.setCompanyId(companyVo.getId());
            data.setUseCompanyId(companyVo.getId());
            //data.setCompanyCode(companyVo.getCode());
            //权限判断
            if(Constant.Is.NO.equals(isEnterpriseAdmin)){
                if (!ShiroUtils.hasPerm(Constant.MenuDefine.ACCOUNTING_ITEM_IMPORT, data.getCompanyId())) {
                    return CheckResult.error(ResultCode.PERMISSION_NOT.message);
                }
            }
            //校验成功
            return CheckResult.ok();
        };
    }
    /**
     * 校验使用公司是否存在
     * @return
     */
    private CheckHandler<AccountingItemValueDto> getUseCompanyCheckHandler() {
        return data -> {
            CompanyVo companyVo = getCompanyVo(data);
            /*if(companyVo == null) {
                return CheckResult.error(ResultCode.ACCOUNTING_ITEM_USECOMPANY_NOEXIST.message);
            }*/
            //给公司ID赋值
            if(companyVo != null){
                data.setUseCompanyId(companyVo.getId());
            }
            //校验成功
            return CheckResult.ok();
        };
    }
    /**
     * 校验项目是否存在
     * @return
     */
    private CheckHandler<AccountingItemValueDto> getItemNameCheckHandler() {
       return data -> {
            AccountingItemDto accountingItemDto = new AccountingItemDto();
            accountingItemDto.setCode(data.getItemCode());
            AccountingItemVo accountingItemVo = accountingItemService.findAccountingItemByItemName(accountingItemDto);
            if(accountingItemVo == null) {
                return CheckResult.error(ResultCode.ACCOUNTING_ITEM_NOEXIST.message);
            }
            //给项目ID、编码赋值
            data.setItemId(accountingItemVo.getId());
            //校验成功
            return CheckResult.ok();
        };
    }
    /**
     * @Description 根据公司名称 租户id校验公司是否存在
     * @Author 薛永利
     * @Date 2019/8/22 11:45
     * @Param [accountingItemValueDto]
     * @return com.njwd.entity.basedata.vo.CompanyVo
     */
    private CompanyVo getCompanyVo(AccountingItemValueDto accountingItemValueDto) {
        Long rootEnterpriseId = UserUtils.getUserVo().getRootEnterpriseId();
        CompanyDto companyDto = new CompanyDto();
        companyDto.setCode(accountingItemValueDto.getCompanyCode());
        companyDto.setRootEnterpriseId(rootEnterpriseId);
        return companyService.findCompanyByIdOrCodeOrName(companyDto);
    }

    private void setOperatorInfo(AccountingItemValueDto accountingItemValueDto)
    {
        SysUserVo userVo = UserUtils.getUserVo();
        accountingItemValueDto.setCreatorId(userVo.getUserId());
        accountingItemValueDto.setCreatorName(userVo.getName());
        accountingItemValueDto.setCreateTime(new Date());
        accountingItemValueDto.setRootEnterpriseId(userVo.getRootEnterpriseId());
    }
}
