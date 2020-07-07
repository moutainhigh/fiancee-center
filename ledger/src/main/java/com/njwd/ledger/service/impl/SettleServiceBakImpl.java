package com.njwd.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.njwd.common.Constant;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.basedata.AccountBookSystem;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.AccountBookSystemDto;
import com.njwd.entity.basedata.vo.CompanyVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.*;
import com.njwd.entity.ledger.dto.*;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import com.njwd.entity.ledger.vo.DelParameterSetVo;
import com.njwd.entity.platform.AccountSubject;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.entity.platform.vo.AccountingPeriodVo;
import com.njwd.exception.FeignClientErrorMsg;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.ledger.cloudclient.AccountBookFeignClient;
import com.njwd.ledger.cloudclient.AccountBookSystemFeignClient;
import com.njwd.ledger.cloudclient.AccountSubjectFeignClient;
import com.njwd.ledger.cloudclient.CompanyFeignClient;
import com.njwd.ledger.mapper.*;
import com.njwd.ledger.service.*;
import com.njwd.ledger.utils.LedgerUtils;
import com.njwd.support.CheckVoucherResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.MergeUtil;
import com.njwd.utils.UserUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 结账\反结账处理
 * @Date 2019/8/13 19:25
 * @Author 朱小明
 */
//@Service
@Transactional(rollbackFor = Exception.class)
public class SettleServiceBakImpl implements SettleServiceBak {

    @Resource
    private AccountBookSystemFeignClient accountBookSystemFeignClient;
    @Resource
    private AccountBookFeignClient accountBookFeignClient;
    @Resource
    private AccountSubjectFeignClient accountSubjectFeignClient;
    @Resource
    private CompanyFeignClient companyFeignClient;
    @Resource
    private VoucherService voucherService;
    @Resource
    private BalanceSubjectService balanceSubjectService;
    @Resource
    private BalanceSubjectAuxiliaryService balanceSubjectAuxiliarySevice;
    @Resource
    private VoucherEntryAuxiliaryService voucherEntryAuxiliaryService;
    @Resource
    private ParameterSetService parameterSetService;
    @Resource
    private VoucherAdjustService voucherAdjustService;
    @Resource
    private VoucherMapper voucherMapper;
    @Resource
    private VoucherEntryMapper voucherEntryMapper;
    @Resource
    private BalanceSubjectAuxiliaryMapper balanceSubjectAuxiliaryMapper;
    @Resource
    private BalanceSubjectMapper balanceSubjectMapper;
    @Resource
    private BalanceSubjectAuxiliaryItemMapper balanceSubjectAuxiliaryItemMapper;
    @Resource
    private AccountBookPeriodMapper accountBookPeriodMapper;

    /**
     * @param accountBookPeriod
     * @return CheckVoucherResult
     * @Description 财务检查
     * @Author 朱小明
     * @Date 2019/8/13 19:28
     */
    @Override
    public CheckVoucherResult settle(AccountBookPeriodDto accountBookPeriod) {
        //校验是否可结账
        checkCanSettle(accountBookPeriod, true);
        //总账参数信息
        DelParameterSetVo parameterSetVo = new DelParameterSetVo();
        //财务检查
        CheckVoucherResult checkResult = checkFinance(accountBookPeriod, parameterSetVo);
        Map<Long, Byte> balanceDirectionMap = new HashMap<>();
        if (checkResult.getCheckFlag()) {
            return checkResult;
        }
        //凭证整理集合
        List<AccountBookPeriodVo> accountBookPeriods = new ArrayList<>();
        AccountBookPeriodVo abp = new AccountBookPeriodVo();
        FastUtils.copyProperties(accountBookPeriod, abp);
        accountBookPeriods.add(abp);
        //整理凭证号
        voucherAdjustService.adjustExcute(accountBookPeriods);
        // 损益结转处理
        lossProfitExcute(accountBookPeriod, parameterSetVo, checkResult, balanceDirectionMap);
        //余额累计处理
        balanceTotalExcute(accountBookPeriod, balanceDirectionMap);
        // 结账处理
        settleExcutes(accountBookPeriod);
        //返回损益账簿期间最新信息
        checkResult.setAccountBookPeriod(accountBookPeriodMapper.selectById(accountBookPeriod.getId()));
        //将损益凭证放入结果集中,返回给前端
        checkResult.setLossProfitList(voucherMapper.selectLossProfitList(accountBookPeriod));
        return checkResult;
    }

    /**
     * @param accountBookPeriod 账簿期间信息
     * @return void
     * @Description 反结账处理
     * @Author 朱小明
     * @Date 2019/8/27
     **/
    @Override
    public AccountBookPeriod cancelSettle(AccountBookPeriodDto accountBookPeriod) {
        //校验是否可反结账
        checkCanSettle(accountBookPeriod, false);
        // 总账参数信息
        if (Constant.Is.NO.equals(getLedgerParam().getIsOpenAccounts())) {
            throw new ServiceException(ResultCode.FORBID_CANCEL_SETTLE);
        }
        // 查询出所有损益凭证
        List<VoucherDto> lossProfitList = voucherMapper.findLossProfitIdsByAccountBookPeriod(accountBookPeriod);
        if (lossProfitList.size() > 0) {
            //变更因添加或删除凭证引起怕余额变动
            updateBalanceByDeleteOrAddBatch(lossProfitList, accountBookPeriod, false);
            voucherMapper.deleteBatch(lossProfitList);
        }
        // 更新账簿信息
        updateAccountBookPeriod(accountBookPeriod);
        //凭证整理集合
        List<AccountBookPeriodVo> accountBookPeriods = new ArrayList<>();
        AccountBookPeriodVo abp = new AccountBookPeriodVo();
        FastUtils.copyProperties(accountBookPeriod, abp);
        accountBookPeriods.add(abp);
        //整理凭证号，重置流水
        voucherAdjustService.adjustExcute(accountBookPeriods);
        return accountBookPeriod;
    }

    /**
     * @param accountBookPeriod
     * @return void
     * @Description 校验是否可结账
     * @Author 朱小明
     * @Date 2019/9/25
     **/
    private void checkCanSettle(AccountBookPeriodDto accountBookPeriod, Boolean isCanSettle) {
        if (isCanSettle) {
            //如果已结账，不能再次结账
            if (Constant.Is.YES.equals(accountBookPeriod.getIsSettle())) {
                throw new ServiceException(ResultCode.NOT_SETTLE);
            }
            accountBookPeriod.setFuturePeriodNum(LedgerConstant.Settle.FUTURE);
            accountBookPeriod.setIsFuture(Constant.Is.NO);
            //如果不是所有未结账账簿的第一期，不能结账
            List<AccountBookPeriod> accountBookPeriodList = accountBookPeriodMapper.selectFuturePeriodList(accountBookPeriod);
            if (accountBookPeriodList.size() == 1 && Constant.Is.NO.equals(accountBookPeriodList.get(0).getIsSettle())) {
                throw new ServiceException(ResultCode.CAN_NOT_SETTLE);
            }
        } else {
            //如果未结账，不能反结账
            if (Constant.Is.NO.equals(accountBookPeriod.getIsSettle())) {
                throw new ServiceException(ResultCode.NOT_CANCEL_SETTLE);
            }
            accountBookPeriod.setFuturePeriodNum(LedgerConstant.Settle.FUTURE);
            accountBookPeriod.setIsFuture(Constant.Is.YES);
            List<AccountBookPeriod> accountBookPeriodList = accountBookPeriodMapper.selectFuturePeriodList(accountBookPeriod);
            //如果不是所有已结账账簿最后一期，不能反结账过来
            if (Constant.Is.YES.equals(accountBookPeriodList.get(0).getIsSettle())) {
                throw new ServiceException(ResultCode.CAN_NOT_CANCEL_SETTLE);
            }
        }
    }

    /**
     * @param accountBookPeriod, balanceDirectionMap
     * @return void
     * @Description 余额累计处理
     * @Author 朱小明
     * @Date 2019/9/11
     **/
    private void balanceTotalExcute(AccountBookPeriodDto accountBookPeriod, Map<Long, Byte> balanceDirectionMap) {
        accountBookPeriod.setBeforeOrFuture(LedgerConstant.Settle.BEFORE);
        List<Integer> periods = accountBookPeriodMapper.selectTwoPeriod(accountBookPeriod);
        accountBookPeriod.setPeriodYears(periods);
        // 查询本月和上个月所有科目余额
        List<BalanceSubject> balanceSubjectLists = balanceSubjectMapper.selectSubjctBalanceList(accountBookPeriod);
        //根据科目ID分组近本月和上个月所有科目信息
        Map<String, List<BalanceSubject>> balanceSubjectListMap = balanceSubjectLists.parallelStream()
                .collect(Collectors.groupingBy(g -> getPerimakeys(g.getAccountBookEntityId(), g.getAccountSubjectId())));
        // 科目余额表累计+期末处理
        List<BalanceSubject> balanceSubjectList = balanceSubjectExcute(balanceSubjectListMap, balanceDirectionMap);
        if (balanceSubjectList.size() > 0) {
            balanceSubjectService.saveOrUpdateBatch(balanceSubjectList);
        }
        // 查询本月和上个月所有科目辅助核算
        List<BalanceSubjectAuxiliaryDto> balanceAuxiliaryList =
                balanceSubjectAuxiliaryMapper.selectSubjctAuxiliaryBalanceList(accountBookPeriod);
        //查询出所有科目辅助核算的值
        if (balanceAuxiliaryList.isEmpty()) {
            return;
        }
        List<BalanceSubjectAuxiliaryItem> auxiliaryItems = balanceSubjectAuxiliaryItemMapper.selectList(new LambdaQueryWrapper<BalanceSubjectAuxiliaryItem>()
                .in(BalanceSubjectAuxiliaryItem::getBalanceAuxiliaryId, balanceAuxiliaryList.parallelStream().map(BalanceSubjectAuxiliary::getId).collect(Collectors.toSet())));
        //根据辅助核算余额表ID将余额表的辅助核算项目拼接到辅助核算余额表中
        MergeUtil.mergeList(balanceAuxiliaryList, auxiliaryItems
                , (balance, item) -> balance.getId().equals(item.getBalanceAuxiliaryId())
                , (balance, items) -> balance.getBalanceSubjectAuxiliaryItems().addAll(items));
        //设置辅助核算余额表的辅助核算项目按  table1_value1_table2_value2组合设置到新建的keySigns中
        balanceAuxiliaryList.parallelStream().forEach(e -> e.setKeySigns(getPerimakeys(e.getAccountBookEntityId(), e.getAccountSubjectId()) + Constant.Symbol.UNDERLINE + getPerimakeys(
                e.getBalanceSubjectAuxiliaryItems().parallelStream().map(e1 -> getPerimakeys(e1.getSourceTable(), e1.getItemValueId())).collect(Collectors.toList()).toArray())));
        //根据辅助核算拼接字段分组
        Map<String, List<BalanceSubjectAuxiliaryDto>> balanceSubjectAuxiliaryMap = balanceAuxiliaryList.parallelStream().collect(Collectors.groupingBy(BalanceSubjectAuxiliaryDto::getKeySigns));
        // 辅助核算余额表累计+期末余额
        List<BalanceSubjectAuxiliaryDto> balanceSubjectAuxiliaryList = balanceSubjectExcute(balanceSubjectAuxiliaryMap, balanceDirectionMap);
        if (balanceSubjectAuxiliaryList.size() > 0) {
            List<BalanceSubjectAuxiliary> balanceSubjectAuxiliaries = new ArrayList<>();
            BalanceSubjectAuxiliary balanceSubjectAuxiliarie;
            for (BalanceSubjectAuxiliaryDto balanceSubjectAuxiliaryDto : balanceSubjectAuxiliaryList) {
                balanceSubjectAuxiliarie = new BalanceSubjectAuxiliary();
                FastUtils.copyProperties(balanceSubjectAuxiliaryDto, balanceSubjectAuxiliarie);
                balanceSubjectAuxiliaries.add(balanceSubjectAuxiliarie);
            }
            //更新辅助核算余额累计和期末余额
            balanceSubjectAuxiliarySevice.saveOrUpdateBatch(balanceSubjectAuxiliaries);
        }
    }

