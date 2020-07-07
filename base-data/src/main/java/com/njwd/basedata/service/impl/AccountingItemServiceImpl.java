package com.njwd.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.mapper.AccountingItemMapper;
import com.njwd.basedata.service.AccountingItemService;
import com.njwd.basedata.service.BaseCustomService;
import com.njwd.basedata.service.SequenceService;
import com.njwd.common.Constant;
import com.njwd.common.MenuCodeConstant;
import com.njwd.entity.basedata.AccountingItem;
import com.njwd.entity.basedata.AccountingItemValue;
import com.njwd.entity.basedata.ReferenceContext;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.dto.AccountingItemDto;
import com.njwd.entity.basedata.vo.AccountingItemVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.Subject;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.service.FileService;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BatchResult;
import com.njwd.utils.FastUtils;
import com.njwd.utils.RedisUtils;
import com.njwd.utils.UserUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description 自定义核算项目 service实现类
 * @Author 薛永利
 * @Date 2019/6/21 8:57
 */
@Service
public class AccountingItemServiceImpl implements AccountingItemService {

    @Resource
    private AccountingItemMapper accountingItemMapper;
    @Resource
    private SequenceService sequenceService;
    @Resource
    private FileService fileService;
    @Resource
    private BaseCustomService baseCustomService;
    @Resource
    private ReferenceRelationService referenceRelationService;
    /**
     * @Description 新增自定义核算项目
     * @Author 薛永利
     * @Date 2019/7/5 14:55
     * @Param [accountingItemDto]
     * @return java.lang.Long
     */
    @Override
    public Long addAccountingItem(AccountingItemDto accountingItemDto) {
        AccountingItem accountingItem = new AccountingItem();
        FastUtils.copyProperties(accountingItemDto, accountingItem);
        // 校验名字唯一性
        checkAccountingItemUniqByName(accountingItemDto);
        int codeType = accountingItemDto.getCodeType();
        //系统自动生成流水号 Z+两位流号
        if (Constant.CodeType.SYSTEMCODE == codeType) {
            accountingItem.setCode(sequenceService.getCode(
                    Constant.BaseCodeRule.ACC_ITEM,
                    Constant.BaseCodeRule.LENGTH_TWO,
                    accountingItem.getRootEnterpriseId(),
                    Constant.BaseCodeRule.ENTERPRISE
            ));
            accountingItemDto.setCode(accountingItem.getCode());
        }
        // 校验编码唯一性
        checkAccountingItemUniqByCode(accountingItemDto);
        accountingItemMapper.insert(accountingItem);
        return accountingItem.getId();
    }
    /**
     * @Description 校验自定义核算项目版本号是否一致
     * @Author 薛永利
     * @Date 2019/6/21 9:14
     * @Param [accountingItemDto]
     * @return void
     */
    private void checkAccountingItemByVersion(AccountingItemDto accountingItemDto) {
        int row = 0;
        row = accountingItemMapper.findAccountingItemByVersion(accountingItemDto);
        if (row != 1) {
            throw new ServiceException(ResultCode.VERSION_ERROR);
        }
    }
    /**
     * @Description 校验租户内自定义核算项目是否重复
     * @Author 薛永利
     * @Date 2019/6/21 9:14
     * @Param [accountingItemDto]
     * @return void
     */
    private void checkAccountingItemUniqByName(AccountingItemDto accountingItemDto) {
        int row = 0;
        row = findAccountingItemByName(accountingItemDto);
        if (row != 0) {
            throw new ServiceException(ResultCode.NAME_EXIST);
        }
    }
    /**
     * @Description 校验租户内自定义核算项目是否重复
     * @Author 薛永利
     * @Date 2019/6/21 9:14
     * @Param [accountingItemDto]
     * @return void
     */
    private void checkAccountingItemUniqByCode(AccountingItemDto accountingItemDto) {
        int row = 0;
        row = findAccountingItemByCode(accountingItemDto);
        if (row != 0) {
            throw new ServiceException(ResultCode.CODE_EXIST);
        }
    }
    /**
     * @Description 根据 编码 查询自定义核算项目数量
     * @Author 薛永利
     * @Date 2019/7/9 10:22
     * @Param [accountingItemDto]
     * @return int
     */
    @Override
    public int findAccountingItemByCode(AccountingItemDto accountingItemDto) {
        return accountingItemMapper.findAccountingItemByCode(accountingItemDto);
    }

