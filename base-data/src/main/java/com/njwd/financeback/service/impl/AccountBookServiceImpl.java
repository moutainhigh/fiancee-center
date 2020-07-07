package com.njwd.financeback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.cloudclient.AccountingPeriodFeignClient;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.*;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.AccountBookEntityDto;
import com.njwd.entity.basedata.dto.AccountBookSystemDto;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.basedata.vo.AccountBookSystemVo;
import com.njwd.entity.basedata.vo.AccountBookVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import com.njwd.entity.platform.dto.AccountingPeriodDto;
import com.njwd.entity.platform.dto.FinancialReportDto;
import com.njwd.entity.platform.dto.FinancialReportItemSetDto;
import com.njwd.entity.platform.vo.AccountingPeriodVo;
import com.njwd.entity.platform.vo.FinancialReportItemSetVo;
import com.njwd.entity.platform.vo.FinancialReportVo;
import com.njwd.entity.platform.vo.SysSystemVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.fileexcel.export.DataGet;
import com.njwd.financeback.cloudclient.AccountBookPeriodFeignClient;
import com.njwd.financeback.cloudclient.FinancialReportItemSetFeignClient;
import com.njwd.financeback.mapper.AccountBookEntityMapper;
import com.njwd.financeback.mapper.AccountBookMapper;
import com.njwd.financeback.service.*;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.StringUtil;
import com.njwd.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 核算账簿
 *
 * @Author: Zhuzs
 * @Date: 2019-05-17 14:51
 */
@Service
public class AccountBookServiceImpl implements AccountBookService {
    @Resource
    private AccountBookMapper accountBookMapper;
    @Autowired
    private AccountBookSystemService accountBookSystemService;
    @Autowired
    private AccountingCashFlowService accountingCashFlowService;
    @Autowired
    private FinancialReportService financialReportService;
    @Autowired
    private ReferenceRelationService referenceRelationService;
    @Resource
    private AccountBookEntityMapper accountBookEntityMapper;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private AccountBookEntityService accountBookEntityService;
    @Autowired
    private AccountBookPeriodFeignClient accountBookPeriodFeignClient;
    @Autowired
    private AccountingPeriodFeignClient accountingPeriodFeignClient;
    @Autowired
    private FinancialReportItemSetFeignClient financialReportItemSetFeignClient;

    @Override
    public AccountBookVo selectById(AccountBookDto accountBookDto) {
        return accountBookMapper.selectEntityById(accountBookDto);
    }

    /**
     * 新增核算账簿
     *
     * @param: [accountBook]
     * @return: int
     * @author: zhuzs
     * @date: 2019-09-16 17:28
     */
    @Override
    public int addAccountBook(AccountBook accountBook) {
        return accountBookMapper.insert(accountBook);
    }

    /**
     * 删除
     *
     * @param: [accountBookDto]
     * @return: com.njwd.support.BatchResult
     * @author: zhuzs
     * @date: 2019-09-16 17:28
     */
    @Override
    @Transactional
    @CacheEvict(value = Constant.RedisCache.ACCOUNT_BOOK, key = "#accountBookDto.accountBookIdList.get(0)")
    public BatchResult delete(AccountBookDto accountBookDto) {
        StringUtil.checkEmpty(accountBookDto.getAccountBookIdList().get(0));
        return deleteAccountBook(accountBookDto);
    }

    /**
     * 批量删除
     *
     * @param: [accountBookDto]
     * @return: com.njwd.support.BatchResult
     * @author: zhuzs
     * @date: 2019-09-16 17:28
     */
    @Override
    @Transactional
    @CacheEvict(value = Constant.RedisCache.ACCOUNT_BOOK, allEntries = true)
    public BatchResult deleteBatch(AccountBookDto accountBookDto) {
        return deleteAccountBook(accountBookDto);
    }

    /**
     * 修改核算账簿
     *
     * @param: [accountBookDto]
     * @return: int
     * @author: zhuzs
     * @date: 2019-09-16 17:28
     */
    @Override
    @CacheEvict(value = Constant.RedisCache.ACCOUNT_BOOK, key = "#accountBookDto.id")
    public int updateAccountBook(AccountBookDto accountBookDto) {
        StringUtil.checkEmpty(accountBookDto.getId());

        SysUserVo operator = UserUtils.getUserVo();
        accountBookDto.setUpdatorId(operator.getUserId());
        accountBookDto.setUpdatorName(operator.getName());
        return accountBookMapper.update(accountBookDto, new LambdaQueryWrapper<AccountBook>().eq(AccountBook::getId, accountBookDto.getId()));
    }