    /**
     * @param voucherList       要添加或删除的凭证列表
     * @param accountBookPeriod
     * @param isAdd             true:添加 false:删除
     * @return void
     * @Description 更新因添加或删除凭证带来的余额变动
     * @Author 朱小明
     * @Date 2019/9/6
     **/
    private void updateBalanceByDeleteOrAddBatch(List<VoucherDto> voucherList, AccountBookPeriodDto accountBookPeriod, Boolean isAdd) {
        //查询出当期所有凭证的分录明细
        List<VoucherEntryDto> voucherEntryList = voucherEntryMapper.findList(voucherList.parallelStream().map(VoucherDto::getId).collect(Collectors.toList()));
        if (voucherEntryList.size() == 0) {
            return;
        }
        //过滤出所有凭证分录的科目
        List<Long> subjectidList = voucherEntryList.parallelStream().map(VoucherEntryDto::getAccountSubjectId).distinct().collect(Collectors.toList());
        //过滤出所有凭证分录的主凭证号
        List<Long> voucherIdList = voucherEntryList.parallelStream().map(VoucherEntryDto::getVoucherId).distinct().collect(Collectors.toList());
        //查询出当期所有分录凭证的辅助核算
        List<VoucherEntryAuxiliaryDto> voucherEntryAuxiliaryList = voucherEntryAuxiliaryService.findList(voucherIdList);
        accountBookPeriod.setProfitLossList(subjectidList);
        //查询出当期科目余额表信息
        List<BalanceSubject> subjctAsList = balanceSubjectMapper.selectSubjctBalanceAsList(accountBookPeriod);
        //查询出当期余额表辅助核算信息
        List<BalanceSubjectAuxiliaryItem> auxiliaryItems = balanceSubjectAuxiliaryItemMapper.selectAuxiliaryIteamList(accountBookPeriod);
        //查询出当期辅助核算余额表信息
        List<BalanceSubjectAuxiliaryDto> balanceAuxiliaryList = balanceSubjectAuxiliaryMapper.selectAuxiliaryList(accountBookPeriod);
        //根据辅助核算余额表ID将余额表的辅助核算项目拼接到辅助核算余额表中
        MergeUtil.mergeList(balanceAuxiliaryList, auxiliaryItems
                , (balance, item) -> balance.getId().equals(item.getBalanceAuxiliaryId())
                , (balance, items) -> balance.getBalanceSubjectAuxiliaryItems().addAll(items));
        //根据凭证分录ID将辅助核算拼接到凭证分录信息中
        MergeUtil.mergeList(voucherEntryList, voucherEntryAuxiliaryList
                , (source, des) -> source.getId().equals(des.getEntryId())
                , (source, desList) -> source.getEditAuxiliaryList().addAll(desList));
        //根据凭证凭证分录ID，将凭证信息绑定到分录信息上
        MergeUtil.merge(voucherEntryList, voucherList
                , (source, des) -> source.getVoucherId().equals(des.getId())
                , (source, des) -> source.setVoucherDto(des));
        //设置辅助核算余额表的辅助核算项目按  table1_value1_table2_value2组合设置到新建的keySigns中
        balanceAuxiliaryList.parallelStream().forEach(e -> e.setKeySigns(getPerimakeys(e.getAccountBookEntityId(), e.getAccountSubjectId()) + Constant.Symbol.UNDERLINE + getPerimakeys(
                e.getBalanceSubjectAuxiliaryItems().parallelStream().map(e1 -> getPerimakeys(e1.getSourceTable(), e1.getItemValueId())).collect(Collectors.toList()).toArray())));
        //将科目余额表按核算主体_科目进行分组
        Map<String, List<BalanceSubject>> subjctAsListMap
                = subjctAsList.parallelStream().collect(Collectors.groupingBy(e -> getPerimakeys(e.getAccountBookEntityId(), e.getAccountSubjectId())));
        //将辅助核算余额表按照keySigns进行分组
        Map<String, List<BalanceSubjectAuxiliaryDto>> auxiliaryAsListMap
                = balanceAuxiliaryList.parallelStream().collect(Collectors.groupingBy(BalanceSubjectAuxiliaryDto::getKeySigns));
        List<BalanceSubject> balanceSubjectList = new ArrayList<>();
        List<BalanceSubjectAuxiliary> balanceSubjecAuxiliarytList = new ArrayList<>();
        //更新余额表
        getBalanceSubjectList(isAdd, voucherEntryList, subjctAsListMap, auxiliaryAsListMap, balanceSubjectList, balanceSubjecAuxiliarytList);
        if (balanceSubjectList.size() > 0) {
            balanceSubjectService.saveOrUpdateBatch(balanceSubjectList);
        }
        if (balanceSubjecAuxiliarytList.size() > 0) {
            balanceSubjectAuxiliarySevice.saveOrUpdateBatch(balanceSubjecAuxiliarytList);
        }
    }