    /**
     * @Description 根据 编码名称 查询自定义核算项目数量
     * @Author 薛永利
     * @Date 2019/8/23 9:27
     * @Param [accountingItemDto]
     * @return com.njwd.entity.basedata.vo.AccountingItemVo
     */
    @Override
    public AccountingItemVo findAccountingItemByItemName(AccountingItemDto accountingItemDto) {
        return accountingItemMapper.findAccountingItemByItemName(accountingItemDto);
    }

    /**
     * @Description 根据 编码名称 查询自定义核算项目
     * @Author 薛永利
     * @Date 2019/7/9 10:22
     * @Param [accountingItemDto]
     * @return int
     */
    @Override
    public int findAccountingItemByName(AccountingItemDto accountingItemDto) {
        return accountingItemMapper.findAccountingItemByName(accountingItemDto);
    }
    /**
     * @Description 查询项目是否有区值
     * @Author 薛永利
     * @Date 2019/8/21 9:37
     * @Param [accountingItemDto]
     * @return java.util.List<java.lang.Long>
     */
    @Override
    public List<Long> findItemRelaValueById(AccountingItemDto accountingItemDto){
        return accountingItemMapper.findItemRelaValueById(accountingItemDto);
    }
    /**
     * @return com.njwd.support.BatchResult
     * @Description 根据ID删除自定义核算项目
     * @Author 薛永利
     * @Date 2019/7/5 11:36
     * @Param [accountingItemDto]
     */
    @Override
    @CacheEvict(value = Constant.RedisCache.ACCOUNTING_ITEM, key = "#accountingItemDto.id")
    public BatchResult deleteAccountingItemById(AccountingItemDto accountingItemDto) {
        return deleteAccountingItem(accountingItemDto);
    }

    /**
     * @Description 根据IDS批量删除自定义核算项目
     * @Author 薛永利
     * @Date 2019/7/5 11:36
     * @Param [accountingItemDto]
     * @return com.njwd.support.BatchResult
     */
    @Override
    public BatchResult deleteAccountingItemByIds(AccountingItemDto accountingItemDto) {
        //清除指定多个缓存
        RedisUtils.removeBatch("accountingItem",accountingItemDto.getIds());
        return deleteAccountingItem(accountingItemDto);
    }

