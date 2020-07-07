package com.njwd.financeback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.LedgerConstant;
import com.njwd.common.MenuCodeConstant;
import com.njwd.entity.basedata.*;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.BusinessUnitDto;
import com.njwd.entity.basedata.dto.CompanyDto;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.basedata.vo.BusinessUnitVo;
import com.njwd.entity.basedata.vo.CompanyVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.dto.AccountBookCategoryDto;
import com.njwd.entity.platform.dto.AccountingCalendarDto;
import com.njwd.entity.platform.dto.FinancialReportDto;
import com.njwd.entity.platform.dto.SysAuxDataDto;
import com.njwd.entity.platform.vo.AccountingCalendarVo;
import com.njwd.entity.platform.vo.FinancialReportVo;
import com.njwd.entity.platform.vo.SysAuxDataVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.financeback.mapper.CompanyMapper;
import com.njwd.financeback.service.*;
import com.njwd.service.FileService;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BatchResult;
import com.njwd.utils.FastUtils;
import com.njwd.utils.RedisUtils;
import com.njwd.utils.StringUtil;
import com.njwd.utils.UserUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


/**
 * 公司
 *
 * @Author: Zhuzs
 * @Date: 2019-05-15 11:08
 */
@Service
public class CompanyServiceImpl implements CompanyService {

    @Resource
    private CompanyMapper companyMapper;
    @Resource
    private AccountBookService accountBookService;
    @Resource
    private BusinessUnitService businessUnitService;
    @Resource
    private AccountBookEntityService accountBookEntityService;
    @Resource
    private FileService fileService;
    @Resource
    private SysAuxDataService sysAuxDataService;
    @Resource
    private AccountingCalendarService accountingCalendarService;
    @Resource
    private AccountingSubjectService accountingSubjectService;
    @Resource
    private AccountingCashFlowService accountingCashFlowService;
    @Resource
    private FinancialReportService financialReportService;
    @Resource
    private ReferenceRelationService referenceRelationService;
    @Resource
    private AccountStandardService accountingStandardService;
    @Resource
    private CompanyService companyService;

    /**
     * 新增公司
     *
     * @param: [company]
     * @return: com.njwd.entity.basedata.Company
     * @author: zhuzs
     * @date: 2019-09-16 17:04
     */
    @Override
    @Transactional
    @CacheEvict(value = Constant.RedisCache.COMPANY,key="#company.id" )
    public Company addCompany(Company company) {
        // 设置基础数据
        setBasicInfo(company,"companyAdd");
        RedisUtils.lock("addCompany"+company.getCode(), LedgerConstant.Ledger.LOCK_TIMEOUT, () -> {
            // 校验唯一性
            checkCompanyUniqueness(company, "add");
            companyMapper.insert(company);
            // 新增 本部业务单元
            addCadresBusinessUnit(company);
            return company;
        });
        return company;
    }

    /**
     * 删除
     *
     * @param: [companyDto]
     * @return: com.njwd.support.BatchResult
     * @author: zhuzs
     * @date: 2019-09-16 17:04
     */
    @Override
    @CacheEvict(value = Constant.RedisCache.COMPANY,key="#companyDto.idList.get(0)" )
    public BatchResult delete(CompanyDto companyDto) {
        StringUtil.checkEmpty(companyDto.getIdList().get(0));
        return deleteCompany(companyDto);
    }

    /**
     * 批量删除
     *
     * @param: [companyDto]
     * @return: com.njwd.support.BatchResult
     * @author: zhuzs
     * @date: 2019-09-16 17:04
     */
    @Override
    public BatchResult deleteBatch(CompanyDto companyDto) {
        return deleteCompany(companyDto);
    }

