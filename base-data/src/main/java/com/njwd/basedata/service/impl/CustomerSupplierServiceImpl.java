package com.njwd.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.cloudclient.BalanceSubjectAuxiliaryItemFeignClient;
import com.njwd.basedata.cloudclient.VoucherFeignClient;
import com.njwd.basedata.mapper.CustomerCompanyMapper;
import com.njwd.basedata.mapper.CustomerMapper;
import com.njwd.basedata.mapper.SupplierCompanyMapper;
import com.njwd.basedata.mapper.SupplierMapper;
import com.njwd.basedata.service.*;
import com.njwd.common.Constant;
import com.njwd.common.MenuCodeConstant;
import com.njwd.config.YmlProperties;
import com.njwd.entity.basedata.*;
import com.njwd.entity.basedata.dto.CustomerSupplierCompanyDto;
import com.njwd.entity.basedata.dto.CustomerSupplierDto;
import com.njwd.entity.basedata.vo.CustomerCompanyVo;
import com.njwd.entity.basedata.vo.CustomerSupplierVo;
import com.njwd.entity.basedata.vo.SupplierCompanyVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.financeback.service.AccountBookService;
import com.njwd.service.FileService;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BatchResult;
import com.njwd.utils.*;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 客户供应商功能Service层实现类
 * @Author 朱小明
 * @Date 2019/6/14 13:48
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerSupplierServiceImpl implements CustomerSupplierService {

    @Resource
    private SupplierMapper supplierMapper;

    @Resource
    private CustomerMapper customerMapper;

    @Resource
    private CustomerCompanyMapper customerCompanyMapper;
    @Resource
    private SupplierCompanyMapper supplierCompanyMapper;

    @Resource
    private SequenceService sequenceService;

    @Resource
    private FileService fileService;

    @Resource
    private ReferenceRelationService referenceRelationService;

    @Resource
    private YmlProperties ymlProperties;

    @Resource
    private BaseCustomService baseCustomService;

    @Resource
    private AccountBookService accountBookService;

    @Resource
    private BalanceSubjectAuxiliaryItemFeignClient balanceSubjectAuxiliaryItemFeignClient;

    @Resource
    private VoucherFeignClient voucherFeignClient;

    @Resource
    private MenuControlStrategyService menuControlStrategyService;

    @Resource
    private ProjectService projectService;

    @Resource
    private CustomerSupplierService customerSupplierService;

    /**
     * @return java.lang.Long
     * @Description 新增客户供应商
     * @Author 朱小明
     * @Date 2019/6/13 14:54
     * @Param [customerSupplierd]
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addCustomerSupplier(CustomerSupplierDto customerSupplierDto) {
        customerSupplierDto.setCreatorName(UserUtils.getUserVo().getName());
        customerSupplierDto.setCreatorId(UserUtils.getUserVo().getUserId());
        // 补参：企业ID
        customerSupplierDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        if (Constant.CustomerSupplier.ENTERPRISE.equals(customerSupplierDto.getCustomerType())) {
            setGlobalId(customerSupplierDto);
        }
        //系统自动编码
        setCode(customerSupplierDto);
        CustomerSupplierCompanyDto customerSupplierCompanyDto = new CustomerSupplierCompanyDto();
        //校验数据是重复数据
        if (checkDuplicate(customerSupplierDto)) {
            insertCustomerSupplier(customerSupplierDto, customerSupplierCompanyDto);
        } else {
            //有重复，则抛出异常并把重复字段返回给前台
            throw new ServiceException(ResultCode.COLUMN_EXIST, customerSupplierDto.getRcSet());
        }
        return customerSupplierDto.getId();
    }

    /**
     * @return java.lang.Long
     * @Description 批量新增客户供应商
     * @Author wuweiming
     * @Date 2019/8/30
     * @Param [customerSupplierd]
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long batchAddCustomerSupplier(CustomerSupplierDto customerSupplierDto) {
        customerSupplierDto.setCreatorName(UserUtils.getUserVo().getName());
        customerSupplierDto.setCreatorId(UserUtils.getUserVo().getUserId());
        // 补参：企业ID
        customerSupplierDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        if (Constant.CustomerSupplier.ENTERPRISE.equals(customerSupplierDto.getCustomerType())) {
            setGlobalId(customerSupplierDto);
        }
        //系统自动编码
        //setCode(customerSupplierDto);
        CustomerSupplierCompanyDto customerSupplierCompanyDto = new CustomerSupplierCompanyDto();
        //校验数据是重复数据
        insertCustomerSupplier(customerSupplierDto, customerSupplierCompanyDto);

        if (Constant.Is.YES.equals(customerSupplierDto.getIsEnterpriseAdmin())) {
            return customerSupplierDto.getId();
        } else {
            return customerSupplierCompanyDto.getId();
        }
    }

    /**
     * @return void
     * @Description 客户供应商插入处理
     * @Author 朱小明
     * @Date 2019/8/13 10:10
     * @Param [customerSupplierDto, customerSupplierCompanyDto]
     **/
    @Transactional(rollbackFor = Exception.class)
    public void insertCustomerSupplier(CustomerSupplierDto customerSupplierDto, CustomerSupplierCompanyDto customerSupplierCompanyDto) {
        if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())) {
            //客户处理
            //判断是否在创建集团共享数据
            Customer customer = new Customer();
            CustomerCompany customerCompany = new CustomerCompany();
            FastUtils.copyProperties(customerSupplierDto, customer);
            if (NumberUtils.LONG_ZERO.equals(customerSupplierDto.getCompanyId())) {
                FastUtils.copyProperties(customerSupplierDto, customerSupplierCompanyDto);
                //集团创建，直接添加进【客户供应商】表
                customerMapper.insert(customer);
            } else {
                Long id = customerMapper.selectCustomerIdForInsert(customerSupplierDto);
                FastUtils.copyProperties(customerSupplierDto, customerCompany);
                if (id != null) {
                    customerCompany.setCustomerId(id);
                } else {
                    customerMapper.insert(customer);
                    customerCompany.setCustomerId(customer.getId());
                }
                Long sonId = checkUseCompany(customerCompany, null);
                if (sonId == null) {
                    customerCompanyMapper.insert(customerCompany);
                } else {
                    //有重复，则抛出异常并把重复字段返回给前台
                    throw new ServiceException(ResultCode.CITE_DATA_EXIST, sonId);
                }
            }
            customerSupplierCompanyDto.setId(customerCompany.getId());
            customerSupplierDto.setId(customer.getId());
        } else {
            //供应商处理
            //判断是否在创建集团共享数据
            Supplier supplier = new Supplier();
            SupplierCompany supplierCompany = new SupplierCompany();
            FastUtils.copyProperties(customerSupplierDto, supplier);
            if (NumberUtils.LONG_ZERO.equals(customerSupplierDto.getCompanyId())) {
                FastUtils.copyProperties(customerSupplierDto, customerSupplierCompanyDto);
                //集团创建，直接添加进【客户供应商】表
                supplierMapper.insert(supplier);
            } else {
                Long id = supplierMapper.selectSupplierIdForInsert(customerSupplierDto);
                FastUtils.copyProperties(customerSupplierDto, supplierCompany);
                if (id != null) {
                    supplierCompany.setSupplierId(id);
                } else {
                    supplierMapper.insert(supplier);
                    supplierCompany.setSupplierId(supplier.getId());
                }
                Long sonId = checkUseCompany(null, supplierCompany);
                if (sonId == null) {
                    supplierCompanyMapper.insert(supplierCompany);
                } else {
                    //有重复，则抛出异常并把重复字段返回给前台
                    throw new ServiceException(ResultCode.CITE_DATA_EXIST, sonId);
                }
            }
            customerSupplierCompanyDto.setId(supplierCompany.getId());
            customerSupplierDto.setId(supplier.getId());
        }
    }

    /**
     * @return com.njwd.support.BatchResult
     * @Description 根据id集合批量删除客户供应商
     * @Author 朱小明
     * @Date 2019/7/4 21:25
     * @Param [customerSupplierDto]
     **/
    @Override
    @Caching(evict = {@CacheEvict(value = Constant.RedisCache.CUSTOMER
            , condition = "#customerSupplierDto.getDataType() == 0 and #customerSupplierDto.getIdS().size() == 1 and 1 == #customerSupplierDto.getIsEnterpriseAdmin()", key = "#customerSupplierDto.getIdS().get(0)")
            , @CacheEvict(value = Constant.RedisCache.CUSTOMER
            , condition = "#customerSupplierDto.getDataType() == 0 and #customerSupplierDto.getIdS().size() > 1 and 1 == #customerSupplierDto.getIsEnterpriseAdmin()", allEntries = true),
            @CacheEvict(value = Constant.RedisCache.SUPPLIER
                    , condition = "#customerSupplierDto.getDataType() == 1 and #customerSupplierDto.getIdS().size() == 1 and 1 == #customerSupplierDto.getIsEnterpriseAdmin()", key = "#customerSupplierDto.getIdS().get(0)")
            , @CacheEvict(value = Constant.RedisCache.SUPPLIER
            , condition = "#customerSupplierDto.getDataType() == 1 and #customerSupplierDto.getIdS().size() > 1 and 1 == #customerSupplierDto.getIsEnterpriseAdmin()", allEntries = true)})
    @Transactional(rollbackFor = Exception.class)
    public BatchResult updateBatchDelete(CustomerSupplierDto customerSupplierDto) {
        BatchResult batchResult = new BatchResult();
        List<Long> idList = new ArrayList<>();
        idList.addAll(customerSupplierDto.getIdS());
        if (CollectionUtils.isNotEmpty(idList)) {
            if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())) {
                deleteBatchCustomer(customerSupplierDto, batchResult, idList);
            } else {
                deleteBatchSupplier(customerSupplierDto, batchResult, idList);
            }
        }
        return batchResult;
    }

    /**
     * @param customerSupplierDto
     * @param batchResult
     * @param idList
     * @return void
     * @Description 批量更新处理供应商
     * @Author 朱小明
     * @Date 2019/10/9
     **/
    private void deleteBatchSupplier(CustomerSupplierDto customerSupplierDto, BatchResult batchResult, List<Long> idList) {
        filterSupplier(customerSupplierDto, batchResult, idList, Constant.MenuDefine.SUPPLIER_ITEM_DELETE);
        Supplier supplier = new Supplier();
        supplier.setIsDel(Constant.Is.YES);
        FastUtils.updateBatch(supplierMapper, supplier, Constant.ColumnName.ID, idList, null);
        batchResult.setSuccessList(idList);
        updateCustomerSupplierCoByMianId(false, idList, Constant.UpdateStatus.DELETE);
    }

    /**
     * @param customerSupplierDto
     * @param batchResult
     * @param idList
     * @param menuDefine
     * @return void
     * @Description 过滤供应商角色，权限，集团数据，已删除数据
     * @Author 朱小明
     * @Date 2019/10/9
     **/
    private void filterSupplier(CustomerSupplierDto customerSupplierDto, BatchResult batchResult, List<Long> idList, String menuDefine) {
        if (Constant.Is.YES.equals(customerSupplierDto.getIsEnterpriseAdmin())) {
            //校验角色权限
            SysUserVo operator = UserUtils.getUserVo();
            ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        } else {
            //过滤集团共享的数据
            FastUtils.filterIdsByGroupId(supplierMapper, idList, batchResult.getFailList());
            if (!idList.isEmpty()) {
                customerSupplierDto.setIdS(idList);
                //过滤掉无权限的数据
                List<CustomerSupplierVo> list = supplierMapper.selectSupplierByParams(customerSupplierDto);
                //权限校验
                FastUtils.copyProperties(ShiroUtils.filterNotPermData(list, menuDefine, new ShiroUtils.CheckPermSupport<CustomerSupplierVo>() {
                    @Override
                    public Long getBusinessId(CustomerSupplierVo csVo) {
                        return csVo.getId();
                    }

                    @Override
                    public Long getCompanyId(CustomerSupplierVo csVo) {
                        return csVo.getCompanyId();
                    }
                }), batchResult);
                //先清空
                idList.clear();
                //遍历
                for (CustomerSupplierVo cs : list) {
                    idList.add(cs.getId());
                }
            }
        }
        if (idList.isEmpty()) {
            return;
        }
        //过滤掉被删除的数据
        FastUtils.filterIds(ResultCode.IS_DEL, supplierMapper, new QueryWrapper<Supplier>().eq(Constant.ColumnName.IS_DEL, Constant.Is.YES), Constant.ColumnName.ID, idList, batchResult.getFailList());
        if (Constant.MenuDefine.SUPPLIER_ITEM_DELETE.equals(menuDefine)) {
            // 查询被引用的记录,有则放入操作失败集合
            ReferenceContext referenceContext = referenceRelationService.isReference(Constant.Reference.SUPPLIER, idList);
            batchResult.getFailList().addAll(referenceContext.getReferences());
            idList.clear();
            idList.addAll(referenceContext.getNotReferences());
            if (idList.isEmpty()) {
                return;
            }
        }

        //查询出需要更新的数据版本号
        List<Integer> versionList = new ArrayList<>();
        for (int i = 0; i < customerSupplierDto.getIdS().size(); i++) {
            if (idList.contains(customerSupplierDto.getIdS().get(i))) {
                versionList.add(customerSupplierDto.getVersions().get(i));
            }
        }
        //过滤版本号不一致的数据
        FastUtils.filterVersionIds(supplierMapper, new QueryWrapper<>(), Constant.ColumnName.ID, idList, versionList, batchResult.getFailList());
        if (idList.isEmpty()) {
            return;
        }
        //过滤被分配的数据
        List<Long> fpFailList = supplierCompanyMapper.selectForFilterFp(idList);
        idList.removeAll(fpFailList);
        ReferenceDescription rd;
        for (Long id : fpFailList) {
            rd = new ReferenceDescription();
            rd.setBusinessId(id);
            rd.setReferenceDescription(ResultCode.DATA_IS_DISTRIBUTE.message);
            batchResult.getFailList().add(rd);
        }
    }

    /**
     * @param customerSupplierDto
     * @param batchResult
     * @param idList
     * @return void
     * @Description 批量更新处理客户
     * @Author 朱小明
     * @Date 2019/10/9
     **/
    private void deleteBatchCustomer(CustomerSupplierDto customerSupplierDto, BatchResult batchResult, List<Long> idList) {
        filterCustomer(customerSupplierDto, batchResult, idList, Constant.MenuDefine.CUSTOMER_ITEM_DELETE);
        Customer customer = new Customer();
        customer.setIsDel(Constant.Is.YES);
        FastUtils.updateBatch(customerMapper, customer, Constant.ColumnName.ID, idList, null);
        batchResult.setSuccessList(idList);
        updateCustomerSupplierCoByMianId(true, idList, Constant.UpdateStatus.DELETE);
    }

    /**
     * @param customerSupplierDto
     * @param batchResult
     * @param idList
     * @param menuDefine
     * @return void
     * @Description 过滤客户角色，权限，集团数据，已删除数据
     * @Author 朱小明
     * @Date 2019/10/9
     **/
    private void filterCustomer(CustomerSupplierDto customerSupplierDto, BatchResult batchResult, List<Long> idList, String menuDefine) {
        if (Constant.Is.YES.equals(customerSupplierDto.getIsEnterpriseAdmin())) {
            //校验角色权限
            SysUserVo operator = UserUtils.getUserVo();
            ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        } else {
            //过滤集团共享的数据
            FastUtils.filterIdsByGroupId(customerMapper, idList, batchResult.getFailList());
            if (!idList.isEmpty()) {
                customerSupplierDto.setIdS(idList);
                //过滤掉无权限的数据
                List<CustomerSupplierVo> list = customerMapper.selectCustomerByParams(customerSupplierDto);
                //权限校验
                FastUtils.copyProperties(ShiroUtils.filterNotPermData(list, menuDefine, new ShiroUtils.CheckPermSupport<CustomerSupplierVo>() {
                    @Override
                    public Long getBusinessId(CustomerSupplierVo csVo) {
                        return csVo.getId();
                    }

                    @Override
                    public Long getCompanyId(CustomerSupplierVo csVo) {
                        return csVo.getCompanyId();
                    }
                }), batchResult);
                //先清空
                idList.clear();
                //遍历
                for (CustomerSupplierVo cs : list) {
                    idList.add(cs.getId());
                }
            }
        }
        if (idList.isEmpty()) {
            return;
        }
        //过滤掉被删除的数据
        FastUtils.filterIds(ResultCode.IS_DEL, customerMapper, new QueryWrapper<Customer>().eq(Constant.ColumnName.IS_DEL, Constant.Is.YES), Constant.ColumnName.ID, idList, batchResult.getFailList());
        if (Constant.MenuDefine.CUSTOMER_ITEM_DELETE.equals(menuDefine)) {
            // 查询被引用的记录,有则放入操作失败集合
            ReferenceContext referenceContext = referenceRelationService.isReference(Constant.Reference.CUSTOMER, idList);
            batchResult.getFailList().addAll(referenceContext.getReferences());
            idList.clear();
            idList.addAll(referenceContext.getNotReferences());
            if (idList.isEmpty()) {
                return;
            }
        }
        //过滤出需要更新的数据版本号
        List<Integer> versionList = new ArrayList<>();
        for (int i = 0; i < customerSupplierDto.getIdS().size(); i++) {
            if (idList.contains(customerSupplierDto.getIdS().get(i))) {
                versionList.add(customerSupplierDto.getVersions().get(i));
            }
        }
        //过滤版本号不一致的数据
        FastUtils.filterVersionIds(customerMapper, new QueryWrapper<>(), Constant.ColumnName.ID, idList, versionList, batchResult.getFailList());
        if (idList.isEmpty()) {
            return;
        }
        //过滤被分配的数据
        List<Long> fpFailList = customerCompanyMapper.selectForFilterFp(idList);
        idList.removeAll(fpFailList);
        ReferenceDescription rd;
        for (Long id : fpFailList) {
            rd = new ReferenceDescription();
            rd.setBusinessId(id);
            rd.setReferenceDescription(ResultCode.DATA_IS_DISTRIBUTE.message);
            batchResult.getFailList().add(rd);
        }
    }

    /**
     * 升级
     * wuweiming
     *
     * @param customerSupplierDto
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer upGradeById(CustomerSupplierDto customerSupplierDto) {
        CustomerSupplierVo customerSupplierVo;
        SysUserVo operator = UserUtils.getUserVo();
        //判断是 客户 还是 供应商
        if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())) {
            //根据id查询客户信息
            customerSupplierVo = customerMapper.selectCustomerById(customerSupplierDto.getId());
            //判断版本号是否相等
            if (!customerSupplierDto.getVersion().equals(customerSupplierVo.getVersion())) {
                throw new ServiceException(ResultCode.IS_CHANGE);
            } else {
                Customer customer = new Customer();
                customer.setId(customerSupplierDto.getId());
                customer.setDataTypes(Constant.dataType.DISTRIBUTION);
                customer.setUpdatorId(operator.getUserId());
                customer.setUpdatorName(operator.getName());
                customer.setUpdateTime(new Date(System.currentTimeMillis() / 1000 * 1000));
                customer.setVersion(customerSupplierVo.getVersion());
                customerMapper.updateById(customer);
                return customer.getVersion();
            }
        } else {
            //根据id查询供应商信息
            customerSupplierVo = supplierMapper.selectSupplierById(customerSupplierDto.getId());
            //判断版本号是否相等
            if (!customerSupplierDto.getVersion().equals(customerSupplierVo.getVersion())) {
                throw new ServiceException(ResultCode.IS_CHANGE);
            } else {
                Supplier supplier = new Supplier();
                supplier.setId(customerSupplierDto.getId());
                supplier.setDataTypes(Constant.dataType.DISTRIBUTION);
                supplier.setUpdatorId(operator.getUserId());
                supplier.setUpdatorName(operator.getName());
                supplier.setUpdateTime(new Date(System.currentTimeMillis() / 1000 * 1000));
                supplier.setVersion(customerSupplierVo.getVersion());
                supplierMapper.updateById(supplier);
                return supplier.getVersion();
            }
        }
    }

    /**
     * @return void
     * @Description 根据主表ID更新子表使用公司与归属公司相同的数据
     * @Author 朱小明
     * @Date 2019/7/24 9:20
     * @Param [notReferences]
     **/
    @Transactional(rollbackFor = Exception.class)
    public void updateCustomerSupplierCoByMianId(Boolean isCustomer, List<Long> idList, int flag) {
        if (idList.isEmpty()) {
            return;
        }
        if (isCustomer) {
            customerCompanyMapper.updateCustomerFromMain(idList, flag);
        } else {
            supplierCompanyMapper.updateSupplierFromMain(idList, flag);
        }

    }

    /**
     * @return com.njwd.support.BatchResult
     * @Description 根据id集合批量禁用和反禁
     * @Author 朱小明
     * @Date 2019/7/4 21:40
     * @Param [customerSupplierDto]
     **/
    @Override
    @Caching(evict = {@CacheEvict(value = Constant.RedisCache.CUSTOMER
            , condition = "#customerSupplierDto.getDataType() == 0 and #customerSupplierDto.getIdS().size() == 1 and 1 == #customerSupplierDto.getIsEnterpriseAdmin()", key = "#customerSupplierDto.getIdS().get(0)")
            , @CacheEvict(value = Constant.RedisCache.CUSTOMER
            , condition = "#customerSupplierDto.getDataType() == 0 and #customerSupplierDto.getIdS().size() > 1 and 1 == #customerSupplierDto.getIsEnterpriseAdmin()", allEntries = true),
            @CacheEvict(value = Constant.RedisCache.SUPPLIER
                    , condition = "#customerSupplierDto.getDataType() == 1 and #customerSupplierDto.getIdS().size() == 1 and 1 == #customerSupplierDto.getIsEnterpriseAdmin()", key = "#customerSupplierDto.getIdS().get(0)")
            , @CacheEvict(value = Constant.RedisCache.SUPPLIER
            , condition = "#customerSupplierDto.getDataType() == 1 and #customerSupplierDto.getIdS().size() > 1 and 1 == #customerSupplierDto.getIsEnterpriseAdmin()", allEntries = true)})
    @Transactional(rollbackFor = Exception.class)
    public BatchResult updateBatchEnable(CustomerSupplierDto customerSupplierDto) {
        BatchResult batchResult = new BatchResult();
        List<Long> idList = new ArrayList<>();
        idList.addAll(customerSupplierDto.getIdS());
        String menuDefine;
        if (CollectionUtils.isNotEmpty(idList)) {
            if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())) {
                if (Constant.Is.NO.equals(customerSupplierDto.getIsEnable())) {
                    menuDefine = Constant.MenuDefine.CUSTOMER_ITEM_DISABLE;
                } else {
                    menuDefine = Constant.MenuDefine.CUSTOMER_ITEM_ENABLE;
                }
                //过滤客户角色，权限，集团数据，已删除数据
                filterCustomer(customerSupplierDto, batchResult, idList, menuDefine);
                if (!idList.isEmpty()) {
                    //过滤启用状态已变更成功的记录,筛选
                    FastUtils.filterIds(Constant.Is.NO.equals(customerSupplierDto.getIsEnable()) ? ResultCode.IS_DISABLE : ResultCode.IS_ENABLE, customerMapper
                            , new QueryWrapper<Customer>().eq(Constant.ColumnName.IS_ENABLE, customerSupplierDto.getIsEnable()), Constant.ColumnName.ID, idList, batchResult.getFailList());
                    //更新状态
                    batchResult.setSuccessList(idList);
                    if (!idList.isEmpty()) {
                        toBatch(true, customerSupplierDto, idList);
                    }
                }
            } else {
                if (Constant.Is.NO.equals(customerSupplierDto.getIsEnable())) {
                    menuDefine = Constant.MenuDefine.SUPPLIER_ITEM_DISABLE;
                } else {
                    menuDefine = Constant.MenuDefine.SUPPLIER_ITEM_ENABLE;
                }
                //过滤供应商角色，权限，集团数据，已删除数据
                filterSupplier(customerSupplierDto, batchResult, idList, menuDefine);
                if (!idList.isEmpty()) {
                    //过滤启用状态已变更成功的记录,筛选
                    FastUtils.filterIds(Constant.Is.NO.equals(customerSupplierDto.getIsEnable()) ? ResultCode.IS_DISABLE : ResultCode.IS_ENABLE, supplierMapper
                            , new QueryWrapper<Supplier>().eq(Constant.ColumnName.IS_ENABLE, customerSupplierDto.getIsEnable()), Constant.ColumnName.ID, idList, batchResult.getFailList());
                    batchResult.setSuccessList(idList);
                    if (!idList.isEmpty()) {
                        toBatch(false, customerSupplierDto, idList);
                    }
                }
            }
        }
        return batchResult;
    }

    /**
     * @return java.lang.Long
     * @Description 更新项目
     * @Author 朱小明
     * @Date 2019/6/19 9:49
     * @Param [customerSupplierDto]
     **/
    @Transactional(rollbackFor = Exception.class)
    @Override
    @Caching(evict = {@CacheEvict(value = Constant.RedisCache.CUSTOMER, key = "#customerSupplierDto.id", condition = "#customerSupplierDto.getDataType() == 0"),
            @CacheEvict(value = Constant.RedisCache.SUPPLIER, key = "#customerSupplierDto.id", condition = "#customerSupplierDto.getDataType() == 1")})
    public int updateCustomerSupplierById(CustomerSupplierDto customerSupplierDto) {
        //校验数据是重复数据
        if (checkDuplicate(customerSupplierDto)) {
            if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())) {
                //员工批量操作校验数据操作权限
                if (Constant.Is.NO.equals(customerSupplierDto.getIsEnterpriseAdmin())) {
                    List<ReferenceDescription> failList = new ArrayList<>();
                    List<Long> ids = new ArrayList<>();
                    ids.add(customerSupplierDto.getId());
                    FastUtils.filterIdsByGroupId(customerMapper, ids, failList);
                }
                Customer c = customerMapper.selectById(customerSupplierDto.getId());
                if (!customerSupplierDto.getVersion().equals(c.getVersion())) {
                    throw new ServiceException(ResultCode.IS_CHANGE);
                }
                //查询子表信息
                List<CustomerCompanyVo> customerCompanyList = customerCompanyMapper.findCustomerCompanyList(customerSupplierDto);
                CustomerCompany customerCompany = new CustomerCompany();
                if (customerCompanyList.size() == 1 && !customerCompanyList.get(0).getCompanyId().equals(customerSupplierDto.getCompanyId())) {
                    customerCompany.setId(customerCompanyList.get(0).getId());
                    customerCompany.setUpdatorId(customerSupplierDto.getUpdatorId());
                    customerCompany.setUpdatorName(customerSupplierDto.getUpdatorName());
                    customerCompany.setUpdateTime(new Date(System.currentTimeMillis() / 1000 * 1000));
                    customerCompany.setVersion(customerCompanyList.get(0).getVersion());
                    if (Constant.BlocInfo.BLOCID.equals(customerSupplierDto.getCompanyId())) {
                        //私有数据变更为集团数据,更新子表的is_del
                        customerCompany.setIsDel(Constant.Is.YES);
                        customerCompanyMapper.updateById(customerCompany);
                    } else {
                        //创建公司更改,更新子表的company_id
                        customerCompany.setCompanyId(customerSupplierDto.getCompanyId());
                        customerCompanyMapper.updateById(customerCompany);
                    }
                } else if (customerCompanyList.size() == 0 && customerSupplierDto.getDataTypes().equals(Constant.dataType.PRIVATE)) {
                    //集团数据更改为公司私有型,新增子表数据
                    customerCompany.setCustomerId(customerSupplierDto.getId());
                    customerCompany.setCompanyId(customerSupplierDto.getCompanyId());
                    customerCompany.setCreatorId(UserUtils.getUserVo().getUserId());
                    customerCompany.setCreatorName(UserUtils.getUserVo().getName());
                    customerCompanyMapper.insert(customerCompany);
                }
                Customer customer = new Customer();
                FastUtils.copyPropertiesExistsBlank(customerSupplierDto, customer);
                customer.setUpdateTime(new Date(System.currentTimeMillis() / 1000 * 1000));
                customer.setVersion(c.getVersion());
                return customerMapper.updateById(customer);
            } else {
                //员工批量操作校验数据操作权限
                if (Constant.Is.NO.equals(customerSupplierDto.getIsEnterpriseAdmin())) {
                    List<ReferenceDescription> failList = new ArrayList<>();
                    List<Long> ids = new ArrayList<>();
                    ids.add(customerSupplierDto.getId());
                    FastUtils.filterIdsByGroupId(supplierMapper, ids, failList);
                }
                Supplier s = supplierMapper.selectById(customerSupplierDto.getId());
                if (!customerSupplierDto.getVersion().equals(s.getVersion())) {
                    throw new ServiceException(ResultCode.IS_CHANGE);
                }
                //查询子表信息
                List<SupplierCompanyVo> supplierCompanyList = supplierCompanyMapper.findSupplierCompanyList(customerSupplierDto);
                SupplierCompany supplierCompany = new SupplierCompany();
                if (supplierCompanyList.size() == 1 && !supplierCompanyList.get(0).getCompanyId().equals(customerSupplierDto.getCompanyId())) {
                    supplierCompany.setId(supplierCompanyList.get(0).getId());
                    supplierCompany.setUpdatorId(customerSupplierDto.getUpdatorId());
                    supplierCompany.setUpdatorName(customerSupplierDto.getUpdatorName());
                    supplierCompany.setUpdateTime(new Date(System.currentTimeMillis() / 1000 * 1000));
                    supplierCompany.setVersion(supplierCompanyList.get(0).getVersion());
                    if (Constant.BlocInfo.BLOCID.equals(customerSupplierDto.getCompanyId())) {
                        //私有数据变更为集团数据,更新子表的is_del
                        supplierCompany.setIsDel(Constant.Is.YES);
                        supplierCompanyMapper.updateById(supplierCompany);
                    } else {
                        //创建公司更改,同时更新子表的company_id
                        supplierCompany.setCompanyId(customerSupplierDto.getCompanyId());
                        supplierCompanyMapper.updateById(supplierCompany);
                    }
                } else if (supplierCompanyList.size() == 0 && customerSupplierDto.getDataTypes().equals(Constant.dataType.PRIVATE)) {
                    //集团数据更改为公司私有型,新增子表数据
                    supplierCompany.setSupplierId(customerSupplierDto.getId());
                    supplierCompany.setCompanyId(customerSupplierDto.getCompanyId());
                    supplierCompany.setCreatorId(UserUtils.getUserVo().getUserId());
                    supplierCompany.setCreatorName(UserUtils.getUserVo().getName());
                    supplierCompanyMapper.insert(supplierCompany);
                }
                Supplier supplier = new Supplier();
                FastUtils.copyPropertiesExistsBlank(customerSupplierDto, supplier);
                supplier.setUpdateTime(new Date(System.currentTimeMillis() / 1000 * 1000));
                supplier.setVersion(s.getVersion());
                return supplierMapper.updateById(supplier);
            }
        } else {
            //有重复，则抛出异常并把重复字段返回给前台
            throw new ServiceException(ResultCode.COLUMN_EXIST, customerSupplierDto.getRcSet());
        }
    }

    /**
     * @return int
     * @Description 根据ID更新数据
     * USER:以使用公司维度更新客户供应商
     * @Author 朱小明
     * @Date 2019/6/28 16:02
     * @Param [customerSupplierDto]
     */
    @Override
    public int updateCustomerSupplierCoById(CustomerSupplierDto customerSupplierDto) {
        int count;
        if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())) {
            List<Long> ids = new ArrayList<>();
            ids.add(customerSupplierDto.getId());
            List<ReferenceDescription> failList = new ArrayList<>();
            if (Constant.Is.NO.equals(customerSupplierDto.getIsEnterpriseAdmin())) {
                FastUtils.filterIdsByGroupId(customerMapper, ids, failList);
                ShiroUtils.checkPerm(Constant.MenuDefine.CUSTOMER_ITEM_EDIT, customerSupplierDto.getCompanyId());
            }
            count = customerSupplierService.updateCustomerSupplierById(customerSupplierDto);
        } else {
            List<Long> ids = new ArrayList<>();
            ids.add(customerSupplierDto.getId());
            List<ReferenceDescription> failList = new ArrayList<>();
            if (Constant.Is.NO.equals(customerSupplierDto.getIsEnterpriseAdmin())) {
                FastUtils.filterIdsByGroupId(supplierMapper, ids, failList);
                ShiroUtils.checkPerm(Constant.MenuDefine.SUPPLIER_ITEM_EDIT, customerSupplierDto.getCompanyId());
            }
            count = customerSupplierService.updateCustomerSupplierById(customerSupplierDto);
        }
        return count;
    }

    /**
     * @param id
     * @return com.njwd.entity.basedata.CustomerSupplier
     * @Description 根据ID查询客户数据
     * @Author 朱小明
     * @Date 2019/6/26 14:55
     * @Param [customerSupplierDto]
     */
    @Override
    public CustomerSupplierVo findCustomerById(Long id) {
        CustomerSupplierVo vo = getCustomerCoById(id);
        List<Long> ids = new ArrayList<>();
        CustomerSupplierCompanyDto dto = new CustomerSupplierCompanyDto();
        ids.add(vo.getId());
        //查询使用公司
        dto.setCustomerIds(ids);
        List<CustomerCompanyVo> list = customerCompanyMapper.findCustomerUseCompanysByCustomerIds(dto);
        //拼接使用公司信息
        for (CustomerCompanyVo cc : list) {
            if (vo.getId().equals(cc.getCustomerId()) && !Constant.BlocInfo.BLOCID.equals(cc.getCompanyId())) {
                vo.setUseCompanyIdString(cc.getUseCompanyIdString());
            }
            if (vo.getUseCompanyIdString() == null) {
                vo.setUseCompanyIdString(vo.getCompanyId().toString());
            } else if (!Arrays.asList(vo.getUseCompanyIdString().split(",")).contains(vo.getCompanyId().toString())) {
                vo.setUseCompanyIdString(vo.getUseCompanyIdString().concat(",").concat(vo.getCompanyId().toString()));
            }
        }

        //校验数据是否被引用
        ReferenceResult referenceResult = referenceRelationService.isReference(Constant.Reference.CUSTOMER, id);
        if (referenceResult.isReference()) {
            //有引用数据
            vo.setIsCite(Constant.Is.YES);
        } else {
            //先根据id查询客户信息
            CustomerSupplierVo customer = customerMapper.selectCustomerById(id);
            //判断是否被分配
            CustomerSupplierCompanyDto cscDto = new CustomerSupplierCompanyDto();
            cscDto.setCompanyId(customer.getCompanyId());
            cscDto.setCustomerId(id);
            Integer count = customerCompanyMapper.findCustomerCompanyInfosWithOutSelf(cscDto);
            if (count > 0) {
                vo.setIsCite(Constant.Is.YES);
            } else {
                vo.setIsCite(Constant.Is.NO);
            }
        }
        return vo;
    }

    /**
     * 根据id查询客户供应商信息
     *
     * @param id
     * @return
     */
