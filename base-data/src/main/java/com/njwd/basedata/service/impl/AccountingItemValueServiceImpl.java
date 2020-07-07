package com.njwd.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.mapper.AccountingItemValueMapper;
import com.njwd.basedata.service.AccountingItemValueService;
import com.njwd.basedata.service.BaseCustomService;
import com.njwd.basedata.service.MenuControlStrategyService;
import com.njwd.basedata.service.SequenceService;
import com.njwd.common.Constant;
import com.njwd.common.MenuCodeConstant;
import com.njwd.entity.basedata.AccountingItemValue;
import com.njwd.entity.basedata.ReferenceContext;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.dto.AccountingItemValueDto;
import com.njwd.entity.basedata.dto.CompanyDto;
import com.njwd.entity.basedata.vo.AccountingItemValueVo;
import com.njwd.entity.basedata.vo.CompanyVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.financeback.service.CompanyService;
import com.njwd.service.FileService;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BatchResult;
import com.njwd.utils.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Description 自定义核算项目大区值 service实现类
 * @Author 薛永利
 * @Date 2019/6/21 8:57
 */
@Service
public class AccountingItemValueServiceImpl implements AccountingItemValueService {

    @Resource
    private AccountingItemValueMapper accountingItemValueMapper;
    @Resource
    private CompanyService companyService;
    @Resource
    private FileService fileService;
    @Resource
    private SequenceService sequenceService;
    @Resource
    private BaseCustomService baseCustomService;
    @Resource
    private MenuControlStrategyService menuControlStrategyService;
    @Resource
    private ReferenceRelationService referenceRelationService;
    /**
     * @Description 新增自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/7/5 14:52
     * @Param [accountingItemValueDto]
     * @return java.lang.Long
     */
    @Override
    @CacheEvict(value = Constant.RedisCache.ACCOUNTING_ITEM, key = "#accountingItemValueDto.itemId")
    public Long addAccountingItemValue(AccountingItemValueDto accountingItemValueDto) {
        // 设置基础数据
        AccountingItemValue accountingItemValue = new AccountingItemValue();
        FastUtils.copyProperties(accountingItemValueDto, accountingItemValue);
        // 校验唯一性
        checkAccountingItemValueUniqByName(accountingItemValueDto);
        CompanyVo company = new CompanyVo();
        CompanyDto companyDto = new CompanyDto();
        companyDto.setId(accountingItemValueDto.getCompanyId());
        if (accountingItemValueDto.getCompanyId() == Constant.Number.ZERO.longValue()) {
            //默认集团编码值为0
            company.setId(Constant.AccountSubjectData.GROUP_ID);
            company.setCode("0000");
        } else {
            company = companyService.findCompanyByIdOrCodeOrName(companyDto);
            // 校验是否为空
            if (null == company || StringUtil.isBlank(company.getCode())) {
                throw new ServiceException(accountingItemValueDto.getCompanyId().toString(), ResultCode.INVAILD_COMPANY_ID);
            }
        }
        //获取值编码规则
        int codeType = accountingItemValueDto.getCodeType();
        //系统自动生成流水号 项目编码（Z+两位流水号）+公司编码+三位位流水号
        if (Constant.CodeType.SYSTEMCODE == codeType) {
            accountingItemValue.setCode(
                    sequenceService.getCode(
                            accountingItemValueDto.getItemCode(),
                            Constant.BaseCodeRule.LENGTH_THREE,
                            company.getId(),
                            company.getCode(),
                            Constant.BaseCodeRule.COMPANY
                    ));
            accountingItemValueDto.setCode(accountingItemValue.getCode());
        }
        // 校验唯一性
        checkAccountingItemValueUniqByCode(accountingItemValueDto);
        accountingItemValueMapper.insert(accountingItemValue);
        return accountingItemValue.getId();
    }
    /**
     * @Description 校验自定义核算项目值版本号是否一致
     * @Author 薛永利
     * @Date 2019/6/21 9:14
     * @Param [accountingItemDto]
     * @return void
     */
    private void checkAccountingItemValueByVersion(AccountingItemValueDto accountingItemValueDto) {
        int row = 0;
        row = accountingItemValueMapper.findAccountingItemValueByVersion(accountingItemValueDto);
        if (row != 1) {
            throw new ServiceException(ResultCode.VERSION_ERROR);
        }
    }
    /**
     * @Description 校验租户内自定义核算项目大区值名称是否重复
     * @Author 薛永利
     * @Date 2019/7/9 10:19
     * @Param [accountingItemValueDto]
     * @return void
     */
    private void checkAccountingItemValueUniqByName(AccountingItemValueDto accountingItemValueDto) {
        Integer row = 0;
        row = findAccountingItemValueByName(accountingItemValueDto);
        if (row != 0) {
            throw new ServiceException(ResultCode.NAME_EXIST);
        }
    }

