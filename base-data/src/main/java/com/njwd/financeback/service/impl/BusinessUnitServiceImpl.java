package com.njwd.financeback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.mapper.CustomerMapper;
import com.njwd.basedata.mapper.SupplierMapper;
import com.njwd.basedata.service.SequenceService;
import com.njwd.common.Constant;
import com.njwd.common.MenuCodeConstant;
import com.njwd.entity.basedata.*;
import com.njwd.entity.basedata.dto.*;
import com.njwd.entity.basedata.vo.*;
import com.njwd.entity.platform.dto.SysAuxDataDto;
import com.njwd.entity.platform.vo.SysAuxDataVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.financeback.mapper.AccountBookEntityMapper;
import com.njwd.financeback.mapper.BusinessUnitMapper;
import com.njwd.financeback.service.*;
import com.njwd.service.FileService;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BatchResult;
import com.njwd.utils.FastUtils;
import com.njwd.utils.RedisUtils;
import com.njwd.utils.StringUtil;
import com.njwd.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 业务单元
 *
 * @Author: Zhuzs
 * @Date: 2019-05-15 11:09
 */
@Service
public class BusinessUnitServiceImpl implements BusinessUnitService {

    @Autowired
    private BusinessUnitMapper businessUnitMapper;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private AccountBookService accountBookService;
    @Autowired
    private AccountBookEntityService accountBookEntityService;
    @Autowired
    private FileService fileService;
    @Autowired
    private SysAuxDataService sysAuxDataService;
    @Autowired
    private SequenceService sequenceService;
    @Autowired
    private ReferenceRelationService referenceRelationService;
    @Autowired
    private SupplierMapper supplierMapper;

    @Autowired
    private CustomerMapper customerMapper;
    @Autowired
    private AccountBookEntityMapper accountBookEntityMapper;

    /**
     * 新增业务单元
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.entity.basedata.BusinessUnit
     * @author: zhuzs
     * @date: 2019-09-16 17:16
     */
    @Override
    @Transactional
    public BusinessUnit addBusinessUnit(BusinessUnitDto businessUnitDto) {
        // 校验名称是否重复
        checkBusinessUnitNameUniqueness(businessUnitDto);

        // 设置基础数据
        BusinessUnit businessUnit = new BusinessUnit();
        FastUtils.copyProperties(businessUnitDto, businessUnit);
        setBasicIno(businessUnit, "businessUnitAdd");

        // 公司分账核算，所有业务单元都作为核算主体
        businessUnit.setIsAccountEntity(Constant.Is.NO.equals(businessUnitDto.getHasSubAccount()) ? Constant.Is.NO : Constant.Is.YES);

        // 是否为手动编码
        if (Constant.Is.NO.equals(businessUnitDto.getIsAutoCode())) {
            // 设置编码
            generateCode(businessUnit, businessUnitDto.getCompanyCode());
            businessUnitMapper.insert(businessUnit);
        } else {
            // 校验 编码 是否重复
            checkBusinessUnitCodeUniqueness(businessUnit);
            businessUnitMapper.insert(businessUnit);
        }

        // 依据公司是否建账及是否启用分帐核算 判断是否新增核算主体
        if (Constant.Is.YES.equals(businessUnitDto.getHasSubAccount()) && Constant.Is.YES.equals(businessUnitDto.getIsAccounting())) {
            // 新增核算主体
            Company company = new Company();
            company.setId(businessUnitDto.getCompanyId());
            company.setRootEnterpriseId(company.getRootEnterpriseId());
            addAccountBookEntity(company, businessUnit);
        }

        return businessUnit;
    }

    /**
     * 删除
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.support.BatchResult
     * @author: zhuzs
     * @date: 2019-09-16 17:16
     */
    @Override
    @CacheEvict(value = Constant.RedisCache.COMPANY, key = "#businessUnitDto.idList.get(0)")
    public BatchResult delete(BusinessUnitDto businessUnitDto) {
        StringUtil.checkEmpty(businessUnitDto.getIdList().get(0));
        return deleteBusiness(businessUnitDto);
    }

    /**
     * 批量删除
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.support.BatchResult
     * @author: zhuzs
     * @date: 2019-09-16 17:16
     */
    @Override
    public BatchResult deleteBatch(BusinessUnitDto businessUnitDto) {
        return deleteBusiness(businessUnitDto);
    }