    /**
     * @return com.njwd.support.BatchResult
     * @Description 单个/批量删除自定义核算项目
     * @Author 薛永利
     * @Date 2019/7/5 11:36
     * @Param [accountingItemDto]
     */
    public BatchResult deleteAccountingItem(AccountingItemDto accountingItemDto) {
        BatchResult batchResult = new BatchResult();
        List<ReferenceDescription> failList = new ArrayList<>();
        failList = findIsDel(accountingItemDto, 2);
        ReferenceContext referenceContext = checkItemIsReference(accountingItemDto);
        //未被引用的ID
        if (!referenceContext.getNotReferences().isEmpty()) {
            //返回删除数据
            batchResult.setSuccessList(referenceContext.getNotReferences());
            //批量删除数据
            accountingItemDto.setIds(referenceContext.getNotReferences());
            AccountingItem accountingItem = new AccountingItem();
            accountingItem.setIsDel(Constant.Is.YES);
            if (accountingItemDto.getIds().size() > 0) {
                FastUtils.updateBatch(accountingItemMapper, accountingItem, Constant.ColumnName.ID, accountingItemDto.getIds(),null);
                accountingItemMapper.updateAccountingItemValue(accountingItemDto);
            }
        }
        //被引用的ID及说明
        if (!referenceContext.getReferences().isEmpty()) {
            //不能删除数据及原因
            referenceContext.getReferences().addAll(failList);
            batchResult.setFailList(referenceContext.getReferences());
        } else {
            batchResult.setFailList(failList);
        }
        return batchResult;
    }
    /**
     * @Description 查看项目时 校验项目是否被引用
     * @Author 薛永利
     * @Date 2019/8/29 16:25
     * @Param [accountingItemDto]
     * @return int
     */
    @Override
    public int findItemIsReference(AccountingItemDto accountingItemDto){
        int result = 0;
        //查询项目下区值
        List<Long> listItemValue = accountingItemMapper.findAccountingItemValueByItemId(accountingItemDto);
        ReferenceContext referenceContext = referenceRelationService.isReference(Constant.Reference.ACCOUNTING_ITEM, listItemValue);
        if(!referenceContext.getReferences().isEmpty()){
            result = 1;
        }
        return result;
    }
    /**
     * @Description 校验项目是否被引用
     * @Author 薛永利
     * @Date 2019/8/29 16:12
     * @Param [accountingItemDto]
     * @return com.njwd.entity.basedata.ReferenceContext
     */
    public ReferenceContext checkItemIsReference(AccountingItemDto accountingItemDto){
        //查询项目下区值
        List<Long> listItemValue = accountingItemMapper.findAccountingItemValueByItemId(accountingItemDto);
        ReferenceContext referenceContext = referenceRelationService.isReference(Constant.Reference.ACCOUNTING_ITEM, listItemValue);
        //被引用的值ids
        List<Long> itemValueIds = new ArrayList<>();
        //被引用的ID及说明
        if (!referenceContext.getReferences().isEmpty()) {
            for(int i=0;i< referenceContext.getReferences().size();i++){
                itemValueIds.add(referenceContext.getReferences().get(i).getBusinessId());
            }
        }
        accountingItemDto.setBatchIds(itemValueIds);
        //查询被引用的区值关联的项目
        List<Long> isReferenceItems = accountingItemMapper.findItemRelaValueById(accountingItemDto);
        referenceContext.setNotReferences(accountingItemDto.getIds());
        ReferenceDescription referenceDescription = new ReferenceDescription();
        List<ReferenceDescription> failList = new ArrayList<>();
        for (int i = 0; i < isReferenceItems.size(); i++) {
            referenceDescription.setBusinessId(Long.valueOf(isReferenceItems.get(i)));
            referenceDescription.setReferenceDescription(ResultCode.ACCOUNTING_ITEM_ISRELA_SUBJECT.message);
            failList.add(referenceDescription);
            referenceDescription = new ReferenceDescription();
            accountingItemDto.getIds().remove(isReferenceItems.get(i));
        }
        referenceContext.setReferences(failList);
        return  referenceContext;
    }
    /**
     * @Description 查询自定义核算项目是否已被删除，已被禁用
     * @Author 薛永利
     * @Date 2019/7/9 10:22
     * @Param [accountingItemDto, enableFlag]
     * @return java.util.List<com.njwd.entity.basedata.ReferenceDescription>
     */
    public List<ReferenceDescription> findIsDel(AccountingItemDto accountingItemDto, int enableFlag) {
        ReferenceDescription referenceDescription = new ReferenceDescription();
        List<ReferenceDescription> failList = new ArrayList<>();
        //查询已删除ID
        accountingItemDto.setIsDel(Constant.Is.YES);
        accountingItemDto.setIsEnable(null);
        List<AccountingItemVo> isDelIds = accountingItemMapper.findIsDel(accountingItemDto);
        for (int i = 0; i < isDelIds.size(); i++) {
            referenceDescription.setBusinessId(Long.valueOf(isDelIds.get(i).getId()));
            referenceDescription.setReferenceDescription(ResultCode.ACCOUNTING_ITEM_ISDEL.message);
            failList.add(referenceDescription);
            referenceDescription = new ReferenceDescription();
            //移除已删除ID
            accountingItemDto.getIds().remove(isDelIds.get(i).getId());
        }
        if (enableFlag == Constant.Character.SUCCESS && accountingItemDto.getIds().size() > Constant.Number.ZERO) {
            accountingItemDto.setIsEnable(Constant.Is.YES);
            accountingItemDto.setIsDel(null);
            List<AccountingItemVo> isEnableIds = accountingItemMapper.findIsDel(accountingItemDto);
            for (int i = 0; i < isEnableIds.size(); i++) {
                referenceDescription.setBusinessId(Long.valueOf(isEnableIds.get(i).getId()));
                referenceDescription.setReferenceDescription(ResultCode.ACCOUNTING_ITEM_ISENABLE.message);
                failList.add(referenceDescription);
                referenceDescription = new ReferenceDescription();
                //移除已禁用ID
                accountingItemDto.getIds().remove(isEnableIds.get(i).getId());
            }
        }
        if (enableFlag == Constant.Character.FAIL && accountingItemDto.getIds().size() > Constant.Number.ZERO) {
            accountingItemDto.setIsEnable(Constant.Is.NO);
            accountingItemDto.setIsDel(null);
            List<AccountingItemVo> isDisEnableIds = accountingItemMapper.findIsDel(accountingItemDto);
            for (int i = 0; i < isDisEnableIds.size(); i++) {
                referenceDescription.setBusinessId(Long.valueOf(isDisEnableIds.get(i).getId()));
                referenceDescription.setReferenceDescription(ResultCode.ACCOUNTING_ITEM_ISDISENABLE.message);
                failList.add(referenceDescription);
                referenceDescription = new ReferenceDescription();
                //移除已被反禁用ID
                accountingItemDto.getIds().remove(isDisEnableIds.get(i).getId());
            }
        }
        return failList;
    }