    /**
     * 根据 ID 询核算账簿
     *
     * @param: [accountBookDto]
     * @return: com.njwd.entity.basedata.vo.AccountBookVo
     * @author: zhuzs
     * @date: 2019-09-16 17:28
     */
    @Override
    @Cacheable(value = Constant.RedisCache.ACCOUNT_BOOK, key = "#accountBookDto.id", unless = "#result == null")
    public AccountBookVo findById(AccountBookDto accountBookDto) {
        StringUtil.checkEmpty(accountBookDto.getId());
        // 核算主体列表/子系统状态列表 单独提供接口
        AccountBookVo accountBookVo = accountBookMapper.selectEntityById(accountBookDto);
        // 非空校验
        FastUtils.checkNull(accountBookVo);
        setParams(accountBookVo);
        return accountBookVo;
    }

    /**
     * 根据 公司ID 询核算账簿
     *
     * @param: [accountBookDto]
     * @return: com.njwd.entity.basedata.vo.AccountBookVo
     * @author: zhuzs
     * @date: 2019-09-16 17:27
     */
    @Override
    public AccountBookVo findByCompanyId(AccountBookDto accountBookDto) {
        StringUtil.checkEmpty(accountBookDto.getCompanyId());
        // 核算主体列表/子系统状态列表 单独提供接口
        AccountBookVo accountBookVo = accountBookMapper.selectByCompanyId(accountBookDto);
        // 非空校验
        FastUtils.checkNull(accountBookVo);
        setParams(accountBookVo);
        return accountBookVo;
    }

    /**
     * 根据 公司ID list/账簿ID list 查询核算账簿（默认核算主体，是否启用总帐模块，已打开的会计期间，会计准则，科目表）
     *
     * @param: [accountBookDto]
     * @return: java.util.List<com.njwd.entity.basedata.vo.AccountBookVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:27
     */
    @Override
    @Transactional
    public List<AccountBookVo> findAccBookDetailInfoByCompanyIdOrAccBookId(AccountBookDto accountBookDto) {
        List<AccountBookVo> accountBookVoList = new ArrayList<>();
        SysUserVo operator = UserUtils.getUserVo();
        //获取 账簿、默认核算主体、会计准则、科目表 信息
        accountBookDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        accountBookDto.setUserId(operator.getUserId());

        List<Long> idList = accountBookDto.getAccountBookIdList();
        if (idList != null && idList.size() != 0) {
            for(Long accId:idList){
                accountBookDto.setId(accId);
                AccountBookVo accountBookVoOne = findAccBookDetailInfo(accountBookDto, operator);
                accountBookVoList.add(accountBookVoOne);
            }

        }else{
            AccountBookVo accountBookVo = findAccBookDetailInfo(accountBookDto, operator);
            accountBookVoList.add(accountBookVo);
        }

        return accountBookVoList;
    }