    /**
     * 修改业务单元
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.entity.basedata.BusinessUnit
     * @author: zhuzs
     * @date: 2019-09-16 17:16
     */
    @Override
    @CacheEvict(value = Constant.RedisCache.BUSINESS_UNIT, key = "#businessUnitDto.id")
    public BusinessUnit updateBusinessUnit(BusinessUnitDto businessUnitDto) {
        StringUtil.checkEmpty(businessUnitDto.getId());

        // 判断版本号是否相同
        BusinessUnit businessUnit = new BusinessUnit();
        FastUtils.copyProperties(businessUnitDto, businessUnit);
        if (null != businessUnit.getCompanyId()) {
            CompanyDto companyDto = new CompanyDto();
            companyDto.setId(businessUnit.getCompanyId());
            Company company = companyService.findCompanyByIdOrCodeOrName(companyDto);
            // 公司分账核算 是：所有业务单元都作为核算主体
            businessUnit.setIsAccountEntity(Constant.Is.NO.equals(company.getHasSubAccount()) ? Constant.Is.NO : Constant.Is.YES);
            // 公司是否建账
            if (Constant.Is.YES.equals(company.getIsAccounting()) && Constant.Is.YES.equals(businessUnit.getIsAccountEntity())) {
                // 新增核算主体
                addAccountBookEntity(company, businessUnit);
                // 修改业务单元的引用状态
                businessUnitDto.setIsRef(Constant.Is.YES);
            }
            // 设置编码
            generateCode(businessUnit, company.getCode());
        }
        setBasicIno(businessUnit, "businessUnitUpdate");
        // 校验 业务单元是否被删除
        BusinessUnit result = businessUnitMapper.selectOne(new LambdaQueryWrapper<BusinessUnit>()
                .eq(BusinessUnit::getId, businessUnit.getId())
                .eq(BusinessUnit::getIsDel, Constant.Is.NO));
        FastUtils.checkNull(result);
        businessUnitMapper.update(businessUnit, new LambdaQueryWrapper<BusinessUnit>()
                .eq(BusinessUnit::getId, businessUnit.getId()));
        return businessUnit;
    }

    /**
     * 根据 ID 查询业务单元（含引用关系）
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.entity.basedata.BusinessUnit
     * @author: zhuzs
     * @date: 2019-09-16 17:16
     */
    @Override
    public BusinessUnit findBusinessUnitById(BusinessUnitDto businessUnitDto) {
        BusinessUnit businessUnit = findBusinessUnitBaseInfoById(businessUnitDto);
        // 查看 业务单元是否被引用
        ReferenceResult referenceResult = referenceRelationService.isReference(Constant.Reference.BUSINESS_UNIT, businessUnit.getId());
        if (referenceResult.isReference()) {
            businessUnit.setIsRef(Constant.Is.YES);
        }
        FastUtils.checkNull(businessUnit);
        return businessUnit;
    }

    /**
     * 查询业务单元基础信息
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.entity.basedata.BusinessUnit
     * @author: zhuzs
     * @date: 2019-09-16 17:16
     */
    @Override
    @Cacheable(value = Constant.RedisCache.BUSINESS_UNIT, key = "#businessUnitDto.id", unless = "#result == null")
    public BusinessUnit findBusinessUnitBaseInfoById(BusinessUnitDto businessUnitDto) {
        StringUtil.checkEmpty(businessUnitDto.getId());

        BusinessUnit businessUnit = businessUnitMapper.selectById(businessUnitDto);
        businessUnit.setIsRef(Constant.Is.NO);

        return businessUnit;
    }

    /**
     * 根据 名称 查询业务单元
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.entity.basedata.BusinessUnit
     * @author: zhuzs
     * @date: 2019-09-16 17:17
     */
    @Override
    public BusinessUnit findBusinessUnitByName(BusinessUnitDto businessUnitDto) {
        businessUnitDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        return businessUnitMapper.selectOne(new LambdaQueryWrapper<BusinessUnit>().
                eq(BusinessUnit::getName, businessUnitDto.getName()).
                eq(BusinessUnit::getCompanyId, businessUnitDto.getCompanyId()).
                eq(BusinessUnit::getRootEnterpriseId, businessUnitDto.getRootEnterpriseId()).
                eq(BusinessUnit::getIsDel, Constant.Number.ZERO).
                eq(BusinessUnit::getIsEnable, Constant.Number.ONE));
    }