    /**
     * @param isAdd                       rue：加 false:减
     * @param voucherEntryList,           subjctAsListMap, balanceSubjectList
     * @param auxiliaryAsListMap
     * @param balanceSubjectList
     * @param balanceSubjecAuxiliarytList
     * @return void
     * @Description 更新凭证增删带来的余额变动
     * @Author 朱小明
     * @Date 2019/9/6
     **/
    private void getBalanceSubjectList(Boolean isAdd, List<VoucherEntryDto> voucherEntryList, Map<String, List<BalanceSubject>> subjctAsListMap
            , Map<String, List<BalanceSubjectAuxiliaryDto>> auxiliaryAsListMap, List<BalanceSubject> balanceSubjectList, List<BalanceSubjectAuxiliary> balanceSubjecAuxiliarytList) {
        BalanceSubject balanceSubject;
        BalanceSubjectAuxiliaryDto balanceSubjectAuxiliaryDto;
        BalanceSubjectAuxiliary balanceSubjectAuxiliary;
        List<BalanceSubject> balanceSubjects;
        List<BalanceSubject> balanceSubjectListL;

        if (isAdd) {
            for (VoucherEntryDto voucherEntryDto : voucherEntryList) {
                balanceSubjects = subjctAsListMap.get(getPerimakeys(voucherEntryDto.getVoucherDto().getAccountBookEntityId(), voucherEntryDto.getAccountSubjectId()));
                if (balanceSubjects != null) {
                    balanceSubject = balanceSubjects.get(0);
                } else {
                    //如果余额表中不存在「总账参数中设置的损益科目」，就添加一条
                    balanceSubject = new BalanceSubject();
                    balanceSubject.setAccountBookId(voucherEntryDto.getVoucherDto().getAccountBookId());
                    balanceSubject.setAccountBookEntityId(voucherEntryDto.getVoucherDto().getAccountBookEntityId());
                    balanceSubject.setAccountSubjectId(voucherEntryDto.getAccountSubjectId());
                    balanceSubject.setPeriodYear(voucherEntryDto.getVoucherDto().getPostingPeriodYear());
                    balanceSubject.setPeriodNum(voucherEntryDto.getVoucherDto().getPostingPeriodNum());
                    balanceSubject.setPeriodYearNum(balanceSubject.getPeriodYear() * 100 + balanceSubject.getPeriodNum());
                    balanceSubject.setOpeningBalance(BigDecimal.ZERO);
                    balanceSubject.setClosingBalance(BigDecimal.ZERO);
                    balanceSubject.setDebitAmount(BigDecimal.ZERO);
                    balanceSubject.setCreditAmount(BigDecimal.ZERO);
                    balanceSubject.setTotalDebitAmount(BigDecimal.ZERO);
                    balanceSubject.setTotalCreditAmount(BigDecimal.ZERO);
                    balanceSubject.setPostDebitAmount(BigDecimal.ZERO);
                    balanceSubject.setPostCreditAmount(BigDecimal.ZERO);
                    balanceSubject.setPostTotalDebitAmount(BigDecimal.ZERO);
                    balanceSubject.setPostTotalCreditAmount(BigDecimal.ZERO);
                    balanceSubject.setSyDebitAmount(BigDecimal.ZERO);
                    balanceSubject.setSyCreditAmount(BigDecimal.ZERO);
                    balanceSubject.setSyTotalDebitAmount(BigDecimal.ZERO);
                    balanceSubject.setSyTotalCreditAmount(BigDecimal.ZERO);
                    balanceSubject.setPostSyDebitAmount(BigDecimal.ZERO);
                    balanceSubject.setPostSyCreditAmount(BigDecimal.ZERO);
                    balanceSubject.setPostSyTotalDebitAmount(BigDecimal.ZERO);
                    balanceSubject.setPostSyTotalCreditAmount(BigDecimal.ZERO);
                    balanceSubjectMapper.insert(balanceSubject);
                    balanceSubjectListL = new ArrayList<>();
                    balanceSubjectListL.add(balanceSubject);
                    subjctAsListMap.put(getPerimakeys(voucherEntryDto.getVoucherDto().getAccountBookEntityId(), voucherEntryDto.getAccountSubjectId()), balanceSubjectListL);
                }
                //将分录凭证的余额加到余额表中
                balanceSubject.setDebitAmount(balanceSubject.getDebitAmount().add(voucherEntryDto.getDebitAmount()));
                balanceSubject.setCreditAmount(balanceSubject.getCreditAmount().add(voucherEntryDto.getCreditAmount()));
                balanceSubject.setPostDebitAmount(balanceSubject.getPostDebitAmount().add(voucherEntryDto.getDebitAmount()));
                balanceSubject.setPostCreditAmount(balanceSubject.getPostCreditAmount().add(voucherEntryDto.getCreditAmount()));
                balanceSubject.setSyDebitAmount(balanceSubject.getSyDebitAmount().add(voucherEntryDto.getDebitAmount()));
                balanceSubject.setSyCreditAmount(balanceSubject.getSyCreditAmount().add(voucherEntryDto.getCreditAmount()));
                balanceSubject.setPostSyDebitAmount(balanceSubject.getPostSyDebitAmount().add(voucherEntryDto.getDebitAmount()));
                balanceSubject.setPostSyCreditAmount(balanceSubject.getPostSyCreditAmount().add(voucherEntryDto.getCreditAmount()));
                balanceSubjectList.add(balanceSubject);
                //如果有辅助核算，将分录的值加到辅助核算中
                if (voucherEntryDto.getEditAuxiliaryList() != null && voucherEntryDto.getEditAuxiliaryList().size() > 0) {
                    balanceSubjectAuxiliaryDto = auxiliaryAsListMap.get(getPerimakeys(balanceSubject.getAccountBookEntityId(), balanceSubject.getAccountSubjectId()) + Constant.Symbol.UNDERLINE + getPerimakeys(voucherEntryDto.getEditAuxiliaryList()
                            .parallelStream().map(e1 -> getPerimakeys(e1.getSourceTable(), e1.getItemValueId())).collect(Collectors.toList()).toArray())).get(0);
                    balanceSubjectAuxiliaryDto.setSyDebitAmount(balanceSubjectAuxiliaryDto.getSyDebitAmount().add(voucherEntryDto.getDebitAmount()));
                    balanceSubjectAuxiliaryDto.setSyCreditAmount(balanceSubjectAuxiliaryDto.getSyCreditAmount().add(voucherEntryDto.getCreditAmount()));
                    balanceSubjectAuxiliaryDto.setPostSyDebitAmount(balanceSubjectAuxiliaryDto.getPostSyDebitAmount().add(voucherEntryDto.getDebitAmount()));
                    balanceSubjectAuxiliaryDto.setPostSyCreditAmount(balanceSubjectAuxiliaryDto.getPostSyCreditAmount().add(voucherEntryDto.getCreditAmount()));
                    balanceSubjectAuxiliaryDto.setDebitAmount(balanceSubjectAuxiliaryDto.getDebitAmount().add(voucherEntryDto.getDebitAmount()));
                    balanceSubjectAuxiliaryDto.setCreditAmount(balanceSubjectAuxiliaryDto.getCreditAmount().add(voucherEntryDto.getCreditAmount()));
                    balanceSubjectAuxiliaryDto.setPostDebitAmount(balanceSubjectAuxiliaryDto.getPostDebitAmount().add(voucherEntryDto.getDebitAmount()));
                    balanceSubjectAuxiliaryDto.setPostCreditAmount(balanceSubjectAuxiliaryDto.getPostCreditAmount().add(voucherEntryDto.getCreditAmount()));
                    balanceSubjecAuxiliarytList.add(balanceSubjectAuxiliaryDto);
                }
            }
        } else {
            //将所有删除的分录金额从余额表中删除
            for (VoucherEntryDto voucherEntryDto : voucherEntryList) {
                balanceSubjectAuxiliary = new BalanceSubjectAuxiliary();
                balanceSubject = subjctAsListMap.get(getPerimakeys(voucherEntryDto.getVoucherDto().getAccountBookEntityId(), voucherEntryDto.getAccountSubjectId())).get(0);
                balanceSubject.setDebitAmount(balanceSubject.getDebitAmount().subtract(voucherEntryDto.getDebitAmount()));
                balanceSubject.setCreditAmount(balanceSubject.getCreditAmount().subtract(voucherEntryDto.getCreditAmount()));
                balanceSubject.setPostDebitAmount(balanceSubject.getPostDebitAmount().subtract(voucherEntryDto.getDebitAmount()));
                balanceSubject.setPostCreditAmount(balanceSubject.getPostCreditAmount().subtract(voucherEntryDto.getCreditAmount()));
                balanceSubject.setSyDebitAmount(balanceSubject.getSyDebitAmount().subtract(voucherEntryDto.getDebitAmount()));
                balanceSubject.setSyCreditAmount(balanceSubject.getSyCreditAmount().subtract(voucherEntryDto.getCreditAmount()));
                balanceSubject.setPostSyDebitAmount(balanceSubject.getPostSyDebitAmount().subtract(voucherEntryDto.getDebitAmount()));
                balanceSubject.setPostSyCreditAmount(balanceSubject.getPostSyCreditAmount().subtract(voucherEntryDto.getCreditAmount()));
                balanceSubjectList.add(balanceSubject);
                //如果有辅助核算，从辅助核算的余额中减去删除的分录凭证
                if (voucherEntryDto.getEditAuxiliaryList() != null && voucherEntryDto.getEditAuxiliaryList().size() > 0) {
                    balanceSubjectAuxiliaryDto = auxiliaryAsListMap.get(getPerimakeys(balanceSubject.getAccountBookEntityId(), balanceSubject.getAccountSubjectId()) + Constant.Symbol.UNDERLINE + getPerimakeys(voucherEntryDto.getEditAuxiliaryList()
                            .parallelStream().map(e1 -> getPerimakeys(e1.getSourceTable(), e1.getItemValueId())).collect(Collectors.toList()).toArray())).get(0);
                    balanceSubjectAuxiliaryDto.setSyDebitAmount(balanceSubjectAuxiliaryDto.getSyDebitAmount().subtract(voucherEntryDto.getDebitAmount()));
                    balanceSubjectAuxiliaryDto.setSyCreditAmount(balanceSubjectAuxiliaryDto.getSyCreditAmount().subtract(voucherEntryDto.getCreditAmount()));
                    balanceSubjectAuxiliaryDto.setPostSyDebitAmount(balanceSubjectAuxiliaryDto.getPostSyDebitAmount().subtract(voucherEntryDto.getDebitAmount()));
                    balanceSubjectAuxiliaryDto.setPostSyCreditAmount(balanceSubjectAuxiliaryDto.getPostSyCreditAmount().subtract(voucherEntryDto.getCreditAmount()));
                    balanceSubjectAuxiliaryDto.setDebitAmount(balanceSubjectAuxiliaryDto.getDebitAmount().subtract(voucherEntryDto.getDebitAmount()));
                    balanceSubjectAuxiliaryDto.setCreditAmount(balanceSubjectAuxiliaryDto.getCreditAmount().subtract(voucherEntryDto.getCreditAmount()));
                    balanceSubjectAuxiliaryDto.setPostDebitAmount(balanceSubjectAuxiliaryDto.getPostDebitAmount().subtract(voucherEntryDto.getDebitAmount()));
                    balanceSubjectAuxiliaryDto.setPostCreditAmount(balanceSubjectAuxiliaryDto.getPostCreditAmount().subtract(voucherEntryDto.getCreditAmount()));
                    FastUtils.copyProperties(balanceSubjectAuxiliaryDto, balanceSubjectAuxiliary);
                    balanceSubjecAuxiliarytList.add(balanceSubjectAuxiliary);
                }
            }
        }
    }

    /**
     * @param ids
     * @return java.lang.String
     * @Description 拼接多ID
     * @Author 朱小明
     * @Date 2019/9/6
     **/
    private String getPerimakeys(final Object... ids) {
        return String.join(Constant.Symbol.UNDERLINE, Arrays.stream(ids).map(e -> e.toString()).collect(Collectors.toList()));
    }

    /**
     * @param accountBookPeriod
     * @return void
     * @Description 更新账簿信息
     * @Author 朱小明
     * @Date 2019/8/29
     **/
    private int updateAccountBookPeriod(AccountBookPeriod accountBookPeriod) {
        SysUserVo sysUserVo = UserUtils.getUserVo();
        accountBookPeriod.setIsSettle(Constant.Is.NO);
        accountBookPeriod.setCancelSettleUserId(sysUserVo.getUserId());
        accountBookPeriod.setCancelSettleUserName(sysUserVo.getName());
        accountBookPeriod.setCancelSettleTime(new Date());
        return accountBookPeriodMapper.updateById(accountBookPeriod);
    }

    /**
     * @param accountBookPeriodDto
     * @return void
     * @Description 结账处理
     * @Author 朱小明
     * @Date 2019/8/27
     **/
    private void settleExcutes(AccountBookPeriodDto accountBookPeriodDto) {
        // 更新本账簿信息
        updateThisAbPeriod(accountBookPeriodDto);
        // 根据条件打开未来X期间
        openAbPeriod(accountBookPeriodDto);
        // 查询出未来2期的会计科目余额
        accountBookPeriodDto.setBeforeOrFuture(LedgerConstant.Settle.FUTURE);
        accountBookPeriodDto.setPeriodYears(accountBookPeriodMapper.selectTwoPeriod(accountBookPeriodDto));
        //查询出本期和下一期的科目余额
        List<BalanceSubject> balanceSubjectLists = balanceSubjectMapper.selectSubjctBalanceList(accountBookPeriodDto);
        //查询出本期和下一期的辅助核算余额
        List<BalanceSubjectAuxiliaryDto> balanceAuxiliaryList = balanceSubjectAuxiliaryMapper.selectSubjctAuxiliaryBalanceList(accountBookPeriodDto);
        //查询出本期和下一期的辅助核算项目
        List<BalanceSubjectAuxiliaryItem> auxiliaryItems = balanceSubjectAuxiliaryItemMapper.selectSubjctAuxiliaryItemList(accountBookPeriodDto);
        MergeUtil.mergeList(balanceAuxiliaryList, auxiliaryItems
                , (balance, item) -> balance.getId().equals(item.getBalanceAuxiliaryId())
                , (balance, items) -> balance.getBalanceSubjectAuxiliaryItems().addAll(items));
        balanceAuxiliaryList.parallelStream().forEach(e -> e.setKeySigns(getPerimakeys(e.getAccountBookEntityId(), e.getAccountSubjectId())
                + Constant.Symbol.UNDERLINE + getPerimakeys(e.getBalanceSubjectAuxiliaryItems().parallelStream()
                .map(e1 -> getPerimakeys(e1.getSourceTable(), e1.getItemValueId())).collect(Collectors.toList()).toArray())));
        // 写入下期期初余额
        accountBookPeriodDto.setFuturePeriodNum(LedgerConstant.Settle.FUTURE);
        accountBookPeriodDto.setIsFuture(Constant.Is.YES);
        List<AccountBookPeriod> accountBookPeriods = accountBookPeriodMapper.selectFuturePeriodList(accountBookPeriodDto);
        if (accountBookPeriods.size() == 0) {
            return;
        }
        if (balanceSubjectLists.size() > 0) {
            //将查询的余额根据核算主体+会计科目分组
            Map<String, List<BalanceSubject>> balanceSubjectListMap
                    = balanceSubjectLists.parallelStream().collect(Collectors.groupingBy(e -> getPerimakeys(e.getAccountBookEntityId(), e.getAccountSubjectId())));
            updateBalanceNextOpening(balanceSubjectListMap, accountBookPeriods.get(0), false, null);
        }
        if (balanceAuxiliaryList.size() > 0) {
            //根据辅助核算拼接分组
            Map<String, List<BalanceSubjectAuxiliaryDto>> auxiliaryAsListMap
                    = balanceAuxiliaryList.parallelStream().collect(Collectors.groupingBy(BalanceSubjectAuxiliaryDto::getKeySigns));
            //辅助核算
            updateBalanceNextOpening(auxiliaryAsListMap, accountBookPeriods.get(0), true, auxiliaryItems);
        }
    }