//    @Cacheable(value= Constant.RedisCache.CUSTOMER, key="#id")
    public CustomerSupplierVo getCustomerCoById(Long id) {
        CustomerSupplierVo vo = customerMapper.selectCustomerById(id);

        //校验数据是否被引用
        ReferenceResult referenceResult = referenceRelationService.isReference(Constant.Reference.CUSTOMER, id);
        if (referenceResult.isReference()) {
            //有引用数据
            vo.setIsCite(Constant.Is.YES);
        } else {
            //先根据id查询客户信息
            CustomerSupplierVo customer = customerMapper.selectCustomerById(id);
            //判断是否被分配
            CustomerSupplierCompanyDto cscDto = new CustomerSupplierCompanyDto();
            cscDto.setCompanyId(customer.getCompanyId());
            cscDto.setSupplierId(id);
            Integer count = customerCompanyMapper.findCustomerCompanyInfosWithOutSelf(cscDto);
            if (count > 0) {
                vo.setIsCite(Constant.Is.YES);
            } else {
                vo.setIsCite(Constant.Is.NO);
            }
        }
        return vo;
    }

    /**
     * @param id
     * @return com.njwd.entity.basedata.CustomerSupplier
     * @Description 根据ID查询客户数据
     * @Author 朱小明
     * @Date 2019/6/26 14:55
     * @Param [customerSupplierDto]
     */
    @Override