    /**
     * 修改公司
     *
     * @param: [company]
     * @return: int
     * @author: zhuzs
     * @date: 2019-09-16 17:04
     */
    @Override
    @CacheEvict(value = Constant.RedisCache.COMPANY ,key="#company.id")
    public int updateCompany(Company company) {
        StringUtil.checkEmpty(company.getId());
        // 校验唯一性
        checkCompanyUniqueness(company,"update");
        // 设置基础数据
        setBasicInfo(company,"companyUpdate");
        Company result = companyMapper.selectOne(new LambdaQueryWrapper<Company>()
                .eq(Company::getId,company.getId())
                .eq(Company::getRootEnterpriseId,company.getRootEnterpriseId())
                .eq(Company::getIsDel, Constant.Is.NO));

        // 校验公司是否已经被删除
        FastUtils.checkNull(result);
        return companyMapper.update(company, new LambdaQueryWrapper<Company>().eq(Company::getId,company.getId()));
    }

    /**
     * 删除核算账簿，修改公司建账状态为未建账
     *
     * @param: [idList]
     * @return: int
     * @author: zhuzs
     * @date: 2019-09-16 17:04
     */
    @Override
    public int updateBatch(List<Long> idList) {
        Company company = null;
        AccountBook accountBook = null;
        AccountBookDto accountBookDto = new AccountBookDto();
        for(Long id : idList){
            company = new Company();
            accountBookDto.setId(id);
            accountBook = accountBookService.findById(accountBookDto);
            company.setId(accountBook.getCompanyId());
            company.setIsAccounting(Constant.Is.NO);
            // 设置基础数据
            setBasicInfo(company,"companyUpdate");
            companyMapper.update(company,new LambdaQueryWrapper<Company>().eq(Company::getId,company.getId()));
        }
        return 1;
    }

    /**
     * 公司建账
     *
     * @param: [companyDto]
     * @return: int
     * @author: zhuzs
     * @date: 2019-09-16 17:04
     */
    @Override
    @Transactional
    @CacheEvict(value = Constant.RedisCache.COMPANY,key="#companyDto.id" )
    public int enableAccountBook(CompanyDto companyDto) {
        StringUtil.checkEmpty(companyDto.getId());
        //1.修改公司建账状态
        Company company = new Company();
        FastUtils.copyProperties(companyDto, company);
        setBasicInfo(company, "companyUpdate");
        company.setIsAccounting(Constant.Is.YES);
        companyMapper.update(company, new LambdaQueryWrapper<Company>().eq(Company::getId, company.getId()));

        //2.创建核算账簿
        AccountBook accountBook = new AccountBook();
        accountBook.setCompanyId(company.getId());
        accountBook.setName(company.getName() + Constant.AccountBookType.ACCOUNT_BOOK_NAME);
        accountBook.setCode(company.getCode() + Constant.Character.ONE);
        accountBook.setAccountBookTypeId(Constant.AccountBookType.ACCOUNT_BOOK_ID);
        accountBook.setAccountBookTypeName(Constant.AccountBookType.ACCOUNT_BOOK_NAME);

        setParams(accountBook, companyDto);
        setBasicInfo(accountBook, "accountBookAdd");
        //新增核算账簿
        accountBookService.addAccountBook(accountBook);

        //3.新增核算主体
        // 公司是否分账核算
        if (Constant.Is.YES.equals(company.getHasSubAccount())) {
            //是 所有业务单元作为核算主体，并修改业务单元的引用状态
            List<BusinessUnitVo> businessUnitVos = businessUnitService.findBusinessUnitByCompanyId(company.getRootEnterpriseId(), company.getId());
            if (FastUtils.checkNullOrEmpty(businessUnitVos)) {//无核算主体
                throw new ServiceException(ResultCode.ACCOUNT_ENTITY_NOT_EXIST);
            }
            for (BusinessUnitVo businessUnitVo : businessUnitVos) {
                // 修改业务单元引用状态为 引用
                BusinessUnitDto businessUnitDto = new BusinessUnitDto();
                businessUnitDto.setId(businessUnitVo.getId());
                businessUnitDto.setIsRef(Constant.Is.YES);
                businessUnitService.updateBusinessUnit(businessUnitDto);
                // 新增核算主体
                AccountBookEntity accountBookEntity = new AccountBookEntity();
                copyParameter(businessUnitVo, accountBookEntity, accountBook);
                setBasicInfo(accountBookEntity, "accountBookEntityAdd");
                accountBookEntityService.insert(accountBookEntity);
            }
        } else {
            //否
            AccountBookEntity accountBookEntity = new AccountBookEntity();
            copyParameter(companyDto, accountBookEntity, accountBook);
            setBasicInfo(accountBookEntity, "accountBookEntityAdd");
            accountBookEntityService.insert(accountBookEntity);
        }

        //  清除业务单元的缓存
        List<Long> ids = new ArrayList<>();
        ids.add(companyDto.getId());
        List<Long> businessUnitIds = businessUnitService.findBusinessUnitByCompanyIds(ids);
        clearBusinessUnitsCache(businessUnitIds);

        return Constant.Number.ONE;
    }