    /**
     * @Description 校验公司内自定义核算项目大区值编码是否重复
     * @Author 薛永利
     * @Date 2019/7/9 10:19
     * @Param [accountingItemValueDto]
     * @return void
     */
    private void checkAccountingItemValueUniqByCode(AccountingItemValueDto accountingItemValueDto) {
        Integer row = 0;
        row = findAccountingItemValueByCode(accountingItemValueDto);
        if (row != 0) {
            throw new ServiceException(ResultCode.CODE_EXIST);
        }
    }
    /**
     * @Description 根据 编码 查询自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/7/9 10:19
     * @Param [accountingItemValueDto]
     * @return int
     */
    @Override
    public int findAccountingItemValueByCode(AccountingItemValueDto accountingItemValueDto) {
        return accountingItemValueMapper.findAccountingItemValueByCode(accountingItemValueDto);
    }

    /**
     * @Description 根据 编码名称 查询自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/7/9 10:18
     * @Param [accountingItemValueDto]
     * @return int
     */
    @Override
    public int findAccountingItemValueByName(AccountingItemValueDto accountingItemValueDto) {
        return accountingItemValueMapper.findAccountingItemValueByName(accountingItemValueDto);
    }

    /**
     * @return com.njwd.support.BatchResult
     * @Description 根据IDS批量删除自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/7/5 13:35
     * @Param [accountingItemValueDto]
     */
    @Override
    public BatchResult deleteAccountingItemValueByIds(AccountingItemValueDto accountingItemValueDto) {
        //清除指定单个缓存
        RedisUtils.remove("accountingItem", accountingItemValueDto.getItemId());
        //清除指定单个缓存
        RedisUtils.removeBatch("accountingItemValue", accountingItemValueDto.getIds());
        return deleteAccountingItemValue(accountingItemValueDto);
    }