    /**
     * 查询核算账簿列表（含 子系统信息及子系统启用状态信息） 分页 admin端
     *
     * @param: [accountBookDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountBookVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:27
     */
    @Override
    public Page<AccountBookVo> findAccountBookPage(AccountBookDto accountBookDto) {
        // 核算账簿列表 基础信息
        Page<AccountBookVo> page = accountBookDto.getPage();
        // 记录查询的Size
        Long querySize = page.getSize();
        Long queryCurrent = page.getCurrent();
        page.setSize(DataGet.MAX_PAGE_SIZE);
        page.setCurrent(Constant.Number.ONEL);
        List<AccountBookVo> accountBookVoList = accountBookMapper.findPage(accountBookDto, page);
        AccountBookSystemDto accountBookSystemDto = new AccountBookSystemDto();
        // 整合子系统数据
        for (AccountBookVo accountBookVo : accountBookVoList) {
            accountBookSystemDto.setRootEnterpriseId(accountBookVo.getRootEnterpriseId());
            accountBookSystemDto.setAccountBookId(accountBookVo.getId());
            List<SysSystemVo> sysSystemVos = accountBookSystemService.findList(accountBookSystemDto);
            accountBookVo.setSysSystemVos(sysSystemVos);
        }

        // 过滤 包含子系统状态不满足过滤条件的核算账簿
        List<AccountBookVo> accountBookVoListToRemove = new ArrayList<>();
        MARK_ONE:
        for (AccountBookVo accountBookVo : accountBookVoList) {
            boolean flag = false;
            List<SysSystemVo> sysSystemVos = accountBookVo.getSysSystemVos();
            MARK_TWO:
            for (SysSystemVo acc : sysSystemVos) {

                // 子系统状态 是否不满足过滤条件
                switch (acc.getSystemSign()) {
                    // 总帐
                    case Constant.SystemSignValue.LEDGER:
                        if ((null != accountBookDto.getLedgerStatus()) && !(accountBookDto.getLedgerStatus().equals(null == acc.getStatus() ? 0 : acc.getStatus()))) {
                            flag = true;
                            break MARK_TWO;
                        }
                        continue;
                        // 资产
                    case Constant.SystemSignValue.ASSETS:
                        if ((null != accountBookDto.getAssetsStatus()) && !(accountBookDto.getAssetsStatus().equals(null == acc.getStatus() ? 0 : acc.getStatus()))) {
                            flag = true;
                            break MARK_TWO;
                        }
                        continue;

                        // 应收
                    case Constant.SystemSignValue.RECEIVABLE:
                        if ((null != accountBookDto.getReceivableStatus()) && !(accountBookDto.getReceivableStatus().equals(null == acc.getStatus() ? 0 : acc.getStatus()))) {
                            flag = true;
                            break MARK_TWO;
                        }
                        continue;

                    default:

                }
            }

            // 包含子系统状态不满足过滤条件的核算账簿
            if (flag) {
                accountBookVoListToRemove.add(accountBookVo);
            }
        }

        // 过滤 不满足过滤条件的核算账簿
        accountBookVoList.removeAll(accountBookVoListToRemove);

        for (AccountBookVo accountBookVo : accountBookVoList) {
            Map<String, SysSystemVo> map = new HashMap<>();
            List<SysSystemVo> sysSystemVos = accountBookVo.getSysSystemVos();
            for (SysSystemVo system : sysSystemVos) {
                map.put(system.getSystemSign(), system);
            }
            accountBookVo.setAccountBookSystemMap(map);
            // 获取基础信息
            setParams(accountBookVo);
        }


        // 依据查询规格 返回数据
        page.setSize(querySize);
        page.setCurrent(queryCurrent);
        page.setTotal(page.getTotal()-accountBookVoListToRemove.size());
        List<AccountBookVo> resultRecords = new ArrayList<>();
        Long startIndex = (page.getCurrent()-1)*page.getSize();
        Long endIndex = page.getSize()*page.getCurrent();
        if(page.getTotal()-startIndex<page.getSize()){
            endIndex = page.getTotal();
        }
        for(long i =startIndex ;i<endIndex;i++){
            resultRecords.add(accountBookVoList.get((int)i));
        }
        return page.setRecords(resultRecords);
    }


    /**
     * @param idSet
     * @return java.util.List<com.njwd.entity.basedata.vo.AccountBookVo>
     * @Description 根据idSet查询数据
     * @Author 朱小明
     * @Date 2019/8/7 11:53
     * @Param [idSet]
     */
    @Override
    public List<AccountBookVo> findListByIdSet(Set<Long> idSet) {
        return accountBookMapper.selectListByIdSet(idSet);
    }

    /**
     * @description: 获取权限内所有账簿
     * @param: []
     * @return: java.util.List<com.njwd.entity.basedata.vo.AccountBookVo>
     * @author: xdy
     * @create: 2019-08-22 18-57
     */
    @Override
    public List<AccountBookVo> findAuthAll(AccountBookDto accountBookDto) {
        SysUserVo userVo = UserUtils.getUserVo();
        accountBookDto.setRootEnterpriseId(userVo.getRootEnterpriseId());
        accountBookDto.setUserId(userVo.getUserId());
        return accountBookMapper.findAuthAll(accountBookDto);
    }