    /**
     * 批量建账
     *
     * @param: [companyDtoList]
     * @return: com.njwd.support.BatchResult
     * @author: zhuzs
     * @date: 2019-09-16 17:03
     */
    @Override
    @Transactional
    public BatchResult enableAccountBookBatch(List<CompanyDto> companyDtoList) {
        BatchResult result = new BatchResult();
        List<Long> success = new ArrayList<>();

        for(CompanyDto companyDto:companyDtoList){
            enableAccountBook(companyDto);
            success.add(companyDto.getId());
        }
        result.setSuccessList(success);

        //  清除业务单元的缓存
        List<Long> businessUnitIds = businessUnitService.findBusinessUnitByCompanyIds(success);
        clearBusinessUnitsCache(businessUnitIds);
        return result;
    }

    /**
     * 根据 id/name/code/enterpriseId 查询公司信息
     *
     * @param: [companyDto]
     * @return: com.njwd.entity.basedata.vo.CompanyVo
     * @author: zhuzs
     * @date: 2019-09-16 17:03
     */
    @Override
    public CompanyVo findCompanyByIdOrCodeOrName(CompanyDto companyDto) {
        SysUserVo operator = UserUtils.getUserVo();
        companyDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        Company company = companyMapper.selectByIdOrCodeOrName(companyDto);
        CompanyVo companyVo = new CompanyVo();
        if(company!=null){
            FastUtils.copyProperties(company,companyVo);
            companyVo.setIsRef(Constant.Is.NO);
            // 校验公司是否被引用
            ReferenceResult referenceResult = referenceRelationService.isReference(Constant.Reference.COMPANY,company.getId());
            if(referenceResult.isReference()){
                companyVo.setIsRef(Constant.Is.YES);
            }
        }
        return companyVo;
    }

    /**
     * 根据ID List 查询公司列表 （若是User 端，则查询已配置的公司列表）
     *
     * @param: [companyDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.CompanyVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:03
     */
    @Override
    public Page<CompanyVo> findPage(CompanyDto companyDto) {
        Page<CompanyVo> page = companyDto.getPage();
        List<CompanyVo> companyVoList = companyMapper.findPage(page,companyDto).getRecords();
        AccountBookDto accountBookDto = new AccountBookDto();
        accountBookDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        accountBookDto.setCompanyHasSubAccount(Constant.Is.NO);
        for(CompanyVo companyVo:companyVoList){
            if(Constant.Is.NO.equals(companyVo.getHasSubAccount())){
                accountBookDto.setCompanyId(companyVo.getId());
                List<AccountBookEntityVo> list = accountBookEntityService.findAccountBookEntityPage(accountBookDto).getRecords();
                if(!FastUtils.checkNullOrEmpty(list)){
                    companyVo.setAccountBookEntityVo(list.get(0));
                }
            }
        }
        return page.setRecords(companyVoList);
    }

    /**
     * 根据ID List 查询公司列表 （若是User 端，则查询已配置的公司列表）
     *
     * @param: [companyDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.CompanyVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:03
     */
    @Override
    public Page<CompanyVo> findCompanyPageOut(CompanyDto companyDto) {
        Page<CompanyVo> page = companyDto.getPage();
        Page<CompanyVo> companyVoList = companyMapper.findCompanyPageOut(page,companyDto);
        return companyVoList;
    }