    /**
     * @return com.njwd.support.BatchResult
     * @Description 根据ID删除自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/7/5 13:35
     * @Param [accountingItemValueDto]
     */
    @Override
    @Caching(evict = {@CacheEvict(value = Constant.RedisCache.ACCOUNTING_ITEM, key = "#accountingItemValueDto.itemId")
            , @CacheEvict(value = Constant.RedisCache.ACCOUNTING_ITEM_VALUE, key = "#accountingItemValueDto.id")})
    public BatchResult deleteAccountingItemValueById(AccountingItemValueDto accountingItemValueDto) {
        return deleteAccountingItemValue(accountingItemValueDto);
    }
    /**
     * @Description 批量校验权限
     * @Author 薛永利
     * @Date 2019/9/21 16:42
     * @Param [accountingItemValueDto, menuDefine]
     * @return com.njwd.support.BatchResult
     */
    public BatchResult batchVerifyPermission(AccountingItemValueDto accountingItemValueDto, String menuDefine){
        List<AccountingItemValueVo> accountingItemValueVoList = accountingItemValueMapper.findItemValueListById(accountingItemValueDto);
         BatchResult result =  ShiroUtils.filterNotPermData(accountingItemValueVoList, menuDefine, new ShiroUtils.CheckPermSupport<AccountingItemValueVo>() {
            @Override
            public Long getBusinessId(AccountingItemValueVo accountingItemValueVo) {
                return accountingItemValueVo.getId();
            }
            @Override
            public Long getCompanyId(AccountingItemValueVo accountingItemValueVo) { return accountingItemValueVo.getCompanyId(); }
        });
        //遍历去除无权限数据
        for(ReferenceDescription rf : result.getFailList()){
            Iterator<Long> it = accountingItemValueDto.getIds().iterator();
            while (it.hasNext())
            {
                Long s = it.next();
                if (s.equals(rf.getBusinessId()))
                {
                    it.remove();
                }
            }
        }
        return result;
    }
    /**
     * @Description 删除自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/7/5 13:35
     * @Param [accountingItemValueDto]
     * @return com.njwd.support.BatchResult
     */
    public BatchResult deleteAccountingItemValue(AccountingItemValueDto accountingItemValueDto) {
        BatchResult batchResult = new BatchResult();
        if (Constant.Is.NO.equals(accountingItemValueDto.getIsEnterpriseAdmin())) {
            //验证权限
            batchResult = batchVerifyPermission(accountingItemValueDto,Constant.MenuDefine.ACCOUNTING_ITEM_DELETE);
        }
        List<ReferenceDescription> failList = new ArrayList<>();
        if(accountingItemValueDto.getIds().size() > 0){
            failList = findIsDel(accountingItemValueDto, 2);
            if (Constant.Is.NO.equals(accountingItemValueDto.getIsEnterpriseAdmin())) {
                //排除集团共享的数据
                FastUtils.filterIdsByGroupId(accountingItemValueMapper, accountingItemValueDto.getIds(), failList);
            }
        }
        ReferenceContext referenceContext = referenceRelationService.isReference(Constant.Reference.ACCOUNTING_ITEM_VALUE, accountingItemValueDto.getIds());
        //未被引用的ID
        if (!referenceContext.getNotReferences().isEmpty()) {
            //返回删除数据
            batchResult.setSuccessList(referenceContext.getNotReferences());
            //批量删除数据
            accountingItemValueDto.setIds(referenceContext.getNotReferences());
            AccountingItemValue accountingItemValue = new AccountingItemValue();
            accountingItemValue.setIsDel(Constant.Is.YES);
            if (accountingItemValueDto.getIds().size() > 0) {
                FastUtils.updateBatch(accountingItemValueMapper, accountingItemValue, Constant.ColumnName.ID, accountingItemValueDto.getIds(),null);
            }
        }
        //被引用的ID及说明
        if (!referenceContext.getReferences().isEmpty()) {
            //不能删除数据及原因
            referenceContext.getReferences().addAll(failList);
            batchResult.getFailList().addAll(referenceContext.getReferences());
        } else {
            batchResult.getFailList().addAll(failList);
        }
        return batchResult;
    }
    /**
     * @Description 查看项目值时 校验项目值是否被引用
     * @Author 薛永利
     * @Date 2019/8/29 16:25
     * @Param [accountingItemValueDto]
     * @return int
     */
    @Override
    public int findItemValueIsReference(AccountingItemValueDto accountingItemValueDto){
        int result = 0;
        ReferenceContext referenceContext = referenceRelationService.isReference(Constant.Reference.ACCOUNTING_ITEM_VALUE, accountingItemValueDto.getIds());
        if(!referenceContext.getReferences().isEmpty()){
            result = 1;
        }
        return result;
    }
    /**
     * @Description 查询自定义核算项目是否已被删除，已被禁用
     * @Author 薛永利
     * @Date 2019/7/9 10:18
     * @Param [accountingItemValueDto, enableFlag]
     * @return java.util.List<com.njwd.entity.basedata.ReferenceDescription>
     */
    public List<ReferenceDescription> findIsDel(AccountingItemValueDto accountingItemValueDto, int enableFlag) {
        ReferenceDescription referenceDescription = new ReferenceDescription();
        List<ReferenceDescription> failList = new ArrayList<>();
        //排除版本号不一致的数据
        FastUtils.filterVersionIds(accountingItemValueMapper,new QueryWrapper<>(),Constant.ColumnName.ID,accountingItemValueDto.getIds(),accountingItemValueDto.getVersions(),failList);
        //查询已删除ID
        accountingItemValueDto.setIsDel(Constant.Is.YES);
        accountingItemValueDto.setIsEnable(null);
        List<AccountingItemValueVo> isDelIds = accountingItemValueMapper.findIsDel(accountingItemValueDto);
        for (int i = 0; i < isDelIds.size(); i++) {
            referenceDescription.setBusinessId(Long.valueOf(isDelIds.get(i).getId()));
            referenceDescription.setReferenceDescription(ResultCode.ACCOUNTING_ITEM_VALUE_ISDEL.message);
            failList.add(referenceDescription);
            referenceDescription = new ReferenceDescription();
            //移除已删除ID
            accountingItemValueDto.getIds().remove(isDelIds.get(i).getId());
        }
        if (enableFlag == Constant.Character.SUCCESS && accountingItemValueDto.getIds().size() > Constant.Number.ZERO) {
            accountingItemValueDto.setIsEnable(Constant.Is.YES);
            accountingItemValueDto.setIsDel(null);
            List<AccountingItemValueVo> isEnableIds = accountingItemValueMapper.findIsDel(accountingItemValueDto);
            for (int i = 0; i < isEnableIds.size(); i++) {
                referenceDescription.setBusinessId(Long.valueOf(isEnableIds.get(i).getId()));
                referenceDescription.setReferenceDescription(ResultCode.ACCOUNTING_ITEM_VALUE_ISENABLE.message);
                failList.add(referenceDescription);
                referenceDescription = new ReferenceDescription();
                //移除已禁用ID
                accountingItemValueDto.getIds().remove(isEnableIds.get(i).getId());
            }
        }
        if (enableFlag == Constant.Character.FAIL && accountingItemValueDto.getIds().size() > Constant.Number.ZERO) {
            accountingItemValueDto.setIsEnable(Constant.Is.NO);
            accountingItemValueDto.setIsDel(null);
            List<AccountingItemValueVo> isDisEnableIds = accountingItemValueMapper.findIsDel(accountingItemValueDto);
            for (int i = 0; i < isDisEnableIds.size(); i++) {
                referenceDescription.setBusinessId(Long.valueOf(isDisEnableIds.get(i).getId()));
                referenceDescription.setReferenceDescription(ResultCode.ACCOUNTING_ITEM_VALUE_ISDISENABLE.message);
                failList.add(referenceDescription);
                referenceDescription = new ReferenceDescription();
                //移除已被反禁用ID
                accountingItemValueDto.getIds().remove(isDisEnableIds.get(i).getId());
            }
        }
        return failList;
    }