    /**
     * 查询权限内核算账簿列表 （含核算主体、期间 信息）
     *
     * @param: []
     * @return: java.util.List<com.njwd.entity.basedata.vo.AccountBookVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:27
     */
    @Override
    public List<AccountBookVo> findAuthAllWithEntityInfo(AccountBookDto accountBookDto) {
        SysUserVo userVo = UserUtils.getUserVo();
        accountBookDto.setRootEnterpriseId(userVo.getRootEnterpriseId());
        accountBookDto.setUserId(userVo.getUserId());
        List<AccountBookVo> accountBookVoList = accountBookMapper.findAuthAll(accountBookDto);
        List<AccountBookVo> toRemove = new ArrayList<>();
        for (AccountBookVo accountBookVo : accountBookVoList) {
            // 获取核算主体
            AccountBookEntityDto accountBookEntityDto = new AccountBookEntityDto();
            accountBookEntityDto.setAccountBookId(accountBookVo.getId());
            accountBookEntityDto.setUserId(userVo.getUserId());
            accountBookEntityDto.setRootEnterpriseId(userVo.getRootEnterpriseId());
            AccountBookEntityVo accountBookEntityVo = accountBookEntityService.findAuthOperationalEntity(accountBookEntityDto);

            // 获取期间
            AccountBookPeriodDto accountBookPeriodDto = new AccountBookPeriodDto();
            accountBookPeriodDto.setAccountBookId(accountBookVo.getId());
            accountBookPeriodDto.setSystemSign(Constant.SystemSignValue.LEDGER);
            List<AccountBookPeriodVo> accountBookPeriodVos = accountBookPeriodFeignClient.findPeriodRangeByAccBookId(accountBookPeriodDto).getData();

            if(accountBookPeriodVos == null || accountBookPeriodVos.size() == 0){
                toRemove.add(accountBookVo);
            }else{
                // 设置期间默认值
                AccountBookPeriodVo lastPeriod = new AccountBookPeriodVo();
                for (AccountBookPeriodVo accountBookPeriodVo : accountBookPeriodVos) {
                    if (Constant.Is.YES.equals(accountBookPeriodVo.getIsSettle())) {
                        lastPeriod.setPeriodYear(accountBookPeriodVo.getPeriodYear());
                        lastPeriod.setPeriodNum(accountBookPeriodVo.getPeriodNum());
                        lastPeriod.setManageInfo(accountBookPeriodVo.getManageInfo());
                        break;
                    }
                }

                accountBookVo.setDefaultAccountBookEntityVo(accountBookEntityVo);
                accountBookVo.setLastPostingPeriod(lastPeriod);
                accountBookVo.setAccountBookPeriodVoList(accountBookPeriodVos);

            }
        }
        accountBookVoList.removeAll(toRemove);
        return accountBookVoList;
    }

    /**
     * 会计期间预览数据-平台
     *
     * @param: [accountingPeriodDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.AccountingPeriodVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:27
     */
    @Override
    public List<AccountingPeriodVo> findAccountBookPeriod(AccountingPeriodDto accountingPeriodDto) {
        // 非调整期
        accountingPeriodDto.setIsAdjustment(Constant.Is.NO);
        return accountingPeriodFeignClient.findAccPerByIsAdjAndAccCal(accountingPeriodDto).getData();
    }

    /**
     * 资产负债表 预览数据
     *
     * @param: [accountBookDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.FinancialReportItemSetVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:26
     */
    @Override
    public List<FinancialReportItemSetVo> findAssetReportList(AccountBookDto accountBookDto) {
        FastUtils.checkParams(accountBookDto.getBalanceSheetId());
        FinancialReportItemSetDto param = new FinancialReportItemSetDto();
        param.setReportId(accountBookDto.getBalanceSheetId());
        return findFinancialReportItemSetList(param);

    }

    /**
     * 预览数据
     *
     * @param: [accountBookDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.FinancialReportItemSetVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:26
     */
    @Override
    public List<FinancialReportItemSetVo> findCashFlowReportList(AccountBookDto accountBookDto) {
        FastUtils.checkParams(accountBookDto.getCashFlowId());
        FinancialReportItemSetDto param = new FinancialReportItemSetDto();
        param.setReportId(accountBookDto.getCashFlowId());
        return findFinancialReportItemSetList(param);
    }