    /**
     * 根据ID List 查询公司列表
     *
     * @param: [companyDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.CompanyVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:03
     */
    @Override
    public Page<CompanyVo> findPageForConfigure(CompanyDto companyDto) {
        Page<CompanyVo> pageInfos = new Page<>();
        Page<CompanyVo> page = companyDto.getPage();
        if (companyDto.getIdList() != null && companyDto.getIdList().size() > 0){
            pageInfos = companyMapper.findPageForConfigure(page,companyDto);
        }
        return pageInfos;
    }

    /**
     * 获取核算主体列表
     *
     * @param: [accountBookDtos]
     * @return: java.util.List<com.njwd.entity.basedata.vo.AccountBookEntityVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:03
     */
    @Override
    public List<AccountBookEntityVo> findAccountBookEntityList(List<AccountBookDto> accountBookDtos) {
        List<AccountBookEntityVo> accountBookEntityVoAll = new ArrayList<>();
        for(AccountBookDto accountBookDto:accountBookDtos){
            accountBookDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
            List<AccountBookEntityVo> accountBookEntityVos = accountBookEntityService.findAccountBookEntityList(accountBookDto);
            accountBookEntityVoAll.addAll(accountBookEntityVos);
        }
        return accountBookEntityVoAll;
    }

    /**
     * 获取 公司形态/公司类型/会计准则/纳税人资质 列表
     *
     * @param: [operator]
     * @return: java.util.Map<java.lang.String,java.lang.Object>
     * @author: zhuzs
     * @date: 2019-09-16 17:03
     */
    @Override
    public Map<String, Object> findSysAuxDataList(SysUserVo operator) {
        SysAuxDataDto dataType = new SysAuxDataDto();
        Map<String, Object> sysAuxDataMap = new HashMap<>();

        dataType.setType("form");
        List<SysAuxDataVo> formList = sysAuxDataService.findAuxDataList(dataType);

        dataType.setType("company_type");
        List<SysAuxDataVo> companyTypeList = sysAuxDataService.findAuxDataList(dataType);


        dataType.setType("tax_qualification");
        List<SysAuxDataVo> taxQualificationList = sysAuxDataService.findAuxDataList(dataType);

        AccountBookCategoryDto platformAccountBookCategoryDto = new AccountBookCategoryDto();
        platformAccountBookCategoryDto.setAccountTypeId(Constant.AccountBookType.ACCOUNT_BOOK_ID);
        platformAccountBookCategoryDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        List<AccountStandard> accountStandardList = accountingStandardService.accountStandardList(platformAccountBookCategoryDto);

        sysAuxDataMap.put("form",formList);
        sysAuxDataMap.put("company_type",companyTypeList);
        sysAuxDataMap.put("accounting_standard", accountStandardList);
        sysAuxDataMap.put("tax_qualification",taxQualificationList);
        return sysAuxDataMap;
    }

    /**
     * 导出
     *
     * @param: [companyDto, response]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 17:02
     */
    @Override
    public void exportExcel(CompanyDto companyDto, HttpServletResponse response) {
        companyDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        Page<CompanyVo> page = companyDto.getPage();
        fileService.resetPage(page);
        Page<CompanyVo> companyVoPage = companyMapper.findPage(page,companyDto);
        fileService.exportExcel(response,companyVoPage.getRecords(), MenuCodeConstant.COMPANY,companyDto.getIsEnterpriseAdmin());
    }

    /**
     * 根据公司ID/企业ID 查询公司信息
     *
     * @param: [rootEnterpriseId]
     * @return: java.util.List<com.njwd.entity.basedata.Company>
     * @author: zhuzs
     * @date: 2019-09-16 17:02
     */
    @Override
    public List<Company> findCompanyByEnterprise(Long rootEnterpriseId) {
        List<Company> companyList = companyMapper.selectList(Wrappers.<Company>lambdaQuery()
                .select(Company::getName, Company::getId)
                .eq(Company::getRootEnterpriseId, rootEnterpriseId).eq(Company::getIsDel, Constant.Is.NO));
        return companyList;
    }