    /**
     * @Description 修改自定义核算项目大区值信息
     * @Author 薛永利
     * @Date 2019/7/9 10:20
     * @Param [accountingItemValueDto]
     * @return int
     */
    @Override
    @CacheEvict(value = Constant.RedisCache.ACCOUNTING_ITEM_VALUE, key = "#accountingItemValueDto.id")
    public int updateAccountingItemValue(AccountingItemValueDto accountingItemValueDto) {
        SysUserVo operator = UserUtils.getUserVo();
        accountingItemValueDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        //校验项目值版本号一致性
        checkAccountingItemValueByVersion(accountingItemValueDto);
        // 校验唯一性
        checkAccountingItemValueUniqByName(accountingItemValueDto);
        // 设置基础数据
        AccountingItemValue accountingItemValue = new AccountingItemValue();
        FastUtils.copyProperties(accountingItemValueDto, accountingItemValue);
        return accountingItemValueMapper.update(accountingItemValue, new LambdaQueryWrapper<AccountingItemValue>().eq(AccountingItemValue::getId, accountingItemValue.getId()));
    }

    /**
     * @return com.njwd.support.BatchResult
     * @Description 批量禁用/反禁用自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/7/9 10:20
     * @Param [accountingItemValueDto]
     */
    @Override
    public BatchResult updateBatch(AccountingItemValueDto accountingItemValueDto) {
        //清除指定多个缓存
        RedisUtils.removeBatch("accountingItemValue",accountingItemValueDto.getIds());
        return enableAccountingItemValue(accountingItemValueDto);
    }

    /**
     * @return com.njwd.support.BatchResult
     * @Description 禁用/反禁用自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/7/9 10:20
     * @Param [accountingItemValueDto]
     */
    @Override
    @CacheEvict(value = Constant.RedisCache.ACCOUNTING_ITEM_VALUE, key = "#accountingItemValueDto.id")
    public BatchResult updateById(AccountingItemValueDto accountingItemValueDto) {
        return enableAccountingItemValue(accountingItemValueDto);
    }

    /**
     * @Description 禁用/反禁用自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/7/9 10:20
     * @Param [accountingItemValueDto]
     * @return com.njwd.support.BatchResult
     */
    public BatchResult enableAccountingItemValue(AccountingItemValueDto accountingItemValueDto) {
        AccountingItemValue accountingItemValue = new AccountingItemValue();
        accountingItemValue.setIsDel(accountingItemValueDto.getIsDel());
        accountingItemValue.setIsEnable(accountingItemValueDto.getIsEnable());
        BatchResult batchResult = new BatchResult();
        if (Constant.Is.NO.equals(accountingItemValueDto.getIsEnterpriseAdmin())) {
            //验证权限
            if(Constant.IsEnable.DISABLE.equals(accountingItemValueDto.getIsEnable())){
                batchResult = batchVerifyPermission(accountingItemValueDto,Constant.MenuDefine.ACCOUNTING_ITEM_DISABLE);
            }else if(Constant.IsEnable.ENABLE.equals(accountingItemValueDto.getIsEnable())){
                batchResult = batchVerifyPermission(accountingItemValueDto,Constant.MenuDefine.ACCOUNTING_ITEM_ENABLE);
            }
        }
        List<ReferenceDescription> failList = new ArrayList<>();
        if (accountingItemValueDto.getIds().size() > 0){
        if (Constant.Is.YES.equals(accountingItemValueDto.getIsEnable())) {
            failList = findIsDel(accountingItemValueDto, 1);
        } else {
            failList = findIsDel(accountingItemValueDto, 0);
        }
        if (Constant.Is.NO.equals(accountingItemValueDto.getIsEnterpriseAdmin())) {
            //排除集团共享的数据
            FastUtils.filterIdsByGroupId(accountingItemValueMapper, accountingItemValueDto.getIds(), failList);
        }
        batchResult.getFailList().addAll(failList);
        batchResult.setSuccessList(accountingItemValueDto.getIds());
            accountingItemValueDto.setBatchIds(accountingItemValueDto.getIds());
            // 禁用、反禁用自定义核算项目大区值
            baseCustomService.batchEnable(accountingItemValueDto, accountingItemValueDto.getIsEnable(),accountingItemValueMapper,null);
           //accountingItemValueMapper.update(accountingItemValue, new QueryWrapper<AccountingItemValue>().in("id", accountingItemValueDto.getIds()));
        }
        return batchResult;
    }

