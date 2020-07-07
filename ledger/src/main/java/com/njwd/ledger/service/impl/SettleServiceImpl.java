package com.njwd.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.njwd.common.Constant;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.basedata.AccountBookEntity;
import com.njwd.entity.basedata.AccountBookSystem;
import com.njwd.entity.basedata.dto.AccountBookEntityDto;
import com.njwd.entity.basedata.dto.AccountBookSystemDto;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.basedata.vo.CompanyVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.*;
import com.njwd.entity.ledger.dto.*;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import com.njwd.entity.ledger.vo.BalanceSubjectAuxiliaryVo;
import com.njwd.entity.ledger.vo.ParameterSetVo;
import com.njwd.entity.ledger.vo.SettleResult;
import com.njwd.entity.platform.AccountSubject;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.entity.platform.vo.AccountingPeriodVo;
import com.njwd.exception.FeignClientErrorMsg;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.ledger.cloudclient.*;
import com.njwd.ledger.mapper.BalanceSubjectAuxiliaryItemMapper;
import com.njwd.ledger.mapper.BalanceSubjectAuxiliaryMapper;
import com.njwd.ledger.mapper.BalanceSubjectMapper;
import com.njwd.ledger.mapper.VoucherMapper;
import com.njwd.ledger.service.*;
import com.njwd.ledger.utils.LedgerUtils;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.JsonUtils;
import com.njwd.utils.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/09/20
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SettleServiceImpl implements SettleService {
    private final static Logger LOGGER = LoggerFactory.getLogger(SettleServiceImpl.class);
    @Resource
    private CommonService commonService;
    @Resource
    private AccountBookSystemFeignClient accountBookSystemFeignClient;
    @Resource
    private VoucherAdjustService voucherAdjustService;
    @Resource
    private VoucherService voucherService;
    @Resource
    private CompanyFeignClient companyFeignClient;
    @Resource
    private BalanceSubjectService balanceSubjectService;
    @Resource
    private BalanceSubjectAuxiliaryService balanceSubjectAuxiliaryService;
    @Resource
    private BalanceSubjectAuxiliaryItemService balanceSubjectAuxiliaryItemService;
    @Resource
    private AccountSubjectFeignClient accountSubjectFeignClient;
    @Resource
    private AccountBookPeriodService accountBookPeriodService;
    @Resource
    private AccountBookEntityFeignClient accountBookEntityFeignClient;
    @Resource
    private VoucherEntryService voucherEntryService;
    @Resource
    private VoucherEntryAuxiliaryService voucherEntryAuxiliaryService;
    @Resource
    private VoucherMapper voucherMapper;
    @Resource
    private BalanceSubjectMapper balanceSubjectMapper;
    @Resource
    private BalanceSubjectAuxiliaryMapper balanceSubjectAuxiliaryMapper;
    @Resource
    private BalanceSubjectAuxiliaryItemMapper balanceSubjectAuxiliaryItemMapper;
    @Resource
    private AccountBookFeignClient accountBookFeignClient;

    @Override
    public SettleResult settle(AccountBookPeriodDto accountBookPeriodDto, @Nullable AccountBookPeriod nextPeriod) {
        AccountBookPeriod existAccountBookPeriod = getExistPeriod(accountBookPeriodDto);
        FastUtils.checkNull(existAccountBookPeriod);
        if (Constant.Is.YES.equals(existAccountBookPeriod.getIsSettle())) {
            // 已结账
            throw new ServiceException(ResultCode.ACCOUNT_PERIOD_SETTLE, existAccountBookPeriod);
        }
        SysUserVo operator = UserUtils.getUserVo();
        // 取总账参数设置
        ParameterSetVo parameterSet = commonService.getParameterSet(operator);
        // 取账簿启用子系统记录
        AccountBookSystem existAccountBookSystem = getAccountBookSystem(existAccountBookPeriod.getAccountBookId());
        // 结账检查
        SettleResult settleResult = checkForSettle(existAccountBookPeriod, parameterSet, existAccountBookSystem);
        if (settleResult.getCheckFlag()) {
            throw new ServiceException(ResultCode.NOT_SETTLE_CONDITION, settleResult);
        }
        // 获取所有末级科目,用于生成损益凭证及计算其他科目的本期累计/余额->写入下期
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        accountSubjectDto.setIsFinal(Constant.Is.YES);
        // 查出包含逻辑删除的科目
        accountSubjectDto.setIfFindNotDelOnly(Constant.Is.NO);
        // 查询辅助核算明细
        accountSubjectDto.setIfFindAuxiliary(Constant.Is.YES);
        List<AccountSubjectVo> allSubjects = accountSubjectFeignClient.findInfoForLedger(accountSubjectDto).getData();
        // 根据参数配置，跨平台查出下一期或下二期的期间配置
        List<AccountingPeriodVo> platformAccountingPeriods = getAccountingPeriodVos(existAccountBookPeriod, parameterSet);
        if (nextPeriod == null) {
            // 如果数据库中未查到下一个期间,手动设置下一个期间
            nextPeriod = new AccountBookPeriod();
            // 下一期间索引为0
            AccountingPeriodVo nextAccountingPeriod = platformAccountingPeriods.get(0);
            nextPeriod.setPeriodYear(nextAccountingPeriod.getPeriodYear());
            nextPeriod.setPeriodNum(nextAccountingPeriod.getPeriodNum());
            nextPeriod.setPeriodYearNum(nextAccountingPeriod.getPeriodYear() * 100 + nextAccountingPeriod.getPeriodNum());
        }
        if (allSubjects != null && !allSubjects.isEmpty()) {
            // 筛选出所有损益科目
            Map<Long, AccountSubjectVo> allSubjectDict = new LinkedHashMap<>();
            Map<Long, AccountSubjectVo> profitLossSubjectDict = new LinkedHashMap<>();
            allSubjects.forEach(accountSubjectVo -> {
                if (Constant.Is.YES.equals(accountSubjectVo.getIsProfitAndLoss())) {
                    profitLossSubjectDict.put(accountSubjectVo.getId(), accountSubjectVo);
                }
                allSubjectDict.put(accountSubjectVo.getId(), accountSubjectVo);
            });
            // 损益结转,并更新凭证发生额
            AccountBookPeriodVo accountBookPeriodVo = new AccountBookPeriodVo();
            FastUtils.copyProperties(existAccountBookPeriod, accountBookPeriodVo);
            addProfitLossVouchers(profitLossSubjectDict, accountBookPeriodVo, parameterSet, operator, settleResult);
            // 计算本期余额/累计 并写入下期
            countBalanceToNextPeriod(allSubjectDict, existAccountBookPeriod, existAccountBookSystem, nextPeriod);
        }
        // 结账,打开下一个或两个期间
        settleAndOpenPeriod(existAccountBookPeriod, parameterSet, platformAccountingPeriods, operator);
        return settleResult;
    }

    @Override
    public AccountBookPeriod cancelSettle(AccountBookPeriodDto accountBookPeriodDto) {
        SysUserVo operator = UserUtils.getUserVo();
        // 取总账参数设置
        ParameterSetVo parameterSet = commonService.getParameterSet(operator);
        if (Constant.Is.NO.equals(FastUtils.getParamSetSub(parameterSet, accountBookPeriodDto.getAccountBookId(), Constant.ParameterSetKey.IS_OPEN_ACCOUNTS).getValue().byteValue())) {
            // 未开启反结账功能
            throw new ServiceException(ResultCode.FORBID_CANCEL_SETTLE);
        }
        // 判断是否最后一期结账期间
        AccountBookPeriod lastSettlePeriod = accountBookPeriodService.getOne(new LambdaQueryWrapper<AccountBookPeriod>()
                .eq(AccountBookPeriod::getAccountBookId, accountBookPeriodDto.getAccountBookId())
                .eq(AccountBookPeriod::getSystemSign, Constant.SystemSign.LEDGER)
                .eq(AccountBookPeriod::getIsSettle, Constant.Is.YES)
                .orderByDesc(AccountBookPeriod::getPeriodYearNum)
                .last(Constant.ConcatSql.LIMIT_1));
        if (lastSettlePeriod == null) {
            throw new ServiceException(ResultCode.ACCOUNT_PERIOD_NOT_SETTLE, getExistPeriod(accountBookPeriodDto));
        }
        int compare = Integer.compare(lastSettlePeriod.getPeriodYearNum(), accountBookPeriodDto.getPeriodYearNum());
        if (compare < 0) {
            throw new ServiceException(ResultCode.ACCOUNT_PERIOD_NOT_SETTLE, getExistPeriod(accountBookPeriodDto));
        } else if (compare > 0) {
            String errorMsg = String.format(ResultCode.CANCEL_SETTLE_PERIOD_ERROR.message, lastSettlePeriod.getPeriodYear(), lastSettlePeriod.getPeriodNum(), accountBookPeriodDto.getPeriodYear(), accountBookPeriodDto.getPeriodNum());
            throw new ServiceException(errorMsg, ResultCode.CANCEL_SETTLE_PERIOD_ERROR);
        }
        // 删除损益与结转凭证
        removeProfitLossVouchers(lastSettlePeriod);
        // 变更期间结账状态
        lastSettlePeriod.setIsSettle(Constant.Is.NO);
        lastSettlePeriod.setCancelSettleTime(new Date());
        lastSettlePeriod.setCancelSettleUserId(operator.getUserId());
        lastSettlePeriod.setCancelSettleUserName(operator.getName());
        accountBookPeriodService.updateById(lastSettlePeriod);
        // 触发凭证整理,使序列最大值与最后一张相符
        AccountBookPeriodVo accountBookPeriodVo = new AccountBookPeriodVo();
        FastUtils.copyProperties(lastSettlePeriod, accountBookPeriodVo);
        voucherAdjustService.adjustExcute(Collections.singletonList(accountBookPeriodVo));
        return lastSettlePeriod;
    }

    /**
     * 根据账簿id+期间获取数据库中的数据
     *
     * @param accountBookPeriodDto accountBookPeriodDto
     * @return com.njwd.entity.ledger.AccountBookPeriod
     * @author xyyxhcj@qq.com
     * @date 2019/10/18 21:00
     **/
    private AccountBookPeriod getExistPeriod(AccountBookPeriodDto accountBookPeriodDto) {
        return accountBookPeriodService.getOne(new LambdaQueryWrapper<AccountBookPeriod>()
                .eq(AccountBookPeriod::getAccountBookId, accountBookPeriodDto.getAccountBookId())
                .eq(AccountBookPeriod::getSystemSign, Constant.SystemSign.LEDGER)
                .eq(AccountBookPeriod::getPeriodYearNum, accountBookPeriodDto.getPeriodYearNum())
                .last(Constant.ConcatSql.LIMIT_1));
    }

    /**
     * 删除损益与结转凭证
     *
     * @param lastSettlePeriod lastSettlePeriod
     * @author xyyxhcj@qq.com
     * @date 2019/9/29 9:33
     **/
    private void removeProfitLossVouchers(AccountBookPeriod lastSettlePeriod) {
        // 获取所有损益与结转凭证
        List<VoucherDto> profitLossVouchers = voucherMapper.findLossProfitIdsByAccountBookPeriod(lastSettlePeriod);
        if (!profitLossVouchers.isEmpty()) {
            List<Long> voucherIds = profitLossVouchers.stream().map(Voucher::getId).collect(Collectors.toList());
            // 取出所有涉及的分录+核算明细
            LinkedList<VoucherEntryDto> entryList = voucherEntryService.findList(voucherIds);
            LinkedList<VoucherEntryAuxiliaryDto> entryAuxiliaryList = voucherEntryAuxiliaryService.findList(voucherIds);
            // 构建字典 key为分录id
            Map<Long, VoucherEntryDto> entryIdDict = new LinkedHashMap<>();
            // key为凭证id
            Map<Long, List<VoucherEntryDto>> voucherIdDict = new LinkedHashMap<>();
            // 收集所有科目ID
            Set<Long> subjectIds = new HashSet<>();
            // 整合凭证
            for (VoucherEntryDto entryDto : entryList) {
                entryIdDict.put(entryDto.getId(), entryDto);
                voucherIdDict.computeIfAbsent(entryDto.getVoucherId(), k -> new LinkedList<>()).add(entryDto);
                subjectIds.add(entryDto.getAccountSubjectId());
            }
            entryAuxiliaryList.forEach(entryAuxiliary -> entryIdDict.get(entryAuxiliary.getEntryId()).getEditAuxiliaryList().add(entryAuxiliary));
            profitLossVouchers.forEach(voucher -> voucher.getEditEntryList().addAll(voucherIdDict.get(voucher.getId())));
            // 为了复用计算及更新余额的方法 需要在entry中放入辅助核算余额表对应的auxiliaryBalanceId
            fillEntryBalanceId(lastSettlePeriod, entryList, subjectIds);
            // 计算需更新的发生额
            // 第一个key为核算主体ID 第二个key为科目Id
            Map<Long, Map<Long, BalanceSubjectDto>> balanceSubjectMap = new LinkedHashMap<>();
            // key为balanceId
            Map<Long, BalanceSubjectAuxiliaryDto> balanceSubjectAuxiliaryMap = new LinkedHashMap<>();
            // 计算准备更新的发生额
            countBalance(profitLossVouchers, balanceSubjectMap, balanceSubjectAuxiliaryMap);
            // 更新余额
            AccountBookPeriodVo accountBookPeriodVo = new AccountBookPeriodVo();
            FastUtils.copyProperties(lastSettlePeriod, accountBookPeriodVo);
            updateBalance(null, accountBookPeriodVo, balanceSubjectMap, balanceSubjectAuxiliaryMap, Constant.BalanceUpdateType.SUBTRACT);
            // 逻辑删除
            voucherMapper.deleteBatch(profitLossVouchers);
        }
    }

    /**
     * 在entry中放入辅助核算余额表对应的auxiliaryBalanceId
     *
     * @param existPeriod existPeriod
     * @param entryList   entryList
     * @param subjectIds  subjectIds
     * @author xyyxhcj@qq.com
     * @date 2019/9/29 11:10
     **/
    private void fillEntryBalanceId(AccountBookPeriod existPeriod, LinkedList<VoucherEntryDto> entryList, Set<Long> subjectIds) {
        if (subjectIds.isEmpty()) {
            // 如果辅助核算科目id为空 不继续处理
            return;
        }
        // 查出上期相关科目的辅助核算项列表
        List<BalanceSubjectAuxiliaryItem> balanceSubjectAuxiliaryItems = balanceSubjectAuxiliaryItemMapper.selectList(new LambdaQueryWrapper<BalanceSubjectAuxiliaryItem>()
                .in(BalanceSubjectAuxiliaryItem::getAccountSubjectId, subjectIds)
                .eq(BalanceSubjectAuxiliaryItem::getAccountBookId, existPeriod.getAccountBookId())
                .eq(BalanceSubjectAuxiliaryItem::getPeriodYear, existPeriod.getPeriodYear())
                .eq(BalanceSubjectAuxiliaryItem::getPeriodNum, existPeriod.getPeriodNum()));
        // 将BalanceSubjectAuxiliaryItem归类排序,一级key'核算主体id',二级key'科目id',三级key'balanceId'
        Map<Long, Map<Long, Map<Long, Set<BalanceSubjectAuxiliaryItem>>>> sortAuxiliaryItemMap = new LinkedHashMap<>();
        for (BalanceSubjectAuxiliaryItem auxiliaryItem : balanceSubjectAuxiliaryItems) {
            sortAuxiliaryItemMap.computeIfAbsent(auxiliaryItem.getAccountBookEntityId(), k -> new LinkedHashMap<>())
                    .computeIfAbsent(auxiliaryItem.getAccountSubjectId(), k -> new LinkedHashMap<>())
                    .computeIfAbsent(auxiliaryItem.getBalanceAuxiliaryId(), k -> new HashSet<>()).add(auxiliaryItem);
        }
        // 一级key为核算主体id 二级key为keySign(该keySign包含了科目id标识),value为balanceId
        Map<Long, Map<String, Long>> keySignDict = new LinkedHashMap<>();
        for (Map.Entry<Long, Map<Long, Map<Long, Set<BalanceSubjectAuxiliaryItem>>>> entityIdEntry : sortAuxiliaryItemMap.entrySet()) {
            Long accountBookEntityId = entityIdEntry.getKey();
            for (Map.Entry<Long, Map<Long, Set<BalanceSubjectAuxiliaryItem>>> subjectIdEntry : entityIdEntry.getValue().entrySet()) {
                Long subjectId = subjectIdEntry.getKey();
                for (Map.Entry<Long, Set<BalanceSubjectAuxiliaryItem>> balanceIdEntry : subjectIdEntry.getValue().entrySet()) {
                    Long balanceId = balanceIdEntry.getKey();
                    String keySign = concatKeySign(subjectId, balanceIdEntry.getValue());
                    keySignDict.computeIfAbsent(accountBookEntityId, k -> new LinkedHashMap<>()).put(keySign, balanceId);
                }

            }
        }
        for (VoucherEntryDto entryDto : entryList) {
            if (!entryDto.getEditAuxiliaryList().isEmpty()) {
                // 科目涉及辅助核算 计算keySign 获取balanceId
                StringBuilder keySign = new StringBuilder();
                keySign.append(entryDto.getAccountSubjectId()).append(Constant.Character.UNDER_LINE);
                for (VoucherEntryAuxiliaryDto entryAuxiliary : new HashSet<>(entryDto.getEditAuxiliaryList())) {
                    keySign.append(entryAuxiliary.getSign()).append(Constant.Character.UNDER_LINE);
                }
                Map<String, Long> keySignMap = keySignDict.get(entryDto.getAccountBookEntityId());
                String keyStr = keySign.toString();
                if (keySignMap == null) {
                    LOGGER.warn("未获取到当期的辅助核算余额id,accountBookEntityId:{},keySign:{}", entryDto.getAccountBookEntityId(), keyStr);
                    throw new ServiceException(ResultCode.DATA_ERROR);
                }
                entryDto.setAuxiliaryBalanceId(keySignMap.get(keyStr));
            }
        }
    }

    /**
     * 结账,打开下一个或两个期间
     *
     * @param existAccountBookPeriod    existAccountBookPeriod
     * @param parameterSet              parameterSet
     * @param platformAccountingPeriods platformAccountingPeriods
     * @param operator                  operator
     * @author xyyxhcj@qq.com
     * @date 2019/9/27 14:30
     **/
    private void settleAndOpenPeriod(AccountBookPeriod existAccountBookPeriod, ParameterSetVo parameterSet, List<AccountingPeriodVo> platformAccountingPeriods, SysUserVo operator) {
        List<AccountBookPeriod> needUpdatePeriod = new LinkedList<>();
        needUpdatePeriod.add(existAccountBookPeriod);
        Date now = new Date();
        // 结账当期
        existAccountBookPeriod.setIsSettle(Constant.Is.YES);
        existAccountBookPeriod.setSettleTime(now);
        existAccountBookPeriod.setSettleUserId(operator.getUserId());
        existAccountBookPeriod.setSettleUserName(operator.getName());
        // 判断下两期是否有数据
        List<AccountBookPeriod> nextPeriods = accountBookPeriodService.list(new LambdaQueryWrapper<AccountBookPeriod>()
                .eq(AccountBookPeriod::getAccountBookId, existAccountBookPeriod.getAccountBookId())
                .eq(AccountBookPeriod::getSystemSign, Constant.SystemSign.LEDGER)
                .gt(AccountBookPeriod::getPeriodYearNum, existAccountBookPeriod.getPeriodYearNum())
                .orderByAsc(AccountBookPeriod::getPeriodYearNum));
        // 数据库中已存在的未来期间数量
        int existFuturePeriodSize = nextPeriods.size();
        // 未来期间数设置
        int futureNumSet = FastUtils.getParamSetSub(parameterSet, existAccountBookPeriod.getAccountBookId(), Constant.ParameterSetKey.FUTURE_PERIOD_NUM).getValue().intValue();
        for (int i = 0; i < existFuturePeriodSize && i < futureNumSet; i++) {
            // 判断已存在的未来期间是否打开，未打开时 则打开
            AccountBookPeriod accountBookPeriod = nextPeriods.get(i);
            if (Constant.Status.OFF == accountBookPeriod.getStatus()) {
                accountBookPeriod.setStatus(Constant.Status.ON);
                accountBookPeriod.setUpdateTime(now);
                accountBookPeriod.setUpdatorId(operator.getUserId());
                accountBookPeriod.setUpdatorName(operator.getName());
                needUpdatePeriod.add(accountBookPeriod);
            }
        }
        if (existFuturePeriodSize < futureNumSet) {
            // 期间不存在，写入新的期间
            for (int i = existFuturePeriodSize; i < platformAccountingPeriods.size(); i++) {
                AccountingPeriodVo accountingPeriodVo = platformAccountingPeriods.get(i);
                AccountBookPeriod accountBookPeriod = new AccountBookPeriod();
                accountBookPeriod.setRootEnterpriseId(operator.getRootEnterpriseId());
                accountBookPeriod.setAccountBookId(existAccountBookPeriod.getAccountBookId());
                accountBookPeriod.setAccountBookName(existAccountBookPeriod.getAccountBookName());
                accountBookPeriod.setAccountBookCode(existAccountBookPeriod.getAccountBookCode());
                accountBookPeriod.setAccountBookSystemId(existAccountBookPeriod.getAccountBookSystemId());
                accountBookPeriod.setSystemName(existAccountBookPeriod.getSystemName());
                accountBookPeriod.setSystemSign(existAccountBookPeriod.getSystemSign());
                accountBookPeriod.setPeriodYear(accountingPeriodVo.getPeriodYear());
                accountBookPeriod.setPeriodNum(accountingPeriodVo.getPeriodNum());
                accountBookPeriod.setPeriodYearNum(accountingPeriodVo.getPeriodYear() * 100 + accountBookPeriod.getPeriodNum());
                accountBookPeriod.setStartDate(accountingPeriodVo.getStartDate());
                accountBookPeriod.setEndDate(accountingPeriodVo.getEndDate());
                accountBookPeriod.setStatus(Constant.Status.ON);
                accountBookPeriod.setIsRevisePeriod(accountingPeriodVo.getIsAdjustment());
                accountBookPeriod.setUpdateTime(now);
                accountBookPeriod.setUpdatorId(operator.getUserId());
                accountBookPeriod.setUpdatorName(operator.getName());
                needUpdatePeriod.add(accountBookPeriod);
            }
        }
        accountBookPeriodService.saveOrUpdateBatch(needUpdatePeriod);
    }

    /**
     * 根据参数配置，跨平台查出下一期或下二期的期间配置
     *
     * @param existAccountBookPeriod existAccountBookPeriod
     * @param parameterSet           parameterSet
     * @return java.util.List<com.njwd.entity.platform.vo.AccountingPeriodVo>
     * @author xyyxhcj@qq.com
     * @date 2019/9/27 14:29
     **/
    private List<AccountingPeriodVo> getAccountingPeriodVos(AccountBookPeriod existAccountBookPeriod, ParameterSetVo parameterSet) {
        AccountBookPeriodDto accountBookPeriodDto = new AccountBookPeriodDto();
        accountBookPeriodDto.setAccountBookId(existAccountBookPeriod.getAccountBookId());
        accountBookPeriodDto.setPeriodYearNum(existAccountBookPeriod.getPeriodYearNum());
        Byte futurePeriodNum = FastUtils.getParamSetSub(parameterSet, existAccountBookPeriod.getAccountBookId(), Constant.ParameterSetKey.FUTURE_PERIOD_NUM).getValue().byteValue();
        if (futurePeriodNum.compareTo(LedgerConstant.Settle.FUTURE) < 0) {
            // 防止总账测试的参数未来期间小于1
            throw new ServiceException(ResultCode.DATA_ERROR);
        }
        accountBookPeriodDto.setFuturePeriodNum(futurePeriodNum);
        Result<List<AccountingPeriodVo>> result = accountBookFeignClient.findAccountingPeriodForUpd(accountBookPeriodDto);
        List<AccountingPeriodVo> platformAccountingPeriods = result.getData();
        if (platformAccountingPeriods == null || platformAccountingPeriods.size() != futurePeriodNum) {
            // 如果平台返回期间数与系统配置的期间数不符，表示平台缺少数据
            throw new ServiceException(String.format(ResultCode.FEIGN_CONNECT_ERROR.message, FeignClientErrorMsg.GET_ACCOUNT_PERIOD), ResultCode.FEIGN_CONNECT_ERROR, result);
        }
        return platformAccountingPeriods;
    }

    /**
     * 计算本期余额/累计 并写入下期
     *
     * @param allSubjectDict         所有科目字典 损益科目仍需统计本年累计 key为subjectId
     * @param existAccountBookPeriod existAccountBookPeriod
     * @param existAccountBookSystem 账簿启用子系统记录
     * @param nextPeriod             下一期间
     * @author xyyxhcj@qq.com
     * @date 2019/9/25 18:07
     **/
    private void countBalanceToNextPeriod(Map<Long, AccountSubjectVo> allSubjectDict, AccountBookPeriod existAccountBookPeriod, AccountBookSystem existAccountBookSystem, AccountBookPeriod nextPeriod) {
        // 获取需要写入下期的余额数据 损益科目需要计算累计
        NeedDealBalance needDealBalance = new NeedDealBalance(allSubjectDict, existAccountBookPeriod).invoke(true);
        Map<Long, List<BalanceSubjectAuxiliaryItem>> balanceAuxiliaryItemDict = needDealBalance.balanceAuxiliaryItemDict;
        List<BalanceSubject> allBalanceList = needDealBalance.withOutAuxiliaryBalanceList;
        List<BalanceSubjectAuxiliary> withAuxiliaryBalanceList = needDealBalance.withAuxiliaryBalanceList;
        // 存储上期无辅助核算科目的累计  一级key'核算主体ID',二级key'科目ID'
        // value索引0为借方累计 索引1为贷方累计 索引2为损益借方累计 索引3为损益贷方累计
        Map<Long, Map<Long, BigDecimal[]>> preTotalWithOutAuxiliaryMap = new LinkedHashMap<>();
        // 存储上期有辅助核算科目的累计 一级key'核算主体ID',二级key'科目ID',三级key'sign(使用sourceTable和itemValueId拼接)'
        Map<Long, Map<Long, Map<String, BigDecimal[]>>> preTotalWithAuxiliaryMap = new LinkedHashMap<>();
        // 科目ID分类
        List<Long> withAuxiliarySubjectIds = needDealBalance.withAuxiliarySubjectIds;
        // 本期的辅助核算keySign字典 key为本期balanceID
        Map<Long, String> balanceIdKeySignDict = new LinkedHashMap<>();
        // 如果当前期间不为第一期
        if (existAccountBookPeriod.getPeriodNum() != 1) {
            // 再判断是否当期为系统启用期间
            if (existAccountBookPeriod.getPeriodYear().equals(existAccountBookSystem.getPeriodYear())
                    && existAccountBookPeriod.getPeriodNum().equals(existAccountBookSystem.getPeriodNum())) {
                // 如果当期为系统启用期间,则需要获取初始化数据,取本年借贷累计
                getZeroTotalAmount(existAccountBookPeriod, preTotalWithOutAuxiliaryMap, preTotalWithAuxiliaryMap, withAuxiliarySubjectIds);
            } else {
                // 非系统启用期间,从上一期中获取本年累计
                getPreTotalAmount(existAccountBookPeriod, preTotalWithOutAuxiliaryMap, preTotalWithAuxiliaryMap, withAuxiliarySubjectIds);
            }
        }
        // 计算本期数据
        // 余额主表数据
        for (BalanceSubject balanceSubject : allBalanceList) {
            Long subjectId = balanceSubject.getAccountSubjectId();
            // 计算期末余额 并获取本期发生额 索引0为借 1为贷
            BigDecimal[] amount = countAndGetAmount(allSubjectDict, balanceSubject, subjectId);
            // 计算累计金额 #索引0为借方累计 索引1为贷方累计 索引2为损益借方累计 索引3为损益贷方累计
            Map<Long, BigDecimal[]> accBookEntityDataMap = preTotalWithOutAuxiliaryMap.get(balanceSubject.getAccountBookEntityId());
            BigDecimal[] preTotal = null;
            if (accBookEntityDataMap != null) {
                // 表示该核算主体有上期记录
                preTotal = accBookEntityDataMap.get(subjectId);
            }
            countAndSetTotalAmount(balanceSubject, amount[0], amount[1], preTotal);
        }
        if (!withAuxiliarySubjectIds.isEmpty()) {
            // 辅助核算科目余额表数据
            // 先取出核算项列表，排序，拼接keySign
            List<BalanceSubjectAuxiliaryItem> balanceSubjectAuxiliaryItemList = balanceSubjectAuxiliaryItemMapper.selectList(new LambdaQueryWrapper<BalanceSubjectAuxiliaryItem>()
                    .in(BalanceSubjectAuxiliaryItem::getAccountSubjectId, withAuxiliarySubjectIds)
                    .eq(BalanceSubjectAuxiliaryItem::getAccountBookId, existAccountBookPeriod.getAccountBookId())
                    .eq(BalanceSubjectAuxiliaryItem::getPeriodYear, existAccountBookPeriod.getPeriodYear())
                    .eq(BalanceSubjectAuxiliaryItem::getPeriodNum, existAccountBookPeriod.getPeriodNum()));
            // 以balanceId为key,将BalanceSubjectAuxiliaryItem归类排序
            Map<Long, Set<BalanceSubjectAuxiliaryItem>> sortAuxiliaryItemMap = new LinkedHashMap<>();
            for (BalanceSubjectAuxiliaryItem auxiliaryItem : balanceSubjectAuxiliaryItemList) {
                sortAuxiliaryItemMap.computeIfAbsent(auxiliaryItem.getBalanceAuxiliaryId(), k -> new HashSet<>()).add(auxiliaryItem);
            }
            for (BalanceSubjectAuxiliary balanceSubjectAuxiliary : withAuxiliaryBalanceList) {
                // 拼接keySign 构造字典
                Long balanceId = balanceSubjectAuxiliary.getId();
                Set<BalanceSubjectAuxiliaryItem> auxiliaryItems = sortAuxiliaryItemMap.get(balanceId);
                String keySignStr = getKeySign(balanceSubjectAuxiliary, auxiliaryItems);
                balanceIdKeySignDict.put(balanceId, keySignStr);
                Long subjectId = balanceSubjectAuxiliary.getAccountSubjectId();
                // 计算期末余额 并获取本期发生额 索引0为借 1为贷
                BigDecimal[] amount = countAndGetAmount(allSubjectDict, balanceSubjectAuxiliary, subjectId);
                // 获取上期累计金额 #索引0为借方累计 索引1为贷方累计 索引2为损益借方累计 索引3为损益贷方累计
                Map<Long, Map<String, BigDecimal[]>> accBookEntityDataMap = preTotalWithAuxiliaryMap.get(balanceSubjectAuxiliary.getAccountBookEntityId());
                BigDecimal[] preTotal = null;
                if (accBookEntityDataMap != null) {
                    // key为拼接的sign
                    Map<String, BigDecimal[]> subjectDataMap = accBookEntityDataMap.computeIfAbsent(balanceSubjectAuxiliary.getAccountSubjectId(), k -> new LinkedHashMap<>());
                    preTotal = subjectDataMap.get(keySignStr);
                }
                countAndSetTotalAmount(balanceSubjectAuxiliary, amount[0], amount[1], preTotal);
            }
            if (!withAuxiliaryBalanceList.isEmpty()) {
                // 计算完毕，更新本期
                balanceSubjectAuxiliaryService.updateBatchById(withAuxiliaryBalanceList);
                // 更新辅助核算余额表下期
                writeNextPeriodForBalanceSubjectAuxiliary(existAccountBookPeriod, nextPeriod, balanceAuxiliaryItemDict, withAuxiliaryBalanceList, balanceIdKeySignDict);
            }
        }
        if (!allBalanceList.isEmpty()) {
            // 计算完毕，更新本期
            balanceSubjectService.updateBatchById(allBalanceList);
            // 写入下期 -> 更新科目余额主表下期
            writeNextPeriodForBalanceSubject(existAccountBookPeriod, nextPeriod, allBalanceList);
        }
    }

    /**
     * 从上期取本年累计
     *
     * @param existAccountBookPeriod      existAccountBookPeriod
     * @param preTotalWithOutAuxiliaryMap preTotalWithOutAuxiliaryMap
     * @param preTotalWithAuxiliaryMap    preTotalWithAuxiliaryMap
     * @param withAuxiliarySubjectIds     withAuxiliarySubjectIds
     * @author xyyxhcj@qq.com
     * @date 2019/9/27 18:30
     **/
    private void getPreTotalAmount(AccountBookPeriod existAccountBookPeriod, Map<Long, Map<Long, BigDecimal[]>> preTotalWithOutAuxiliaryMap, Map<Long, Map<Long, Map<String, BigDecimal[]>>> preTotalWithAuxiliaryMap, List<Long> withAuxiliarySubjectIds) {
        int prePeriodNum = existAccountBookPeriod.getPeriodNum() - 1;
        // 取上期科目余额主表 取全部科目
        List<BalanceSubject> balanceSubjectList = balanceSubjectMapper.selectList(new LambdaQueryWrapper<BalanceSubject>()
                .eq(Balance::getAccountBookId, existAccountBookPeriod.getAccountBookId())
                .eq(Balance::getPeriodYear, existAccountBookPeriod.getPeriodYear())
                .eq(Balance::getPeriodNum, prePeriodNum));
        for (BalanceSubject balanceSubject : balanceSubjectList) {
            Long accBookEntityId = balanceSubject.getAccountBookEntityId();
            Map<Long, BigDecimal[]> accBookEntityDataMap = preTotalWithOutAuxiliaryMap.computeIfAbsent(accBookEntityId, k -> new LinkedHashMap<>());
            accBookEntityDataMap.put(balanceSubject.getAccountSubjectId(),
                    // 索引0为借方累计 索引1为贷方累计 索引2为损益借方累计 索引3为损益贷方累计
                    new BigDecimal[]{balanceSubject.getTotalDebitAmount(), balanceSubject.getTotalCreditAmount(),
                            balanceSubject.getSyTotalDebitAmount(), balanceSubject.getSyTotalCreditAmount()});
        }
        if (withAuxiliarySubjectIds.isEmpty()) {
            // 无辅助核算科目时不继续查
            return;
        }
        // 取辅助核算科目余额上期
        List<BalanceSubjectAuxiliary> balanceSubjectAuxiliaryList = balanceSubjectAuxiliaryMapper.selectList(new LambdaQueryWrapper<BalanceSubjectAuxiliary>()
                .in(Balance::getAccountSubjectId, withAuxiliarySubjectIds)
                .eq(Balance::getAccountBookId, existAccountBookPeriod.getAccountBookId())
                .eq(Balance::getPeriodYear, existAccountBookPeriod.getPeriodYear())
                .eq(Balance::getPeriodNum, prePeriodNum));
        List<BalanceSubjectAuxiliaryItem> balanceSubjectAuxiliaryItemList = balanceSubjectAuxiliaryItemMapper.selectList(new LambdaQueryWrapper<BalanceSubjectAuxiliaryItem>()
                .in(BalanceSubjectAuxiliaryItem::getAccountSubjectId, withAuxiliarySubjectIds)
                .eq(BalanceSubjectAuxiliaryItem::getAccountBookId, existAccountBookPeriod.getAccountBookId())
                .eq(BalanceSubjectAuxiliaryItem::getPeriodYear, existAccountBookPeriod.getPeriodYear())
                .eq(BalanceSubjectAuxiliaryItem::getPeriodNum, prePeriodNum));
        // 以balanceId为key,将BalanceSubjectAuxiliaryItem归类排序
        Map<Long, Set<BalanceSubjectAuxiliaryItem>> sortAuxiliaryItemMap = new LinkedHashMap<>();
        for (BalanceSubjectAuxiliaryItem auxiliaryItem : balanceSubjectAuxiliaryItemList) {
            sortAuxiliaryItemMap.computeIfAbsent(auxiliaryItem.getBalanceAuxiliaryId(), k -> new HashSet<>()).add(auxiliaryItem);
        }
        for (BalanceSubjectAuxiliary balanceAuxiliary : balanceSubjectAuxiliaryList) {
            Map<String, BigDecimal[]> subjectDataMap = getKeySignAmountMap(preTotalWithAuxiliaryMap, balanceAuxiliary.getAccountBookEntityId(), balanceAuxiliary.getAccountSubjectId());
            Set<BalanceSubjectAuxiliaryItem> auxiliaryItems = sortAuxiliaryItemMap.get(balanceAuxiliary.getId());
            subjectDataMap.put(getKeySign(balanceAuxiliary, auxiliaryItems),
                    // 索引0为借方累计 索引1为贷方累计 索引2为损益借方累计 索引3为损益贷方累计
                    new BigDecimal[]{balanceAuxiliary.getTotalDebitAmount(), balanceAuxiliary.getTotalCreditAmount(),
                            balanceAuxiliary.getSyTotalDebitAmount(), balanceAuxiliary.getSyTotalCreditAmount()});
        }
    }

    /**
     * 从第0期中取本年累计 2019/10/9 修改期初录入数据获取方法
     *
     * @param existAccountBookPeriod      existAccountBookPeriod
     * @param preTotalWithOutAuxiliaryMap preTotalWithOutAuxiliaryMap
     * @param preTotalWithAuxiliaryMap    preTotalWithAuxiliaryMap
     * @param withAuxiliarySubjectIds     withAuxiliarySubjectIds
     * @author xyyxhcj@qq.com
     * @date 2019/9/27 18:29
     **/
    private void getZeroTotalAmount(AccountBookPeriod existAccountBookPeriod, Map<Long, Map<Long, BigDecimal[]>> preTotalWithOutAuxiliaryMap, Map<Long, Map<Long, Map<String, BigDecimal[]>>> preTotalWithAuxiliaryMap, List<Long> withAuxiliarySubjectIds) {
        // 取科目余额初始化主表 取全部科目
        List<BalanceSubject> balanceSubjectList = balanceSubjectMapper.selectList(new LambdaQueryWrapper<BalanceSubject>()
                .eq(BalanceSubject::getAccountBookId, existAccountBookPeriod.getAccountBookId())
                .eq(BalanceSubject::getPeriodYear, existAccountBookPeriod.getPeriodYear())
                // 初始化记录存的期间=0期
                .eq(BalanceSubject::getPeriodNum, LedgerConstant.Settle.INIT_PERIOD_NUM));
        for (BalanceSubject balance : balanceSubjectList) {
            Long accBookEntityId = balance.getAccountBookEntityId();
            Map<Long, BigDecimal[]> accBookEntityDataMap = preTotalWithOutAuxiliaryMap.computeIfAbsent(accBookEntityId, k -> new LinkedHashMap<>());
            accBookEntityDataMap.put(balance.getAccountSubjectId(),
                    // 索引0为借方累计 索引1为贷方累计 索引2为损益借方累计 索引3为损益贷方累计
                    new BigDecimal[]{balance.getTotalDebitAmount(), balance.getTotalCreditAmount(), BigDecimal.ZERO, BigDecimal.ZERO});
        }
        if (withAuxiliarySubjectIds.isEmpty()) {
            // 无辅助核算科目时不继续查
            return;
        }
        // 取辅助核算科目余额初始化表
        List<BalanceSubjectAuxiliary> balanceSubjectAuxiliaryList = balanceSubjectAuxiliaryMapper.selectList(new LambdaQueryWrapper<BalanceSubjectAuxiliary>()
                .in(BalanceSubjectAuxiliary::getAccountSubjectId, withAuxiliarySubjectIds)
                .eq(BalanceSubjectAuxiliary::getAccountBookId, existAccountBookPeriod.getAccountBookId())
                .eq(BalanceSubjectAuxiliary::getPeriodYear, existAccountBookPeriod.getPeriodYear())
                // 初始化记录存的期间=0期
                .eq(BalanceSubjectAuxiliary::getPeriodNum, LedgerConstant.Settle.INIT_PERIOD_NUM));
        List<BalanceSubjectAuxiliaryItem> balanceSubjectAuxiliaryItemList = balanceSubjectAuxiliaryItemMapper.selectList(new LambdaQueryWrapper<BalanceSubjectAuxiliaryItem>()
                .in(BalanceSubjectAuxiliaryItem::getAccountSubjectId, withAuxiliarySubjectIds)
                .eq(BalanceSubjectAuxiliaryItem::getAccountBookId, existAccountBookPeriod.getAccountBookId())
                .eq(BalanceSubjectAuxiliaryItem::getPeriodYear, existAccountBookPeriod.getPeriodYear())
                // 初始化记录存的期间=0期
                .eq(BalanceSubjectAuxiliaryItem::getPeriodNum, LedgerConstant.Settle.INIT_PERIOD_NUM));
        // 以balanceId为key,将BalanceSubjectAuxiliaryItem归类排序
        Map<Long, Set<BalanceSubjectAuxiliaryItem>> sortAuxiliaryItemMap = new LinkedHashMap<>();
        balanceSubjectAuxiliaryItemList.forEach(auxiliaryItem ->
                sortAuxiliaryItemMap.computeIfAbsent(auxiliaryItem.getBalanceAuxiliaryId(), k -> new HashSet<>()).add(auxiliaryItem));
        for (BalanceSubjectAuxiliary balanceAuxiliary : balanceSubjectAuxiliaryList) {
            Map<String, BigDecimal[]> subjectDataMap = getKeySignAmountMap(preTotalWithAuxiliaryMap, balanceAuxiliary.getAccountBookEntityId(), balanceAuxiliary.getAccountSubjectId());
            Set<BalanceSubjectAuxiliaryItem> auxiliaryItems = sortAuxiliaryItemMap.get(balanceAuxiliary.getId());
            if (auxiliaryItems == null) {
                LOGGER.warn("期初录入的辅助核算科目数据有误:{}", JsonUtils.object2Json(balanceAuxiliary));
                throw new ServiceException(ResultCode.DATA_ERROR, balanceAuxiliary);
            }
            StringBuilder keySign = new StringBuilder();
            keySign.append(balanceAuxiliary.getAccountSubjectId()).append(Constant.Character.UNDER_LINE);
            for (BalanceSubjectAuxiliaryItem auxiliaryItem : auxiliaryItems) {
                keySign.append(auxiliaryItem.getSign()).append(Constant.Character.UNDER_LINE);
            }
            subjectDataMap.put(keySign.toString(),
                    // 索引0为借方累计 索引1为贷方累计 索引2为损益借方累计 索引3为损益贷方累计
                    new BigDecimal[]{balanceAuxiliary.getTotalDebitAmount(), balanceAuxiliary.getTotalCreditAmount(),
                            BigDecimal.ZERO, BigDecimal.ZERO});
        }
    }

    /**
     * 计算期末余额 并获取本期发生额
     *
     * @param subjectDict             subjectDict
     * @param balanceSubjectAuxiliary balanceSubjectAuxiliary
     * @param subjectId               subjectId
     * @return java.math.BigDecimal[] 索引0为借 1为贷
     * @author xyyxhcj@qq.com
     * @date 2019/9/27 18:26
     **/
    private BigDecimal[] countAndGetAmount(Map<Long, AccountSubjectVo> subjectDict, Balance balanceSubjectAuxiliary, Long subjectId) {
        AccountSubjectVo accountSubjectVo = subjectDict.get(subjectId);
        if (accountSubjectVo == null || accountSubjectVo.getBalanceDirection() == null) {
            LOGGER.warn("余额表中有数据,科目表无数据或未配置余额借贷方向,科目ID:{}", subjectId);
            throw new ServiceException(ResultCode.DATA_ERROR, subjectId);
        }
        // 计算余额
        BigDecimal endBalance;
        // 计算金额 索引0为借 1为贷
        BigDecimal[] amount = new BigDecimal[]{balanceSubjectAuxiliary.getDebitAmount(), balanceSubjectAuxiliary.getCreditAmount()};
        if (isUseDebit(accountSubjectVo)) {
            // +借-贷
            endBalance = balanceSubjectAuxiliary.getOpeningBalance().add(amount[0]).subtract(amount[1]);
        } else {
            // +贷-借
            endBalance = balanceSubjectAuxiliary.getOpeningBalance().add(amount[1]).subtract(amount[0]);
        }
        balanceSubjectAuxiliary.setClosingBalance(endBalance);
        return amount;
    }

    /**
     * 计算并更新累计
     *
     * @param balance      balance
     * @param debitAmount  debitAmount
     * @param creditAmount creditAmount
     * @param preTotal     preTotal 索引0为借方累计 索引1为贷方累计 索引2为损益借方累计 索引3为损益贷方累计
     * @author xyyxhcj@qq.com
     * @date 2019/9/27 17:41
     **/
    private void countAndSetTotalAmount(Balance balance, BigDecimal debitAmount, BigDecimal creditAmount, BigDecimal[] preTotal) {
        BigDecimal totalDebit;
        BigDecimal totalCredit;
        BigDecimal syTotalDebit;
        BigDecimal syTotalCredit;
        if (preTotal == null) {
            totalDebit = debitAmount;
            totalCredit = creditAmount;
            syTotalDebit = balance.getSyDebitAmount();
            syTotalCredit = balance.getSyCreditAmount();
        } else {
            totalDebit = debitAmount.add(preTotal[0]);
            totalCredit = creditAmount.add(preTotal[1]);
            syTotalDebit = balance.getSyDebitAmount().add(preTotal[2]);
            syTotalCredit = balance.getSyCreditAmount().add(preTotal[3]);
        }
        // 刷新累计
        balance.setTotalDebitAmount(totalDebit);
        balance.setTotalCreditAmount(totalCredit);
        balance.setPostTotalDebitAmount(totalDebit);
        balance.setPostTotalCreditAmount(totalCredit);
        // 刷新损益累计
        balance.setSyTotalDebitAmount(syTotalDebit);
        balance.setSyTotalCreditAmount(syTotalCredit);
        balance.setPostSyTotalDebitAmount(syTotalDebit);
        balance.setPostSyTotalCreditAmount(syTotalCredit);
    }

    /**
     * 拼接辅助核算余额的keySign
     *
     * @param balanceAuxiliary balanceAuxiliary
     * @param auxiliaryItems   auxiliaryItems
     * @return java.lang.StringBuilder
     * @author xyyxhcj@qq.com
     * @date 2019/9/27 17:05
     **/
    private String getKeySign(BalanceSubjectAuxiliary balanceAuxiliary, Set<BalanceSubjectAuxiliaryItem> auxiliaryItems) {
        if (auxiliaryItems == null) {
            LOGGER.warn("上期的辅助核算科目数据有误:{}", JsonUtils.object2Json(balanceAuxiliary));
            throw new ServiceException(ResultCode.DATA_ERROR, balanceAuxiliary);
        }
        return concatKeySign(balanceAuxiliary.getAccountSubjectId(), auxiliaryItems);
    }

    /**
     * 取出辅助核算科目的所有金额累计
     *
     * @param preTotalWithAuxiliaryMap preTotalWithAuxiliaryMap
     * @param accountBookEntityId      accountBookEntityId
     * @param accountSubjectId         accountSubjectId
     * @return java.util.Map<java.lang.String, java.math.BigDecimal [ ]> key为拼接的sign,value索引0为借方累计 索引1为贷方累计 索引2为损益借方累计 索引3为损益贷方累计
     * @author xyyxhcj@qq.com
     * @date 2019/9/27 17:02
     **/
    private Map<String, BigDecimal[]> getKeySignAmountMap(Map<Long, Map<Long, Map<String, BigDecimal[]>>> preTotalWithAuxiliaryMap, Long accountBookEntityId, Long accountSubjectId) {
        Map<Long, Map<String, BigDecimal[]>> accBookEntityDataMap = preTotalWithAuxiliaryMap.computeIfAbsent(accountBookEntityId, k -> new LinkedHashMap<>());
        return accBookEntityDataMap.computeIfAbsent(accountSubjectId, k -> new LinkedHashMap<>());
    }

    /**
     * 更新辅助核算余额表下期
     *
     * @param existAccountBookPeriod   existAccountBookPeriod
     * @param nextPeriod               nextPeriod
     * @param balanceAuxiliaryItemDict balanceAuxiliaryItemDict
     * @param withAuxiliaryBalanceList 本期已插入到辅助核算余额表的数据
     * @param keySignDict              key为上期的balanceId
     * @author xyyxhcj@qq.com
     * @date 2019/9/27 11:42
     **/
    private void writeNextPeriodForBalanceSubjectAuxiliary(AccountBookPeriod existAccountBookPeriod, AccountBookPeriod nextPeriod, Map<Long, List<BalanceSubjectAuxiliaryItem>> balanceAuxiliaryItemDict, List<BalanceSubjectAuxiliary> withAuxiliaryBalanceList, Map<Long, String> keySignDict) {
        List<BalanceSubjectAuxiliaryVo> nextWithAuxiliaryBalanceSubjectList = new LinkedList<>();
        for (BalanceSubjectAuxiliary balanceSubjectAuxiliary : withAuxiliaryBalanceList) {
            BalanceSubjectAuxiliaryVo nextBalanceSubject = new BalanceSubjectAuxiliaryVo();
            nextWithAuxiliaryBalanceSubjectList.add(nextBalanceSubject);
            initBalance(nextPeriod, balanceSubjectAuxiliary, nextBalanceSubject);
            // 存储上期的balanceId,如果余额表中无数据，获取上期的辅助核算列表插入
            nextBalanceSubject.setPreBalanceId(balanceSubjectAuxiliary.getId());
            nextBalanceSubject.setKeySign(keySignDict.get(balanceSubjectAuxiliary.getId()));
        }
        // 取出下期所有辅助核算余额数据 需要判断是否已有数据，无则新增 有则更新
        List<BalanceSubjectAuxiliary> existNextBalanceAuxiliaries = balanceSubjectAuxiliaryMapper.selectList(new LambdaQueryWrapper<BalanceSubjectAuxiliary>()
                .eq(Balance::getAccountBookId, existAccountBookPeriod.getAccountBookId())
                .eq(Balance::getPeriodYearNum, nextPeriod.getPeriodYearNum()));
        List<BalanceSubjectAuxiliaryItem> existNextBalanceAuxiliaryItems = balanceSubjectAuxiliaryItemMapper.selectList(new LambdaQueryWrapper<BalanceSubjectAuxiliaryItem>()
                .eq(BalanceSubjectAuxiliaryItem::getAccountBookId, existAccountBookPeriod.getAccountBookId())
                .eq(BalanceSubjectAuxiliaryItem::getPeriodYearNum, nextPeriod.getPeriodYearNum()));
        // 构造字典 一级key'核算主体ID' 二级key'科目ID' 三级key'keySign'
        Map<Long, Map<Long, Map<String, BalanceSubjectAuxiliary>>> existNextBalanceAuxiliaryDict = new LinkedHashMap<>();
        // 一级key'核算主体ID' 二级key'科目ID' 三级key'balanceId'
        Map<Long, Map<Long, Map<Long, Set<BalanceSubjectAuxiliaryItem>>>> existNextBalanceAuxiliaryItemsDict = new LinkedHashMap<>();
        existNextBalanceAuxiliaryItems.forEach(existItem -> existNextBalanceAuxiliaryItemsDict
                .computeIfAbsent(existItem.getAccountBookEntityId(), k -> new LinkedHashMap<>())
                .computeIfAbsent(existItem.getAccountSubjectId(), k -> new LinkedHashMap<>())
                .computeIfAbsent(existItem.getBalanceAuxiliaryId(), k -> new HashSet<>()).add(existItem));
        existNextBalanceAuxiliaries.forEach(existBalanceAuxiliary -> {
            Long accBookEntityId = existBalanceAuxiliary.getAccountBookEntityId();
            Map<Long, Map<Long, Set<BalanceSubjectAuxiliaryItem>>> subjectAuxiliaryDictDict = existNextBalanceAuxiliaryItemsDict.get(accBookEntityId);
            if (subjectAuxiliaryDictDict == null) {
                throw new ServiceException(ResultCode.DATA_ERROR);
            }
            Long subjectId = existBalanceAuxiliary.getAccountSubjectId();
            Map<Long, Set<BalanceSubjectAuxiliaryItem>> auxiliaryItemDict = subjectAuxiliaryDictDict.get(subjectId);
            if (auxiliaryItemDict == null) {
                throw new ServiceException(ResultCode.DATA_ERROR);
            }
            Set<BalanceSubjectAuxiliaryItem> auxiliaryItems = auxiliaryItemDict.get(existBalanceAuxiliary.getId());
            if (auxiliaryItems == null) {
                throw new ServiceException(ResultCode.DATA_ERROR);
            }
            // 算出keySign
            String keySign = concatKeySign(subjectId, auxiliaryItems);
            existNextBalanceAuxiliaryDict.computeIfAbsent(accBookEntityId, k -> new LinkedHashMap<>())
                    .computeIfAbsent(subjectId, k -> new LinkedHashMap<>()).put(keySign, existBalanceAuxiliary);
        });
        // 已存在的放入主键,无数据的准备新增
        List<BalanceSubjectAuxiliary> needUpdateBalanceSubjectAuxiliaryList = new LinkedList<>();
        Iterator<BalanceSubjectAuxiliaryVo> iterator = nextWithAuxiliaryBalanceSubjectList.iterator();
        while (iterator.hasNext()) {
            BalanceSubjectAuxiliaryVo balanceSubjectAuxiliaryVo = iterator.next();
            BalanceSubjectAuxiliary existBalanceSubjectAuxiliary = getExistBalanceSubjectAuxiliary(existNextBalanceAuxiliaryDict, balanceSubjectAuxiliaryVo);
            if (existBalanceSubjectAuxiliary == null) {
                continue;
            }
            // 已存在的放入主键
            BalanceSubjectAuxiliary updateAuxiliary = new BalanceSubjectAuxiliary();
            FastUtils.copyProperties(balanceSubjectAuxiliaryVo, updateAuxiliary);
            updateAuxiliary.setId(existBalanceSubjectAuxiliary.getId());
            needUpdateBalanceSubjectAuxiliaryList.add(updateAuxiliary);
            // 从准备插入的数据中移除
            iterator.remove();
        }
        // 更新辅助核算余额表
        if (!nextWithAuxiliaryBalanceSubjectList.isEmpty()) {
            balanceSubjectAuxiliaryMapper.insertBatchForNextPeriod(nextWithAuxiliaryBalanceSubjectList);
            // 同时插入维护辅助核算项表 根据balance拿到本期的itemList 去插入下期
            List<BalanceSubjectAuxiliaryItem> balanceSubjectAuxiliaryItems = getPreBalanceSubjectAuxiliaryItems(nextPeriod, balanceAuxiliaryItemDict, nextWithAuxiliaryBalanceSubjectList);
            balanceSubjectAuxiliaryItemService.saveBatch(balanceSubjectAuxiliaryItems);
        }
        if (!needUpdateBalanceSubjectAuxiliaryList.isEmpty()) {
            balanceSubjectAuxiliaryService.updateBatchById(needUpdateBalanceSubjectAuxiliaryList);
        }
    }

    /**
     * 通过keySign获取已存在的辅助核算科目余额数据
     *
     * @param existNextBalanceAuxiliaryDict 一级key'核算主体ID' 二级key'科目ID' 三级key'keySign'
     * @param balanceSubjectAuxiliaryVo     balanceSubjectAuxiliaryVo
     * @return com.njwd.entity.ledger.BalanceSubjectAuxiliary
     * @author xyyxhcj@qq.com
     * @date 2019/9/29 11:29
     **/
    private @Nullable
    BalanceSubjectAuxiliary getExistBalanceSubjectAuxiliary(Map<Long, Map<Long, Map<String, BalanceSubjectAuxiliary>>> existNextBalanceAuxiliaryDict, BalanceSubjectAuxiliaryVo balanceSubjectAuxiliaryVo) {
        Map<Long, Map<String, BalanceSubjectAuxiliary>> subjectBalanceDict = existNextBalanceAuxiliaryDict.get(balanceSubjectAuxiliaryVo.getAccountBookEntityId());
        if (subjectBalanceDict == null) {
            return null;
        }
        Map<String, BalanceSubjectAuxiliary> keySignAuxiliary = subjectBalanceDict.get(balanceSubjectAuxiliaryVo.getAccountSubjectId());
        if (keySignAuxiliary == null) {
            return null;
        }
        return keySignAuxiliary.get(balanceSubjectAuxiliaryVo.getKeySign());
    }

    /**
     * 拼接辅助核算维度标识
     *
     * @param subjectId      subjectId
     * @param auxiliaryItems auxiliaryItems
     * @return java.lang.StringBuilder
     * @author xyyxhcj@qq.com
     * @date 2019/9/27 18:19
     **/
    private String concatKeySign(Long subjectId, Set<BalanceSubjectAuxiliaryItem> auxiliaryItems) {
        StringBuilder keySign = new StringBuilder();
        keySign.append(subjectId).append(Constant.Character.UNDER_LINE);
        for (BalanceSubjectAuxiliaryItem auxiliaryItem : auxiliaryItems) {
            keySign.append(auxiliaryItem.getSign()).append(Constant.Character.UNDER_LINE);
        }
        return keySign.toString();
    }

    /**
     * 根据balance拿到本期的itemList
     *
     * @param nextPeriod                          nextPeriod
     * @param auxiliaryItemDict                   本期的辅助核算itemList字典,key为balanceId
     * @param nextWithAuxiliaryBalanceSubjectList nextWithAuxiliaryBalanceSubjectList
     * @return java.util.List<com.njwd.entity.ledger.BalanceSubjectAuxiliaryItem>
     * @author xyyxhcj@qq.com
     * @date 2019/9/27 11:44
     **/
    private List<BalanceSubjectAuxiliaryItem> getPreBalanceSubjectAuxiliaryItems(AccountBookPeriod nextPeriod, Map<Long, List<BalanceSubjectAuxiliaryItem>> auxiliaryItemDict, List<BalanceSubjectAuxiliaryVo> nextWithAuxiliaryBalanceSubjectList) {
        List<BalanceSubjectAuxiliaryItem> balanceSubjectAuxiliaryItems = new LinkedList<>();
        for (BalanceSubjectAuxiliaryVo balanceSubjectAuxiliaryVo : nextWithAuxiliaryBalanceSubjectList) {
            List<BalanceSubjectAuxiliaryItem> auxiliaryItems = auxiliaryItemDict.get(balanceSubjectAuxiliaryVo.getPreBalanceId());
            if (auxiliaryItems == null || auxiliaryItems.isEmpty()) {
                throw new ServiceException(ResultCode.DATA_ERROR);
            }
            for (BalanceSubjectAuxiliaryItem auxiliaryItem : auxiliaryItems) {
                auxiliaryItem.setId(null);
                auxiliaryItem.setBalanceAuxiliaryId(balanceSubjectAuxiliaryVo.getId());
                auxiliaryItem.setPeriodYear(nextPeriod.getPeriodYear());
                auxiliaryItem.setPeriodNum(nextPeriod.getPeriodNum());
                auxiliaryItem.setPeriodYearNum(nextPeriod.getPeriodYearNum());
                balanceSubjectAuxiliaryItems.add(auxiliaryItem);
            }
        }
        return balanceSubjectAuxiliaryItems;
    }

    /**
     * 更新科目余额主表下期
     *
     * @param existAccountBookPeriod existAccountBookPeriod
     * @param nextPeriod             nextPeriod
     * @param allBalanceList         allBalanceList
     * @author xyyxhcj@qq.com
     * @date 2019/9/26 17:47
     **/
    private void writeNextPeriodForBalanceSubject(AccountBookPeriod existAccountBookPeriod, AccountBookPeriod nextPeriod, List<BalanceSubject> allBalanceList) {
        List<BalanceSubject> nextAllBalanceSubjectList = new LinkedList<>();
        for (BalanceSubject balanceSubject : allBalanceList) {
            BalanceSubject nextBalanceSubject = new BalanceSubject();
            nextAllBalanceSubjectList.add(nextBalanceSubject);
            initBalance(nextPeriod, balanceSubject, nextBalanceSubject);
        }
        // 写入下期期初，需要判断是否已有数据，无则新增 有则更新
        // 取出下期所有主表余额数据
        List<BalanceSubject> existNextBalanceSubjects = balanceSubjectMapper.selectList(new LambdaQueryWrapper<BalanceSubject>()
                .eq(Balance::getAccountBookId, existAccountBookPeriod.getAccountBookId())
                .eq(Balance::getPeriodYearNum, nextPeriod.getPeriodYearNum()));
        // 构造字典 一级key为核算主体ID 二级key为科目ID
        Map<Long, Map<Long, BalanceSubject>> existNextBalanceSubjectDict = new LinkedHashMap<>();
        existNextBalanceSubjects.forEach(existNextBalanceSubject ->
                existNextBalanceSubjectDict.computeIfAbsent(existNextBalanceSubject.getAccountBookEntityId(), k -> new LinkedHashMap<>())
                        .put(existNextBalanceSubject.getAccountSubjectId(), existNextBalanceSubject));
        // 已存在的放入主键,无数据的准备新增
        List<BalanceSubject> needUpdateBalanceSubjectList = new LinkedList<>();
        Iterator<BalanceSubject> iterator = nextAllBalanceSubjectList.iterator();
        while (iterator.hasNext()) {
            BalanceSubject balanceSubject = iterator.next();
            Map<Long, BalanceSubject> balanceSubjectDict = existNextBalanceSubjectDict.get(balanceSubject.getAccountBookEntityId());
            if (balanceSubjectDict == null) {
                continue;
            }
            BalanceSubject existNextBalanceSubject = balanceSubjectDict.get(balanceSubject.getAccountSubjectId());
            if (existNextBalanceSubject == null) {
                continue;
            }
            balanceSubject.setId(existNextBalanceSubject.getId());
            needUpdateBalanceSubjectList.add(balanceSubject);
            // 从准备插入的数据中移除
            iterator.remove();
        }
        // 更新主表
        if (!nextAllBalanceSubjectList.isEmpty()) {
            balanceSubjectService.saveBatch(nextAllBalanceSubjectList);
        }
        if (!needUpdateBalanceSubjectList.isEmpty()) {
            balanceSubjectService.updateBatchById(needUpdateBalanceSubjectList);
        }
    }

    /**
     * 初始化余额数据
     *
     * @param nextPeriod    nextPeriod
     * @param sourceBalance sourceBalance
     * @param destBalance   destBalance
     * @author xyyxhcj@qq.com
     * @date 2019/9/27 18:16
     **/
    private void initBalance(AccountBookPeriod nextPeriod, Balance sourceBalance, Balance destBalance) {
        destBalance.setAccountBookId(sourceBalance.getAccountBookId());
        destBalance.setAccountBookEntityId(sourceBalance.getAccountBookEntityId());
        destBalance.setAccountSubjectId(sourceBalance.getAccountSubjectId());
        destBalance.setPeriodYear(nextPeriod.getPeriodYear());
        destBalance.setPeriodNum(nextPeriod.getPeriodNum());
        destBalance.setPeriodYearNum(nextPeriod.getPeriodYearNum());
        destBalance.setOpeningBalance(sourceBalance.getClosingBalance());
    }

    /**
     * 添加损益结转凭证,并更新发生额(直接审核及过账)
     *
     * @param profitLossSubjectDict  所有末级损益科目,key为科目id
     * @param existAccountBookPeriod existAccountBookPeriod
     * @param parameterSet           parameterSet
     * @param operator               operator
     * @param settleResult           settleResult
     * @author xyyxhcj@qq.com
     * @date 2019/9/23 13:40
     **/
    private void addProfitLossVouchers(Map<Long, AccountSubjectVo> profitLossSubjectDict, AccountBookPeriodVo existAccountBookPeriod, ParameterSetVo parameterSet, SysUserVo operator, SettleResult settleResult) {
        List<VoucherDto> generateVoucherList = generateVouchers(profitLossSubjectDict, existAccountBookPeriod, parameterSet, operator);
        if (generateVoucherList.isEmpty()) {
            // 不需要生成损益结转凭证
            return;
        }
        // 插入主表
        voucherMapper.insertGenerateBatch(generateVoucherList);
        // 保存生成凭证的id
        List<Long> lossProfitListVoucherIds = new LinkedList<>();
        settleResult.setLossProfitListVoucherIds(lossProfitListVoucherIds);
        // 插入分录及辅助核算
        for (VoucherDto generateVoucher : generateVoucherList) {
            Long generateVoucherId = generateVoucher.getId();
            lossProfitListVoucherIds.add(generateVoucherId);
            voucherEntryService.insertBatch(generateVoucher.getEditEntryList(), generateVoucherId);
            List<VoucherEntryAuxiliary> saveAuxiliaryList = voucherService.getVoucherEntryAuxiliaries(generateVoucher, generateVoucherId, null);
            if (!saveAuxiliaryList.isEmpty()) {
                voucherEntryAuxiliaryService.saveBatch(saveAuxiliaryList);
            }
        }
        // 计算需更新的发生额
        // 第一个key为核算主体ID 第二个key为科目Id
        Map<Long, Map<Long, BalanceSubjectDto>> balanceSubjectMap = new LinkedHashMap<>();
        // key为balanceId 添加的结转凭证,由于数据均取自余额表,如果有辅助核算余额ID,表示需更新辅助核算余额表(本年利润科目不可配置辅助核算)
        Map<Long, BalanceSubjectAuxiliaryDto> balanceSubjectAuxiliaryMap = new LinkedHashMap<>();
        // 计算准备更新的发生额
        countBalance(generateVoucherList, balanceSubjectMap, balanceSubjectAuxiliaryMap);
        // 更新余额
        updateBalance(parameterSet, existAccountBookPeriod, balanceSubjectMap, balanceSubjectAuxiliaryMap, Constant.BalanceUpdateType.ADD);
        // 获取凭证号主号子号,更新
        voucherService.updateVoucherCodeBatch(new LinkedList<>(), false, generateVoucherList);
        // 触发凭证整理,防止断号
        voucherAdjustService.adjustExcute(Collections.singletonList(existAccountBookPeriod));
    }

    /**
     * 汇总以前年度损益调整 及 损益结转 凭证
     *
     * @param profitLossSubjectDict  profitLossSubjectDict
     * @param existAccountBookPeriod existAccountBookPeriod
     * @param parameterSet           parameterSet
     * @param operator               operator
     * @return java.util.List<com.njwd.entity.ledger.dto.VoucherDto>
     * @author xyyxhcj@qq.com
     * @date 2019/9/25 17:30
     **/
    private List<VoucherDto> generateVouchers(Map<Long, AccountSubjectVo> profitLossSubjectDict, AccountBookPeriod existAccountBookPeriod, ParameterSetVo parameterSet, SysUserVo operator) {
        // 获取需要做结转的余额数据
        NeedDealBalance needDealBalance = new NeedDealBalance(profitLossSubjectDict, existAccountBookPeriod).invoke(false);
        Map<Long, List<BalanceSubjectAuxiliaryItem>> balanceAuxiliaryItemDict = needDealBalance.balanceAuxiliaryItemDict;
        List<BalanceSubject> withOutAuxiliaryBalanceList = needDealBalance.withOutAuxiliaryBalanceList;
        List<BalanceSubjectAuxiliary> withAuxiliaryBalanceList = needDealBalance.withAuxiliaryBalanceList;
        if (parameterSet.getCarrySubjects() == null) {
            // 校验总账设置成功后必有数据,如果无则之前的代码应该被改了
            throw new ServiceException(ResultCode.DATA_ERROR);
        }
        // 查询所有末级的以前年度损益科目
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        accountSubjectDto.setId(parameterSet.getSyAccSubjectId());
        accountSubjectDto.setIsFinal(Constant.Is.YES);
        // 查询出总账参数设置的以前年度科目的所有末级子科目 不拿辅助核算明细，只取辅助核算标识
        Result<List<AccountSubjectVo>> allSySubjectListResult = accountSubjectFeignClient.findAllChildInfo(accountSubjectDto);
        if (allSySubjectListResult.getData() == null) {
            throw new ServiceException(String.format(ResultCode.FEIGN_CONNECT_ERROR.message, FeignClientErrorMsg.GET_SUBJECT_DATA_ERROR), ResultCode.FEIGN_CONNECT_ERROR);
        }
        // 构造以前年度损益末级科目字典 key为科目ID
        Map<Long, AccountSubjectVo> sySubjectDict = new LinkedHashMap<>();
        allSySubjectListResult.getData().forEach(syAccountSubject -> sySubjectDict.put(syAccountSubject.getId(), syAccountSubject));
        // 不包含辅助核算的损益科目 Map<核算主体ID,Map<科目ID,余额数据>>
        Map<Long, Map<Long, BalanceSubject>> withOutAuxiliaryBalanceMap = new LinkedHashMap<>();
        // 包含辅助核算的损益科目 Map<核算主体ID,Map<余额ID,余额数据>>
        Map<Long, Map<Long, BalanceSubjectAuxiliary>> withAuxiliaryBalanceMap = new LinkedHashMap<>();
        // 遍历余额表数据: 将余额表数据分类 核算主体ID->科目ID->余额数据 并取出'以前年度损益调整'科目的数据
        // 第一个key为核算主体,第二个key为科目ID
        Map<Long, Map<Long, BalanceSubject>> withOutAuxiliaryPriorYearBalanceMap = new LinkedHashMap<>();
        // 此处:第一个key为核算主体,每二个key为balanceId
        Map<Long, Map<Long, BalanceSubjectAuxiliary>> withAuxiliaryPriorYearBalanceMap = new LinkedHashMap<>();
        collectBalanceMap(profitLossSubjectDict, sySubjectDict, withOutAuxiliaryBalanceList, withAuxiliaryBalanceList, withOutAuxiliaryBalanceMap, withAuxiliaryBalanceMap, withOutAuxiliaryPriorYearBalanceMap, withAuxiliaryPriorYearBalanceMap);
        // 获取核算账簿字典
        final Map<Long, AccountBookEntityVo> accBookEntityDict = getAccBookEntityDict(existAccountBookPeriod);
        // 获取凭证字
        Byte credentialWord = getCredentialWord(parameterSet, existAccountBookPeriod.getAccountBookId());
        Date now = new Date();
        // 生成以前年度损益调整的结转凭证
        List<VoucherDto> priorYearVoucherList = getPriorYearVouchers(sySubjectDict, existAccountBookPeriod, parameterSet, operator, balanceAuxiliaryItemDict, withOutAuxiliaryPriorYearBalanceMap, withAuxiliaryPriorYearBalanceMap, accBookEntityDict, credentialWord, now);
        // 生成损益结转凭证
        List<List<VoucherDto>> allProfitLossVouchers = getAllProfitLossVouchers(profitLossSubjectDict, existAccountBookPeriod, parameterSet, operator, balanceAuxiliaryItemDict, withOutAuxiliaryBalanceMap, withAuxiliaryBalanceMap, accBookEntityDict, credentialWord, now);
        // 汇总以前年度损益调整凭证及结转凭证
        List<VoucherDto> generateVoucherList = new LinkedList<>(priorYearVoucherList);
        allProfitLossVouchers.forEach(generateVoucherList::addAll);
        return generateVoucherList;
    }

    /**
     * '以前年度损益调整'科目的余额方向是否使用借方
     *
     * @param parameterSet parameterSet
     * @return boolean
     * @author xyyxhcj@qq.com
     * @date 2019/9/25 17:50
     **/
    private boolean isPriorYearSubjectUseDebit(ParameterSetVo parameterSet) {
        Map<String, Object> priorYearSubjectMap = parameterSet.getCarrySubjects().get(parameterSet.getSyAccSubjectId());
        Object priorYearSubjectBalanceDirection = priorYearSubjectMap.get(Constant.PropertyName.BALANCE_DIRECTION);
        if (priorYearSubjectBalanceDirection == null) {
            LOGGER.warn("以前年度损益调整科目数据有误:{}", JsonUtils.object2Json(priorYearSubjectMap));
            throw new ServiceException(ResultCode.DATA_ERROR, priorYearSubjectMap);
        }
        // 标记'以前年度损益调整'科目使用借方
        return priorYearSubjectBalanceDirection.equals(Constant.BalanceDirectionType.DEBIT);
    }

    /**
     * 整理余额表数据(包括辅助核算)
     *
     * @param profitLossSubjectDict               profitLossSubjectDict
     * @param sySubjectDict                       以前年度损益末级科目字典 key为科目ID
     * @param withOutAuxiliaryBalanceList         withOutAuxiliaryBalanceList
     * @param withAuxiliaryBalanceList            withAuxiliaryBalanceList
     * @param withOutAuxiliaryBalanceMap          未开启辅助核算科目 的余额表数据
     * @param withAuxiliaryBalanceMap             开启辅助核算科目 的余额表数据
     * @param withOutAuxiliaryPriorYearBalanceMap 未开启辅助核算的'以前年度损益'科目 余额数据
     * @param withAuxiliaryPriorYearBalanceMap    开启辅助核算的'以前年度损益'科目 余额数据
     * @author xyyxhcj@qq.com
     * @date 2019/9/25 17:43
     **/
    private void collectBalanceMap(Map<Long, AccountSubjectVo> profitLossSubjectDict,
                                   Map<Long, AccountSubjectVo> sySubjectDict,
                                   List<BalanceSubject> withOutAuxiliaryBalanceList,
                                   List<BalanceSubjectAuxiliary> withAuxiliaryBalanceList,
                                   final Map<Long, Map<Long, BalanceSubject>> withOutAuxiliaryBalanceMap,
                                   final Map<Long, Map<Long, BalanceSubjectAuxiliary>> withAuxiliaryBalanceMap,
                                   final Map<Long, Map<Long, BalanceSubject>> withOutAuxiliaryPriorYearBalanceMap,
                                   final Map<Long, Map<Long, BalanceSubjectAuxiliary>> withAuxiliaryPriorYearBalanceMap) {
        for (BalanceSubject balanceSubject : withOutAuxiliaryBalanceList) {
            Long subjectId = balanceSubject.getAccountSubjectId();
            AccountSubjectVo accountSubjectVo = profitLossSubjectDict.get(subjectId);
            if (accountSubjectVo == null) {
                throw new ServiceException(ResultCode.DATA_ERROR, balanceSubject);
            }
            Long accountBookEntityId = balanceSubject.getAccountBookEntityId();
            if (sySubjectDict.containsKey(subjectId)) {
                // 判断'以前年度损益调整'科目是否配置辅助核算
                if (!isHasAuxiliaryForSy(sySubjectDict.get(subjectId))) {
                    // 未配置辅助核算
                    withOutAuxiliaryPriorYearBalanceMap.computeIfAbsent(accountBookEntityId, k -> new LinkedHashMap<>())
                            .put(subjectId, balanceSubject);
                }
            } else {
                withOutAuxiliaryBalanceMap.computeIfAbsent(accountBookEntityId, k -> new LinkedHashMap<>())
                        .put(subjectId, balanceSubject);
            }
        }
        // 遍历辅助核算余额表数据: 将余额表数据分类 核算主体ID->科目ID->balanceId->余额数据 并取出'以前年度损益调整'科目的数据
        for (BalanceSubjectAuxiliary balanceSubjectAuxiliary : withAuxiliaryBalanceList) {
            Long subjectId = balanceSubjectAuxiliary.getAccountSubjectId();
            AccountSubjectVo accountSubjectVo = profitLossSubjectDict.get(subjectId);
            if (accountSubjectVo == null) {
                throw new ServiceException(ResultCode.DATA_ERROR, balanceSubjectAuxiliary);
            }
            Long accountBookEntityId = balanceSubjectAuxiliary.getAccountBookEntityId();
            if (sySubjectDict.containsKey(subjectId)) {
                if (isHasAuxiliaryForSy(sySubjectDict.get(subjectId))) {
                    // 配置了辅助核算
                    withAuxiliaryPriorYearBalanceMap.computeIfAbsent(accountBookEntityId, k -> new LinkedHashMap<>())
                            .put(balanceSubjectAuxiliary.getId(), balanceSubjectAuxiliary);
                }
            } else {
                withAuxiliaryBalanceMap.computeIfAbsent(accountBookEntityId, k -> new LinkedHashMap<>())
                        .put(balanceSubjectAuxiliary.getId(), balanceSubjectAuxiliary);
            }
        }
    }

    /**
     * 汇总损益结转凭证
     *
     * @param profitLossSubjectDict      profitLossSubjectDict
     * @param existAccountBookPeriod     existAccountBookPeriod
     * @param parameterSet               parameterSet
     * @param operator                   operator
     * @param balanceAuxiliaryItemDict   balanceAuxiliaryItemDict
     * @param withOutAuxiliaryBalanceMap withOutAuxiliaryBalanceMap
     * @param withAuxiliaryBalanceMap    withAuxiliaryBalanceMap
     * @param accBookEntityDict          accBookEntityDict
     * @param credentialWord             credentialWord
     * @param now                        now
     * @return java.util.List<java.util.List < com.njwd.entity.ledger.dto.VoucherDto>>
     * @author xyyxhcj@qq.com
     * @date 2019/9/25 17:38
     **/
    private List<List<VoucherDto>> getAllProfitLossVouchers(Map<Long, AccountSubjectVo> profitLossSubjectDict,
                                                            AccountBookPeriod existAccountBookPeriod,
                                                            ParameterSetVo parameterSet, SysUserVo operator,
                                                            Map<Long, List<BalanceSubjectAuxiliaryItem>> balanceAuxiliaryItemDict,
                                                            Map<Long, Map<Long, BalanceSubject>> withOutAuxiliaryBalanceMap,
                                                            Map<Long, Map<Long, BalanceSubjectAuxiliary>> withAuxiliaryBalanceMap,
                                                            Map<Long, AccountBookEntityVo> accBookEntityDict, Byte credentialWord, Date now) {
        List<List<VoucherDto>> allProfitLossVouchers = new LinkedList<>();
        // 摘要
        String entryContent = String.format(LedgerConstant.Settle.VOUCHER_REMARK_LP, existAccountBookPeriod.getPeriodYear(), existAccountBookPeriod.getPeriodNum());
        // 先获取所有核算主体 遍历构造凭证

        Set<Long> allEntityId = new LinkedHashSet<>();
        allEntityId.addAll(withAuxiliaryBalanceMap.keySet());
        allEntityId.addAll(withOutAuxiliaryBalanceMap.keySet());
        for (Long accBookEntityId : allEntityId) {
            // 一个核算主体两张(分单) 只写分单的逻辑(先做收益凭证 后做损失凭证);如果合并,再把两张并一张
            LinkedList<VoucherDto> accBookVouchers = new LinkedList<>();
            allProfitLossVouchers.add(accBookVouchers);
            VoucherDto profitVoucherDto = addVoucherDtoAndFillEntity(accBookEntityDict, accBookVouchers, accBookEntityId);
            VoucherDto lossVoucherDto = addVoucherDtoAndFillEntity(accBookEntityDict, accBookVouchers, accBookEntityId);
            // 生成不包含辅助核算的分录
            // Map<核算主体ID,Map<科目ID,余额数据>>
            Map<Long, BalanceSubject> subjectIdBalanceMap = withOutAuxiliaryBalanceMap.get(accBookEntityId);
            if (subjectIdBalanceMap != null && !subjectIdBalanceMap.isEmpty()) {
                for (Map.Entry<Long, BalanceSubject> mapEntry : subjectIdBalanceMap.entrySet()) {
                    // 第二个key为科目ID
                    Long subjectId = mapEntry.getKey();
                    // 计算应写入的借/贷发生额,每个科目添加一条分录,并统计本年利润的分录合计金额
                    BalanceSubject balanceSubject = mapEntry.getValue();
                    // 计算分录金额 索引0为借 1为贷
                    BigDecimal[] entryAmount = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};
                    boolean useDebit = isUseDebit(profitLossSubjectDict, subjectId);
                    flushVoucherAmount(profitVoucherDto, lossVoucherDto, entryAmount, useDebit, balanceSubject.getOpeningBalance(), balanceSubject.getDebitAmount(), balanceSubject.getCreditAmount());
                    // 非本年利润的分录
                    addEntryDto(useDebit ? lossVoucherDto : profitVoucherDto, entryContent, entryAmount[0], entryAmount[1], subjectId, null, false);
                }
            }
            // 生成包含辅助核算的分录
            // 该map第一级key为核算主体ID,通过核算主体直接获取每个核算主体下需要生成结转分录的数据
            Map<Long, BalanceSubjectAuxiliary> balanceIdWithAuxiliaryMap = withAuxiliaryBalanceMap.get(accBookEntityId);
            if (balanceIdWithAuxiliaryMap != null && !balanceIdWithAuxiliaryMap.isEmpty()) {
                for (Map.Entry<Long, BalanceSubjectAuxiliary> balanceIdEntry : balanceIdWithAuxiliaryMap.entrySet()) {
                    // 第二个key为余额ID,每个余额ID对应一组辅助核算数据 => 每组数据生成一条分录
                    // 计算应写入的借/贷发生额,每个辅助核算项添加一条分录,并统计本年利润的分录合计金额
                    BalanceSubjectAuxiliary balanceSubject = balanceIdEntry.getValue();
                    Long subjectId = balanceSubject.getAccountSubjectId();
                    Long balanceId = balanceIdEntry.getKey();
                    // 计算分录金额 索引0为借 1为贷
                    BigDecimal[] entryAmount = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};
                    boolean useDebit = isUseDebit(profitLossSubjectDict, subjectId);
                    flushVoucherAmount(profitVoucherDto, lossVoucherDto, entryAmount, useDebit, balanceSubject.getOpeningBalance(), balanceSubject.getDebitAmount(), balanceSubject.getCreditAmount());
                    // 非本年利润的分录
                    addEntryDtoWithAuxiliary(balanceAuxiliaryItemDict, entryContent, useDebit ? lossVoucherDto : profitVoucherDto, subjectId, balanceId, entryAmount[0], entryAmount[1]);
                }
            }
            // 往两张凭证各插入一条本年利润分录,收益科目凭证插到最后一条,损失科目凭证插到第一条 分单时不一定都有数据 ,同时更新对方余额
            addEntryDto(profitVoucherDto, entryContent, BigDecimal.ZERO, profitVoucherDto.getDebitAmount(), parameterSet.getLrAccSubjectId(), null, false);
            profitVoucherDto.setCreditAmount(profitVoucherDto.getDebitAmount());
            addEntryDto(lossVoucherDto, entryContent, lossVoucherDto.getCreditAmount(), BigDecimal.ZERO, parameterSet.getLrAccSubjectId(), null, true);
            lossVoucherDto.setDebitAmount(lossVoucherDto.getCreditAmount());
            // 判断凭证分单方式 “按损/益科目分开结转”
            if (Constant.Is.NO.equals(FastUtils.getParamSetSub(parameterSet, existAccountBookPeriod.getAccountBookId(), Constant.ParameterSetKey.CREDENTIAL_TYPE).getValue().byteValue())) {
                // 不分单,合并两张凭证,将损失科目凭证的分录添加到收益科目凭证中
                profitVoucherDto.getEditEntryList().addAll(lossVoucherDto.getEditEntryList());
                // 合并金额 移除损失凭证
                profitVoucherDto.setDebitAmount(profitVoucherDto.getDebitAmount().add(lossVoucherDto.getDebitAmount()));
                profitVoucherDto.setCreditAmount(profitVoucherDto.getCreditAmount().add(lossVoucherDto.getCreditAmount()));
                accBookVouchers.removeLast();
            }
            removeEmptyVoucherAndFillHeader(existAccountBookPeriod, operator, credentialWord, now, accBookVouchers, entryContent);
        }
        return allProfitLossVouchers;
    }

    private void flushVoucherAmount(VoucherDto profitVoucherDto, VoucherDto lossVoucherDto, BigDecimal[] entryAmount, boolean useDebit, BigDecimal openingBalance, BigDecimal debitAmount, BigDecimal creditAmount) {
        if (useDebit) {
            // 原方向借方->损失->统计损失凭证 金额从借方转到贷方
            entryAmount[1] = openingBalance.add(debitAmount.subtract(creditAmount));
            lossVoucherDto.setCreditAmount(lossVoucherDto.getCreditAmount().add(entryAmount[1]));
        } else {
            // 原方向贷方->收益->统计收益凭证 金额从贷方转到借方
            entryAmount[0] = openingBalance.add(creditAmount.subtract(debitAmount));
            profitVoucherDto.setDebitAmount(profitVoucherDto.getDebitAmount().add(entryAmount[0]));
        }
    }

    /**
     * 汇总以前年度损益调整的凭证
     *
     * @param sySubjectDict                       以前年度损益末级科目字典
     * @param existAccountBookPeriod              existAccountBookPeriod
     * @param parameterSet                        parameterSet
     * @param operator                            operator
     * @param balanceAuxiliaryItemDict            balanceAuxiliaryItemDict
     * @param withOutAuxiliaryPriorYearBalanceMap 第一个key为核算主体,第二个key为科目ID
     * @param withAuxiliaryPriorYearBalanceMap    第一个key为核算主体,第二个key为balanceId
     * @param accBookEntityDict                   accBookEntityDict
     * @param credentialWord                      credentialWord
     * @param now                                 now
     * @return java.util.List<com.njwd.entity.ledger.dto.VoucherDto>
     * @author xyyxhcj@qq.com
     * @date 2019/9/25 17:34
     **/
    private List<VoucherDto> getPriorYearVouchers(Map<Long, AccountSubjectVo> sySubjectDict, AccountBookPeriod existAccountBookPeriod,
                                                  ParameterSetVo parameterSet,
                                                  SysUserVo operator,
                                                  Map<Long, List<BalanceSubjectAuxiliaryItem>> balanceAuxiliaryItemDict,
                                                  Map<Long, Map<Long, BalanceSubject>> withOutAuxiliaryPriorYearBalanceMap,
                                                  Map<Long, Map<Long, BalanceSubjectAuxiliary>> withAuxiliaryPriorYearBalanceMap,
                                                  Map<Long, AccountBookEntityVo> accBookEntityDict, Byte credentialWord, Date now) {
        List<VoucherDto> priorYearVoucherList = new LinkedList<>();
        // 摘要
        String entryContent = LedgerConstant.Settle.VOUCHER_REMARK_FP;
        // 先获取所有核算主体 遍历构造凭证
        Set<Long> allEntityId = new LinkedHashSet<>();
        allEntityId.addAll(withAuxiliaryPriorYearBalanceMap.keySet());
        allEntityId.addAll(withOutAuxiliaryPriorYearBalanceMap.keySet());
        for (Long accBookEntityId : allEntityId) {
            VoucherDto voucherDto = addVoucherDtoAndFillEntity(accBookEntityDict, priorYearVoucherList, accBookEntityId);
            // 生成不包含辅助核算的分录 第一个key为核算主体,第二个key为科目ID
            Map<Long, BalanceSubject> subjectIdBalanceMap = withOutAuxiliaryPriorYearBalanceMap.get(accBookEntityId);
            if (subjectIdBalanceMap != null && !subjectIdBalanceMap.isEmpty()) {
                for (Map.Entry<Long, BalanceSubject> mapEntry : subjectIdBalanceMap.entrySet()) {
                    // 第二个key为科目ID
                    Long subjectId = mapEntry.getKey();
                    // 计算应写入的借/贷发生额,每个科目添加一条分录,并统计利润分配的分录合计金额
                    BalanceSubject balanceSubject = mapEntry.getValue();
                    // 计算分录金额 索引0为借 1为贷
                    BigDecimal[] entryAmount = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};
                    flushVoucherAmount(voucherDto, voucherDto, entryAmount, isUseDebit(sySubjectDict, subjectId), balanceSubject.getOpeningBalance(), balanceSubject.getDebitAmount(), balanceSubject.getCreditAmount());
                    // 非利润分配的分录
                    addEntryDto(voucherDto, entryContent, entryAmount[0], entryAmount[1], subjectId, null, false);
                }
            }
            // 生成包含辅助核算的分录
            // 该map第一级key为核算主体ID,通过核算主体直接获取每个核算主体下需要生成分录的数据
            Map<Long, BalanceSubjectAuxiliary> balanceIdWithAuxiliaryMap = withAuxiliaryPriorYearBalanceMap.get(accBookEntityId);
            if (balanceIdWithAuxiliaryMap != null && !balanceIdWithAuxiliaryMap.isEmpty()) {
                for (Map.Entry<Long, BalanceSubjectAuxiliary> balanceIdEntry : balanceIdWithAuxiliaryMap.entrySet()) {
                    // 第二个key为余额ID,每个余额ID对应一组辅助核算数据 => 每组数据生成一条分录
                    // 计算应写入的借/贷发生额,每个辅助核算项添加一条分录,并统计本年利润的分录合计金额
                    BalanceSubjectAuxiliary balanceSubject = balanceIdEntry.getValue();
                    Long subjectId = balanceSubject.getAccountSubjectId();
                    Long balanceId = balanceIdEntry.getKey();
                    // 计算分录金额 索引0为借 1为贷
                    BigDecimal[] entryAmount = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};
                    flushVoucherAmount(voucherDto, voucherDto, entryAmount, isUseDebit(sySubjectDict, subjectId), balanceSubject.getOpeningBalance(), balanceSubject.getDebitAmount(), balanceSubject.getCreditAmount());
                    // 非利润分配的分录
                    addEntryDtoWithAuxiliary(balanceAuxiliaryItemDict, entryContent, voucherDto, subjectId, balanceId, entryAmount[0], entryAmount[1]);
                }
            }
            // 插入一条利润分配分录,如果以前年度父科目余额方向在借,利润分配分录插到第一条,使用借方,否则插到末条(使用贷方),同时更新对方余额
            if (isPriorYearSubjectUseDebit(parameterSet)) {
                BigDecimal countDebit = voucherDto.getCreditAmount().subtract(voucherDto.getDebitAmount());
                // 插到第一条
                addEntryDto(voucherDto, entryContent, countDebit, BigDecimal.ZERO, parameterSet.getFpAccSubjectId(), null, true);
                voucherDto.setDebitAmount(voucherDto.getDebitAmount().add(countDebit));
            } else {
                BigDecimal countCredit = voucherDto.getDebitAmount().subtract(voucherDto.getCreditAmount());
                // 插到最后一条
                addEntryDto(voucherDto, entryContent, BigDecimal.ZERO, countCredit, parameterSet.getFpAccSubjectId(), null, false);
                voucherDto.setCreditAmount(voucherDto.getCreditAmount().add(countCredit));
            }
        }
        removeEmptyVoucherAndFillHeader(existAccountBookPeriod, operator, credentialWord, now, priorYearVoucherList, entryContent);
        return priorYearVoucherList;
    }

    /**
     * 移除无分录凭证 并写入凭证头数据
     *
     * @param existAccountBookPeriod existAccountBookPeriod
     * @param operator               operator
     * @param credentialWord         credentialWord
     * @param now                    now
     * @param priorYearVoucherList   priorYearVoucherList
     * @param entryContent           entryContent
     * @author xyyxhcj@qq.com
     * @date 2019/9/27 22:45
     **/
    private void removeEmptyVoucherAndFillHeader(AccountBookPeriod existAccountBookPeriod, SysUserVo operator, Byte credentialWord, Date now, List<VoucherDto> priorYearVoucherList, String entryContent) {
        Iterator<VoucherDto> iterator = priorYearVoucherList.iterator();
        while (iterator.hasNext()) {
            VoucherDto voucherDto = iterator.next();
            // 如果没有分录数据，则删除
            if (voucherDto.getEditEntryList().isEmpty()) {
                iterator.remove();
                continue;
            }
            // 写凭证头
            fillVoucherHeader(existAccountBookPeriod, operator, credentialWord, now, voucherDto, entryContent);
        }
    }

    /**
     * 更新损益结转凭证发生额(包含过账数据)
     *
     * @param parameterSet               parameterSet
     * @param existAccountBookPeriod     existAccountBookPeriod
     * @param balanceSubjectMap          第一个key为核算主体ID,第二个key为科目Id
     * @param balanceSubjectAuxiliaryMap key为balanceId
     * @param updateType                 余额变更类型 1加 -1减
     * @author xyyxhcj@qq.com
     * @date 2019/9/25 16:09
     **/
    private void updateBalance(@Nullable ParameterSetVo parameterSet, AccountBookPeriodVo existAccountBookPeriod, Map<Long, Map<Long, BalanceSubjectDto>> balanceSubjectMap, Map<Long, BalanceSubjectAuxiliaryDto> balanceSubjectAuxiliaryMap, byte updateType) {
        if (balanceSubjectMap.isEmpty()) {
            return;
        }
        // 本年利润 利润分配 当期不一定有数据,添加凭证触发的更新需要判断是否插入初始数据
        if (Constant.BalanceUpdateType.ADD == updateType && parameterSet != null) {
            // 查询未初始化的数据
            Object[] subjectIds = new Long[]{parameterSet.getLrAccSubjectId(), parameterSet.getFpAccSubjectId()};
            List<BalanceSubject> balanceList = balanceSubjectService.list(new LambdaQueryWrapper<BalanceSubject>()
                    .eq(BalanceSubject::getAccountBookId, existAccountBookPeriod.getAccountBookId())
                    .eq(BalanceSubject::getPeriodYear, existAccountBookPeriod.getPeriodYear())
                    .eq(BalanceSubject::getPeriodNum, existAccountBookPeriod.getPeriodNum())
                    .in(BalanceSubject::getAccountSubjectId, subjectIds)
                    .select(Balance::getAccountBookEntityId, BalanceSubject::getAccountSubjectId));
            // 记录已初始化的核算主体和科目id key为核算主体ID,value为科目ids
            Map<Long, Set<Long>> existBalanceDict = new LinkedHashMap<>();
            balanceList.forEach(balance ->
                    existBalanceDict.computeIfAbsent(balance.getAccountBookEntityId(), k -> new LinkedHashSet<>())
                            .add(balance.getAccountSubjectId()));
            // 构造初始数据
            List<BalanceSubject> initBalances = new LinkedList<>();
            for (Long accBookEntityId : balanceSubjectMap.keySet()) {
                Set<Long> existSubjectIds = existBalanceDict.get(accBookEntityId);
                if (existSubjectIds == null) {
                    // 表示该核算主体需初始化两条科目余额
                    addInitBalance(existAccountBookPeriod, initBalances, accBookEntityId, parameterSet.getLrAccSubjectId());
                    addInitBalance(existAccountBookPeriod, initBalances, accBookEntityId, parameterSet.getFpAccSubjectId());
                    continue;
                }
                if (existSubjectIds.size() == subjectIds.length) {
                    // 两个科目均已初始化
                    continue;
                }
                if (!existSubjectIds.contains(parameterSet.getLrAccSubjectId())) {
                    addInitBalance(existAccountBookPeriod, initBalances, accBookEntityId, parameterSet.getLrAccSubjectId());
                } else {
                    // 初始化 fpAccSubjectId
                    addInitBalance(existAccountBookPeriod, initBalances, accBookEntityId, parameterSet.getFpAccSubjectId());
                }
            }
            if (!initBalances.isEmpty()) {
                // 插入初始数据
                balanceSubjectService.saveBatch(initBalances);
            }
        }
        balanceSubjectMap.forEach((accBookEntityId, subjectIdBalanceMap) -> {
            if (!subjectIdBalanceMap.isEmpty()) {
                balanceSubjectMapper.updateBatchForProfitLoss(subjectIdBalanceMap.values(), existAccountBookPeriod, accBookEntityId, updateType);
            }
        });
        if (!balanceSubjectAuxiliaryMap.isEmpty()) {
            balanceSubjectAuxiliaryMapper.updateBatchForProfitLoss(balanceSubjectAuxiliaryMap.values(), updateType);
        }
    }

    private void addInitBalance(AccountBookPeriod existAccountBookPeriod, final List<BalanceSubject> initBalances, Long accBookEntityId, Long lrAccSubjectId) {
        BalanceSubject balance = new BalanceSubject();
        initBalances.add(balance);
        balance.setAccountBookId(existAccountBookPeriod.getAccountBookId());
        balance.setAccountBookEntityId(accBookEntityId);
        balance.setPeriodYear(existAccountBookPeriod.getPeriodYear());
        balance.setPeriodNum(existAccountBookPeriod.getPeriodNum());
        balance.setPeriodYearNum(existAccountBookPeriod.getPeriodYearNum());
        balance.setAccountSubjectId(lrAccSubjectId);
    }

    /**
     * 统计结转凭证列表将产生的发生额
     *
     * @param generateVoucherList        generateVoucherList
     * @param balanceSubjectMap          第一个key为核算主体ID,第二个key为科目Id
     * @param balanceSubjectAuxiliaryMap key为balanceId
     * @author xyyxhcj@qq.com
     * @date 2019/9/25 15:23
     **/
    private void countBalance(List<VoucherDto> generateVoucherList, Map<Long, Map<Long, BalanceSubjectDto>> balanceSubjectMap, Map<Long, BalanceSubjectAuxiliaryDto> balanceSubjectAuxiliaryMap) {
        for (VoucherDto voucherDto : generateVoucherList) {
            for (VoucherEntryDto entryDto : voucherDto.getEditEntryList()) {
                Long subjectId = entryDto.getAccountSubjectId();
                Long balanceId = entryDto.getAuxiliaryBalanceId();
                // 累计科目余额
                BalanceSubjectDto balanceSubject = balanceSubjectMap
                        .computeIfAbsent(voucherDto.getAccountBookEntityId(), k -> new LinkedHashMap<>())
                        .computeIfAbsent(subjectId, k -> new BalanceSubjectDto());
                addEntryAmount(entryDto, balanceSubject);
                balanceSubject.setSubjectId(subjectId);
                // 标记是已过账的损益凭证
                balanceSubject.setIsPost(Constant.Is.YES);
                balanceSubject.setIsSy(Constant.Is.YES);
                // 如果存在balanceId,累计辅助核算科目余额
                if (balanceId != null) {
                    BalanceSubjectAuxiliaryDto balanceSubjectAuxiliary = balanceSubjectAuxiliaryMap
                            .computeIfAbsent(balanceId, k -> new BalanceSubjectAuxiliaryDto());
                    addEntryAmount(entryDto, balanceSubjectAuxiliary);
                    balanceSubjectAuxiliary.setId(balanceId);
                    balanceSubjectAuxiliary.setIsPost(Constant.Is.YES);
                    balanceSubjectAuxiliary.setIsSy(Constant.Is.YES);
                }
            }
        }
    }

    /**
     * 累计分录发生额
     *
     * @param entryDto                entryDto
     * @param balanceSubjectAuxiliary balanceSubjectAuxiliary
     * @author xyyxhcj@qq.com
     * @date 2019/9/27 17:49
     **/
    private void addEntryAmount(VoucherEntryDto entryDto, Balance balanceSubjectAuxiliary) {
        if (balanceSubjectAuxiliary.getDebitAmount() == null) {
            balanceSubjectAuxiliary.setDebitAmount(BigDecimal.ZERO);
            balanceSubjectAuxiliary.setCreditAmount(BigDecimal.ZERO);
        }
        balanceSubjectAuxiliary.setDebitAmount(balanceSubjectAuxiliary.getDebitAmount().add(entryDto.getDebitAmount()));
        balanceSubjectAuxiliary.setCreditAmount(balanceSubjectAuxiliary.getCreditAmount().add(entryDto.getCreditAmount()));
    }

    /**
     * 添加包含辅助核算的分录
     *
     * @param balanceAuxiliaryItemDict balanceAuxiliaryItemDict
     * @param entryContent             entryContent
     * @param lossVoucherDto           lossVoucherDto
     * @param subjectId                subjectId
     * @param balanceId                balanceId
     * @param entryDebit               entryDebit
     * @param entryCredit              entryCredit
     * @author xyyxhcj@qq.com
     * @date 2019/9/25 15:11
     **/
    private void addEntryDtoWithAuxiliary(Map<Long, List<BalanceSubjectAuxiliaryItem>> balanceAuxiliaryItemDict, String entryContent, VoucherDto lossVoucherDto, Long subjectId, Long balanceId, BigDecimal entryDebit, BigDecimal entryCredit) {
        VoucherEntryDto entryOther = addEntryDto(lossVoucherDto, entryContent, entryDebit, entryCredit, subjectId, balanceId, false);
        // 根据balanceId获取辅助核算明细
        List<BalanceSubjectAuxiliaryItem> auxiliaryItems = balanceAuxiliaryItemDict.get(balanceId);
        // 放入辅助核算项目数据
        for (BalanceSubjectAuxiliaryItem auxiliaryItem : auxiliaryItems) {
            VoucherEntryAuxiliaryDto entryAuxiliary = new VoucherEntryAuxiliaryDto();
            entryOther.getEditAuxiliaryList().add(entryAuxiliary);
            entryAuxiliary.setSourceTable(auxiliaryItem.getSourceTable());
            entryAuxiliary.setItemValueId(auxiliaryItem.getItemValueId());
        }
    }

    /**
     * 判断科目余额方向是否为借方
     *
     * @param profitLossSubjectDict profitLossSubjectDict
     * @param subjectId             subjectId
     * @return boolean
     * @author xyyxhcj@qq.com
     * @date 2019/9/25 15:08
     **/
    private boolean isUseDebit(Map<Long, AccountSubjectVo> profitLossSubjectDict, Long subjectId) {
        AccountSubjectVo accountSubjectVo = profitLossSubjectDict.get(subjectId);
        if (accountSubjectVo == null || accountSubjectVo.getBalanceDirection() == null) {
            throw new ServiceException(ResultCode.DATA_ERROR, profitLossSubjectDict);
        }
        return isUseDebit(accountSubjectVo);
    }

    /**
     * 判断科目余额方向是否为借方
     *
     * @param accountSubjectVo accountSubjectVo
     * @return boolean
     * @author xyyxhcj@qq.com
     * @date 2019/9/26 9:33
     **/
    private boolean isUseDebit(AccountSubjectVo accountSubjectVo) {
        boolean useDebit = false;
        if (Constant.BalanceDirection.DEBIT.equals(accountSubjectVo.getBalanceDirection())) {
            // 标记科目使用借方
            useDebit = true;
        }
        return useDebit;
    }

    /**
     * 往凭证列表中添加初始化凭证(金额累计设为0),并放入核算主体数据
     *
     * @param accBookEntityDict   accBookEntityDict
     * @param voucherDtoList      voucherDtoList
     * @param accountBookEntityId accountBookEntityId
     * @return com.njwd.entity.ledger.dto.VoucherDto
     * @author xyyxhcj@qq.com
     * @date 2019/9/24 17:00
     **/
    private VoucherDto addVoucherDtoAndFillEntity(Map<Long, AccountBookEntityVo> accBookEntityDict, List<VoucherDto> voucherDtoList, Long accountBookEntityId) {
        AccountBookEntityVo accBookEntityVo = accBookEntityDict.get(accountBookEntityId);
        if (accBookEntityVo == null) {
            LOGGER.warn("核算主体数据异常:ID:{},dict:{}", accountBookEntityId, JsonUtils.object2Json(accBookEntityDict));
            throw new ServiceException(ResultCode.DATA_ERROR, accountBookEntityId);
        }
        VoucherDto voucherDto = new VoucherDto();
        voucherDto.setAccountBookEntityId(accountBookEntityId);
        voucherDto.setAccountBookEntityName(accBookEntityVo.getEntityName());
        // 借贷金额合计先存储到对应的凭证中
        voucherDto.setDebitAmount(BigDecimal.ZERO);
        voucherDto.setCreditAmount(BigDecimal.ZERO);
        voucherDtoList.add(voucherDto);
        return voucherDto;
    }

    /**
     * 往凭证中加入一条分录,并返回
     *
     * @param voucherDto   voucherDto
     * @param entryContent entryContent
     * @param debit        debit
     * @param credit       credit
     * @param subjectId    subjectId
     * @param balanceId    辅助核算余额表id
     * @param addFirst     true表示分录插到第一条 false表示插到最后一条
     * @return com.njwd.entity.ledger.dto.VoucherEntryDto
     * @author xyyxhcj@qq.com
     * @date 2019/9/25 9:22
     **/
    private VoucherEntryDto addEntryDto(VoucherDto voucherDto, String entryContent, BigDecimal debit, BigDecimal credit, Long subjectId,
                                        @Nullable Long balanceId, boolean addFirst) {
        VoucherEntryDto entry = new VoucherEntryDto();
        if (BigDecimal.ZERO.compareTo(debit) == 0 && BigDecimal.ZERO.compareTo(credit) == 0) {
            // 分录金额均为零时不放入凭证
            return entry;
        }
        if (addFirst) {
            voucherDto.getEditEntryList().add(0, entry);
        } else {
            voucherDto.getEditEntryList().add(entry);
        }
        entry.setAccountSubjectId(subjectId);
        // 用于直接更新辅助核算余额表
        entry.setAuxiliaryBalanceId(balanceId);
        fillEntry(entryContent, debit, credit, entry);
        return entry;
    }

    /**
     * 写入分录数据
     *
     * @param entryContent entryContent
     * @param debit        debit
     * @param credit       credit
     * @param entry        entry
     * @author xyyxhcj@qq.com
     * @date 2019/9/24 16:04
     **/
    private void fillEntry(String entryContent, BigDecimal debit, BigDecimal credit, VoucherEntryDto entry) {
        entry.setAbstractContent(entryContent);
        entry.setDebitAmount(debit);
        entry.setCreditAmount(credit);
        entry.setOriginalCoin(LedgerConstant.Ledger.DEFAULT_CURRENCY);
        entry.setExchangeRate(LedgerConstant.ExchangeRate.DEFAULT);
        entry.setOriginalDebitAmount(debit);
        entry.setOriginalCreditAmount(credit);
        entry.setCashFlowType(Constant.CashFlowType.NEEDLESS);
        entry.setInteriorType(Constant.InteriorType.NEEDLESS);
    }

    /**
     * 写以前年度损益结转的凭证头
     *
     * @param existAccountBookPeriod existAccountBookPeriod
     * @param operator               operator
     * @param credentialWord         credentialWord
     * @param now                    now
     * @param voucherDto             voucherDto
     * @param firstAbstract          firstAbstract
     * @author xyyxhcj@qq.com
     * @date 2019/9/24 15:44
     **/
    private void fillVoucherHeader(AccountBookPeriod existAccountBookPeriod, SysUserVo operator, Byte credentialWord, Date now, VoucherDto voucherDto, String firstAbstract) {
        voucherDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        voucherDto.setAccountBookId(existAccountBookPeriod.getAccountBookId());
        voucherDto.setAccountBookName(existAccountBookPeriod.getAccountBookName());
        voucherDto.setVoucherDate(existAccountBookPeriod.getEndDate());
        voucherDto.setBillNum(LedgerConstant.Ledger.DEFAULT_BILL_NUM);
        voucherDto.setPostingPeriodYear(existAccountBookPeriod.getPeriodYear());
        voucherDto.setPostingPeriodNum(existAccountBookPeriod.getPeriodNum());
        voucherDto.setPeriodYearNum(existAccountBookPeriod.getPeriodYearNum());
        voucherDto.setCredentialWord(credentialWord);
        voucherDto.setSourceType(LedgerConstant.SourceType.FORWARD);
        voucherDto.setSourceSystem(Constant.SourceSystem.LEDGER);
        // 设置一个空字符串,为了复用原有的凭证批量插入方法
        voucherDto.setSourceCode("");
        voucherDto.setFirstAbstract(firstAbstract);
        voucherDto.setCashCheckType(Constant.CashFlowCheckType.NEEDLESS);
        voucherDto.setCashFlowAmount(BigDecimal.ZERO);
        voucherDto.setStatus(LedgerConstant.VoucherStatus.POST);
        voucherDto.setInteriorType(Constant.InteriorType.NEEDLESS);
        voucherDto.setIsOffset(Constant.Is.NO);
        voucherDto.setCreateTime(now);
        voucherDto.setCreatorId(operator.getUserId());
        voucherDto.setCreatorName(operator.getName());
        voucherDto.setApproveStatus(Constant.Is.YES);
        voucherDto.setApproveTime(now);
        voucherDto.setApproverId(operator.getUserId());
        voucherDto.setApproverName(operator.getName());
        voucherDto.setPostingStatus(Constant.Is.YES);
        voucherDto.setPostingTime(now);
        voucherDto.setPostingUserId(operator.getUserId());
        voucherDto.setPostingUserName(operator.getName());
    }

    /**
     * 获取凭证字 返回:记或转
     *
     * @param parameterSet parameterSet
     * @param accBookId    accBookId
     * @return java.lang.Byte
     * @author xyyxhcj@qq.com
     * @date 2019/9/24 11:35
     **/
    private Byte getCredentialWord(ParameterSetVo parameterSet, Long accBookId) {
        byte credentialWordType = FastUtils.getParamSetSub(parameterSet, accBookId, Constant.ParameterSetKey.CREDENTIAL_WORD_TYPE).getValue().byteValue();
        if (Constant.CredentialWordSet.RECORD == credentialWordType) {
            return Constant.CredentialWordType.RECORD;
        } else {
            return Constant.CredentialWordType.TRANSFER;
        }
    }

    /**
     * 获取核算账簿字典
     *
     * @param existAccountBookPeriod existAccountBookPeriod
     * @return java.util.Map<java.lang.Long, com.njwd.entity.basedata.vo.AccountBookEntityVo>
     * @author xyyxhcj@qq.com
     * @date 2019/9/24 11:33
     **/
    private Map<Long, AccountBookEntityVo> getAccBookEntityDict(AccountBookPeriod existAccountBookPeriod) {
        AccountBookEntityDto accountBookEntityDto = new AccountBookEntityDto();
        // 先获取账簿下的所有核算主体
        accountBookEntityDto.setAccountBookIdList(Collections.singletonList(existAccountBookPeriod.getAccountBookId()));
        Result<List<AccountBookEntityVo>> accBookEntitiesResult = accountBookEntityFeignClient.findAccountBookEntityListByAccBookIdList(accountBookEntityDto);
        if (accBookEntitiesResult.getData() == null || accBookEntitiesResult.getData().isEmpty()) {
            throw new ServiceException(String.format(ResultCode.FEIGN_CONNECT_ERROR.message, FeignClientErrorMsg.GET_ACCOUNT_ENTITY_ERROR), ResultCode.FEIGN_CONNECT_ERROR);
        }
        // 创建核算主体字典,key为核算主体ID
        return accBookEntitiesResult.getData().stream()
                .collect(Collectors.toMap(AccountBookEntity::getId, accountBookEntityVo -> accountBookEntityVo));
    }

    /**
     * 判断'以前年度损益调整'末级科目是否配置辅助核算 不需要放入辅助核算数据,辅助核算数据通过balanceId获取
     **/
    private boolean isHasAuxiliaryForSy(AccountSubjectVo syAccountSubject) {
        // return syAccountSubject.getAccountSubjectAuxiliaryList() != null && !syAccountSubject.getAccountSubjectAuxiliaryList().isEmpty();
        return syAccountSubject.getAuxiliaryNum() > 0;
    }

    /**
     * 获取账簿启用子系统记录
     *
     * @param accountBookId accountBookId
     * @return com.njwd.entity.basedata.AccountBookSystem
     * @author xyyxhcj@qq.com
     * @date 2019/9/20 10:21
     **/
    private @NotNull AccountBookSystem getAccountBookSystem(Long accountBookId) {
        AccountBookSystemDto accountBookSystemDto = new AccountBookSystemDto();
        accountBookSystemDto.setSystemSign(Constant.SystemSign.LEDGER);
        accountBookSystemDto.setAccountBookId(accountBookId);
        Result<AccountBookSystem> systemResult = accountBookSystemFeignClient.findInitStatusByCondition(accountBookSystemDto);
        if (systemResult.getData() == null) {
            throw new ServiceException(String.format(ResultCode.FEIGN_CONNECT_ERROR.message, FeignClientErrorMsg.GET_ACCOUNT_SYS_INIT_DATA_ERROR), ResultCode.FEIGN_CONNECT_ERROR);
        }
        return systemResult.getData();
    }

    /**
     * 结账前检查
     * 关联系统结账情况-未关联
     * 凭证空号检查
     * 凭证过账检查
     * 凭证现金流量分析检查
     * 系统对账检查-未开启
     * 业务单元内部往来平衡检查
     * 损益结转参数设置检查
     *
     * @param existAccountBookPeriod existAccountBookPeriod
     * @param parameterSet           parameterSet
     * @param existAccountBookSystem existAccountBookSystem
     * @return com.njwd.entity.ledger.vo.SettleResult
     * @author xyyxhcj@qq.com
     * @date 2019/9/20 10:05
     **/
    private SettleResult checkForSettle(AccountBookPeriod existAccountBookPeriod, ParameterSetVo parameterSet, AccountBookSystem existAccountBookSystem) {
        SettleResult settleResult = new SettleResult();
        settleResult.setAccountBookPeriod(existAccountBookPeriod);
        // 期初初始化检查
        checkInit(existAccountBookPeriod, existAccountBookSystem, settleResult);
        // 凭证空号检查
        checkBroken(existAccountBookPeriod.getId(), settleResult);
        // 凭证过账检查
        checkPosting(existAccountBookPeriod, settleResult);
        // 凭证现金流量分析检查
        checkCashFlowAnalysis(existAccountBookPeriod, parameterSet, existAccountBookSystem, settleResult);
        // 业务单元内部往来平衡检查
        checkInteriorBalance(existAccountBookPeriod, settleResult);
        // 损益结转参数设置检查
        checkParameterSet(existAccountBookPeriod, parameterSet, settleResult);
        return settleResult;
    }

    /**
     * 损益结转参数设置检查
     *
     * @param existAccountBookPeriod existAccountBookPeriod
     * @param parameterSet           parameterSet
     * @param settleResult           settleResult
     * @author xyyxhcj@qq.com
     * @date 2019/9/20 17:41
     **/
    private void checkParameterSet(AccountBookPeriod existAccountBookPeriod, final ParameterSetVo parameterSet, SettleResult settleResult) {
        List<Long> carrySubjectIds = new LinkedList<>();
        Long lrAccSubjectId = FastUtils.getParamSetSub(parameterSet, existAccountBookPeriod.getAccountBookId(), Constant.ParameterSetKey.LR_ACC_SUBJECT_ID).getValue();
        Long fpAccSubjectId = FastUtils.getParamSetSub(parameterSet, existAccountBookPeriod.getAccountBookId(), Constant.ParameterSetKey.FP_ACC_SUBJECT_ID).getValue();
        Long syAccSubjectId = FastUtils.getParamSetSub(parameterSet, existAccountBookPeriod.getAccountBookId(), Constant.ParameterSetKey.SY_ACC_SUBJECT_ID).getValue();
        parameterSet.setLrAccSubjectId(lrAccSubjectId);
        parameterSet.setFpAccSubjectId(fpAccSubjectId);
        parameterSet.setSyAccSubjectId(syAccSubjectId);
        carrySubjectIds.add(lrAccSubjectId);
        carrySubjectIds.add(fpAccSubjectId);
        carrySubjectIds.add(syAccSubjectId);
        Map<Long, Map<String, Object>> carrySubjects = LedgerUtils.getSubjectDict(accountSubjectFeignClient, carrySubjectIds, null);
        // 默认设为不通过
        settleResult.setParameterSetStatus(Constant.Is.NO);
        if (carrySubjects == null || carrySubjects.size() != carrySubjectIds.size()) {
            LOGGER.warn("未配置三个结转使用的科目");
            settleResult.setCheckFlag(true);
            return;
        }
        // 校验损益科目是否删除,是否末级,损益凭证不校验禁用
        for (Map.Entry<Long, Map<String, Object>> entry : carrySubjects.entrySet()) {
            Map<String, Object> subjectMap = entry.getValue();
            Boolean isDel = FastUtils.isStatus(subjectMap, Constant.PropertyName.IS_DEL);
            Boolean isFinal = FastUtils.isStatus(subjectMap, Constant.PropertyName.IS_FINAL);
            if (isDel) {
                LOGGER.warn("总账参数配置的科目已删除:{}", JsonUtils.object2Json(subjectMap));
                settleResult.setCheckFlag(true);
                return;
            }
            if (entry.getKey().equals(lrAccSubjectId) || entry.getKey().equals(fpAccSubjectId)) {
                if (!isFinal) {
                    LOGGER.warn("本年利润/利润分配科目非末级:{}", JsonUtils.object2Json(subjectMap));
                    settleResult.setIsFinal(Constant.Is.NO);
                    settleResult.setCheckFlag(true);
                    return;
                }
                // 本年利润/利润分配科目不可配置辅助核算
                Object auxiliaryListObj = subjectMap.get(Constant.PropertyName.ACCOUNT_SUBJECT_AUXILIARY_LIST);
                if (auxiliaryListObj instanceof List) {
                    List auxiliaryList = (List) auxiliaryListObj;
                    if (!auxiliaryList.isEmpty()) {
                        // 配置了辅助核算,校验不通过
                        LOGGER.warn("本年利润/利润分配科目不可配置辅助核算:{}", JsonUtils.object2Json(subjectMap));
                        settleResult.setParameterSetStatus(LedgerConstant.Settle.PARAMETER_SET_ERROR);
                        settleResult.setCheckFlag(true);
                        return;
                    }
                }
            }
        }
        // 能运行到此,表示校验通过 把这三个科目数据放到vo
        parameterSet.setCarrySubjects(carrySubjects);
        if (settleResult.getCheckFlag() == null) {
            // 表示之前的校验全部通过
            settleResult.setCheckFlag(false);
        } else {
            settleResult.setCheckFlag(settleResult.getCheckFlag());
        }
        settleResult.setParameterSetStatus(Constant.Is.YES);
        settleResult.setIsFinal(Constant.Is.YES);
    }

    /**
     * 业务单元内部往来平衡检查
     *
     * @param existAccountBookPeriod existAccountBookPeriod
     * @param settleResult           settleResult
     * @author xyyxhcj@qq.com
     * @date 2019/9/20 16:18
     **/
    private void checkInteriorBalance(AccountBookPeriod existAccountBookPeriod, SettleResult settleResult) {
        // 取第一个有权限的账簿
        CompanyVo companyVo = LedgerUtils.getCompanyVoResult(companyFeignClient, Collections.singleton(existAccountBookPeriod.getAccountBookId())).getCompanyVoList().get(0);
        if (companyVo != null && Constant.Is.NO.equals(companyVo.getHasSubAccount())) {
            // 未启用分账核算
            settleResult.setNotBalancedStatus(Constant.BalancedStatus.NEEDLESS);
            return;
        }
        // 启用分账核算 获取所有内部往来科目
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        accountSubjectDto.setIsInterior(Constant.Is.YES);
        accountSubjectDto.setIsFinal(Constant.Is.YES);
        List<AccountSubjectVo> interiorSubjects = accountSubjectFeignClient.findInfoForLedger(accountSubjectDto).getData();
        if (interiorSubjects != null && !interiorSubjects.isEmpty()) {
            // 科目id分组分别求借贷发生额之和,逐一判断借贷发生额是否相等
            List<Long> interiorSubjectIds = interiorSubjects.stream().map(AccountSubject::getId).collect(Collectors.toList());
            List<BalanceSubject> interiorBalances = balanceSubjectMapper.selectList(new LambdaQueryWrapper<BalanceSubject>()
                    .eq(Balance::getAccountBookId, existAccountBookPeriod.getAccountBookId())
                    .eq(Balance::getPeriodYearNum, existAccountBookPeriod.getPeriodYearNum())
                    .in(Balance::getAccountSubjectId, interiorSubjectIds));
            // 统计借贷方已过账发生额,key为科目id
            Map<Long, BigDecimal[]> interiorBalanceAmount = new LinkedHashMap<>();
            for (BalanceSubject balance : interiorBalances) {
                // 索引0为借方 索引1为贷方,
                BigDecimal[] amount = interiorBalanceAmount.computeIfAbsent(balance.getAccountSubjectId(), k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                amount[0] = amount[0].add(balance.getPostDebitAmount());
                amount[1] = amount[1].add(balance.getPostCreditAmount());
            }
            // 判断是否存在不平衡的科目
            for (BigDecimal[] amount : interiorBalanceAmount.values()) {
                if (amount[0].compareTo(amount[1]) != 0) {
                    // 检查状态=“未通过”，检查结果=“业务单元内部往来不平衡
                    settleResult.setCheckFlag(true);
                    settleResult.setNotBalancedStatus(Constant.Is.NO);
                    return;
                }
            }
            // 检查状态=“已通过”，检查结果=“业务单元内部往来平衡”
            settleResult.setNotBalancedStatus(Constant.Is.YES);
        }
    }

    /**
     * 凭证现金流量分析检查
     *
     * @param existAccountBookPeriod existAccountBookPeriod
     * @param parameterSet           parameterSet
     * @param existAccountBookSystem existAccountBookSystem
     * @param settleResult           settleResult
     * @author xyyxhcj@qq.com
     * @date 2019/9/20 15:09
     **/
    private void checkCashFlowAnalysis(AccountBookPeriod existAccountBookPeriod, ParameterSetVo parameterSet, @NotNull AccountBookSystem existAccountBookSystem, SettleResult settleResult) {
        if (Constant.Is.NO.equals(existAccountBookSystem.getCashFlowEnableStatus())) {
            // 未启用现金流量
            settleResult.setNeedAnalysisStatus(Constant.CashFlowCheckType.NEEDLESS);
            return;
        }
        // 启用现金流量,获取未做现金流量分析的凭证
        List<Voucher> needAnalysisVouchers = getVoucherList(new LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getAccountBookId, existAccountBookPeriod.getAccountBookId())
                .eq(Voucher::getPeriodYearNum, existAccountBookPeriod.getPeriodYearNum())
                .eq(Voucher::getCashCheckType, Constant.CashFlowCheckType.UNEXAMINED)
                .eq(Voucher::getIsDel, Constant.Is.NO));
        if (needAnalysisVouchers.isEmpty()) {
            // 凭证均已成功指定现金流量, 检查状态=“已通过”，检查结果=“本期不存在未现金流量分析凭证”
            settleResult.setNeedAnalysisStatus(Constant.Is.NO);
            return;
        }
        settleResult.setNotAnalysisList(needAnalysisVouchers);
        // 存在未做现金流量分析的凭证
        if (Constant.Is.YES.equals(FastUtils.getParamSetSub(parameterSet, existAccountBookPeriod.getAccountBookId(), Constant.ParameterSetKey.IS_CHECK_CASH_FLOW).getValue().byteValue())) {
            // 结账时需要检查现金流量分析 检查状态=“未通过”，检查结果=“本期存在未现金流量分析凭证”
            settleResult.setCheckFlag(true);
            // 1表示未通过
            settleResult.setNeedAnalysisStatus(Constant.Is.YES);
        } else {
            // 检查状态=“警告”，检查结果=“本期存在未现金流量分析凭证”
            settleResult.setNeedAnalysisStatus(Constant.Is.NO);
        }
    }

    /**
     * 凭证过账检查
     *
     * @param existAccountBookPeriod existAccountBookPeriod
     * @param settleResult           settleResult
     * @author xyyxhcj@qq.com
     * @date 2019/9/20 14:57
     **/
    private void checkPosting(AccountBookPeriod existAccountBookPeriod, SettleResult settleResult) {
        List<Voucher> notPostingVouchers = getVoucherList(new LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getAccountBookId, existAccountBookPeriod.getAccountBookId())
                .eq(Voucher::getPeriodYearNum, existAccountBookPeriod.getPeriodYearNum())
                .eq(Voucher::getPostingStatus, Constant.Is.NO)
                .eq(Voucher::getIsDel, Constant.Is.NO));
        settleResult.setNotPostingList(notPostingVouchers);
        if (!notPostingVouchers.isEmpty()) {
            settleResult.setCheckFlag(true);
        }
    }

    /**
     * 获取凭证列表(仅获取所需字段)
     *
     * @param wrapper wrapper
     * @return java.util.List<com.njwd.entity.ledger.Voucher>
     * @author xyyxhcj@qq.com
     * @date 2019/9/20 15:53
     **/
    private List<Voucher> getVoucherList(LambdaQueryWrapper<Voucher> wrapper) {
        return voucherService.list(wrapper.select(
                Voucher::getCredentialWord,
                Voucher::getMainCode,
                Voucher::getChildCode,
                Voucher::getDebitAmount,
                Voucher::getCreditAmount));
    }

    /**
     * 凭证空号检查
     *
     * @param accountBookPeriodId accountBookPeriodId
     * @param settleResult        settleResult
     * @author xyyxhcj@qq.com
     * @date 2019/9/20 12:21
     **/
    private void checkBroken(Long accountBookPeriodId, SettleResult settleResult) {
        List<Long> ids = Collections.singletonList(accountBookPeriodId);
        List<VoucherAdjust> voucherAdjusts = voucherAdjustService.checkBroken(ids);
        int checkPeriodLength = ids.size();
        if (voucherAdjusts.size() == checkPeriodLength) {
            // 返回的校验数据与传参一致
            List<Voucher> brokenVouchers = voucherAdjusts.get(checkPeriodLength - 1).getBrokenVoucher();
            if (brokenVouchers != null && !brokenVouchers.isEmpty()) {
                // 存在断号
                settleResult.setCheckFlag(true);
                settleResult.setCutOffList(brokenVouchers);
            }
        }
    }

    /**
     * 期初初始化检查
     *
     * @param existAccountBookPeriod 表中存储的账簿期间
     * @param accountBookSystem      accountBookSystem
     * @param settleResult           settleResult
     * @author xyyxhcj@qq.com
     * @date 2019/9/20 10:09
     **/
    private void checkInit(AccountBookPeriod existAccountBookPeriod, @NotNull AccountBookSystem accountBookSystem, @NotNull SettleResult settleResult) {
        // 获取上一期
        AccountBookPeriod prePeriod = accountBookPeriodService.getOne(new LambdaQueryWrapper<AccountBookPeriod>()
                .eq(AccountBookPeriod::getRootEnterpriseId, existAccountBookPeriod.getRootEnterpriseId())
                .eq(AccountBookPeriod::getAccountBookId, existAccountBookPeriod.getAccountBookId())
                .eq(AccountBookPeriod::getSystemSign, existAccountBookPeriod.getSystemSign())
                .eq(AccountBookPeriod::getStatus, Constant.Status.ON)
                .lt(AccountBookPeriod::getPeriodYearNum, existAccountBookPeriod.getPeriodYearNum())
                .orderByDesc(AccountBookPeriod::getPeriodYearNum)
                .last(Constant.ConcatSql.LIMIT_1));
        if (prePeriod == null) {
            // 无上期,校验是否初始化
            if (accountBookSystem.getStatus() == null || Constant.Status.ON != accountBookSystem.getStatus() || Constant.Is.NO.equals(accountBookSystem.getIsInitalized())) {
                // 未启用或未初始化
                settleResult.setCheckFlag(true);
            }
            settleResult.setOpenStatus(accountBookSystem.getIsInitalized());
            // 没有上期,不需要校验上期是否结账
            return;
        }
        if (Constant.Is.YES.equals(prePeriod.getIsSettle())) {
            // 上期已结账 不继续检查
            return;
        }
        // 上期未结账 获取第一期未结账期间，返回对应提示 如：2019年第1期未结账，2019年第2期不可结账
        AccountBookPeriod firstNotSettlePeriod = accountBookPeriodService.getOne(new LambdaQueryWrapper<AccountBookPeriod>()
                .eq(AccountBookPeriod::getAccountBookId, existAccountBookPeriod.getAccountBookId())
                .eq(AccountBookPeriod::getSystemSign, Constant.SystemSign.LEDGER)
                .eq(AccountBookPeriod::getIsSettle, Constant.Is.NO)
                .orderByAsc(AccountBookPeriod::getPeriodYearNum)
                .last(Constant.ConcatSql.LIMIT_1));
        if (firstNotSettlePeriod == null) {
            throw new ServiceException(ResultCode.DATA_ERROR);
        }
        String errorMsg = String.format(ResultCode.SETTLE_PERIOD_ERROR.message, firstNotSettlePeriod.getPeriodYear(), firstNotSettlePeriod.getPeriodNum(), existAccountBookPeriod.getPeriodYear(), existAccountBookPeriod.getPeriodNum());
        throw new ServiceException(errorMsg, ResultCode.SETTLE_PERIOD_ERROR);
    }

    private class NeedDealBalance {
        /**
         * 需要获取余额表数据的科目字典
         */
        private Map<Long, AccountSubjectVo> subjectDict;
        /**
         * 期间数据
         */
        private AccountBookPeriod existAccountBookPeriod;
        /**
         * key为balanceId 用于存储有辅助核算的余额items
         **/
        private Map<Long, List<BalanceSubjectAuxiliaryItem>> balanceAuxiliaryItemDict = new LinkedHashMap<>();
        /**
         * 未配置辅助核算的余额数据
         **/
        private List<BalanceSubject> withOutAuxiliaryBalanceList;
        /**
         * 配置辅助核算的余额数据
         **/
        private List<BalanceSubjectAuxiliary> withAuxiliaryBalanceList;
        /**
         * 无辅助核算的科目ids
         **/
        private List<Long> withOutAuxiliarySubjectIds = new LinkedList<>();
        /**
         * 有辅助核算的科目ids
         **/
        private List<Long> withAuxiliarySubjectIds = new LinkedList<>();

        NeedDealBalance(Map<Long, AccountSubjectVo> subjectDict, AccountBookPeriod existAccountBookPeriod) {
            this.subjectDict = subjectDict;
            this.existAccountBookPeriod = existAccountBookPeriod;
        }

        /**
         * 获取余额表数据
         * 只查询有期初或发生额的数据,无数据的科目不需要做期末处理
         *
         * @param isCollectAllBalance withOutAuxiliaryBalanceList余额主表是否查出包含辅助核算的数据，true查全部 false-只查无辅助核算的数据
         * @return com.njwd.ledger.service.impl.BackupServiceImpl.NeedDealBalance
         * @author xyyxhcj@qq.com
         * @date 2019/9/26 10:51
         **/
        NeedDealBalance invoke(boolean isCollectAllBalance) {
            // 判断损益科目是否有辅助核算,分别获取对应的科目数据
            for (AccountSubjectVo vo : subjectDict.values()) {
                if (vo.getAccountSubjectAuxiliaryList() != null && !vo.getAccountSubjectAuxiliaryList().isEmpty()
                        // 如果为内部往来科目，余额处理方式等同辅助核算
                        || Constant.Is.YES.equals(vo.getIsInterior())) {
                    withAuxiliarySubjectIds.add(vo.getId());
                } else {
                    withOutAuxiliarySubjectIds.add(vo.getId());
                }
            }
            if (!withOutAuxiliarySubjectIds.isEmpty()) {
                // 获取科目余额表数据
                LambdaQueryWrapper<BalanceSubject> balanceSubjectWrapper = new LambdaQueryWrapper<BalanceSubject>()
                        .eq(Balance::getAccountBookId, existAccountBookPeriod.getAccountBookId())
                        .eq(Balance::getPeriodYearNum, existAccountBookPeriod.getPeriodYearNum());
                if (!isCollectAllBalance) {
                    // 根据参数判断 true查全部
                    balanceSubjectWrapper.in(Balance::getAccountSubjectId, withOutAuxiliarySubjectIds);
                }
                withOutAuxiliaryBalanceList = balanceSubjectMapper.selectList(balanceSubjectWrapper);
                withOutAuxiliaryBalanceList.removeIf(balance -> BigDecimal.ZERO.compareTo(balance.getOpeningBalance()) == 0
                        && BigDecimal.ZERO.compareTo(balance.getDebitAmount()) == 0
                        && BigDecimal.ZERO.compareTo(balance.getCreditAmount()) == 0
                        && BigDecimal.ZERO.compareTo(balance.getClosingBalance()) == 0);
            } else {
                withOutAuxiliaryBalanceList = Collections.emptyList();
            }
            if (!withAuxiliarySubjectIds.isEmpty()) {
                // 获取辅助核算科目余额表数据(只取有发生额或有初始余额/累计的数据)
                LambdaQueryWrapper<BalanceSubjectAuxiliary> balanceSubjectAuxiliaryWrapper = new LambdaQueryWrapper<BalanceSubjectAuxiliary>()
                        .eq(Balance::getAccountBookId, existAccountBookPeriod.getAccountBookId())
                        .eq(Balance::getPeriodYearNum, existAccountBookPeriod.getPeriodYearNum())
                        .in(Balance::getAccountSubjectId, withAuxiliarySubjectIds);
                withAuxiliaryBalanceList = balanceSubjectAuxiliaryMapper.selectList(balanceSubjectAuxiliaryWrapper);
                withAuxiliaryBalanceList.removeIf(balance -> BigDecimal.ZERO.compareTo(balance.getOpeningBalance()) == 0
                        && BigDecimal.ZERO.compareTo(balance.getDebitAmount()) == 0
                        && BigDecimal.ZERO.compareTo(balance.getCreditAmount()) == 0
                        && BigDecimal.ZERO.compareTo(balance.getClosingBalance()) == 0);
                if (!withAuxiliaryBalanceList.isEmpty()) {
                    // 查出关联的辅助核算明细
                    List<BalanceSubjectAuxiliaryItem> auxiliaryItemList = balanceSubjectAuxiliaryItemMapper.selectList(new LambdaQueryWrapper<BalanceSubjectAuxiliaryItem>()
                            .eq(BalanceSubjectAuxiliaryItem::getAccountBookId, existAccountBookPeriod.getAccountBookId())
                            .eq(BalanceSubjectAuxiliaryItem::getPeriodYearNum, existAccountBookPeriod.getPeriodYearNum()));
                    // 根据balanceId对辅助核算明细归类
                    for (BalanceSubjectAuxiliaryItem auxiliaryItem : auxiliaryItemList) {
                        balanceAuxiliaryItemDict.computeIfAbsent(auxiliaryItem.getBalanceAuxiliaryId(), k -> new LinkedList<>()).add(auxiliaryItem);
                    }
                }
            } else {
                withAuxiliaryBalanceList = Collections.emptyList();
            }
            return this;
        }
    }
}