    /**
     * @return CompanyVo
     * @Description 校验簿是否启用分账核算
     * @Author 朱小明
     * @Date 2019/8/19 15:54
     * @Param [companyDto]
     */
    @Override
    public CompanyVo checkHasSubAccount(AccountBookDto accountBookDto) {
        List<CompanyVo> companyVos = companyMapper.checkHasSubAccount(accountBookDto);
        CompanyVo companyVo;
        if (accountBookDto.getId() != null) {
            companyVo = companyVos.size() > 0 ? companyVos.get(0) : null;
        } else {
            companyVo = new CompanyVo();
            companyVo.setBatchIds(new LinkedList<>());
            companyVos.forEach(vo->companyVo.getBatchIds().add(vo.getId()));
            companyVo.setCompanyVoList(companyVos);
        }
        return companyVo;
    }

    /**
     * 获取 资产负债表/现金流量表/利润表 ID
     *
     * @param: [accountBook, platformFinancialReportDto]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-17 10:03
     */
    @Override
    @Cacheable(value = Constant.RedisCache.COMPANY,key="#platformFinancialReportDto.accStandardId",unless = "#result == null")
    public void getFinancialReportIds(AccountBook accountBook, FinancialReportDto platformFinancialReportDto) {
        // 资产负债表
        List<FinancialReportVo> assetList = financialReportService.findAssetListByAccStandardId(platformFinancialReportDto);
        // 现金流量表
        List<FinancialReportVo> cashFlowList = financialReportService.findCashFlowListByAccStandardId(platformFinancialReportDto);
        // 利润表
        List<FinancialReportVo> profitList = financialReportService.findProfitListByAccStandardId(platformFinancialReportDto);
        accountBook.setBalanceSheetId(assetList.get(0).getId());
        accountBook.setIncomeStatementId(profitList.get(0).getId());
        accountBook.setCashFlowId(cashFlowList.get(0).getId());
    }

    /**
     * 删除公司
     *
     * @param: [companyDto]
     * @return: com.njwd.support.BatchResult
     * @author: zhuzs
     * @date: 2019-09-16 17:02
     */
    private BatchResult deleteCompany(CompanyDto companyDto){
        SysUser sysUser = UserUtils.getUserVo();
        // 操作详情
        BatchResult batchResult = new BatchResult();
        List<ReferenceDescription> failList = new ArrayList<>();
        batchResult.setFailList(failList);
        List<Long> idList = companyDto.getIdList();

        if (CollectionUtils.isNotEmpty(idList)) {
            // 查询已删除的记录id , 放入操作失败集合
            FastUtils.filterIds(ResultCode.IS_DEL, companyMapper, new QueryWrapper<Company>().eq(Constant.ColumnName.IS_DEL, Constant.Is.YES), Constant.ColumnName.ID, idList, failList);
            // 查询被引用的记录,有则放入操作失败集合
            ReferenceContext referenceContext = referenceRelationService.isReference(Constant.Reference.COMPANY, idList);
            failList.addAll(referenceContext.getReferences());

            if (!referenceContext.getNotReferences().isEmpty()) {
                // 操作未被引用的数据
                Company company = new CompanyDto();
                company.setIsDel(Constant.Is.YES);
                company.setUpdatorId(sysUser.getUserId());
                company.setUpdatorName(sysUser.getName());
                FastUtils.updateBatch(companyMapper, company, Constant.ColumnName.ID, referenceContext.getNotReferences(),null);
                batchResult.setSuccessList(referenceContext.getNotReferences());
            }
        }
        return batchResult ;
    }

    /**
     * 新增本部业务单元
     *
     * @param: [company]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 17:02
     */
    public void addCadresBusinessUnit(Company company){
        BusinessUnitDto businessUnitDto = new BusinessUnitDto();
        // 设置基础数据
        businessUnitDto.setName(company.getName());
        businessUnitDto.setUnitFormId(company.getCompanyFormId());
        businessUnitDto.setUnitFormName(company.getCompanyFormName());
        businessUnitDto.setCompanyId(company.getId());
        businessUnitDto.setCreatorId(company.getCreatorId());
        businessUnitDto.setCreatorName(company.getCreatorName());
        businessUnitDto.setRootEnterpriseId(company.getRootEnterpriseId());
        businessUnitDto.setIsCompany(Constant.IsCompany.YES);
        businessUnitDto.setIsAccountEntity(company.getHasSubAccount());

        // 设置编码 本部业务单元的编码为 公司编码 + '001'
        businessUnitDto.setCode(company.getCode()+Constant.Character.ONE);
        businessUnitDto.setIsAutoCode(Constant.Is.YES);
        businessUnitDto.setHasSubAccount(company.getHasSubAccount());
        businessUnitService.addBusinessUnit(businessUnitDto);
    }