//    @Cacheable(value= Constant.RedisCache.SUPPLIER, key="#id")
    public CustomerSupplierVo findSupplierById(Long id) {
        CustomerSupplierVo vo = supplierMapper.selectSupplierById(id);
        List<Long> ids = new ArrayList<>();
        CustomerSupplierCompanyDto dto = new CustomerSupplierCompanyDto();
        ids.add(vo.getId());
        //查询使用公司
        dto.setCustomerIds(ids);
        List<SupplierCompanyVo> list = supplierCompanyMapper.findSupplierUseCompanysBySupplierIds(dto);
        //拼接使用公司信息
        for (SupplierCompanyVo sc : list) {
            if (vo.getId().equals(sc.getSupplierId()) && !Constant.BlocInfo.BLOCID.equals(sc.getCompanyId())) {
                vo.setUseCompanyIdString(sc.getUseCompanyIdString());
            }
            if (vo.getUseCompanyIdString() == null) {
                vo.setUseCompanyIdString(vo.getCompanyId().toString());
            } else if (!Arrays.asList(vo.getUseCompanyIdString().split(",")).contains(vo.getCompanyId().toString())) {
                vo.setUseCompanyIdString(vo.getUseCompanyIdString().concat(",").concat(vo.getCompanyId().toString()));
            }
        }
        //校验数据是否被引用
        ReferenceResult referenceResult = referenceRelationService.isReference(Constant.Reference.SUPPLIER, id);
        if (referenceResult.isReference()) {
            //有引用数据
            vo.setIsCite(Constant.Is.YES);
        } else {
            //先根据id查询供应商信息
            CustomerSupplierVo supplier = supplierMapper.selectSupplierById(id);
            //判断是否被分配
            CustomerSupplierCompanyDto cscDto = new CustomerSupplierCompanyDto();
            cscDto.setCompanyId(supplier.getCompanyId());
            cscDto.setSupplierId(id);
            Integer count = supplierCompanyMapper.findSupplierCompanyInfosWithOutSelf(cscDto);
            if (count > 0) {
                vo.setIsCite(Constant.Is.YES);
            } else {
                vo.setIsCite(Constant.Is.NO);
            }
        }
        return vo;
    }

    /**
     * @param id
     * @return com.njwd.entity.basedata.vo.CustomerSupplierVo
     * @Description 根据ID查询客户数据
     * USER:以使用公司维度查询客户
     * @Author 朱小明
     * @Date 2019/6/27 13:23
     * @Param [customerSupplierDto]
     */
    @Override
    public CustomerSupplierVo findCustomerCoById(Long id) {
        //CustomerSupplierVo vo = customerMapper.selectCustomerCoById(id);
        CustomerSupplierVo vo = customerMapper.selectCustomerById(id);
        List<Long> ids = new ArrayList<>();
        CustomerSupplierCompanyDto dto = new CustomerSupplierCompanyDto();
        ids.add(vo.getId());
        //查询使用公司
        dto.setCustomerIds(ids);
        List<CustomerCompanyVo> list = customerCompanyMapper.findCustomerUseCompanysByCustomerIds(dto);
        //拼接使用公司信息
        for (CustomerCompanyVo cc : list) {
            if (vo.getId().equals(cc.getCustomerId()) && !Constant.BlocInfo.BLOCID.equals(cc.getCompanyId())) {
                vo.setUseCompanyIdString(cc.getUseCompanyIdString());
            }
            if (vo.getUseCompanyIdString() == null) {
                vo.setUseCompanyIdString(vo.getCompanyId().toString());
            } else if (!Arrays.asList(vo.getUseCompanyIdString().split(",")).contains(vo.getCompanyId().toString())) {
                vo.setUseCompanyIdString(vo.getUseCompanyIdString().concat(",").concat(vo.getCompanyId().toString()));
            }
        }
        //校验数据是否被引用
        ReferenceResult referenceResult = referenceRelationService.isReference(Constant.Reference.CUSTOMER, id);
        if (referenceResult.isReference()) {
            //有引用数据
            vo.setIsCite(Constant.Is.YES);
        } else {
            //先根据id查询客户信息
            CustomerSupplierVo customer = customerMapper.selectCustomerById(id);
            //判断是否被分配
            CustomerSupplierCompanyDto cscDto = new CustomerSupplierCompanyDto();
            cscDto.setCompanyId(customer.getCompanyId());
            cscDto.setCustomerId(id);
            Integer count = customerCompanyMapper.findCustomerCompanyInfosWithOutSelf(cscDto);
            if (count > 0) {
                vo.setIsCite(Constant.Is.YES);
            } else {
                vo.setIsCite(Constant.Is.NO);
            }
        }
        return vo;
    }

    /**
     * @param id
     * @return com.njwd.entity.basedata.vo.CustomerSupplierVo
     * @Description 根据ID查询供应商数据
     * USER:以使用公司维度查询供应商
     * @Author 朱小明
     * @Date 2019/6/27 13:23
     * @Param [customerSupplierDto]
     */
    @Override
    public CustomerSupplierVo findSupplierCoById(Long id) {
        //CustomerSupplierVo vo = supplierMapper.selectSupplierCoById(id);
        CustomerSupplierVo vo = supplierMapper.selectSupplierById(id);
        List<Long> ids = new ArrayList<>();
        CustomerSupplierCompanyDto dto = new CustomerSupplierCompanyDto();
        ids.add(vo.getId());
        //查询使用公司
        dto.setCustomerIds(ids);
        List<SupplierCompanyVo> list = supplierCompanyMapper.findSupplierUseCompanysBySupplierIds(dto);
        //拼接使用公司信息
        for (SupplierCompanyVo sc : list) {
            if (vo.getId().equals(sc.getSupplierId()) && !Constant.BlocInfo.BLOCID.equals(sc.getCompanyId())) {
                vo.setUseCompanyIdString(sc.getUseCompanyIdString());
            }
            if (vo.getUseCompanyIdString() == null) {
                vo.setUseCompanyIdString(vo.getCompanyId().toString());
            } else if (!Arrays.asList(vo.getUseCompanyIdString().split(",")).contains(vo.getCompanyId().toString())) {
                vo.setUseCompanyIdString(vo.getUseCompanyIdString().concat(",").concat(vo.getCompanyId().toString()));
            }
        }
        //校验数据是否被引用
        ReferenceResult referenceResult = referenceRelationService.isReference(Constant.Reference.SUPPLIER, id);
        if (referenceResult.isReference()) {
            //有引用数据
            vo.setIsCite(Constant.Is.YES);
        } else {
            //先根据id查询供应商信息
            CustomerSupplierVo supplier = supplierMapper.selectSupplierById(id);
            //判断是否被分配
            CustomerSupplierCompanyDto cscDto = new CustomerSupplierCompanyDto();
            cscDto.setCompanyId(supplier.getCompanyId());
            cscDto.setSupplierId(id);
            Integer count = supplierCompanyMapper.findSupplierCompanyInfosWithOutSelf(cscDto);
            if (count > 0) {
                vo.setIsCite(Constant.Is.YES);
            } else {
                vo.setIsCite(Constant.Is.NO);
            }
        }
        return vo;
    }

    /**
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page
     * <com.njwd.entity.basedata.vo.CustomerSupplierVo>
     * @Description 以使用公司维度查询客户供应商
     * @Author 朱小明
     * @Date 2019/6/28 11:40
     * @Param [customerSupplierDto]
     **/
    @Override
    public Page<CustomerSupplierVo> findCustomerSupplierCoPage(CustomerSupplierDto customerSupplierDto) {
        Page<CustomerSupplierVo> page = new Page<>();
        List<Long> ids = new ArrayList<>();
        List<Long> companyList;
        CustomerSupplierCompanyDto dto = new CustomerSupplierCompanyDto();
        if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())) {
            companyList = ShiroUtils.filterPerm(Constant.MenuDefine.CUSTOMER_ITEM_FIND, customerSupplierDto.getCompanyList());
            if (CollectionUtils.isEmpty(companyList)) {
                return page;
            }
            customerSupplierDto.setCompanyList(companyList);
            page = customerMapper.selectCustomerCoPage(customerSupplierDto.getPage(), customerSupplierDto);
        } else {
            companyList = ShiroUtils.filterPerm(Constant.MenuDefine.SUPPLIER_ITEM_FIND, customerSupplierDto.getCompanyList());
            if (CollectionUtils.isEmpty(companyList)) {
                return page;
            }
            customerSupplierDto.setCompanyList(companyList);
            page = supplierMapper.selectSupplierCoPage(customerSupplierDto.getPage(), customerSupplierDto);
            for (CustomerSupplierVo vo : page.getRecords()) {
                ids.add(vo.getId());
            }
            //查询使用公司
            dto.setCustomerIds(ids);
            List<SupplierCompanyVo> list = supplierCompanyMapper.findSupplierUseCompanysBySupplierIds(dto);
            //拼接使用公司信息
            for (CustomerSupplierVo vo : page.getRecords()) {
                for (SupplierCompanyVo sc : list) {
                    if (vo.getId().equals(sc.getSupplierId()) && !Constant.BlocInfo.BLOCID.equals(sc.getCompanyId())) {
                        vo.setUseCompanyIdString(sc.getUseCompanyIdString());
                    }
                }
            }
        }
        return page;
    }

    /**
     * @return com.baomidou.mybatisplus.extension.plugins.pagination
     * .Page<com.njwd.entity.basedata.vo.CustomerSupplierVo>
     * @Description 查询列表（分页）
     * @Author 朱小明
     * @Date 2019/6/18 13:44
     * @Param [customerSupplierDto]
     */
    @Override
    public Page<CustomerSupplierVo> findCustomerSupplierPage(CustomerSupplierDto customerSupplierDto) {
        Page<CustomerSupplierVo> page;
        List<Long> ids = new ArrayList<>();
        CustomerSupplierCompanyDto dto = new CustomerSupplierCompanyDto();
        //对应数据库实体
        if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())) {
            page = customerMapper.selectCustomerVoPage(customerSupplierDto.getPage(), customerSupplierDto);
            for (CustomerSupplierVo vo : page.getRecords()) {
                ids.add(vo.getId());
            }
            //查询使用公司
            dto.setCustomerIds(ids);
            List<CustomerCompanyVo> list = customerCompanyMapper.findCustomerUseCompanysByCustomerIds(dto);
            //拼接使用公司信息
            for (CustomerSupplierVo vo : page.getRecords()) {
                for (CustomerCompanyVo cc : list) {
                    //!Arrays.asList(cc.getUseCompanyIdString().split(",")).contains(vo.getCompanyId().toString())
                    if (vo.getId().equals(cc.getCustomerId()) && !Constant.BlocInfo.BLOCID.equals(cc.getCompanyId())) {
                        vo.setUseCompanyIdString(cc.getUseCompanyIdString());
                    }
                }
//                if (vo.getUseCompanyIdString() == null){
//                    vo.setUseCompanyIdString(vo.getCompanyId().toString());
//                }else if (!Arrays.asList(vo.getUseCompanyIdString().split(",")).contains(vo.getCompanyId().toString())){
//                    vo.setUseCompanyIdString(vo.getUseCompanyIdString().concat(",").concat(vo.getCompanyId().toString()));
//                }
            }
        } else {
            page = supplierMapper.selectSupplierVoPage(customerSupplierDto.getPage(), customerSupplierDto);
            for (CustomerSupplierVo vo : page.getRecords()) {
                ids.add(vo.getId());
            }
            //查询使用公司
            dto.setCustomerIds(ids);
            List<SupplierCompanyVo> list = supplierCompanyMapper.findSupplierUseCompanysBySupplierIds(dto);
            //拼接使用公司信息
            for (CustomerSupplierVo vo : page.getRecords()) {
                for (SupplierCompanyVo sc : list) {
                    if (vo.getId().equals(sc.getSupplierId()) && !Constant.BlocInfo.BLOCID.equals(sc.getCompanyId())) {
                        vo.setUseCompanyIdString(sc.getUseCompanyIdString());
                    }
                }
            }
        }

        return page;
    }

    /**
     * @return 返回 true:重复  false:不重复
     * @Description 校验要插入客户供应商表中的编码、名称、身份证、社会统一信用码是否唯一
     * 供应商和客户两种类型可以有相同编码、名称、身份证、社会统一信用码
     * @Author 朱小明
     * @Date 2019/6/14 9:40
     * @Param [customerSupplier]
     **/
    @Override
    public Boolean checkDuplicate(CustomerSupplierDto customerSupplierDto) {
        //对应数据库实体
        //拷贝DTO数据到数据库实体
        Boolean uniqueFlag = false;
        //code校验
        if (checkCode(customerSupplierDto)
                //名称校验
                & checkName(customerSupplierDto)
                //身份证校验
                & checkIdCardNum(customerSupplierDto)
                //社会统一信用码校验
                & checkUnifiedSocialCreditCode(customerSupplierDto)) {
            uniqueFlag = true;
        }
        return uniqueFlag;
    }

    /**
     * @return void
     * @Description 导出excel
     * ADMIN:以归属公司维度查询客户供应商
     * @Author 朱小明
     * @Date 2019/7/1 13:12
     * @Param [customerSupplierDto, response]
     **/
    @Override
    public void exportExcelForAdmin(CustomerSupplierDto customerSupplierDto, HttpServletResponse response) {
        Page<CustomerSupplierVo> page = new Page<>();
        fileService.resetPage(page);
        if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())) {
            Page<CustomerSupplierVo> list = customerMapper.selectCustomerVoPage(page, customerSupplierDto);
            exportExcel(response, customerSupplierDto, list);
        } else {
            Page<CustomerSupplierVo> list = supplierMapper.selectSupplierVoPage(page, customerSupplierDto);
            exportExcel(response, customerSupplierDto, list);
        }
    }

    /**
     * @param customerSupplierDto
     * @param response
     * @return void
     * @Description 管理员(user画面)导出数据
     * @Author 朱小明
     * @Date 2019/6/28 10:30
     * @Param [customerSupplierDto, response]
     */
    @Override
    public void exportExcelForUser(CustomerSupplierDto customerSupplierDto, HttpServletResponse response) {
        Page<CustomerSupplierVo> page = new Page<>();
        fileService.resetPage(page);
        List<Long> companyList;
        Page<CustomerSupplierVo> list = new Page<>();
        if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())) {
            companyList = ShiroUtils.filterPerm(Constant.MenuDefine.CUSTOMER_ITEM_FIND, customerSupplierDto.getCompanyList());
            if (CollectionUtils.isNotEmpty(companyList)) {
                customerSupplierDto.setCompanyList(companyList);
                list = customerMapper.selectCustomerCoPage(page, customerSupplierDto);
            }
            exportExcel(response, customerSupplierDto, list);
        } else {
            companyList = ShiroUtils.filterPerm(Constant.MenuDefine.SUPPLIER_ITEM_FIND, customerSupplierDto.getCompanyList());
            if (CollectionUtils.isNotEmpty(companyList)) {
                customerSupplierDto.setCompanyList(companyList);
                list = supplierMapper.selectSupplierCoPage(page, customerSupplierDto);
            }
            exportExcel(response, customerSupplierDto, list);
        }
    }

    /**
     * @return java.lang.Long
     * @Description 使用公司校验
     * @Author 朱小明
     * @Date 2019/7/3 20:04
     * @Param [customerSupplierCompany]
     **/
    private Long checkUseCompany(CustomerCompany customerCompany, SupplierCompany supplierCompany) {
        if (customerCompany != null) {
            return customerCompanyMapper.selectCustomerIdForCheckUseCompany(customerCompany);
        } else {
            return supplierCompanyMapper.selectSupplierIdForCheckUseCompany(supplierCompany);
        }

    }

    /**
     * @return false:正常
     * @Description 校验社会统一信用码
     * @Author 朱小明
     * @Date 2019/6/26 21:01
     * @Param [customerSupplierDto]
     **/
    private boolean checkUnifiedSocialCreditCode(CustomerSupplierDto customerSupplierDto) {
        if (StringUtil.isEmpty(customerSupplierDto.getUnifiedSocialCreditCode())) {
            return true;
        }
        int count;
        if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())) {
            count = customerMapper.selectCustomerCountForCheckUscc(customerSupplierDto);
        } else {
            count = supplierMapper.selectSupplierCountForCheckUscc(customerSupplierDto);
        }
        if (count == 0) {
            return true;
        } else {
            customerSupplierDto.getRcSet().add(Constant.EntityName.UNIFIED_SOCIAL_CREDIT_CODE);
            return false;
        }
    }

    /**
     * @return false:正常
     * @Description 校验身份证号
     * @Author 朱小明
     * @Date 2019/6/26 21:01
     * @Param [customerSupplierDto]
     **/
    private boolean checkIdCardNum(CustomerSupplierDto customerSupplierDto) {
        if (StringUtil.isEmpty(customerSupplierDto.getIdCardNum())) {
            return true;
        }
        int count;
        if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())) {
            count = customerMapper.selectCustomerCountForCheckIdCard(customerSupplierDto);
        } else {
            count = supplierMapper.selectSupplierCountForCheckIdCard(customerSupplierDto);
        }
        if (count == 0) {
            return true;
        } else {
            customerSupplierDto.getRcSet().add(Constant.EntityName.ID_CARD_NUM);
            return false;
        }
    }


    /**
     * @return false:正常
     * @Description 校验名称
     * @Author 朱小明
     * @Date 2019/6/26 21:01
     * @Param [customerSupplierDto]
     **/
    private boolean checkName(CustomerSupplierDto customerSupplierDto) {
        if (StringUtil.isEmpty(customerSupplierDto.getName())) {
            return true;
        }
        int count;
        if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())) {
            count = customerMapper.selectCustomerCountForCheckName(customerSupplierDto);
        } else {
            count = supplierMapper.selectSupplierCountForCheckName(customerSupplierDto);
        }
        if (count == 0) {
            return true;
        } else {
            customerSupplierDto.getRcSet().add(Constant.EntityName.NAME);
            return false;
        }
    }

    /**
     * @return false:正常
     * @Description 校验编码
     * @Author 朱小明
     * @Date 2019/6/26 21:01
     * @Param [customerSupplierDto]
     **/
    private boolean checkCode(CustomerSupplierDto customerSupplierDto) {
        if (StringUtil.isEmpty(customerSupplierDto.getCode())) {
            return true;
        }
        int count;
        if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())) {
            count = customerMapper.selectCustomerCountForCheckCode(customerSupplierDto);
        } else {
            count = supplierMapper.selectSupplierCountForCheckCode(customerSupplierDto);
        }

        if (count == 0) {
            return true;
        } else {
            customerSupplierDto.getRcSet().add(Constant.EntityName.CODE);
            return false;
        }
    }

    /**
     * @return boolean
     * @Description 校验生成并生成编码
     * @Author 朱小明
     * @Date 2019/6/25 13:41
     * @Param [code]
     **/
    private void setCode(CustomerSupplierDto customerSupplierDto) {
        //根据数据类型设置编码
        String csPre;
        if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())) {
            csPre = Constant.BaseCodeRule.CUSTOMER;
        } else {
            csPre = Constant.BaseCodeRule.SUPPLIER;
        }
        while (true) {
            customerSupplierDto.setCode(sequenceService.getCode(
                    csPre, Constant.BaseCodeRule.LENGTH_FOUR
                    , customerSupplierDto.getRootEnterpriseId()
                    , Constant.BaseCodeRule.ENTERPRISE));
            if (checkCode(customerSupplierDto)) {
                break;
            }
        }
    }

    /**
     * @return void
     * @Description 获取客户供应商平台id设置为全局ID
     * @Author 朱小明
     * @Date 2019/7/5 14:25
     * @Param [customerSupplierDto]
     **/
    private void setGlobalId(CustomerSupplierDto customerSupplierDto) {
        SysUserVo suv = UserUtils.getUserVo();
        //设置调用客户供应商平台接口参数
        CustomerSupplierParam customerSupplierParam = new CustomerSupplierParam();
        customerSupplierParam.setCreator_id(suv.getUserId().toString());
        customerSupplierParam.setCreator_name(suv.getName());
        customerSupplierParam.setName(customerSupplierDto.getName());
        customerSupplierParam.setRoot_enterprise_id(customerSupplierDto.getRootEnterpriseId().toString());
        customerSupplierParam.setUniCode(customerSupplierDto.getUnifiedSocialCreditCode());
        customerSupplierParam.setAddress(customerSupplierDto.getBusinessAddress());
        customerSupplierParam.setLinktel(customerSupplierDto.getContactNumber());
        try {
            //调用客户供应商平台接口
            String result = HttpUtils.restGetWithJson(ymlProperties.getNjwdCoreUrl()
                    + Constant.Url.FINT_CUSTOMER_SUPPLIER_GLOBAL_ID, String.class, JsonUtils.object2Json(customerSupplierParam));
            Map<String, Object> resultMap = JsonUtils.json2Pojo(result, Map.class);
            //获取ID并转换成LONG
            Long globalId = Optional.ofNullable(resultMap).map(maps -> maps.get("id")).map(id -> Long.valueOf(id.toString())).get();
            customerSupplierDto.setGlobalId(globalId);
        } catch (Exception e) {
            throw new ServiceException(ResultCode.CUSTOMER_SUPPLIER_PLATFORM_FAIL);
        }
    }

    /**
     * @return void
     * @Description 根据数据类型导入数据
     * @Author 朱小明
     * @Date 2019/7/17 11:03
     * @Param [response, customerSupplierDto, list]
     **/
    private void exportExcel(HttpServletResponse response, CustomerSupplierDto customerSupplierDto, Page<CustomerSupplierVo> list) {
        if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())) {
            //fileService.exportExcel(response,list.getRecords(), MenuCodeConstant.CUSTOMER);
            fileService.exportExcel(response, list.getRecords(), MenuCodeConstant.CUSTOMER, customerSupplierDto.getIsEnterpriseAdmin());
        } else {
            //fileService.exportExcel(response,list.getRecords(), MenuCodeConstant.SUPPLIER);
            fileService.exportExcel(response, list.getRecords(), MenuCodeConstant.SUPPLIER, customerSupplierDto.getIsEnterpriseAdmin());
        }
    }

    /**
     * @return void
     * @Description 批量启用禁用方法提取
     * @Author 朱小明
     * @Date 2019/7/19 10:11
     * @Param [customerSupplierDto, idList, failList]
     **/
    private void toBatch(Boolean isCustomer, CustomerSupplierDto customerSupplierDto, List<Long> idList) {
        if (idList == null || idList.size() == 0) {
            return;
        }
        if (isCustomer) {
            //更新信息表状态
            Customer customer = new Customer();
            customer.setBatchIds(idList);
            baseCustomService.batchEnable(customer, customerSupplierDto.getIsEnable(), null, null);
            //更新使用表状态
            List<Long> companyIds = customerCompanyMapper.selectList(new LambdaQueryWrapper<CustomerCompany>().in(CustomerCompany::getCustomerId, idList)
                    .select(CustomerCompany::getId)).stream().map(CustomerCompany::getId).collect(Collectors.toList());
            if (!companyIds.isEmpty()) {
                CustomerCompany customerCompany = new CustomerCompany();
                customerCompany.setBatchIds(companyIds);
                baseCustomService.batchEnable(customerCompany, customerSupplierDto.getIsEnable(), null, null);
            }
        } else {
            //更新信息表状态
            Supplier supplier = new Supplier();
            supplier.setBatchIds(idList);
            baseCustomService.batchEnable(supplier, customerSupplierDto.getIsEnable(), null, null);
            //更新使用表状态
            List<Long> companyIds = supplierCompanyMapper.selectList(new LambdaQueryWrapper<SupplierCompany>().in(SupplierCompany::getSupplierId, idList)
                    .select(SupplierCompany::getId)).stream().map(SupplierCompany::getId).collect(Collectors.toList());
            if (!companyIds.isEmpty()) {
                CustomerCompany customerCompany = new CustomerCompany();
                customerCompany.setBatchIds(companyIds);
                baseCustomService.batchEnable(customerCompany, customerSupplierDto.getIsEnable(), null, null);
            }
        }
    }

    /**
     * @return com.njwd.support.BatchResult
     * @description: (批量)分配 客户-公司
     * @Param [CustomerSupplierDto]
     * @author wuweiming
     * @date 2019/8/24 16:42
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResult updateBatchCustomerUseCompany(CustomerSupplierDto dto) {
        //清楚缓存
        RedisUtils.removeBatch(Constant.RedisCache.CUSTOMER, dto.getIdS());
        BatchResult batchResult = batchCustomerHandle(dto);
        return batchResult;
    }

    /**
     * @return com.njwd.support.BatchResult
     * @Description (批量)分配 客户-公司
     * @Author wuweiming
     * @Date 2019/8/24 16:56
     * @Param [CustomerSupplierDto]
     */
    public BatchResult batchCustomerHandle(CustomerSupplierDto dto) {
        BatchResult batchResult = new BatchResult();
        List<ReferenceDescription> failList = new ArrayList<>();
        //获取数据操作的控制策略
        MenuControlStrategy menuControlStrategy = menuControlStrategyService.findMenuControlStrategy(MenuCodeConstant.CUSTOMER);
        /*** step1:查询所有客户的数据信息 ***/
        List<CustomerSupplierVo> customers = customerMapper.selectCustomerByParams(dto);
        //数据校验
        checkData(customers, failList, dto, menuControlStrategy);
        if (CollectionUtils.isEmpty(customers)) {
            batchResult.setFailList(failList);
            return batchResult;
        }
        /*** step2:查询客户所有使用公司 ***/
        CustomerSupplierCompanyDto newDto = new CustomerSupplierCompanyDto();
        newDto.setCustomerIds(dto.getIdS());
        List<CustomerCompany> customerCompanies = customerCompanyMapper.selectAllCompaniesByCustomerIds(newDto);
        List<ReferenceDescription> successDetailsList = new LinkedList<>();
        List<Long> companyList = new ArrayList<>();
        MergeUtil.mergeList(customers, customerCompanies,
                (customer, customerCompany) -> customer.getId().equals(customerCompany.getCustomerId()),
                (customer, customerCompanyList) -> {
                    /*** step3:新增未分配的公司 ***/
                    //遍历原有的公司
                    companyList.addAll(dto.getCompanyList());
                    ReferenceDescription rfd;
                    for (CustomerCompany cc : customerCompanyList) {
                        if (companyList.contains(cc.getCompanyId())) {
                            companyList.remove(cc.getCompanyId());
                            rfd = new ReferenceDescription();
                            rfd.setBusinessId(customer.getId());
                            rfd.setCompanyId(cc.getCompanyId());
                            rfd.setReferenceDescription(ResultCode.DATA_IS_DISTRIBUTE.message);
                            failList.add(rfd);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(companyList)) {
                        CustomerCompany cc;
                        /*** step4:遍历 新增客户供应商使用公司 ***/
                        SysUserVo operator = UserUtils.getUserVo();
                        for (Long companyId : companyList) {
                            cc = new CustomerCompany();
                            cc.setCustomerId(customer.getId());
                            cc.setCompanyId(companyId);
                            cc.setIsEnable(Constant.IsEnable.ENABLE);
                            cc.setIsDel(Constant.Is.NO);
                            cc.setCreatorId(operator.getUserId());
                            cc.setCreatorName(operator.getName());
                            customerCompanyMapper.insert(cc);
                            rfd = new ReferenceDescription();
                            rfd.setBusinessId(customer.getId());
                            rfd.setCompanyId(cc.getCompanyId());
                            successDetailsList.add(rfd);
                        }
                        companyList.clear();
                    }
                });
        batchResult.setFailList(failList);
        batchResult.setSuccessDetailsList(successDetailsList);
        return batchResult;
    }

    /**
     * wuweiming
     * 数据校验
     *
     * @param list
     * @param failList
     * @param dto
     * @param menuControlStrategy
     */
    private void checkData(List<CustomerSupplierVo> list, List<ReferenceDescription> failList, CustomerSupplierDto dto, MenuControlStrategy menuControlStrategy) {
        List<CustomerSupplierVo> voList = new ArrayList<>();
        ReferenceDescription rfd;
        List<Long> companyList = dto.getCompanyList();
        if (!FastUtils.checkNullOrEmpty(list)) {
            for (CustomerSupplierVo vo : list) {
                //校验数据是否已删除
                if (Constant.Is.YES.equals(vo.getIsDel())) {
                    //如果数据已被删除，设置结果集无法删除数据的id和无法删除的原因
                    for (Long companyId : companyList) {
                        rfd = new ReferenceDescription();
                        rfd.setBusinessId(vo.getId());
                        rfd.setCompanyId(companyId);
                        rfd.setReferenceDescription(ResultCode.IS_DEL.message);
                        failList.add(rfd);
                    }
                    //删除的项目不允许修改项目信息
                    dto.getIdS().remove(vo.getId());
                    voList.add(vo);
                    continue;
                }
                if (Constant.Is.NO.equals(vo.getIsEnable())) {
                    //如果数据已被禁用，设置结果集无法禁用数据的id和无法禁用的原因
                    for (Long companyId : companyList) {
                        rfd = new ReferenceDescription();
                        rfd.setBusinessId(vo.getId());
                        rfd.setCompanyId(companyId);
                        rfd.setReferenceDescription(ResultCode.IS_DISABLE.message);
                        failList.add(rfd);
                    }
                    //禁用的项目不允许修改项目信息
                    dto.getIdS().remove(vo.getId());
                    voList.add(vo);
                    continue;
                }

                //如果数据类型不是分配型，不允许分配
                if (!Constant.dataType.DISTRIBUTION.equals(vo.getDataTypes())) {
                    //如果数据不是分配型,设置结果集无法分配的数据id和无法分配的原因
                    for (Long companyId : companyList) {
                        rfd = new ReferenceDescription();
                        rfd.setBusinessId(vo.getId());
                        rfd.setCompanyId(companyId);
                        rfd.setReferenceDescription(ResultCode.USECOMPANY_DATATYPEDISTRIBUTION.message);
                        failList.add(rfd);
                    }
                    //不是分配型不允许分配
                    dto.getIdS().remove(vo.getId());
                    voList.add(vo);
                    continue;
                }
            }
        }

        if (!FastUtils.checkNullOrEmpty(voList)) {
            list.removeAll(voList);
        }
    }

    /**
     * @return com.njwd.support.BatchResult
     * @description: 批量取消分配客户
     * @Param [CustomerSupplierDto]
     * @author wuweiming
     * @date 2019/8/24 16:42
     */
    @Override
    public BatchResult cancelBatchCustomerUseCompany(CustomerSupplierDto dto) {
        //清除缓存
        RedisUtils.removeBatch(Constant.RedisCache.CUSTOMER, dto.getIdS());
        BatchResult batchResult = cancelBatchCustomerHandle(dto);
        return batchResult;
    }

    /**
     * @return com.njwd.support.BatchResult
     * @Description (批量)取消分配
     * @Author wuweiming
     * @Date 2019/8/24 16:56
     * @Param [CustomerSupplierDto]
     */
    public BatchResult cancelBatchCustomerHandle(CustomerSupplierDto dto) {
        BatchResult batchResult = new BatchResult();
        List<Long> successList = new ArrayList<>();
        List<ReferenceDescription> failList = new ArrayList<>();
        ReferenceDescription rfd = new ReferenceDescription();
        SysUserVo operator = UserUtils.getUserVo();
        //遍历
        //for (int i=0;i<dto.getIdS().size();i++){
        /*** 校验数据状态 ***/
        Customer customer = customerMapper.selectById(dto.getIdS().get(0));
        //如果数据已变更设置结果无法删除数据的id和无法删除的原因
        if (!dto.getVersions().get(0).equals(customer.getVersion())) {
            rfd.setBusinessId(customer.getId());
            rfd.setReferenceDescription(ResultCode.IS_CHANGE.message);
            failList.add(rfd);
        } else if (Constant.Is.YES.equals(customer.getIsDel())) {
            //如果数据已被删除，设置结果集无法删除数据的id和无法删除的原因
            rfd.setBusinessId(customer.getId());
            rfd.setReferenceDescription(ResultCode.IS_DEL.message);
            failList.add(rfd);
        } else if (Constant.Is.NO.equals(customer.getIsEnable())) {
            //如果数据已被禁用，设置结果集无法禁用数据的id和无法禁用的原因
            rfd.setBusinessId(customer.getId());
            rfd.setReferenceDescription(ResultCode.IS_DISABLE.message);
            failList.add(rfd);
        }
        /*** 对取消分配的使用公司进行校验 ***/
        if (dto.getCompanyList() != null) {
            CustomerSupplierCompanyDto scDto = null;
            for (Long companyId : dto.getCompanyList()) {
                ReferenceContext referenceContext = referenceRelationService.isReference(Constant.Reference.CUSTOMER, dto.getIdS());
                if (!referenceContext.getReferences().isEmpty()) {
                    //有引用数据
                    failList = (referenceContext.getReferences());
                    for (ReferenceDescription referenceDescription : failList) {
                        dto.getIdS().remove(referenceDescription.getBusinessId());
                    }
                }
                //数据引用校验
                //if (!projectService.checkDataReferenceForDistribution(Constant.TableName.CUSTOMER, failList, companyId, dto.getIdS().get(i))) {
                if (dto.getIdS().size() > 0) {
                    //如果数据未被引用,取消分配的使用公司
                    scDto = new CustomerSupplierCompanyDto();
                    scDto.setIsDel(Constant.Is.YES);
                    scDto.setCustomerIds(dto.getIdS());
                    scDto.setCompanyId(companyId);
                    customerCompanyMapper.deleteByCustomerIdAndCompanyId(scDto);
                    //变更版本号
                    Customer c = new Customer();
                    c.setId(dto.getIdS().get(0));
                    c.setVersion(customer.getVersion());
                    c.setUpdatorId(operator.getUserId());
                    c.setUpdatorName(operator.getName());
                    c.setUpdateTime(new Date(System.currentTimeMillis() / 1000 * 1000));
                    customerMapper.updateById(c);
                    successList.add(companyId);
                }
            }
        }
        batchResult.setFailList(failList);
        batchResult.setSuccessList(successList);
        return batchResult;
    }

    /**
     * @return com.njwd.support.BatchResult
     * @description: (批量)分配 供应商-公司
     * @Param [CustomerSupplierDto]
     * @author wuweiming
     * @date 2019/8/24 16:42
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResult updateBatchSupplierUseCompany(CustomerSupplierDto dto) {
        //清除缓存
        RedisUtils.removeBatch(Constant.RedisCache.SUPPLIER, dto.getIdS());
        BatchResult batchResult = batchSupplierHandle(dto);
        return batchResult;
    }

    /**
     * @return com.njwd.support.BatchResult
     * @Description (批量)分配 供应商-公司
     * @Author wuweiming
     * @Date 2019/8/24 16:56
     * @Param [CustomerSupplierDto]
     */
    public BatchResult batchSupplierHandle(CustomerSupplierDto dto) {
        BatchResult batchResult = new BatchResult();
        List<ReferenceDescription> failList = new ArrayList<>();
        //获取数据操作的控制策略
        MenuControlStrategy menuControlStrategy = menuControlStrategyService.findMenuControlStrategy(MenuCodeConstant.SUPPLIER);
        /*** step1:查询所有供应商的数据信息 ***/
        List<CustomerSupplierVo> suppliers = supplierMapper.selectSupplierByParams(dto);
        //数据校验
        checkData(suppliers, failList, dto, menuControlStrategy);
        if (CollectionUtils.isEmpty(suppliers)) {
            batchResult.setFailList(failList);
            return batchResult;
        }
        /*** step2:查询供应商所有使用公司 ***/
        CustomerSupplierCompanyDto newDto = new CustomerSupplierCompanyDto();
        newDto.setSupplierIds(dto.getIdS());
        List<SupplierCompany> supplierCompanies = supplierCompanyMapper.selectAllCompaniesBySupplierIds(newDto);
        List<ReferenceDescription> successDetailsList = new LinkedList<>();
        List<Long> companyList = new ArrayList<>();
        MergeUtil.mergeList(suppliers, supplierCompanies,
                (supplier, supplierCompany) -> supplier.getId().equals(supplierCompany.getSupplierId()),
                (supplier, supplierCompanyList) -> {
                    /*** step3:新增未分配的公司 ***/
                    //遍历 原有的公司
                    companyList.addAll(dto.getCompanyList());
                    ReferenceDescription rfd;
                    for (SupplierCompany sc : supplierCompanyList) {
                        if (companyList.contains(sc.getCompanyId())) {
                            companyList.remove(sc.getCompanyId());
                            rfd = new ReferenceDescription();
                            rfd.setBusinessId(supplier.getId());
                            rfd.setCompanyId(sc.getCompanyId());
                            rfd.setReferenceDescription(ResultCode.DATA_IS_DISTRIBUTE.message);
                            failList.add(rfd);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(companyList)) {
                        SupplierCompany sc;
                        /*** step4:遍历 新增客户供应商使用公司 ***/
                        SysUserVo operator = UserUtils.getUserVo();
                        for (Long companyId : companyList) {
                            sc = new SupplierCompany();
                            sc.setSupplierId(supplier.getId());
                            sc.setCompanyId(companyId);
                            sc.setIsEnable(Constant.IsEnable.ENABLE);
                            sc.setIsDel(Constant.Is.NO);
                            sc.setCreatorId(operator.getUserId());
                            sc.setCreatorName(operator.getName());
                            supplierCompanyMapper.insert(sc);
                            rfd = new ReferenceDescription();
                            rfd.setBusinessId(supplier.getId());
                            rfd.setCompanyId(sc.getCompanyId());
                            successDetailsList.add(rfd);
                        }
                        companyList.clear();
                    }
                });
        batchResult.setFailList(failList);
        batchResult.setSuccessDetailsList(successDetailsList);
        return batchResult;
    }

    /**
     * @return com.njwd.support.BatchResult
     * @description: 批量取消分配客户
     * @Param [CustomerSupplierDto]
     * @author wuweiming
     * @date 2019/8/24 16:42
     */
    @Override
    public BatchResult cancelBatchSupplierUseCompany(CustomerSupplierDto dto) {
        //清楚缓存
        RedisUtils.removeBatch(Constant.RedisCache.SUPPLIER, dto.getIdS());
        BatchResult batchResult = cancelBatchSupplierHandle(dto);
        return batchResult;
    }

    /**
     * @return com.njwd.support.BatchResult
     * @Description (批量)取消分配
     * @Author wuweiming
     * @Date 2019/8/24 16:56
     * @Param [CustomerSupplierDto]
     */
    public BatchResult cancelBatchSupplierHandle(CustomerSupplierDto dto) {
        BatchResult batchResult = new BatchResult();
        List<Long> successList = new ArrayList<>();
        List<ReferenceDescription> failList = new ArrayList<>();
        ReferenceDescription rfd = new ReferenceDescription();
        SysUserVo operator = UserUtils.getUserVo();
        /*** 校验数据状态 ***/
        Supplier supplier = supplierMapper.selectById(dto.getIdS().get(0));
        if (!supplier.getVersion().equals(dto.getVersions().get(0))) {
            rfd.setBusinessId(supplier.getId());
            rfd.setReferenceDescription(ResultCode.IS_CHANGE.message);
            failList.add(rfd);
        }
        if (Constant.Is.YES.equals(supplier.getIsDel())) {
            //如果数据已被删除，设置结果集无法删除数据的id和无法删除的原因
            rfd.setBusinessId(supplier.getId());
            rfd.setReferenceDescription(ResultCode.IS_DEL.message);
            failList.add(rfd);
        }
        if (Constant.Is.NO.equals(supplier.getIsEnable())) {
            //如果数据已被禁用，设置结果集无法禁用数据的id和无法禁用的原因
            rfd.setBusinessId(supplier.getId());
            rfd.setReferenceDescription(ResultCode.IS_DISABLE.message);
            failList.add(rfd);
        }
        /*** 对取消分配的使用公司进行校验 ***/
        if (dto.getCompanyList() != null) {
            CustomerSupplierCompanyDto scDto = null;
            for (Long companyId : dto.getCompanyList()) {
                ReferenceContext referenceContext = referenceRelationService.isReference(Constant.Reference.SUPPLIER, dto.getIdS());
                if (!referenceContext.getReferences().isEmpty()) {
                    //有引用数据
                    failList = (referenceContext.getReferences());
                    for (ReferenceDescription referenceDescription : failList) {
                        dto.getIdS().remove(referenceDescription.getBusinessId());
                    }
                }
                //数据引用校验
                if (dto.getIdS().size() > 0) {
                    //数据引用校验
                    //if (!projectService.checkDataReferenceForDistribution(Constant.TableName.SUPPLIER, failList, companyId, dto.getIdS().get(0))) {
                    //如果数据未被引用,取消分配的使用公司
                    scDto = new CustomerSupplierCompanyDto();
                    scDto.setIsDel(Constant.Is.YES);
                    scDto.setSupplierIds(dto.getIdS());
                    scDto.setCompanyId(companyId);
                    supplierCompanyMapper.deleteBySupplierIdAndCompanyId(scDto);
                    successList.add(companyId);
                    //变更版本号
                    Supplier s = new Supplier();
                    s.setId(dto.getIdS().get(0));
                    s.setVersion(supplier.getVersion());
                    s.setUpdatorId(operator.getUserId());
                    s.setUpdatorName(operator.getName());
                    s.setUpdateTime(new Date(System.currentTimeMillis() / 1000 * 1000));
                    supplierMapper.updateById(s);
                    successList.add(companyId);
                }
            }
        }
        batchResult.setFailList(failList);
        batchResult.setSuccessList(successList);
        return batchResult;
    }

}