    /**
     * 预览数据
     *
     * @param: [accountBookDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.FinancialReportItemSetVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:26
     */
    @Override
    public List<FinancialReportItemSetVo> findProfitReportList(AccountBookDto accountBookDto) {
        FastUtils.checkParams(accountBookDto.getIncomeStatementId());
        FinancialReportItemSetDto param = new FinancialReportItemSetDto();
        param.setReportId(accountBookDto.getIncomeStatementId());
        return findFinancialReportItemSetList(param);
    }

    /**
     * 预览数据-平台
     *
     * @param: [platformFinancialReportItemSetDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.FinancialReportItemSetVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:26
     */
    public List<FinancialReportItemSetVo> findFinancialReportItemSetList(FinancialReportItemSetDto platformFinancialReportItemSetDto) {
        return financialReportItemSetFeignClient.findFinancialReportItemSetList(platformFinancialReportItemSetDto).getData();
    }

    /**
     * 查询核算账簿（默认核算主体，是否启用总帐模块，已打开的会计期间，会计准则，科目表）
     *
     * @param: [accountBookDto, operator]
     * @return: com.njwd.entity.basedata.vo.AccountBookVo
     * @author: zhuzs
     * @date: 2019-09-16 17:26
     */
    private AccountBookVo findAccBookDetailInfo(AccountBookDto accountBookDto, SysUserVo operator) {
        // 获取 默认核算主体
        accountBookDto.setIsDefault(Constant.Is.YES);
        AccountBookVo accountBookVo = accountBookMapper.findAccBookDetailInfoByCompanyIdOrAccBookId(accountBookDto);
        if (accountBookVo == null) {
            // 无默认核算主体 则按照核算主体code升序 取第一位
            accountBookDto.setIsDefault(null);
            accountBookVo = accountBookMapper.findAccBookDetailInfoByCompanyIdOrAccBookId(accountBookDto);
        }
        if(accountBookVo == null){
            throw new ServiceException(ResultCode.OPERATIONAL_ACCOUNTBOOKENTITY_NOT_EXIST);
        }

        // 获取账簿期间数据——latest
        AccountBookPeriodDto accountBookPeriodDto = new AccountBookPeriodDto();
        accountBookPeriodDto.setAccountBookId(accountBookVo.getId());
        accountBookPeriodDto.setSystemSign(Constant.SystemSignValue.LEDGER);
        List<AccountBookPeriodVo> accountBookPeriodVoList = accountBookPeriodFeignClient.findOpenedPeriodListByAccBookIdAndSystemSign(accountBookPeriodDto).getData();
        // 非空校验
        FastUtils.checkNull(accountBookPeriodVoList);
        accountBookVo.setAccountBookPeriodVoList(accountBookPeriodVoList);

        // 是否启用总帐
        accountBookVo.setLedgerStatus(Constant.Is.NO);
        AccountBookSystemDto accountBookSystemDto = new AccountBookSystemDto();
        accountBookSystemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        accountBookSystemDto.setAccountBookId(accountBookVo.getId());
        accountBookSystemDto.setSystemSign(Constant.SystemSignValue.LEDGER);
        List<AccountBookSystemVo> accountBookSystemVoList = accountBookSystemService.findEnableList(accountBookSystemDto);
        if (accountBookSystemVoList != null && accountBookSystemVoList.size() != 0) {
            accountBookVo.setLedgerStatus(Constant.Is.YES);
            accountBookVo.setCashFlowEnableStatus(accountBookSystemVoList.get(0).getCashFlowEnableStatus());
        }
        return accountBookVo;
    }