    /**
     * 设置基础信息
     *
     * @param: [accountBook, companyDto]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 17:02
     */
    private void setParams(AccountBook accountBook,CompanyDto companyDto) {
        // 会计日历表 平台
        AccountingCalendarDto platFormAccountingCalendarDto = new AccountingCalendarDto();
        platFormAccountingCalendarDto.setAccountTypeId(accountBook.getAccountBookTypeId());
        platFormAccountingCalendarDto.setAccStandardId(Constant.AccountBookType.ACCOUNT_BOOK_ID);
        List<AccountingCalendarVo> accountingCalendarVoList = accountingCalendarService.findAccCaListByAccTypeAndStand(platFormAccountingCalendarDto);

        // 科目表 后台
        AccountingSubject accountingSubject = new AccountingSubject();
        accountingSubject.setRootEnterpriseId(companyDto.getRootEnterpriseId());
        accountingSubject.setAccStandardId(companyDto.getAccountingStandardId());
        accountingSubject.setAccountBookTypeId(Constant.AccountBookType.ACCOUNT_BOOK_ID);
        accountingSubject = accountingSubjectService.findSubject(accountingSubject);

        // 现金流量项目表 后台获取
        AccountCashFlow accountCashFlow = new AccountCashFlow();
        accountCashFlow.setRootEnterpriseId(companyDto.getRootEnterpriseId());
        accountCashFlow.setAccStandardId(companyDto.getAccountingStandardId());
        accountCashFlow.setAccountBookTypeId(Constant.AccountBookType.ACCOUNT_BOOK_ID);
        accountCashFlow = accountingCashFlowService.findAccountCashFlow(accountCashFlow);

        accountBook.setAccountingCalendarId(accountingCalendarVoList.get(0).getId());
        accountBook.setAccountingCalendarName(accountingCalendarVoList.get(0).getName());
        // wd_subject 主键ID
        accountBook.setSubjectId(accountingSubject.getId());
        accountBook.setSubjectName(accountingSubject.getSubjectName());
        accountBook.setCashFlowItemId(accountCashFlow.getId());

        // 获取资产负债表/现金流量表/利润表 ID 平台
        FinancialReportDto platformFinancialReportDto = new FinancialReportDto();
        platformFinancialReportDto.setAccStandardId(companyDto.getAccountingStandardId());
        companyService.getFinancialReportIds(accountBook, platformFinancialReportDto);
    }

    /**
     * 清除公司下业务单元缓存
     *
     * @param: [businessUnitIds]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 17:01
     */
    private void clearBusinessUnitsCache(List<Long> businessUnitIds) {
        RedisUtils.removeBatch(Constant.RedisCache.BUSINESS_UNIT,businessUnitIds);
    }