    /**
     * 根据 编码 查询业务单元
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.entity.basedata.BusinessUnit
     * @author: zhuzs
     * @date: 2019-09-16 17:17
     */
    @Override
    public BusinessUnit findBusinessByCode(BusinessUnitDto businessUnitDto) {
        businessUnitDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        return businessUnitMapper.selectOne(new LambdaQueryWrapper<BusinessUnit>().
                eq(BusinessUnit::getCode, businessUnitDto.getCode()).
                eq(BusinessUnit::getCompanyId, businessUnitDto.getCompanyId()).
                eq(BusinessUnit::getRootEnterpriseId, businessUnitDto.getRootEnterpriseId()).
                eq(BusinessUnit::getIsDel, Constant.Number.ZERO).
                eq(BusinessUnit::getIsEnable, Constant.Number.ONE));
    }

    /**
     * 根据 编码 查询业务单元
     *
     * @param: [businessUnit]
     * @return: java.lang.Integer
     * @author: zhuzs
     * @date: 2019-09-16 17:17
     */
    @Override
    public Integer findBusinessUnitByCode(BusinessUnit businessUnit) {
        businessUnit.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        return businessUnitMapper.selectCount(new LambdaQueryWrapper<BusinessUnit>().
                eq(BusinessUnit::getCode, businessUnit.getCode()).
                eq(BusinessUnit::getRootEnterpriseId, businessUnit.getRootEnterpriseId()).
                eq(BusinessUnit::getIsDel, Constant.Number.ZERO).
                eq(BusinessUnit::getIsEnable, Constant.Number.ONE));
    }

    /**
     * 根据 公司ID 查询业务单元
     *
     * @param: [rootEnterpriseId, companyId]
     * @return: java.util.List<com.njwd.entity.basedata.vo.BusinessUnitVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:17
     */
    @Override
    public List<BusinessUnitVo> findBusinessUnitByCompanyId(Long rootEnterpriseId, Long companyId) {
        return businessUnitMapper.findPageByCompanyId(rootEnterpriseId, companyId);
    }

    /**
     * 根据公司ID List 查询业务单元
     *
     * @param: [companyIds]
     * @return: java.util.List<java.lang.Long>
     * @author: zhuzs
     * @date: 2019-09-16 17:17
     */
    @Override
    public List<Long> findBusinessUnitByCompanyIds(List<Long> companyIds) {
        return businessUnitMapper.findListByCompanyIds(companyIds);
    }

    /**
     * 查询业务单元列表(含公司ID、公司名称）
     *
     * @param: [businessUnitDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.BusinessUnitVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:17
     */
    @Override
    public Page<BusinessUnitVo> findBusinessUnitPage(BusinessUnitDto businessUnitDto) {
        businessUnitDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        Page<BusinessUnitVo> page = businessUnitDto.getPage();
        Page<BusinessUnitVo> businessUnitVoList = businessUnitMapper.findPage(page, businessUnitDto);
        return businessUnitVoList;
    }

    /**
     * 查询业务单元列表(含公司ID、公司名称）
     *
     * @param: [businessUnitDto]
     * @return: java.util.List<com.njwd.entity.basedata.vo.BusinessUnitVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:17
     */
    @Override
    public List<BusinessUnitVo> findBusinessUnitList(BusinessUnitDto businessUnitDto) {
        //先查询客户Or供应商所有已经引入的内部客户的信息
        List<CustomerSupplierVo> list = new ArrayList<>();
        CustomerSupplierDto dto = new CustomerSupplierDto();
        dto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        dto.setIsDel(Constant.Is.NO);
        if (Constant.CustomerSupplier.CUSTOMER.equals(businessUnitDto.getDataType())) {
            list = customerMapper.selectCustomerByParams(dto);
        } else {
            list = supplierMapper.selectSupplierByParams(dto);
        }
        List<String> codes = new ArrayList<>();
        //遍历
        for (CustomerSupplierVo vo : list) {
            codes.add(vo.getCode());
        }
        //剔除已经引入的内部客户信息
        businessUnitDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        businessUnitDto.setCodes(codes);
        List<BusinessUnitVo> businessUnitVoList = businessUnitMapper.findBusinessUnitList(businessUnitDto);
        return businessUnitVoList;
    }