    /**
     * 删除核算账簿
     *
     * @param: [accountBookDto]
     * @return: com.njwd.support.BatchResult
     * @author: zhuzs
     * @date: 2019-09-16 17:26
     */
    @Transactional
    protected BatchResult deleteAccountBook(AccountBookDto accountBookDto) {
        SysUserVo operator = UserUtils.getUserVo();

        // 操作详情
        BatchResult batchResult = new BatchResult();
        List<ReferenceDescription> failList = new ArrayList<>();
        batchResult.setFailList(failList);
        List<Long> idList = accountBookDto.getAccountBookIdList();

        if (CollectionUtils.isNotEmpty(idList)) {
            // 查询已删除的记录id , 放入操作失败集合
            FastUtils.filterIds(ResultCode.IS_DEL, accountBookMapper, new QueryWrapper<AccountBook>().eq(Constant.ColumnName.IS_DEL, Constant.Is.YES), Constant.ColumnName.ID, idList, failList);
            // 查询被引用的记录,有则放入操作失败集合
            ReferenceContext referenceContext = referenceRelationService.isReference(Constant.Reference.ACCOUNT_BOOK, idList);
            failList.addAll(referenceContext.getReferences());

            if (!referenceContext.getNotReferences().isEmpty()) {
                // 修改公司 建账状态
                idList = referenceContext.getNotReferences();
                companyService.updateBatch(idList);

                // 删除 核算账簿
                AccountBook accountBook = new AccountBook();
                accountBook.setIsDel(Constant.Is.YES);
                accountBook.setUpdatorId(operator.getUpdatorId());
                accountBook.setUpdatorName(operator.getUpdatorName());
                FastUtils.updateBatch(accountBookMapper, accountBook, Constant.ColumnName.ID, referenceContext.getNotReferences(),null);

                // 删除 账簿关联的核算主体
                AccountBookEntity accountBookEntity = new AccountBookEntity();
                accountBookEntity.setIsDel(Constant.Is.YES);
                accountBookEntity.setUpdatorId(operator.getUpdatorId());
                accountBookEntity.setUpdatorName(operator.getUpdatorName());
                FastUtils.updateBatch(accountBookEntityMapper, accountBookEntity, Constant.ColumnName.ACCOUNT_BOOK_ID, referenceContext.getNotReferences(),null);

                batchResult.setSuccessList(referenceContext.getNotReferences());
            }
        }

        return batchResult;
    }

    /**
     * 设置基础数据
     *
     * @param: [accountBookVo]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 17:25
     */
    public void setParams(AccountBookVo accountBookVo) {
        // 现金流量项目表 后台获取
        AccountCashFlow accountCashFlow = new AccountCashFlow();
        accountCashFlow.setRootEnterpriseId(accountBookVo.getRootEnterpriseId());
        accountCashFlow.setAccStandardId(accountBookVo.getAccountingStandardId());
        accountCashFlow.setAccountBookTypeId(accountBookVo.getAccountBookTypeId());
        accountCashFlow = accountingCashFlowService.findAccountCashFlow(accountCashFlow);

        FinancialReportDto platformFinancialReportDto = new FinancialReportDto();
        platformFinancialReportDto.setAccStandardId(accountBookVo.getAccountingStandardId());
        // 资产负债表
        List<FinancialReportVo> assetList = financialReportService.findAssetListByAccStandardId(platformFinancialReportDto);
        // 现金流量表
        List<FinancialReportVo> cashFlowList = financialReportService.findCashFlowListByAccStandardId(platformFinancialReportDto);
        // 利润表
        List<FinancialReportVo> profitList = financialReportService.findProfitListByAccStandardId(platformFinancialReportDto);

        accountBookVo.setAccountCashFlow(accountCashFlow);
        accountBookVo.setAssetList(assetList);
        accountBookVo.setCashFlowList(cashFlowList);
        accountBookVo.setProfitList(profitList);
    }

    /**
     * @param accountBookPeriodDto
     * @return java.util.List<com.njwd.entity.platform.vo.AccountingPeriodVo>
     * @Description
     * @Author 朱小明
     * @Date 2019/9/16
     **/
    @Override
    public List<AccountingPeriodVo> findAccountingPeriodForUpd(AccountBookPeriodDto accountBookPeriodDto) {
        AccountBook accountBook = accountBookMapper.selectById(accountBookPeriodDto.getAccountBookId());
        AccountingPeriodDto accountingPeriodDto = new AccountingPeriodDto();
        accountingPeriodDto.setAccCalendarId(accountBook.getAccountingCalendarId());
        accountingPeriodDto.setIsAdjustment(Constant.Is.NO);
        Result<List<AccountingPeriodVo>> accountingResult = accountingPeriodFeignClient.findAccPerByIsAdjAndAccCal(accountingPeriodDto);
        if (accountingResult.getData() == null) {
            throw new ServiceException(ResultCode.FEIGN_CONNECT_ERROR);
        }
        return accountingResult.getData().stream().sorted(Comparator.comparing(AccountingPeriodVo::getStartDate)).filter(e->e.getPeriodYear()*100+e.getPeriodNum()
                > accountBookPeriodDto.getPeriodYearNum()).limit(accountBookPeriodDto.getFuturePeriodNum()).collect(Collectors.toList());
    }

}