    /**
     * 校验 公司编码/名称是否重复
     *
     * @param: [companyDto, scenes]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 17:01
     */
    private void checkCompanyUniqueness(Company companyDto,String scenes) {
        Company company = null;
        switch(scenes){
            case "add":
                // 编码
                company = companyMapper.selectOne(new LambdaQueryWrapper<Company>()
                        .eq(Company::getCode,companyDto.getCode())
                        .eq(Company::getRootEnterpriseId,companyDto.getRootEnterpriseId())
                        .eq(Company::getIsDel, Constant.Is.NO));
                if(null != company) {
                    throw new ServiceException(ResultCode.CODE_EXIST);
                }
                // 名称
                company = companyMapper.selectOne(new LambdaQueryWrapper<Company>()
                        .eq(Company::getName,companyDto.getName())
                        .eq(Company::getRootEnterpriseId,companyDto.getRootEnterpriseId())
                        .eq(Company::getIsDel, Constant.Is.NO));
                if(null != company) {
                    throw new ServiceException(ResultCode.NAME_EXIST);
                }
                // 简称
                company = companyMapper.selectOne(new LambdaQueryWrapper<Company>()
                        .eq(Company::getSimpleName,companyDto.getSimpleName())
                        .eq(Company::getRootEnterpriseId,companyDto.getRootEnterpriseId())
                        .eq(Company::getIsDel, Constant.Is.NO)
                        .ne(Company::getSimpleName,Constant.Character.NULL_VALUE));
                if(null != company) {
                    throw new ServiceException(ResultCode.SIMPLE_NAME_EXIST);
                }
                // 统一社会信用代码
                company = companyMapper.selectOne(new LambdaQueryWrapper<Company>()
                        .eq(Company::getCreditCode,companyDto.getCreditCode())
                        .eq(Company::getIsDel, Constant.Is.NO)
                        .ne(Company::getCreditCode,Constant.Character.NULL_VALUE));
                if(null != company) {
                    throw new ServiceException(ResultCode.CREDIT_CODE_EXIST);
                }
                // 工商注册号
                company = companyMapper.selectOne(new LambdaQueryWrapper<Company>()
                        .eq(Company::getRegisteNumber,companyDto.getRegisteNumber())
                        .eq(Company::getIsDel, Constant.Is.NO)
                        .ne(Company::getRegisteNumber,Constant.Character.NULL_VALUE));
                if(null != company) {
                    throw new ServiceException(ResultCode.REGISTE_NUMBER_EXIST);
                }
                // 纳税人识别号
                company = companyMapper.selectOne(new LambdaQueryWrapper<Company>()
                        .eq(Company::getTaxPayerNumber,companyDto.getTaxPayerNumber())
                        .eq(Company::getIsDel, Constant.Is.NO)
                        .ne(Company::getTaxPayerNumber,Constant.Character.NULL_VALUE));
                if(null != company) {
                    throw new ServiceException(ResultCode.TAXPAYER_NUMBER_EXIST);
                }
                break;
            case "update":
                company = companyMapper.selectOne(new LambdaQueryWrapper<Company>()
                        .eq(Company::getCode,companyDto.getCode())
                        .eq(Company::getRootEnterpriseId,companyDto.getRootEnterpriseId())
                        .eq(Company::getIsDel, Constant.Is.NO)
                        .ne(Company::getId,companyDto.getId()));
                if(null != company) {
                    throw new ServiceException(ResultCode.CODE_EXIST);
                }
                company = companyMapper.selectOne(new LambdaQueryWrapper<Company>()
                        .eq(Company::getName,companyDto.getName())
                        .eq(Company::getRootEnterpriseId,companyDto.getRootEnterpriseId())
                        .eq(Company::getIsDel, Constant.Is.NO)
                        .ne(Company::getId,companyDto.getId()));
                if(null != company) {
                    throw new ServiceException(ResultCode.NAME_EXIST);
                }
                company = companyMapper.selectOne(new LambdaQueryWrapper<Company>()
                        .eq(Company::getSimpleName,companyDto.getSimpleName())
                        .eq(Company::getRootEnterpriseId,companyDto.getRootEnterpriseId())
                        .eq(Company::getIsDel, Constant.Is.NO)
                        .ne(Company::getSimpleName,Constant.Character.NULL_VALUE)
                        .ne(Company::getId,companyDto.getId()));
                if(null != company) {
                    throw new ServiceException(ResultCode.SIMPLE_NAME_EXIST);
                }
                // 统一社会信用代码
                company = companyMapper.selectOne(new LambdaQueryWrapper<Company>()
                        .eq(Company::getCreditCode,companyDto.getCreditCode())
                        .eq(Company::getIsDel, Constant.Is.NO)
                        .ne(Company::getCreditCode,Constant.Character.NULL_VALUE)
                        .ne(Company::getId,companyDto.getId()));
                if(null != company) {
                    throw new ServiceException(ResultCode.CREDIT_CODE_EXIST);
                }
                // 工商注册号
                company = companyMapper.selectOne(new LambdaQueryWrapper<Company>()
                        .eq(Company::getRegisteNumber,companyDto.getRegisteNumber())
                        .eq(Company::getIsDel, Constant.Is.NO)
                        .ne(Company::getRegisteNumber,Constant.Character.NULL_VALUE)
                        .ne(Company::getId,companyDto.getId()));
                if(null != company) {
                    throw new ServiceException(ResultCode.REGISTE_NUMBER_EXIST);
                }
                // 纳税人识别号
                company = companyMapper.selectOne(new LambdaQueryWrapper<Company>()
                        .eq(Company::getTaxPayerNumber,companyDto.getTaxPayerNumber())
                        .eq(Company::getIsDel, Constant.Is.NO)
                        .ne(Company::getTaxPayerNumber,Constant.Character.NULL_VALUE)
                        .ne(Company::getId,companyDto.getId()));

                if(null != company) {
                    throw new ServiceException(ResultCode.TAXPAYER_NUMBER_EXIST);
                }
                break;
            default:
        }

    }