    /**
     * 获取业务单元形态列表
     *
     * @param: []
     * @return: java.util.List<com.njwd.entity.platform.vo.SysAuxDataVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:18
     */
    @Override
    public List<SysAuxDataVo> findFormList() {
        SysAuxDataDto dataType = new SysAuxDataDto();
        dataType.setType("form");
        List<SysAuxDataVo> formList = sysAuxDataService.findAuxDataList(dataType);
        return formList;
    }

    /**
     * 导出
     *
     * @param: [businessUnitDto, response]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 17:18
     */
    @Override
    public void exportExcel(BusinessUnitDto businessUnitDto, HttpServletResponse response) {
        businessUnitDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        Page<BusinessUnitVo> page = businessUnitDto.getPage();
        fileService.resetPage(page);
        Page<BusinessUnitVo> businessUnitVoPage = businessUnitMapper.findPage(page, businessUnitDto);
        fileService.exportExcel(response, businessUnitVoPage.getRecords(), MenuCodeConstant.BUSINESS_UNIT, businessUnitDto.getIsEnterpriseAdmin());
    }

    /**
     * 新增核算主体
     *
     * @param: [company, businessUnit]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 17:18
     */
    private void addAccountBookEntity(Company company, BusinessUnit businessUnit) {
        // 新增核算主体
        AccountBookEntity accountBookEntity = new AccountBookEntity();
        // 1.获取核算账簿信息
        AccountBookDto accountBookDto = new AccountBookDto();
        accountBookDto.setCompanyId(company.getId());
        accountBookDto.setRootEnterpriseId(company.getRootEnterpriseId());
        AccountBookVo accountBookVo = accountBookService.findByCompanyId(accountBookDto);
        // 2.设置基础信息
        accountBookEntity.setForm(Constant.Number.BUSINESS);
        accountBookEntity.setEntityId(businessUnit.getId());
        accountBookEntity.setAccountBookId(accountBookVo.getId());
        accountBookEntity.setAccountBookName(accountBookVo.getName());
        setBasicIno(accountBookEntity, "accountBookEntityAdd");
        accountBookEntityService.insert(accountBookEntity);

        // 清除核算账簿缓存
        List<Long> ids = new ArrayList<>();
        ids.add(accountBookVo.getId());
        RedisUtils.removeBatch(Constant.RedisCache.ACCOUNT_BOOK, ids);

    }

    /**
     * 删除业务单元
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.support.BatchResult
     * @author: zhuzs
     * @date: 2019-09-16 17:18
     */
    private BatchResult deleteBusiness(BusinessUnitDto businessUnitDto) {
        // 操作详情
        BatchResult batchResult = new BatchResult();
        List<ReferenceDescription> failList = new ArrayList<>();
        batchResult.setFailList(failList);
        List<Long> idList = businessUnitDto.getIdList();

        if (CollectionUtils.isNotEmpty(idList)) {
            // 查询已删除的记录id , 放入操作失败集合
            FastUtils.filterIds(ResultCode.IS_DEL, businessUnitMapper, new QueryWrapper<BusinessUnit>().eq(Constant.ColumnName.IS_DEL, Constant.Is.YES), Constant.ColumnName.ID, idList, failList);
            // 查询被引用的记录,有则放入操作失败集合
            ReferenceContext businessReferenceContext = referenceRelationService.isReference(Constant.Reference.BUSINESS_UNIT, idList);
            failList.addAll(businessReferenceContext.getReferences());
            List<Long> successList = businessReferenceContext.getNotReferences();

            if (!successList.isEmpty()) {
                // 校验 业务单元关联的核算主体 是否被引用
                AccountBookEntityDto accountBookEntityDto = new AccountBookEntityDto();
                accountBookEntityDto.setNotReferencesIdList(successList);
                List<AccountBookEntityVo> entityList = accountBookEntityMapper.findIdsByBusinessIdList(accountBookEntityDto);
                if(!entityList.isEmpty()){
                    List<Long> entityIdList = new ArrayList<>();
                    for(AccountBookEntityVo entityVo:entityList){
                        entityIdList.add(entityVo.getId());
                    }
                    ReferenceContext entityReferenceContext = referenceRelationService.isReference(Constant.Reference.ACCOUNT_BOOK_ENTITY, entityIdList);

                    // 过滤 被引用部分
                    if(!entityReferenceContext.getReferences().isEmpty()){
                        for(ReferenceDescription referenceDescription:entityReferenceContext.getReferences()){
                            for(AccountBookEntityVo entityVo:entityList ){
                                if(referenceDescription.getBusinessId().equals(entityVo.getId())){
                                    failList.add(referenceDescription);
                                    successList.remove(entityVo.getEntityId());
                                    break;
                                }
                            }
                        }
                    }
                }

                // 查询被引用的记录,有则放入操作失败集合
                // 操作未被引用的数据
                BusinessUnit businessUnit = new BusinessUnitDto();
                businessUnit.setIsDel(Constant.Is.YES);
                FastUtils.updateBatch(businessUnitMapper, businessUnit, Constant.ColumnName.ID, successList, null);
                batchResult.setSuccessList(successList);
            }
        }

        return batchResult;
    }