    /**
     * @param balanceSubjectListMap
     * @param auxiliaryItems
     * @return void
     * @Description 写入下期期初余额
     * @Author 朱小明
     * @Date 2019/8/29
     **/
    private <T extends Balance> void updateBalanceNextOpening(Map<String, List<T>> balanceSubjectListMap,
                                                              AccountBookPeriod accountBookPeriod, Boolean isAuxiliary, List<BalanceSubjectAuxiliaryItem> auxiliaryItems) {
        if (isAuxiliary) {
            balanceSubjectListMap.forEach((key, balanceSubjectList) -> {
                //如果本期和下期都有数据
                if (balanceSubjectList.size() == 2) {
                    balanceSubjectList.get(0).setOpeningBalance(balanceSubjectList.get(1).getClosingBalance());
                    balanceSubjectAuxiliaryMapper.updateById((BalanceSubjectAuxiliary) balanceSubjectList.get(0));
                } else {
                    BalanceSubjectAuxiliary balance =
                            (BalanceSubjectAuxiliary) getBalanceParam(accountBookPeriod, balanceSubjectList);
                    balanceSubjectAuxiliaryMapper.insert(balance);
                    List<BalanceSubjectAuxiliaryItem> auxiliaryItemList = auxiliaryItems.parallelStream()
                            .filter(e -> e.getBalanceAuxiliaryId().equals(balanceSubjectList.get(0).getId())).collect(Collectors.toList());
                    //写入下期辅助核算项目
                    for (BalanceSubjectAuxiliaryItem auxiliaryItem : auxiliaryItemList) {
                        auxiliaryItem.setId(null);
                        auxiliaryItem.setAccountBookId(balanceSubjectList.get(0).getAccountBookId());
                        auxiliaryItem.setAccountBookEntityId(balanceSubjectList.get(0).getAccountBookEntityId());
                        auxiliaryItem.setAccountSubjectId(balanceSubjectList.get(0).getAccountSubjectId());
                        auxiliaryItem.setPeriodYear(accountBookPeriod.getPeriodYear());
                        auxiliaryItem.setPeriodNum(accountBookPeriod.getPeriodNum());
                        auxiliaryItem.setPeriodYearNum(accountBookPeriod.getPeriodYearNum());
                        auxiliaryItem.setBalanceAuxiliaryId(balance.getId());
                        balanceSubjectAuxiliaryItemMapper.insert(auxiliaryItem);
                    }
                }
            });
        } else {
            balanceSubjectListMap.forEach((key, balanceSubjectList) -> {
                if (balanceSubjectList.size() == 2) {
                    balanceSubjectList.get(0).setOpeningBalance(balanceSubjectList.get(1).getClosingBalance());
                    balanceSubjectMapper.updateById((BalanceSubject) balanceSubjectList.get(0));
                } else {
                    BalanceSubject balance = (BalanceSubject) getBalanceParam(accountBookPeriod, balanceSubjectList);
                    balanceSubjectMapper.insert(balance);
                }
            });
        }
    }