    /**
     * @Description 修改自定义核算项目信息
     * @Author 薛永利
     * @Date 2019/7/9 10:22
     * @Param [accountingItemDto]
     * @return int
     */
    @Override
    @CacheEvict(value = Constant.RedisCache.ACCOUNTING_ITEM, key = "#accountingItemDto.id")
    public int updateAccountingItem(AccountingItemDto accountingItemDto) {
        SysUserVo operator = UserUtils.getUserVo();
        accountingItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        //校验版本号一致性
        checkAccountingItemByVersion(accountingItemDto);
        // 校验唯一性
        checkAccountingItemUniqByName(accountingItemDto);
        // 设置基础数据
        AccountingItem accountingItem = new AccountingItem();
        FastUtils.copyProperties(accountingItemDto, accountingItem);
        return accountingItemMapper.update(accountingItem, new LambdaQueryWrapper<AccountingItem>().eq(AccountingItem::getId, accountingItem.getId()));
    }

    /**
     * @return com.njwd.support.BatchResult
     * @Description 单个禁用/反禁用自定义核算项目
     * @Author 薛永利
     * @Date 2019/7/5 11:49
     * @Param [accountingItemDto]
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = Constant.RedisCache.ACCOUNTING_ITEM, key = "#accountingItemDto.id")
    public BatchResult updateById(AccountingItemDto accountingItemDto) {
        return enableAccountingItem(accountingItemDto);
    }

    /**
     * @Description 批量禁用/反禁用自定义核算项目
     * @Author 薛永利
     * @Date 2019/7/5 11:49
     * @Param [accountingItemDto]
     * @return com.njwd.support.BatchResult
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResult updateBatch(AccountingItemDto accountingItemDto) {
        //清除指定多个缓存
        RedisUtils.removeBatch("accountingItem",accountingItemDto.getIds());
        return enableAccountingItem(accountingItemDto);
    }

    /**
     * @return com.njwd.support.BatchResult
     * @Description 禁用/反禁用自定义核算项目
     * @Author 薛永利
     * @Date 2019/7/5 11:49
     * @Param [accountingItemDto]
     */
    @Transactional(rollbackFor = Exception.class)
    public BatchResult enableAccountingItem(AccountingItemDto accountingItemDto) {
        AccountingItem accountingItem = new AccountingItem();
        accountingItem.setIsDel(accountingItemDto.getIsDel());
        accountingItem.setIsEnable(accountingItemDto.getIsEnable());
        BatchResult batchResult = new BatchResult();
        List<ReferenceDescription> failList = new ArrayList<>();
        if (Constant.Is.YES.equals(accountingItemDto.getIsEnable())) {
            failList = findIsDel(accountingItemDto, 1);
        } else {
            failList = findIsDel(accountingItemDto, 0);
        }
        batchResult.setFailList(failList);
        batchResult.setSuccessList(accountingItemDto.getIds());
        if (accountingItemDto.getIds() != null&& accountingItemDto.getIds().size() > 0) {
            accountingItemDto.setBatchIds(accountingItemDto.getIds());
            //根据itemId查询大区值id
            List<Long> ids = accountingItemMapper.findAccountingItemValueByItemId(accountingItemDto);
            baseCustomService.batchEnable(accountingItemDto, accountingItemDto.getIsEnable(),accountingItemMapper,null);
            if(ids != null && ids.size()>0){
                AccountingItemValue accountingItemValue = new AccountingItemValue();
                accountingItemValue.setBatchIds(ids);
                // 禁用、反禁用自定义核算项目大区值
                baseCustomService.batchEnable(accountingItemDto, accountingItemDto.getIsEnable(),accountingItemMapper,null);
                //accountingItemMapper.update(accountingItem, new QueryWrapper<AccountingItem>().in("id", accountingItemDto.getIds()));
            }
        }
        return batchResult;
    }