    /**
     * @Description 查询自定义核算项目大区值列表 分页
     * @Author 薛永利
     * @Date 2019/7/9 10:20
     * @Param [accountingItemValueDto]
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountingItemValueVo>
     */
    @Override
    public Page<AccountingItemValueVo> findAccountingItemValueList(AccountingItemValueDto accountingItemValueDto) {
        Page<AccountingItemValueVo> page = accountingItemValueDto.getPage();
        Page<AccountingItemValueVo> accountingItemValueVoList = accountingItemValueMapper.findPage(page, accountingItemValueDto);
        return accountingItemValueVoList;
    }

    /**
     * @Description 根据id查自定义核算项目大区值详情
     * @Author 薛永利
     * @Date 2019/7/9 10:21
     * @Param [accountingItemValueDto]
     * @return com.njwd.entity.basedata.vo.AccountingItemValueVo
     */
    @Override
    @Cacheable(value = Constant.RedisCache.ACCOUNTING_ITEM_VALUE, key = "#accountingItemValueDto.id", unless = "#result==null")
    public AccountingItemValueVo findById(AccountingItemValueDto accountingItemValueDto) {
        return accountingItemValueMapper.findById(accountingItemValueDto.getId());
    }

    /**
     * @Description 导出自定义核算项目大区值excel
     * @Author 薛永利
     * @Date 2019/7/9 10:21
     * @Param [accountingItemValueDto, response]
     * @return void
     */
    @Override
    public void exportExcel(AccountingItemValueDto accountingItemValueDto, HttpServletResponse response) {
        Page<AccountingItemValueVo> page = accountingItemValueDto.getPage();
        fileService.resetPage(page);
        SysUserVo operator = UserUtils.getUserVo();
        accountingItemValueDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        Page<AccountingItemValueVo> accountingItemValueVoList = accountingItemValueMapper.findPage(page, accountingItemValueDto);
        //user用户比admin用户多使用公司
        fileService.exportExcel(response, accountingItemValueVoList.getRecords(), MenuCodeConstant.ACCOUNTING_ITEM, accountingItemValueDto.getIsEnterpriseAdmin());
    }

    /**
     * @Description 批量插入自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/7/9 10:21
     * @Param [accountingItemValueList]
     * @return int
     */
    @Override
    public int addBatchAccountingItemValue(List<AccountingItemValue> accountingItemValueList) {
        //清除指定多个缓存
        for(int i = 0;i<accountingItemValueList.size(); i++) {
            RedisUtils.remove("accountingItem", accountingItemValueList.get(i).getItemId());
        }
        return accountingItemValueMapper.addBatchAccountingItemValue(accountingItemValueList);
    }

    /**
     * @Description 查询所有未删除的自定义核算
     * @Author wuweiming
     * @Param [AccountingItemValueDto]
     * @return Result<List<AccountingItemValueVo>>
     */
    @Override
    public List<AccountingItemValueVo> findAllAccountItemValueByItemId(AccountingItemValueDto dto){
        return accountingItemValueMapper.findAllAccountItemValueByItemId(dto);
    }

    /**
     * @Description 查询所有未删除的辅助核算
     * @Author wuweiming
     * @Param []
     * @return List<AuxiliaryItemVo>
     **/
    @Override
    public List<AccountingItemValueVo> findAllAuxiliaryItemValue(AccountingItemValueDto dto){
        return accountingItemValueMapper.findAllAuxiliaryItemValue(dto);
    }
}