    /**
     * @param accountBookPeriod, balanceSubjectList
     * @return T
     * @Description 获取余额参数
     * @Author 朱小明
     * @Date 2019/8/29
     **/
    private <T extends Balance> T getBalanceParam(AccountBookPeriod accountBookPeriod, List<T> balanceSubjectList) {
        T balance = null;
        try {
            balance = (T) balanceSubjectList.get(0).getClass().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (balance != null) {
            balance.setAccountBookId(balanceSubjectList.get(0).getAccountBookId());
            balance.setAccountBookEntityId(balanceSubjectList.get(0).getAccountBookEntityId());
            balance.setAccountSubjectId(balanceSubjectList.get(0).getAccountSubjectId());
            balance.setPeriodYear(accountBookPeriod.getPeriodYear());
            balance.setPeriodNum(accountBookPeriod.getPeriodNum());
            balance.setPeriodYearNum(accountBookPeriod.getPeriodYearNum());
            balance.setOpeningBalance(balanceSubjectList.get(0).getClosingBalance());
        }
        return balance;
    }

    /**
     * @param accountBookPeriodDto
     * @return void
     * @Description 打开未来X期账簿
     * @Author 朱小明
     * @Date 2019/8/27
     **/
    @Override
    public void openAbPeriod(AccountBookPeriodDto accountBookPeriodDto) {
        FastUtils.checkParams(accountBookPeriodDto.getAccountBookId(), accountBookPeriodDto.getPeriodYearNum());
        accountBookPeriodDto.setFuturePeriodNum(getLedgerParam().getFuturePeriodNum());
        //查询2期会计期间
        Result<List<AccountingPeriodVo>> result = accountBookFeignClient.findAccountingPeriodForUpd(accountBookPeriodDto);
        //如果未查询到，期考查询到的数据不足
        if (result.getData() == null || result.getData().size() != accountBookPeriodDto.getFuturePeriodNum()) {
            throw new ServiceException(String.format(ResultCode.FEIGN_CONNECT_ERROR.message, FeignClientErrorMsg.GET_ACCOUNT_PERIOD), ResultCode.FEIGN_CONNECT_ERROR);
        }
        accountBookPeriodDto.setIsFuture(Constant.Is.YES);
        List<AccountBookPeriod> accountBookPeriodList = accountBookPeriodMapper.selectFuturePeriodList(accountBookPeriodDto);
        SysUserVo userVo = UserUtils.getUserVo();
        AccountBookPeriod accountBookPeriod;
        //将查询到的会计期间循环插入到账簿期间
        for (AccountingPeriodVo accountingPeriodVo : result.getData()) {
            accountBookPeriod = new AccountBookPeriod();
            FastUtils.copyProperties(accountBookPeriodDto, accountBookPeriod);
            accountBookPeriod.setStatus(Constant.Is.YES);
            accountBookPeriod.setSettleTime(null);
            accountBookPeriod.setSettleUserName(null);
            accountBookPeriod.setSettleUserId(null);
            accountBookPeriod.setCancelSettleTime(null);
            accountBookPeriod.setCancelSettleUserName(null);
            accountBookPeriod.setCancelSettleUserId(null);
            accountBookPeriod.setStartDate(accountingPeriodVo.getStartDate());
            accountBookPeriod.setEndDate(accountingPeriodVo.getEndDate());
            accountBookPeriod.setPeriodYear(accountingPeriodVo.getPeriodYear());
            accountBookPeriod.setPeriodNum(accountingPeriodVo.getPeriodNum());
            accountBookPeriod.setIsSettle(Constant.Is.NO);
            accountBookPeriod.setPeriodYearNum(accountBookPeriod.getPeriodYear() * 100 + accountBookPeriod.getPeriodNum());
            accountBookPeriod.setId(null);
            for (AccountBookPeriod accountBookPeriodz : accountBookPeriodList) {
                if (accountBookPeriodz.getPeriodYearNum().equals(accountBookPeriod.getPeriodYearNum())) {
                    accountBookPeriod.setId(accountBookPeriodz.getId());
                }
            }
            if (accountBookPeriod.getId() == null) {
                accountBookPeriod.setCreatorId(userVo.getUserId());
                accountBookPeriod.setCreatorName(userVo.getName());
                accountBookPeriod.setCreateTime(new Date());
                accountBookPeriodMapper.insert(accountBookPeriod);
            } else {
                accountBookPeriod.setUpdatorId(userVo.getUserId());
                accountBookPeriod.setUpdatorName(userVo.getName());
                accountBookPeriod.setUpdateTime(new Date());
                accountBookPeriodMapper.updateById(accountBookPeriod);
            }
        }
    }

    /**
     * @param accountBookPeriodDto
     * @return void
     * @Description 更新账簿状态
     * @Author 朱小明
     * @Date 2019/8/27
     **/
    private void updateThisAbPeriod(AccountBookPeriodDto accountBookPeriodDto) {
        SysUserVo sysUserVo = UserUtils.getUserVo();
        AccountBookPeriod accountBookPeriod = new AccountBookPeriod();
        FastUtils.copyProperties(accountBookPeriodDto, accountBookPeriod);
        // 账簿状态更新为已结账
        accountBookPeriod.setIsSettle(Constant.Is.YES);
        // 更新结账时间、结账人ID、结账人
        accountBookPeriod.setSettleUserId(sysUserVo.getUserId());
        accountBookPeriod.setSettleUserName(sysUserVo.getName());
        accountBookPeriod.setSettleTime(new Date());
        accountBookPeriodMapper.updateById(accountBookPeriod);
    }

    /**
     * @param accountBookPeriod, parameterSetVo
     * @param checkResult
     * @return void
     * @Description 损益结转处理
     * @Author 朱小明
     * @Date 2019/8/27
     **/
    private void lossProfitExcute(AccountBookPeriodDto accountBookPeriod, DelParameterSetVo parameterSetVo, CheckVoucherResult checkResult, Map<Long, Byte> balanceDirectionMap) {
        // 查询所有科目
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        accountSubjectDto.setIsFinal(Constant.Is.YES);
        accountSubjectDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        Result<List<AccountSubjectVo>> allSubjeclistResult =
                accountSubjectFeignClient.findAccountSubjectByElement(accountSubjectDto);
        if (allSubjeclistResult.getData() == null) {
            throw new ServiceException(String.format(ResultCode.FEIGN_CONNECT_ERROR.message, FeignClientErrorMsg.GET_SUBJECT_DATA_ERROR), ResultCode.FEIGN_CONNECT_ERROR);
        }
        // 拼装科目余额方向Map
        balanceDirectionMap.putAll(allSubjeclistResult.getData().parallelStream()
                .collect(Collectors.toMap(AccountSubject::getId, AccountSubject::getBalanceDirection)));
        // 过滤出损益类科目ID
        List<Long> subjectIdList = allSubjeclistResult.getData().parallelStream()
                .filter(e -> (!e.getId().equals(parameterSetVo.getLrAccSubjectId()))
                        && Constant.Is.YES.equals(e.getIsProfitAndLoss()))
                .map(AccountSubject::getId).collect(Collectors.toList());
        //查询出所有「以前年度科目的末级科目」
        //查询条件：总账参数设置的损益科目
        accountSubjectDto.setId(parameterSetVo.getSyAccSubjectId());
        //查询出总账参数设置的以前年度科目的所有末级子科目
        Result<List<AccountSubjectVo>> listResult = accountSubjectFeignClient.findAllChildInfo(accountSubjectDto);
        //如果未取到损益科目则数据异常
        if (listResult.getData() == null) {
            throw new ServiceException(String.format(ResultCode.FEIGN_CONNECT_ERROR.message, FeignClientErrorMsg.GET_SUBJECT_DATA_ERROR), ResultCode.FEIGN_CONNECT_ERROR);
        }
        //过虑出所有以前年度的科目
        List<Long> fpFinalSubjectList = listResult.getData().parallelStream().map(AccountSubjectVo::getId).collect(Collectors.toList());
        //查询出损益科目余额中带辅助核算的科目
        accountBookPeriod.setProfitLossList(subjectIdList);
        accountBookPeriod.setBeforeOrFuture(Constant.Is.NO);
        //查询出当期辅助核算余额表信息
        List<BalanceSubjectAuxiliaryItem> auxiliaryAsList =
                balanceSubjectAuxiliaryItemMapper.selectAuxiliaryIteamList(accountBookPeriod);
        //查询出当期科目余额表信息
        List<BalanceSubject> subjctAsList = balanceSubjectMapper.selectSubjctBalanceAsList(accountBookPeriod);
        //如果本期存在需要结转损益的科目余额
        if (subjctAsList.size() > 0) {
            //查询出有辅助核算的科目信息
            List<Long> auxiliaryItemList = auxiliaryAsList.stream().map(BalanceSubjectAuxiliaryItem::getAccountSubjectId)
                    .collect(Collectors.toList());
            // 损益结转凭证添加
            addLossAndProfitVoucher(accountBookPeriod, parameterSetVo, subjectIdList, subjctAsList,
                    auxiliaryItemList, balanceDirectionMap, fpFinalSubjectList);
            // 查询出所有损益凭证
            List<VoucherDto> lossProfitList = voucherMapper.findLossProfitIdsByAccountBookPeriod(accountBookPeriod);
            if (lossProfitList.size() > 0) {
                //余额处理
                updateBalanceByDeleteOrAddBatch(lossProfitList, accountBookPeriod, true);
            }
        }
    }

    /**
     * @param balanceSubjectListMap, balanceDirectionMap
     * @param balanceDirectionMap
     * @return void
     * @Description 科目余额表累计+期末处理
     * @Author 朱小明
     * @Date 2019/8/29
     **/
    private <T extends Balance> List<T> balanceSubjectExcute(Map<String, List<T>> balanceSubjectListMap, Map<Long, Byte> balanceDirectionMap) {
        List<T> tList = new ArrayList<>();
        balanceSubjectListMap.forEach((accountEntityBookId, balanceSubjectList) -> {
            if (Constant.BalanceDirection.DEBIT.equals(balanceDirectionMap.get(balanceSubjectList.get(0).getAccountSubjectId()))) {
                balanceSubjectList.get(0).setClosingBalance(balanceSubjectList.get(0).getOpeningBalance()
                        .add(balanceSubjectList.get(0).getDebitAmount().subtract(balanceSubjectList.get(0).getCreditAmount())));
            } else {
                balanceSubjectList.get(0).setClosingBalance(balanceSubjectList.get(0).getOpeningBalance()
                        .add(balanceSubjectList.get(0).getCreditAmount().subtract(balanceSubjectList.get(0).getDebitAmount())));
            }
            //当结账的科目有两条时，并且本次不是本年第一期，进行余额累计
            if (balanceSubjectList.size() == LedgerConstant.Settle.TWO_PERIOD
                    && (!LedgerConstant.Settle.FIRST_NUM.equals(balanceSubjectList.get(0).getPeriodNum()))) {
                balanceSubjectList.get(0).setTotalCreditAmount(balanceSubjectList.get(1).getTotalCreditAmount()
                        .add(balanceSubjectList.get(0).getCreditAmount()));
                balanceSubjectList.get(0).setTotalDebitAmount(balanceSubjectList.get(1).getTotalDebitAmount()
                        .add(balanceSubjectList.get(0).getDebitAmount()));
                balanceSubjectList.get(0).setPostTotalCreditAmount(balanceSubjectList.get(1).getPostTotalCreditAmount()
                        .add(balanceSubjectList.get(0).getPostCreditAmount()));
                balanceSubjectList.get(0).setPostTotalDebitAmount(balanceSubjectList.get(1).getPostTotalDebitAmount()
                        .add(balanceSubjectList.get(0).getPostDebitAmount()));
                balanceSubjectList.get(0).setSyTotalCreditAmount(balanceSubjectList.get(1).getSyTotalCreditAmount()
                        .add(balanceSubjectList.get(0).getSyCreditAmount()));
                balanceSubjectList.get(0).setSyTotalDebitAmount(balanceSubjectList.get(1).getSyTotalDebitAmount()
                        .add(balanceSubjectList.get(0).getSyDebitAmount()));
                balanceSubjectList.get(0).setPostSyTotalCreditAmount(balanceSubjectList.get(1).getTotalCreditAmount()
                        .add(balanceSubjectList.get(0).getPostSyCreditAmount()));
                balanceSubjectList.get(0).setPostSyTotalDebitAmount(balanceSubjectList.get(1).getTotalDebitAmount()
                        .add(balanceSubjectList.get(0).getPostSyDebitAmount()));
            } else {
                //第一期或者上期没有的科目，累计即是本期发生额
                balanceSubjectList.get(0).setTotalCreditAmount(balanceSubjectList.get(0).getCreditAmount());
                balanceSubjectList.get(0).setTotalDebitAmount(balanceSubjectList.get(0).getDebitAmount());
                balanceSubjectList.get(0).setPostTotalCreditAmount(balanceSubjectList.get(0).getPostCreditAmount());
                balanceSubjectList.get(0).setPostTotalDebitAmount(balanceSubjectList.get(0).getPostDebitAmount());
                balanceSubjectList.get(0).setSyTotalCreditAmount(balanceSubjectList.get(0).getSyCreditAmount());
                balanceSubjectList.get(0).setSyTotalDebitAmount(balanceSubjectList.get(0).getSyDebitAmount());
                balanceSubjectList.get(0).setPostSyTotalCreditAmount(balanceSubjectList.get(0).getPostSyCreditAmount());
                balanceSubjectList.get(0).setPostSyTotalDebitAmount(balanceSubjectList.get(0).getPostSyDebitAmount());
            }
            tList.add(balanceSubjectList.get(0));
        });

        return tList;
    }

    /**
     * @param accountBookPeriod,  parameterSetVo, profitSubjectIdList, lossSubjectIdList, subjctAsList, auxiliaryItemList
     * @param balanceDirectionMap
     * @param fpFinalSubjectList
     * @return void
     * @Description 损益结转凭证处理
     * @Author 朱小明
     * @Date 2019/8/29
     **/
    private void addLossAndProfitVoucher(AccountBookPeriodDto accountBookPeriod, DelParameterSetVo parameterSetVo,
                                         List<Long> subjectIdList, List<BalanceSubject> subjctAsList,
                                         List<Long> auxiliaryItemList, Map<Long, Byte> balanceDirectionMap, List<Long> fpFinalSubjectList) {
        //将所有需要结转的科目余额按照核算主体分组
        Map<Long, List<BalanceSubject>> listMap =
                subjctAsList.parallelStream().collect(Collectors.groupingBy(BalanceSubject::getAccountBookEntityId));
        //参数设置本期要有余额的核算主体
        accountBookPeriod.setAbEntitySet(listMap.keySet());
        //根据账簿期间信息查出本期凭证信息，按照核算主体进行分组
        Map<Long, List<Voucher>> vlistMap = voucherMapper.selectListByAbEntity(accountBookPeriod).parallelStream()
                .collect(Collectors.groupingBy(Voucher::getAccountBookEntityId));
        //将分组后的余额进行结算
        //建立一个三张凭证的凭证数组「0：收益凭证、1：损失凭证、2：以前年度结转凭证」，如果总账参数设置分单方式为合并，则不婾和1元素
        final Voucher[] mainVoucher = new Voucher[3];
        listMap.forEach((abEntity, abEntityBalanceList) -> {
            mainVoucher[0] = null;
            mainVoucher[1] = null;
            mainVoucher[2] = null;
            //添加凭证分录信息
            addVoucherEntryInfo(fpFinalSubjectList, accountBookPeriod, parameterSetVo, auxiliaryItemList
                    , abEntityBalanceList.parallelStream().filter(balanceSubject -> subjectIdList.contains(balanceSubject.getAccountSubjectId()))
                            .collect(Collectors.toList()), mainVoucher, balanceDirectionMap, abEntity, vlistMap);
            if (Arrays.asList(mainVoucher).parallelStream().filter(e -> e != null).map(Voucher::getId).collect(Collectors.toList()).size() == 0) {
                return;
            }
            //根据生成的损益凭证ID查询所有损益凭证分录信息
            List<VoucherEntry> voucherEntryList = voucherEntryMapper.selectList(new LambdaQueryWrapper<VoucherEntry>().in(VoucherEntry::getVoucherId,
                    Arrays.asList(mainVoucher).parallelStream().filter(e -> e != null).map(Voucher::getId).collect(Collectors.toList())));
            //将所有所有损益凭证分录信息根据主凭证分组
            Map<Long, List<VoucherEntry>> voucherEntryListMap = voucherEntryList.parallelStream().collect(Collectors.groupingBy(VoucherEntry::getVoucherId));
            //计算出凭证借贷方金额,更新主凭证
            for (Voucher voucher : Arrays.asList(mainVoucher).parallelStream().filter(e -> e != null).collect(Collectors.toList())) {
                updateVoucherAmount(voucherEntryListMap, voucher, parameterSetVo, balanceDirectionMap);
            }
        });
    }

    /**
     * @param voucherEntryListForAuxiMap
     * @param voucher
     * @param parameterSetVo
     * @param balanceDirectionMap
     * @return void
     * @Description
     * @Author 朱小明
     * @Date 2019/9/21
     **/
    private void updateVoucherAmount(Map<Long, List<VoucherEntry>> voucherEntryListForAuxiMap,
                                     Voucher voucher, DelParameterSetVo parameterSetVo, Map<Long, Byte> balanceDirectionMap) {
        //插入利润科目或分配科目并算出最终凭证金额
        VoucherEntry voucherEntryLrAndFp;
        //中转变量：贷方金额
        BigDecimal tranCreditAmount;
        //中转变量：借方金额
        BigDecimal tranDebitAmount;
        if (voucher != null && voucherEntryListForAuxiMap.get(voucher.getId()) != null) {
            //循环处理更新主凭证借贷方金额，和摘要
            for (VoucherEntry voucherEntry : voucherEntryListForAuxiMap.get(voucher.getId())) {
                voucher.setDebitAmount(voucher.getDebitAmount().add(voucherEntry.getDebitAmount()));
                voucher.setCreditAmount(voucher.getCreditAmount().add(voucherEntry.getCreditAmount()));
                voucher.setFirstAbstract(voucherEntry.getAbstractContent());
                voucherMapper.updateById(voucher);
            }
            tranCreditAmount = voucher.getCreditAmount();
            tranDebitAmount = voucher.getDebitAmount();
            //损益年度的处理
            if (LedgerConstant.Settle.VOUCHER_REMARK_FP.equals(voucher.getFirstAbstract())) {
                voucherEntryLrAndFp = buildVoucherEntity();
                voucherEntryLrAndFp.setAccountSubjectId(parameterSetVo.getFpAccSubjectId());
                voucherEntryLrAndFp.setVoucherId(voucher.getId());
                voucherEntryLrAndFp.setAbstractContent(voucher.getFirstAbstract());
                //判断出以前年度科目的余额方向
                if (Constant.BalanceDirection.CREDIT.equals
                        (balanceDirectionMap.get(parameterSetVo.getSyAccSubjectId()))) {
                    voucherEntryLrAndFp.setRowNum(LedgerConstant.ROWNUM.SORT_FOUR);
                    voucherEntryLrAndFp.setCreditAmount(tranDebitAmount.subtract(tranCreditAmount));
                    voucherEntryLrAndFp.setDebitAmount(BigDecimal.ZERO);
                    voucherEntryLrAndFp.setOriginalCreditAmount(tranDebitAmount.subtract(tranCreditAmount));
                    voucherEntryLrAndFp.setOriginalDebitAmount(BigDecimal.ZERO);
                    voucher.setCreditAmount(tranCreditAmount.add(voucherEntryLrAndFp.getCreditAmount()));
                } else {
                    voucherEntryLrAndFp.setRowNum(LedgerConstant.ROWNUM.SORT_ONE);
                    voucherEntryLrAndFp.setDebitAmount(tranCreditAmount.subtract(tranDebitAmount));
                    voucherEntryLrAndFp.setCreditAmount(BigDecimal.ZERO);
                    voucherEntryLrAndFp.setOriginalDebitAmount(tranCreditAmount.subtract(tranDebitAmount));
                    voucherEntryLrAndFp.setOriginalCreditAmount(BigDecimal.ZERO);
                    voucher.setDebitAmount(tranDebitAmount.add(voucherEntryLrAndFp.getDebitAmount()));
                }
                voucherEntryMapper.insert(voucherEntryLrAndFp);
            } else {
                //结果借方金额不0，则将科目金额结转到利润科目中
                if (BigDecimal.ZERO.compareTo(voucher.getDebitAmount()) != 0) {
                    voucherEntryLrAndFp = buildVoucherEntity();
                    voucherEntryLrAndFp.setRowNum(LedgerConstant.ROWNUM.SORT_FOUR);
                    voucherEntryLrAndFp.setVoucherId(voucher.getId());
                    voucherEntryLrAndFp.setAccountSubjectId(parameterSetVo.getLrAccSubjectId());
                    voucherEntryLrAndFp.setAbstractContent(voucher.getFirstAbstract());
                    voucherEntryLrAndFp.setCreditAmount(voucher.getDebitAmount());
                    voucherEntryLrAndFp.setOriginalCreditAmount(voucher.getDebitAmount());
                    voucherEntryMapper.insert(voucherEntryLrAndFp);
                    voucher.setCreditAmount(tranCreditAmount.add(voucherEntryLrAndFp.getCreditAmount()));
                }
                //结果贷方金额不0，则将科目金额结转到利润科目中
                if (BigDecimal.ZERO.compareTo(tranCreditAmount) != 0) {
                    voucherEntryLrAndFp = buildVoucherEntity();
                    voucherEntryLrAndFp.setRowNum(LedgerConstant.ROWNUM.SORT_ONE);
                    voucherEntryLrAndFp.setVoucherId(voucher.getId());
                    voucherEntryLrAndFp.setAccountSubjectId(parameterSetVo.getLrAccSubjectId());
                    voucherEntryLrAndFp.setAbstractContent(voucher.getFirstAbstract());
                    voucherEntryLrAndFp.setDebitAmount(tranCreditAmount);
                    voucherEntryLrAndFp.setOriginalDebitAmount(tranCreditAmount);
                    voucherEntryMapper.insert(voucherEntryLrAndFp);
                    voucher.setDebitAmount(tranDebitAmount.add(voucherEntryLrAndFp.getDebitAmount()));
                }
            }
            voucherMapper.updateById(voucher);
        }
    }

    /**
     * @param accountBookPeriod, parameterSetVo, auxiliaryItemList, abEntity, list
     * @param fpFinalSubjectList
     * @param abEntity
     * @param vlistMap
     * @return void
     * @Description 添加凭证信息
     * @Author 朱小明
     * @Date 2019/8/25
     **/
    private void addVoucherEntryInfo(List<Long> fpFinalSubjectList, AccountBookPeriodDto accountBookPeriod, DelParameterSetVo parameterSetVo,
                                     List<Long> auxiliaryItemList, List<BalanceSubject> abEntityBalanceList,
                                     Voucher[] voucher, Map<Long, Byte> balanceDirectionMap, Long abEntity, Map<Long, List<Voucher>> vlistMap) {
        //查询出当期余额表辅助核算信息
        List<BalanceSubjectAuxiliaryItem> auxiliaryItems = balanceSubjectAuxiliaryItemMapper.selectAuxiliaryIteamList(accountBookPeriod);
        //查询出当期辅助核算余额表信息
        List<BalanceSubjectAuxiliaryDto> balanceAuxiliaryList = balanceSubjectAuxiliaryMapper.selectAuxiliaryList(accountBookPeriod);
        //根据余额方向算出辅助核算余额
        balanceAuxiliaryList.parallelStream().forEach(e -> {
            if (Constant.BalanceDirection.DEBIT.equals(balanceDirectionMap.get(e.getAccountSubjectId()))) {
                e.setDebitAmount(e.getDebitAmount().subtract(e.getCreditAmount()));
                e.setCreditAmount(BigDecimal.ZERO);
            } else {
                e.setCreditAmount(e.getCreditAmount().subtract(e.getDebitAmount()));
                e.setDebitAmount(BigDecimal.ZERO);
            }
        });
        MergeUtil.mergeList(balanceAuxiliaryList, auxiliaryItems
                , (balance, item) -> balance.getId().equals(item.getBalanceAuxiliaryId())
                , (balance, items) -> balance.getBalanceSubjectAuxiliaryItems().addAll(items));
        Map<Long, List<BalanceSubjectAuxiliaryDto>> auxiliaryBalanceAsListsMap
                = balanceAuxiliaryList.parallelStream().collect(Collectors.groupingBy(BalanceSubjectAuxiliaryDto::getAccountSubjectId));
        //根据余额方向算出科目余额
        abEntityBalanceList.parallelStream().forEach(e -> {
            if (Constant.BalanceDirection.DEBIT.equals(balanceDirectionMap.get(e.getAccountSubjectId()))) {
                e.setDebitAmount(e.getDebitAmount().subtract(e.getCreditAmount()));
                e.setCreditAmount(BigDecimal.ZERO);
            } else {
                e.setCreditAmount(e.getCreditAmount().subtract(e.getDebitAmount()));
                e.setDebitAmount(BigDecimal.ZERO);
            }
        });

        //根据余额表和辅助核算余额表添加凭证
        for (BalanceSubject balanceSubject : abEntityBalanceList) {
            //科目余额为零的数据不进行结转
            if (BigDecimal.ZERO.compareTo(balanceSubject.getDebitAmount()) == 0 && BigDecimal.ZERO.compareTo(balanceSubject.getCreditAmount()) == 0) {
                continue;
            }
            //优先处理以前年度结转
            if (fpFinalSubjectList.contains(balanceSubject.getAccountSubjectId())) {
                // 有辅助核算
                if (auxiliaryItemList.contains(balanceSubject.getAccountSubjectId())) {
                    auxiliaryLossAndProfit(accountBookPeriod, voucher, parameterSetVo, auxiliaryBalanceAsListsMap, balanceSubject, abEntity, vlistMap, false);
                } else {
                    //不带辅助核算处理
                    noAuxiaryLossAndProfit(accountBookPeriod, voucher, parameterSetVo, balanceSubject, abEntity, vlistMap, false);
                }
            } else {
                // 有辅助核算
                if (auxiliaryItemList.contains(balanceSubject.getAccountSubjectId())) {
                    auxiliaryLossAndProfit(accountBookPeriod, voucher, parameterSetVo, auxiliaryBalanceAsListsMap, balanceSubject, abEntity, vlistMap, true);
                } else {
                    //不带辅助核算处理
                    noAuxiaryLossAndProfit(accountBookPeriod, voucher, parameterSetVo, balanceSubject, abEntity, vlistMap, true);
                }
            }

        }


    }

    /**
     * @param accountBookPeriod, voucher, balanceDirectionMap, parameterSetVo, voucherEntryList, balanceSubject
     * @param abEntity
     * @param vlistMap
     * @param isLossProfit
     * @return void
     * @Description 没有辅助核算的损益调整
     * @Author 朱小明
     * @Date 2019/9/5
     **/
    private void noAuxiaryLossAndProfit(AccountBookPeriodDto accountBookPeriod, Voucher[] voucher,
                                        DelParameterSetVo parameterSetVo, BalanceSubject balanceSubject,
                                        Long abEntity, Map<Long, List<Voucher>> vlistMap, boolean isLossProfit) {
        VoucherEntry voucherEntry;
        //如果贷方金额不为零
        if (BigDecimal.ZERO.compareTo(balanceSubject.getDebitAmount()) != 0) {
            //分录凭证结转贷方
            voucherEntry = buildVoucherEntity();
            // 设置分录排序
            voucherEntry.setRowNum(LedgerConstant.ROWNUM.SORT_TWO);
            // 设置分录科目ID-当前科目
            voucherEntry.setAccountSubjectId(balanceSubject.getAccountSubjectId());
            // 设置分录的借方金额
            voucherEntry.setCreditAmount(balanceSubject.getDebitAmount());
            // 设置分录的借方元币金额
            voucherEntry.setOriginalCreditAmount(balanceSubject.getDebitAmount());
            if (isLossProfit) {
                if (voucher[0] == null) {
                    voucher[0] = getMainVoucher(accountBookPeriod, parameterSetVo, abEntity, vlistMap);
                }
                voucherEntry.setVoucherId(voucher[0].getId());
                // 设置分录摘要
                voucherEntry.setAbstractContent(String.format(LedgerConstant.Settle.VOUCHER_REMARK_LP, accountBookPeriod.getPeriodYear(), accountBookPeriod.getPeriodNum()));
            } else {
                if (voucher[2] == null) {
                    voucher[2] = getMainVoucher(accountBookPeriod, parameterSetVo, abEntity, vlistMap);
                }
                voucherEntry.setVoucherId(voucher[2].getId());
                // 设置分录摘要
                voucherEntry.setAbstractContent(LedgerConstant.Settle.VOUCHER_REMARK_FP);
            }
            voucherEntryMapper.insert(voucherEntry);
        }
        //如果贷方金额不为零
        if (BigDecimal.ZERO.compareTo(balanceSubject.getCreditAmount()) != 0) {
            //分录凭证结转贷方
            voucherEntry = buildVoucherEntity();
            // 设置分录排序
            voucherEntry.setRowNum(LedgerConstant.ROWNUM.SORT_THREE);
            // 设置分录科目ID-当前科目
            voucherEntry.setAccountSubjectId(balanceSubject.getAccountSubjectId());
            // 设置分录摘要
            voucherEntry.setAbstractContent(String.format(LedgerConstant.Settle.VOUCHER_REMARK_LP, accountBookPeriod.getPeriodYear(), accountBookPeriod.getPeriodNum()));
            // 设置分录的借方金额
            voucherEntry.setDebitAmount(balanceSubject.getCreditAmount());
            // 设置分录的借方金额
            voucherEntry.setOriginalDebitAmount(balanceSubject.getCreditAmount());
            // 设置分录的贷方元币金额
            if (isLossProfit) {
                // 设置分录摘要
                voucherEntry.setAbstractContent(String.format(LedgerConstant.Settle.VOUCHER_REMARK_LP, accountBookPeriod.getPeriodYear(), accountBookPeriod.getPeriodNum()));
                if (Constant.Is.NO.equals(parameterSetVo.getCredentialType())) {
                    if (voucher[0] == null) {
                        voucher[0] = getMainVoucher(accountBookPeriod, parameterSetVo, abEntity, vlistMap);
                    }
                    voucherEntry.setVoucherId(voucher[0].getId());
                } else {
                    if (voucher[1] == null) {
                        voucher[1] = getMainVoucher(accountBookPeriod, parameterSetVo, abEntity, vlistMap);
                    }
                    voucherEntry.setVoucherId(voucher[1].getId());
                }
            } else {
                if (voucher[2] == null) {
                    voucher[2] = getMainVoucher(accountBookPeriod, parameterSetVo, abEntity, vlistMap);
                }
                voucherEntry.setVoucherId(voucher[2].getId());
                voucherEntry.setAbstractContent(LedgerConstant.Settle.VOUCHER_REMARK_FP);
            }
            //插入凭证分录
            voucherEntryMapper.insert(voucherEntry);
        }
    }

    /**
     * @param accountBookPeriod,voucher, balanceDirectionMap, parameterSetVo, auxiliaryBalanceAsListsMap, voucherEntryList, voucherEntryAuxiliaryList, balanceSubject]
     * @param auxiliaryBalanceAsListsMap
     * @param abEntity
     * @param vlistMap
     * @param isLossProfit
     * @return void
     * @Description 有辅助核算的损益调整
     * @Author 朱小明
     * @Date 2019/9/5
     **/
    private void auxiliaryLossAndProfit(AccountBookPeriodDto accountBookPeriod, Voucher[] voucher
            , DelParameterSetVo parameterSetVo, Map<Long, List<BalanceSubjectAuxiliaryDto>> auxiliaryBalanceAsListsMap
            , BalanceSubject balanceSubject, Long abEntity, Map<Long, List<Voucher>> vlistMap, boolean isLossProfit) {
        //根据辅助核算余额的iD进行分组
        List<BalanceSubjectAuxiliaryDto> auxiliaryBalanceAsList =
                auxiliaryBalanceAsListsMap.get(balanceSubject.getAccountSubjectId());
        //辅助核算贷方
        VoucherEntry voucherEntry;
        for (BalanceSubjectAuxiliaryDto auxiliaryBalance : auxiliaryBalanceAsList) {
            //如果贷方金额不为零
            if (BigDecimal.ZERO.compareTo(auxiliaryBalance.getDebitAmount()) != 0) {
                //分录凭证结转贷方结转
                voucherEntry = buildVoucherEntity();
                // 设置分录排序
                voucherEntry.setRowNum(LedgerConstant.ROWNUM.SORT_TWO);
                // 设置分录科目ID-当前科目
                voucherEntry.setAccountSubjectId(balanceSubject.getAccountSubjectId());
                // 设置分录的借方金额
                voucherEntry.setCreditAmount(auxiliaryBalance.getDebitAmount());
                // 设置分录的贷方元币金额
                voucherEntry.setOriginalCreditAmount(auxiliaryBalance.getDebitAmount());
                if (isLossProfit) {
                    if (voucher[0] == null) {
                        voucher[0] = getMainVoucher(accountBookPeriod, parameterSetVo, abEntity, vlistMap);
                    }
                    voucherEntry.setVoucherId(voucher[0].getId());
                    // 设置分录摘要
                    voucherEntry.setAbstractContent(String.format(LedgerConstant.Settle.VOUCHER_REMARK_LP, accountBookPeriod.getPeriodYear(), accountBookPeriod.getPeriodNum()));
                } else {
                    if (voucher[2] == null) {
                        voucher[2] = getMainVoucher(accountBookPeriod, parameterSetVo, abEntity, vlistMap);
                    }
                    voucherEntry.setVoucherId(voucher[2].getId());
                    // 设置分录摘要
                    voucherEntry.setAbstractContent(LedgerConstant.Settle.VOUCHER_REMARK_FP);
                }
                voucherEntryMapper.insert(voucherEntry);
                saveVoucherEntityAuxiliary(voucherEntry, auxiliaryBalance);
            }

            //如果贷方金额不为零
            if (BigDecimal.ZERO.compareTo(auxiliaryBalance.getCreditAmount()) != 0) {
                //分录凭证结转贷方
                voucherEntry = buildVoucherEntity();
                // 设置分录科目ID-当前科目
                voucherEntry.setAccountSubjectId(balanceSubject.getAccountSubjectId());
                // 设置分录排序
                voucherEntry.setRowNum(LedgerConstant.ROWNUM.SORT_THREE);
                // 设置分录的借方金额
                voucherEntry.setDebitAmount(auxiliaryBalance.getCreditAmount());
                // 设置分录的借方元币金额
                voucherEntry.setOriginalDebitAmount(auxiliaryBalance.getCreditAmount());
                //如果总账参数设置的分单方式为分开结转，那新建损失凭证
                if (isLossProfit) {
                    // 设置分录摘要
                    voucherEntry.setAbstractContent(String.format(LedgerConstant.Settle.VOUCHER_REMARK_LP, accountBookPeriod.getPeriodYear(), accountBookPeriod.getPeriodNum()));
                    if (Constant.Is.NO.equals(parameterSetVo.getCredentialType())) {
                        if (voucher[0] == null) {
                            voucher[0] = getMainVoucher(accountBookPeriod, parameterSetVo, abEntity, vlistMap);
                        }
                        voucherEntry.setVoucherId(voucher[0].getId());
                    } else {
                        if (voucher[1] == null) {
                            voucher[1] = getMainVoucher(accountBookPeriod, parameterSetVo, abEntity, vlistMap);
                        }
                        voucherEntry.setVoucherId(voucher[1].getId());
                    }
                } else {
                    if (voucher[2] == null) {
                        voucher[2] = getMainVoucher(accountBookPeriod, parameterSetVo, abEntity, vlistMap);
                    }
                    voucherEntry.setVoucherId(voucher[2].getId());
                    voucherEntry.setAbstractContent(LedgerConstant.Settle.VOUCHER_REMARK_FP);
                }
                //插入凭证分录
                voucherEntryMapper.insert(voucherEntry);
                //保存辅助核算
                saveVoucherEntityAuxiliary(voucherEntry, auxiliaryBalance);
            }
        }
    }

    /**
     * @param voucherEntry, auxiliaryBalance
     * @return void
     * @Description 保存辅助核算
     * @Author 朱小明
     * @Date 2019/9/21
     **/
    private void saveVoucherEntityAuxiliary(VoucherEntry voucherEntry, BalanceSubjectAuxiliaryDto auxiliaryBalance) {
        VoucherEntryAuxiliary voucherEntryAuxiliary;
        List<VoucherEntryAuxiliary> voucherEntryAuxiliaryList = new ArrayList<>();
        //循环插入辅助核算信息
        for (BalanceSubjectAuxiliaryItem balanceSubjectAuxiliaryItem : auxiliaryBalance.getBalanceSubjectAuxiliaryItems()) {
            //凭证分录的辅助核算
            voucherEntryAuxiliary = new VoucherEntryAuxiliary();
            voucherEntryAuxiliary.setEntryId(voucherEntry.getId());
            voucherEntryAuxiliary.setVoucherId(voucherEntry.getVoucherId());
            voucherEntryAuxiliary.setItemValueId(balanceSubjectAuxiliaryItem.getItemValueId());
            voucherEntryAuxiliary.setSourceTable(balanceSubjectAuxiliaryItem.getSourceTable());
            voucherEntryAuxiliaryList.add(voucherEntryAuxiliary);
        }
        //统一保存
        if (voucherEntryAuxiliaryList.size() > 0) {
            voucherEntryAuxiliaryService.saveBatch(voucherEntryAuxiliaryList);
        }
    }

    /**
     * @return VoucherEntry
     * @Description 添加损益凭证分录固定值
     * @Author 朱小明
     * @Date 2019/9/21
     **/
    private VoucherEntry buildVoucherEntity() {
        VoucherEntry voucherEntry = new VoucherEntry();
        voucherEntry.setCashFlowType(Constant.Is.NO);
        voucherEntry.setInteriorType(Constant.Is.NO);
        voucherEntry.setOriginalCoin(LedgerConstant.Ledger.DEFAULT_CURRENCY);
        return voucherEntry;
    }


    /**
     * @param accountBookPeriod, parameterSetVo, abEntity
     * @param vlistMap
     * @return com.njwd.entity.ledger.Voucher
     * @Description 插入一条主凭证
     * @Author 朱小明
     * @Date 2019/8/25
     **/
    private Voucher getMainVoucher(AccountBookPeriodDto accountBookPeriod, DelParameterSetVo parameterSetVo,
                                   Long abEntity, Map<Long, List<Voucher>> vlistMap) {
        Voucher voucher = buildStaicVoucher();
        voucher.setAccountBookEntityId(abEntity);
        //冗余字段 核算主体名称
        voucher.setAccountBookEntityName(vlistMap.get(abEntity).get(0).getAccountBookEntityName());
        setVoucherParam(accountBookPeriod, parameterSetVo, voucher);
        // 插入凭证表
        voucherMapper.insert(voucher);
        return voucher;
    }

    /**
     * @param accountBookPeriod, parameterSetVo, voucher
     * @return void
     * @Description 设置凭证参数
     * @Author 朱小明
     * @Date 2019/8/23
     **/
    private Voucher setVoucherParam(AccountBookPeriodDto accountBookPeriod, DelParameterSetVo parameterSetVo,
                                    Voucher voucher) {
        // 制单日期=本期最后一天
        voucher.setVoucherDate(accountBookPeriod.getEndDate());
        // 设置凭证账簿ID
        voucher.setAccountBookId(accountBookPeriod.getAccountBookId());
        // 设置期间年
        voucher.setPostingPeriodYear(accountBookPeriod.getPeriodYear());
        // 设置期间数
        voucher.setPostingPeriodNum(accountBookPeriod.getPeriodNum());
        // 设置总账凭证字
        voucher.setCredentialWord(LedgerUtils.ConvertCredentialWord(parameterSetVo.getCredentialWordType()));
        // 设置凭证号
        voucherService.generateCode(voucher);
        //贷方金额
        voucher.setCreditAmount(BigDecimal.ZERO);
        //借方金额
        voucher.setDebitAmount(BigDecimal.ZERO);
        // 冗余字段 年月
        voucher.setPeriodYearNum(accountBookPeriod.getPeriodYear() * 100 + accountBookPeriod.getPeriodNum());
        //冗余字段 账簿名称
        voucher.setAccountBookName(accountBookPeriod.getAccountBookName());
        return voucher;
    }

    /**
     * @return com.njwd.entity.ledger.Voucher
     * @Description 创建凭证实体
     * @Author 朱小明
     * @Date 2019/8/23
     **/

    private Voucher buildStaicVoucher() {
        Voucher voucher = new Voucher();
        SysUserVo sysUserVo = UserUtils.getUserVo();
        // 单据张数=0
        voucher.setBillNum(Constant.Number.ANTI_INITLIZED);
        // 来源方式=“损益凭证”
        voucher.setSourceType(LedgerConstant.SourceType.FORWARD);
        // 来源系统=“总账”
        voucher.setSourceSystem(Constant.SourceSystem.LEDGER);
        // 是否是否通过现金流量检查=“是”
        voucher.setCashCheckType(Constant.Is.YES);
        // 凭证状态=“已过账”
        voucher.setStatus(LedgerConstant.VoucherStatus.POST);
        voucher.setPostingStatus(Constant.Is.YES);
        // 审核状态=“已审核”
        voucher.setApproveStatus(Constant.Is.YES);
        // 复合状态=默认值0
        voucher.setReviewStatus(Constant.Is.NO);
        // 过账状态=“已过账”
        voucher.setPostingStatus(Constant.Is.YES);
        // 记账人、审核人、过账人=当前操作user
        voucher.setCreatorName(sysUserVo.getName());
        voucher.setCreatorId(sysUserVo.getUserId());
        voucher.setCreateTime(new Date());
        voucher.setApproverName(sysUserVo.getName());
        voucher.setApproverId(sysUserVo.getUserId());
        voucher.setApproveTime(new Date());
        voucher.setPostingUserName(sysUserVo.getName());
        voucher.setPostingUserId(sysUserVo.getUserId());
        voucher.setPostingTime(new Date());
        // 企业id
        voucher.setRootEnterpriseId(sysUserVo.getRootEnterpriseId());
        return voucher;
    }

    /**
     * @param accountBookPeriod, checkResult, voucherList, parameterSetVo
     * @return void
     * @Description 财务检查
     * @Author 朱小明
     * @Date 2019/8/22
     **/
    private CheckVoucherResult checkFinance(AccountBookPeriodDto accountBookPeriod, DelParameterSetVo parameterSetVo) {
        CheckVoucherResult checkResult = new CheckVoucherResult();
        List<Voucher> voucherList = new ArrayList<>();
        // ------财务检查开始------//
        // a.检查初始化进程
        AccountBookSystem accountBookSystem = checkInit(accountBookPeriod, checkResult);
        // b.凭证断号情况检查
        checkBroken(accountBookPeriod, checkResult);
        // c.检查凭证过账情况检查
        checkPosting(accountBookPeriod, checkResult);
        // d.检查凭证现金流量分析检查
        checkAnalysis(accountBookPeriod, checkResult, voucherList, parameterSetVo, accountBookSystem.getCashFlowEnableStatus());
        // e.检查核算主体内部往来平衡
        checkBalanced(accountBookPeriod, checkResult, voucherList);
        // f.检查账簿总账参数-损益结转设置参数是否设置完成
        checkParameterSet(checkResult, parameterSetVo);
        // ------财务检查结束------//
        return checkResult;
    }

    /**
     * @param accountBookPeriod, checkResult
     * @return void
     * @Description 断号检查
     * @Author 朱小明
     * @Date 2019/8/22
     **/
    private void checkBroken(AccountBookPeriodDto accountBookPeriod, CheckVoucherResult checkResult) {
        List<VoucherAdjust> vaList = voucherAdjustService.checkBroken(Arrays.asList(accountBookPeriod.getId()));
        if (vaList.size() == 1) {
            if (vaList.get(0).getBrokenVoucher() != null && vaList.get(0).getBrokenVoucher().size() > 0) {
                checkResult.setCutOffList(vaList.get(0).getBrokenVoucher());
                checkResult.setCheckFlag(true);
            } else {
                checkResult.setCutOffList(new ArrayList<>());
            }
        }
    }

    /**
     * @param checkResult
     * @return void
     * @Description 检查账簿总账参数-损益结转设置参数是否设置完成
     * @Author 朱小明
     * @Date 2019/8/22
     **/
    private void checkParameterSet(CheckVoucherResult checkResult, DelParameterSetVo parameterSetVo) {
        if (parameterSetVo != null) {
            AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
            List<Long> ids = new ArrayList<>();
            ids.add(parameterSetVo.getLrAccSubjectId());
            ids.add(parameterSetVo.getSyAccSubjectId());
            ids.add(parameterSetVo.getFpAccSubjectId());
            accountSubjectDto.setIds(ids);
            // 查询出所有损益科目信息
            Result<List<AccountSubjectVo>> listResult = accountSubjectFeignClient.findNamesByIds(accountSubjectDto);
            if (listResult.getData() == null) {
                throw new ServiceException(String.format(ResultCode.FEIGN_CONNECT_ERROR.message, FeignClientErrorMsg.GET_SUBJECT_DATA_ERROR), ResultCode.FEIGN_CONNECT_ERROR);
            }
            //校验科目全是未删除状态和非禁用状态
            boolean subjectStatus = listResult.getData().parallelStream().allMatch(accountSubject ->
                    (Constant.Is.YES.equals(accountSubject.getIsEnable()) && Constant.Is.NO.equals(accountSubject.getIsDel())));
            if (listResult.getData().size() == LedgerConstant.Settle.SET_SUBJECT && subjectStatus) {
                checkResult.setParameterSetStatus(Constant.Is.YES);
                Boolean isFinal = listResult.getData().parallelStream()
                        .filter(e -> !e.getId().equals(parameterSetVo.getSyAccSubjectId())).allMatch(e1 -> Constant.Is.YES.equals(e1.getIsFinal()));
                //如果是非末级
                if (isFinal) {
                    checkResult.setIsFinal(Constant.Is.YES);
                } else {
                    checkResult.setIsFinal(Constant.Is.NO);
                    checkResult.setCheckFlag(true);
                }
            } else {
                checkResult.setParameterSetStatus(Constant.Is.NO);
                checkResult.setCheckFlag(true);
            }
        } else {
            checkResult.setParameterSetStatus(Constant.Is.NO);
            checkResult.setCheckFlag(true);
        }
    }

    /**
     * @return com.njwd.entity.ledger.vo.ParameterSetVo
     * @Description 获取总账参数
     * @Author 朱小明
     * @Date 2019/8/27
     **/
    private DelParameterSetVo getLedgerParam() {
        DelParameterSetDto parameterSetDto = new DelParameterSetDto();
        parameterSetDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        // return parameterSetService.findParameterSet(parameterSetDto);
        return null;
    }

    /**
     * @param accountBookPeriod, checkResult, voucherList
     * @return void
     * @Description 检查内部往来平衡
     * @Author 朱小明
     * @Date 2019/8/22
     **/
    private void checkBalanced(AccountBookPeriodDto accountBookPeriod, CheckVoucherResult checkResult,
                               List<Voucher> voucherList) {
        AccountBookDto abd = new AccountBookDto();
        abd.setId(accountBookPeriod.getAccountBookId());
        Result<CompanyVo> companyVoResult = companyFeignClient.checkHasSubAccount(abd);
        if (companyVoResult.getData() == null) {
            throw new ServiceException(String.format(ResultCode.FEIGN_CONNECT_ERROR.message, FeignClientErrorMsg.GET_COMPANY_DATA_ERROR), ResultCode.FEIGN_CONNECT_ERROR);
        }
        // 如果未启用分账核算返回 -1
        accountBookPeriod.setHasSubAccount(companyVoResult.getData().getHasSubAccount());
        if (Constant.Is.NO.equals(companyVoResult.getData().getHasSubAccount())) {
            checkResult.setNotBalancedStatus(Constant.BalancedStatus.NEEDLESS);
        } else {
            if (voucherList.stream().map(Voucher::getDebitAmount).reduce(BigDecimal.ZERO, BigDecimal::add).equals(
                    voucherList.stream().map(Voucher::getDebitAmount).reduce(BigDecimal.ZERO, BigDecimal::add))) {
                // 启用分账核算且平衡返回 1
                checkResult.setNotBalancedStatus(Constant.Is.YES);
            } else {
                // 启用分账核算且不平衡返回 0
                checkResult.setNotBalancedStatus(Constant.Is.NO);
                checkResult.setCheckFlag(true);
            }
        }
    }

    /**
     * @param accountBookPeriod,   checkResult, voucherList
     * @param cashFlowEnableStatus
     * @return void
     * @Description 检查现金流量分析
     * @Author 朱小明
     * @Date 2019/8/22
     **/
    private void checkAnalysis(AccountBookPeriodDto accountBookPeriod, CheckVoucherResult checkResult,
                               List<Voucher> voucherList, DelParameterSetVo parameterSetVo, Byte cashFlowEnableStatus) {
        FastUtils.copyProperties(getLedgerParam(), parameterSetVo);
        voucherList.addAll(voucherMapper.selectPostingVoucher(accountBookPeriod, null));
        // 将未做现金流量分析的凭证存入集合中
        if (Constant.Is.YES.equals(cashFlowEnableStatus)) {
            checkResult.setNotAnalysisList(voucherList.parallelStream()
                    .filter(voucher -> Constant.CashFlowCheckType.UNEXAMINED == voucher.getCashCheckType())
                    .collect(Collectors.toList()));
            if (checkResult.getNotAnalysisList() != null && checkResult.getNotAnalysisList().size() > 0
                    && Constant.Is.YES.equals(parameterSetVo.getIsCheckCashFlow())) {
                checkResult.setNeedNalysisStatus(Constant.Is.YES);
                checkResult.setCheckFlag(true);
            } else {
                checkResult.setNeedNalysisStatus(Constant.Is.NO);
            }
        } else {
            checkResult.setNeedNalysisStatus(Constant.CashFlowCheckType.NEEDLESS);
        }
    }

    /**
     * @param accountBookPeriod,checkResult
     * @return void
     * @Description 检查凭证过账情况
     * @Author 朱小明
     * @Date 2019/8/22
     **/
    private void checkPosting(AccountBookPeriodDto accountBookPeriod, CheckVoucherResult checkResult) {
        checkResult.setNotPostingList(voucherMapper.selectPostingVoucher(accountBookPeriod, Constant.Is.NO));
        if (checkResult.getNotPostingList().size() > 0) {
            checkResult.setCheckFlag(true);
        }
    }

    /**
     * @param accountBookPeriod
     * @param checkResult
     * @return void
     * @Description 检查初期化
     * @Author 朱小明
     * @Date 2019/8/22
     **/
    private AccountBookSystem checkInit(AccountBookPeriodDto accountBookPeriod, CheckVoucherResult checkResult) {

        AccountBookSystemDto accountBookSystemDto = new AccountBookSystemDto();
        accountBookSystemDto.setSystemSign(Constant.SystemSign.LEDGER);
        accountBookSystemDto.setAccountBookId(accountBookPeriod.getAccountBookId());
        Result<AccountBookSystem> byteResult = accountBookSystemFeignClient.findInitStatusByCondition(accountBookSystemDto);
        if (byteResult.getData() == null) {
            throw new ServiceException(String.format(ResultCode.FEIGN_CONNECT_ERROR.message, FeignClientErrorMsg.GET_ACCOUNT_SYS_INIT_DATA_ERROR), ResultCode.FEIGN_CONNECT_ERROR);
        }
        checkResult.setOpenStatus(byteResult.getData().getIsInitalized());
        if (Constant.Is.NO.equals(byteResult.getData().getIsInitalized())) {
            checkResult.setCheckFlag(true);
        }
        return byteResult.getData();
    }

}