    /**
     * 生成业务单元编码
     *
     * @param: [businessUnit, companyCode]
     * @return: com.njwd.entity.basedata.BusinessUnit
     * @author: zhuzs
     * @date: 2019-09-16 17:18
     */
    @Override
    public BusinessUnit generateCode(BusinessUnit businessUnit, String companyCode) {
        String code = sequenceService.getCode(Constant.Number.LENGTH, businessUnit.getCompanyId(), companyCode, Constant.EntityType.COMPANY);
        businessUnit.setCode(code);
        Integer row = Constant.Number.ZERO;
        while (true) {
            row = findBusinessUnitByCode(businessUnit);
            if (Constant.Number.ZERO.equals(row)) {
                break;
            } else {
                code = sequenceService.getCode(Constant.Number.LENGTH, businessUnit.getCompanyId(), companyCode, Constant.EntityType.COMPANY);
                businessUnit.setCode(code);
            }
        }
        return businessUnit;
    }

    /**
     * 校验 业务单元名称 是否重复
     *
     * @param: [businessUnitDto]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 17:18
     */
    private void checkBusinessUnitNameUniqueness(BusinessUnitDto businessUnitDto) {
        BusinessUnit businessUnit = findBusinessUnitByName(businessUnitDto);
        if (null != businessUnit) {
            throw new ServiceException(ResultCode.NAME_EXIST);
        }
    }

    /**
     * 校验 业务单元编码 是否重复
     *
     * @param: [businessUnit]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 17:18
     */
    private void checkBusinessUnitCodeUniqueness(BusinessUnit businessUnit) {
        Integer row = findBusinessUnitByCode(businessUnit);
        if (!Constant.Number.ZERO.equals(row)) {
            throw new ServiceException(ResultCode.CODE_EXIST);
        }
    }

    /**
     * 根据场景设置基础信息
     *
     * @param: [obj, scenes(场景)]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 17:18
     */
    private void setBasicIno(Object obj, String scenes) {
        SysUserVo userVo = UserUtils.getUserVo();
        BusinessUnit businessUnit = null;
        AccountBookEntity accountBookEntity = null;
        switch (scenes) {
            case "businessUnitAdd":
                businessUnit = (BusinessUnit) obj;
                businessUnit.setRootEnterpriseId(userVo.getRootEnterpriseId());
                businessUnit.setCreatorId(userVo.getUserId());
                businessUnit.setCreatorName(userVo.getName());
                break;
            case "businessUnitUpdate":
                businessUnit = (BusinessUnit) obj;
                businessUnit.setRootEnterpriseId(userVo.getRootEnterpriseId());
                businessUnit.setUpdatorId(userVo.getUserId());
                businessUnit.setUpdatorName(userVo.getName());
                break;
            case "accountBookEntityAdd":
                accountBookEntity = (AccountBookEntity) obj;
                accountBookEntity.setRootEnterpriseId(userVo.getRootEnterpriseId());
                accountBookEntity.setCreatorId(userVo.getUserId());
                accountBookEntity.setCreatorName(userVo.getName());
                break;
            case "accountBookEntityUpdate":
                accountBookEntity = (AccountBookEntity) obj;
                accountBookEntity.setRootEnterpriseId(userVo.getRootEnterpriseId());
                accountBookEntity.setUpdatorId(userVo.getUserId());
                accountBookEntity.setUpdatorName(userVo.getName());
                break;

        }

    }

}