    /**
     * @Description 查询自定义核算项目列表 分页
     * @Author 薛永利
     * @Date 2019/6/21 9:18
     * @Param [accountingItemDto]
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountingItemVo>
     */
    @Override
    public Page<AccountingItemVo> findAccountingItemList(AccountingItemDto accountingItemDto) {
        Page<AccountingItemVo> page = accountingItemDto.getPage();
        Page<AccountingItemVo> accountingItemVoList = accountingItemMapper.findPage(page, accountingItemDto);
        return accountingItemVoList;
    }

    /**
     * @Description 根据id查自定义核算项目详情
     * @Author 薛永利
     * @Date 2019/6/21 9:18
     * @Param [accountingItemDto]
     * @return com.njwd.entity.basedata.vo.AccountingItemVo
     */
    @Override
    @Cacheable(value = Constant.RedisCache.ACCOUNTING_ITEM, key = "#accountingItemDto.id", unless = "#result==null")
    public AccountingItemVo findById(AccountingItemDto accountingItemDto) {
        return accountingItemMapper.findById(accountingItemDto.getId());
    }

    /**
     * @Description 导出自定义核算项目excel
     * @Author 薛永利
     * @Date 2019/6/21 9:21
     * @Param [accountingItemDto, response]
     * @return void
     */
    @Override
    public void exportExcel(AccountingItemDto accountingItemDto, HttpServletResponse response) {
        Page<AccountingItemVo> page = accountingItemDto.getPage();
        fileService.resetPage(page);
        SysUserVo operator = UserUtils.getUserVo();
        accountingItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        Page<AccountingItemVo> accountingItemVoList = accountingItemMapper.findPage(page, accountingItemDto);
        fileService.exportExcel(response, accountingItemVoList.getRecords(), MenuCodeConstant.ACCOUNTING_ITEM, accountingItemDto.getIsEnterpriseAdmin());
    }

    /**
     * @Description 批量插入自定义核算项目
     * @Author 薛永利
     * @Date 2019/7/9 10:23
     * @Param [accountingItemList]
     * @return int
     */
    @Override
    public int addBatchAccountingItem(List<AccountingItem> accountingItemList) {
        return accountingItemMapper.addBatchAccountingItem(accountingItemList);
    }

    /**
     * @Description 查询所有未删除的自定义核算类型
     * @Author 周鹏
     * @Date 2019/8/23 17:12
     * @Param subjectInfo
     * @return List<AccountingItemVo>
     */
    @Override
    public List<AccountingItemVo> findAllAccountItem(Subject subjectInfo) {
        //查询自定义核算项
        List<AccountingItemVo> list = accountingItemMapper.findAllAccountItemInfo(subjectInfo);
        return list;
    }

}