    /**
     * 根据场景设置基础信息
     *
     * @param: [obj, scenes(场景)]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 17:01
     */
     private void setBasicInfo(Object obj,String scenes){
        SysUserVo userVo = UserUtils.getUserVo();
        Company company = null;
        AccountBook accountBook = null;
        AccountBookEntity accountBookEntity = null;
        switch (scenes){
            case "companyAdd":
                company = (Company) obj;
                company.setRootEnterpriseId(userVo.getRootEnterpriseId());
                company.setCreatorId(userVo.getUserId());
                company.setCreatorName(userVo.getName());
                break;
            case "companyUpdate":
                company = (Company) obj;
                company.setRootEnterpriseId(userVo.getRootEnterpriseId());
                company.setUpdatorId(userVo.getUserId());
                company.setUpdatorName(userVo.getName());
                break;
            case "accountBookAdd":
                accountBook = (AccountBook)obj;
                accountBook.setRootEnterpriseId(userVo.getRootEnterpriseId());
                accountBook.setCreatorId(userVo.getUserId());
                accountBook.setCreatorName(userVo.getName());
                break;
            case "accountBookUpdate":
                accountBook = (AccountBook)obj;
                accountBook.setRootEnterpriseId(userVo.getRootEnterpriseId());
                accountBook.setUpdatorId(userVo.getUserId());
                accountBook.setUpdatorName(userVo.getName());
                break;
            case "accountBookEntityAdd":
                accountBookEntity = (AccountBookEntity)obj;
                accountBookEntity.setRootEnterpriseId(userVo.getRootEnterpriseId());
                accountBookEntity.setCreatorId(userVo.getUserId());
                accountBookEntity.setCreatorName(userVo.getName());
                break;
            case "accountBookEntityUpdate":
                accountBookEntity = (AccountBookEntity)obj;
                accountBookEntity.setRootEnterpriseId(userVo.getRootEnterpriseId());
                accountBookEntity.setUpdatorId(userVo.getUserId());
                accountBookEntity.setUpdatorName(userVo.getName());
                break;
            default:
        }

    }

    /**
     * 数据拷贝
     *
     * @param: [obj, accountBookEntity, accountBook]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 17:01
     */
    public void copyParameter(Object obj, AccountBookEntity accountBookEntity, AccountBook accountBook){
        if(obj instanceof CompanyDto){
            CompanyDto companyDto = (CompanyDto)obj;
            accountBookEntity.setForm(Constant.Number.COMPANY);
            accountBookEntity.setAccountBookId(accountBook.getId());
            accountBookEntity.setAccountBookName(accountBook.getName());
            accountBookEntity.setEntityId(companyDto.getId());
            setBasicInfo(accountBookEntity,"accountEntityAdd");
        }else{
            BusinessUnitVo businessUnitVo = (BusinessUnitVo)obj;
            accountBookEntity.setForm(Constant.Number.BUSINESS);
            accountBookEntity.setAccountBookId(accountBook.getId());
            accountBookEntity.setAccountBookName(accountBook.getName());
            accountBookEntity.setEntityId(businessUnitVo.getId());
            setBasicInfo(accountBookEntity,"accountEntityAdd");
        }

    }



}
