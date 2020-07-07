package com.njwd.ledger.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.njwd.common.Constant;
import com.njwd.common.ExcelColumnConstant;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.basedata.dto.AccountBookEntityDto;
import com.njwd.entity.ledger.BalanceSubjectAuxiliary;
import com.njwd.entity.ledger.dto.*;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import com.njwd.entity.ledger.vo.BalanceSubjectAuxiliaryItemVo;
import com.njwd.entity.ledger.vo.BalanceSubjectAuxiliaryVo;
import com.njwd.entity.ledger.vo.BalanceSubjectVo;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.ledger.cloudclient.AccountSubjectFeignClient;
import com.njwd.ledger.mapper.BalanceSubjectAuxiliaryItemMapper;
import com.njwd.ledger.mapper.BalanceSubjectAuxiliaryMapper;
import com.njwd.ledger.service.AccountBookPeriodService;
import com.njwd.ledger.service.BalanceSubjectAuxiliaryService;
import com.njwd.ledger.service.BalanceSubjectService;
import com.njwd.service.FileService;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.MergeUtil;
import com.njwd.utils.StringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 科目辅助核算项目余额
 *
 * @author zhuzs
 * @date 2019-08-09 13:55
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BalanceSubjectAuxiliaryServiceImpl extends ServiceImpl<BalanceSubjectAuxiliaryMapper, BalanceSubjectAuxiliary> implements BalanceSubjectAuxiliaryService {
    @Resource
    private BalanceSubjectAuxiliaryMapper balanceSubjectAuxiliaryMapper;
    @Resource
    private BalanceSubjectAuxiliaryItemMapper balanceSubjectAuxiliaryItemMapper;

    @Resource
    private AccountSubjectFeignClient accountSubjectFeignClient;

    @Resource
    private AccountBookPeriodService accountBookPeriodService;

    @Autowired
    private FileService fileService;

    @Autowired
    private BalanceSubjectService balanceSubjectService;

    @Override
    public void updateBatch(Collection<BalanceSubjectAuxiliaryDto> balanceSubjectAuxiliaries, VoucherDto voucherDto, byte updateType) {
        balanceSubjectAuxiliaryMapper.updateBatch(balanceSubjectAuxiliaries, voucherDto, updateType);
    }

    /**
     * 重分类 包含未过账
     *
     * @param accountSubjectVoList
     * @return
     */
    @Override
    public BalanceSubjectVo findBySubjectIdList(List<AccountSubjectVo> accountSubjectVoList, BalanceDto balanceDto) {
        return balanceSubjectAuxiliaryMapper.findBySubjectIdList(accountSubjectVoList, balanceDto);
    }

    /**
     * 重分类 不包含未过账
     *
     * @param accountSubjectVoList
     * @return
     */
    @Override
    public BalanceSubjectVo findPostingBySubjectIdList(List<AccountSubjectVo> accountSubjectVoList, BalanceDto balanceDto) {
        return balanceSubjectAuxiliaryMapper.findPostingBySubjectIdList(accountSubjectVoList, balanceDto);
    }

    /**
     * 根据条件统计辅助核算余额表
     *
     * @param balanceSubjectQueryDto
     * @return BalanceSubjectVo
     * @author: 周鹏
     * @create: 2019/8/16
     */
    @Override
    public List<BalanceSubjectAuxiliaryVo> findListByParam(BalanceSubjectAuxiliaryItemQueryDto balanceSubjectQueryDto) {
        //最终结果集
        List<BalanceSubjectAuxiliaryVo> finalResultList = new LinkedList<>();
        //处理结果集
        List<BalanceSubjectAuxiliaryVo> resultList = new ArrayList<>();
        //step1:查询辅助核算项数据
        List<BalanceSubjectAuxiliaryItemVo> auxiliaryItemVoList = balanceSubjectAuxiliaryItemMapper.findListByParam(balanceSubjectQueryDto);
        if (auxiliaryItemVoList.size() > 0) {
            //根据核算主体id组装信息
            MergeUtil.merge(auxiliaryItemVoList, balanceSubjectQueryDto.getAccountBookEntityList(),
                    BalanceSubjectAuxiliaryItemVo::getAccountBookEntityId, AccountBookEntityDto::getId,
                    (auxiliaryItemVo, accountBookEntity) -> {
                        auxiliaryItemVo.setAccountBookName(accountBookEntity.getAccountBookName());
                        auxiliaryItemVo.setAccountBookEntityName(accountBookEntity.getEntityName());
                    });
            //step2:查询所选会计科目区间信息
            AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
            FastUtils.copyProperties(balanceSubjectQueryDto, accountSubjectDto);
            accountSubjectDto.setIds(balanceSubjectQueryDto.getSubjectIds());
            Result<List<AccountSubjectVo>> subjectResult = accountSubjectFeignClient.findInfoForLedger(accountSubjectDto);
            List<AccountSubjectVo> subjectList = subjectResult.getData();
            //step3:根据会计科目id组装信息
            initAuxiliaryData(balanceSubjectQueryDto, auxiliaryItemVoList, subjectList, resultList);
            //step4:根据显示条件过滤数据
            initResultList(balanceSubjectQueryDto, resultList);
            //step5:排序并计算合计
            if (CollectionUtils.isNotEmpty(resultList)) {
                //排序
                resultList.stream().sorted(Comparator.comparing(BalanceSubjectAuxiliaryVo::getAccountBookId)
                        .thenComparing(BalanceSubjectAuxiliaryVo::getAccountBookEntityId)
                        .thenComparing(BalanceSubjectAuxiliaryVo::getAuxiliaryCode)
                        .thenComparing(BalanceSubjectAuxiliaryVo::getPeriodYear)
                        .thenComparing(BalanceSubjectAuxiliaryVo::getCode)).collect(Collectors.toList());
                //计算合计并返回最终结果集
                countTotalInfo(finalResultList, resultList, balanceSubjectQueryDto.getIsShowAuxiliaryCount());
            }
        }
        return finalResultList;
    }

    /**
     * 初始化
     *
     * @param balanceSubjectAuxiliaries balanceSubjectAuxiliaries
     * @param voucherDto                voucherDto
     * @author xyyxhcj@qq.com
     * @date 2019/8/14 12:18
     **/
    @Override
    public void initBatch(List<BalanceSubjectAuxiliaryDto> balanceSubjectAuxiliaries, VoucherDto voucherDto) {
        balanceSubjectAuxiliaryMapper.insertBatch(balanceSubjectAuxiliaries, voucherDto);
        for (BalanceSubjectAuxiliaryDto balanceSubjectAuxiliaryDto : balanceSubjectAuxiliaries) {
            balanceSubjectAuxiliaryItemMapper.insertBatch(balanceSubjectAuxiliaryDto.getBalanceSubjectAuxiliaryItems(), balanceSubjectAuxiliaryDto.getId(), voucherDto);
        }
    }

    /**
     * Excel 导出辅助核算余额表
     *
     * @param queryDto
     * @param response
     * @author: 周鹏
     * @create: 2019/8/29
     */
    @Override
    public void exportListExcel(BalanceSubjectAuxiliaryItemQueryDto queryDto, HttpServletResponse response) {
        List<BalanceSubjectAuxiliaryVo> list = findListByParam(queryDto);
        //是否跨年标识
        Boolean beyondYearFlag = queryDto.getPeriodYears().get(0) < queryDto.getPeriodYears().get(1);
        if (beyondYearFlag) {
            fileService.exportExcel(response, list,
                    ExcelColumnConstant.BalanceSubject.ACCOUNT_BOOK_NAME, ExcelColumnConstant.BalanceSubject.ACCOUNT_BOOK_ENTITY_NAME,
                    ExcelColumnConstant.BalanceSubject.PERIOD_YEAR, ExcelColumnConstant.BalanceSubject.AUXILIARY_CODE,
                    ExcelColumnConstant.BalanceSubject.AUXILIARY_NAME, ExcelColumnConstant.BalanceSubject.CODE,
                    ExcelColumnConstant.BalanceSubject.NAME, ExcelColumnConstant.BalanceSubject.OPENING_DIRECTION_NAME,
                    ExcelColumnConstant.BalanceSubject.OPENING_BALANCE, ExcelColumnConstant.BalanceSubject.DEBIT_AMOUNT,
                    ExcelColumnConstant.BalanceSubject.CREDIT_AMOUNT, ExcelColumnConstant.BalanceSubject.TOTAL_DEBIT_AMOUNT,
                    ExcelColumnConstant.BalanceSubject.TOTAL_CREDIT_AMOUNT, ExcelColumnConstant.BalanceSubject.CLOSING_DIRECTION_NAME,
                    ExcelColumnConstant.BalanceSubject.CLOSING_BALANCE);
        } else {
            fileService.exportExcel(response, list,
                    ExcelColumnConstant.BalanceSubject.ACCOUNT_BOOK_NAME, ExcelColumnConstant.BalanceSubject.ACCOUNT_BOOK_ENTITY_NAME,
                    ExcelColumnConstant.BalanceSubject.AUXILIARY_CODE,
                    ExcelColumnConstant.BalanceSubject.AUXILIARY_NAME, ExcelColumnConstant.BalanceSubject.CODE,
                    ExcelColumnConstant.BalanceSubject.NAME, ExcelColumnConstant.BalanceSubject.OPENING_DIRECTION_NAME,
                    ExcelColumnConstant.BalanceSubject.OPENING_BALANCE, ExcelColumnConstant.BalanceSubject.DEBIT_AMOUNT,
                    ExcelColumnConstant.BalanceSubject.CREDIT_AMOUNT, ExcelColumnConstant.BalanceSubject.TOTAL_DEBIT_AMOUNT,
                    ExcelColumnConstant.BalanceSubject.TOTAL_CREDIT_AMOUNT, ExcelColumnConstant.BalanceSubject.CLOSING_DIRECTION_NAME,
                    ExcelColumnConstant.BalanceSubject.CLOSING_BALANCE);
        }
    }

    /**
     * 根据条件过滤数据
     *
     * @param balanceSubjectQueryDto 页面查询条件
     * @param resultList             处理结果集
     */
    private void initResultList(BalanceSubjectAuxiliaryItemQueryDto balanceSubjectQueryDto, List<BalanceSubjectAuxiliaryVo> resultList) {
        if (balanceSubjectQueryDto.getShowCondition() != null) {
            List<BalanceSubjectAuxiliaryVo> removeList = new LinkedList<>();
            if (balanceSubjectQueryDto.getShowCondition().equals(LedgerConstant.ReportShowCondition.HAPPEN_NO)) {
                for (BalanceSubjectAuxiliaryVo info : resultList) {
                    if (info.getDebitAmount().compareTo(BigDecimal.ZERO) == 0 && info.getCreditAmount().compareTo(BigDecimal.ZERO) == 0) {
                        removeList.add(info);
                    }
                }
            } else if (balanceSubjectQueryDto.getShowCondition().equals(LedgerConstant.ReportShowCondition.BALANCE_NO)) {
                for (BalanceSubjectAuxiliaryVo info : resultList) {
                    if (info.getClosingBalance().compareTo(BigDecimal.ZERO) == 0) {
                        removeList.add(info);
                    }
                }
            } else if (balanceSubjectQueryDto.getShowCondition().equals(LedgerConstant.ReportShowCondition.HAPPEN_BALANCE_NO)) {
                for (BalanceSubjectAuxiliaryVo info : resultList) {
                    if (info.getDebitAmount().compareTo(BigDecimal.ZERO) == 0 && info.getCreditAmount().compareTo(BigDecimal.ZERO) == 0
                            && info.getClosingBalance().compareTo(BigDecimal.ZERO) == 0) {
                        removeList.add(info);
                    }
                }
            }
            resultList.removeAll(removeList);
        }
    }

    /**
     * 根据会计科目id组装信息
     *
     * @param balanceSubjectQueryDto 页面查询条件
     * @param auxiliaryItemVoList    辅助核算余额信息列表
     * @param subjectList            会计科目信息列表
     * @param resultList             处理结果集
     */
    private void initAuxiliaryData(BalanceSubjectAuxiliaryItemQueryDto balanceSubjectQueryDto, List<BalanceSubjectAuxiliaryItemVo> auxiliaryItemVoList,
                                   List<AccountSubjectVo> subjectList, List<BalanceSubjectAuxiliaryVo> resultList) {
        //包含未记账凭证标识
        Boolean includeUnbookedFlag = balanceSubjectQueryDto.getIsIncludeUnbooked().equals(Constant.Is.YES);
        //包含损益结转凭证标识
        Boolean includeProfitAndLossFlag = balanceSubjectQueryDto.getIsIncludeProfitAndLoss().equals(Constant.Is.YES);
        //是否跨年标识
        Boolean beyondYearFlag = balanceSubjectQueryDto.getPeriodYears().get(0) < balanceSubjectQueryDto.getPeriodYears().get(1);
        //查询所有辅助核算项信息
        List<List<Map<String, Object>>> allSourceTableList = balanceSubjectService.findAllSourceTableInfo(auxiliaryItemVoList);
        //查询所有辅助核算余额信息
        balanceSubjectService.initAuxiliaryItemQuery(balanceSubjectQueryDto, auxiliaryItemVoList);
        List<BalanceSubjectAuxiliaryVo> auxiliaryBalanceList = balanceSubjectAuxiliaryMapper.findInfoByAuxiliaryItem(balanceSubjectQueryDto);
        //循环auxiliaryItemVoList和auxiliaryBalanceList设置辅助核算余额信息auxiliaryVoList
        List<BalanceSubjectAuxiliaryVo> auxiliaryVoList = new ArrayList<>();
        setAuxiliaryVoList(auxiliaryVoList, auxiliaryItemVoList, auxiliaryBalanceList);
        //循环auxiliaryVoList和balanceSubjectQueryDto.accountBookEntityList设置账簿名称信息
        MergeUtil.merge(auxiliaryVoList, balanceSubjectQueryDto.getAccountBookEntityList(),
                auxiliaryVo -> auxiliaryVo.getAccountBookId() + Constant.Character.UNDER_LINE + auxiliaryVo.getAccountBookEntityId(),
                accountBookEntityDto -> accountBookEntityDto.getAccountBookId() + Constant.Character.UNDER_LINE + accountBookEntityDto.getId(),
                (auxiliaryVo, accountBookEntityDto) -> {
                    auxiliaryVo.setAccountBookName(accountBookEntityDto.getAccountBookName());
                    auxiliaryVo.setAccountBookEntityName(accountBookEntityDto.getEntityName());
                });
        //查询所有辅助核算余额信息对应的期间范围
        List<BalanceSubjectVo> balanceSubjectList = new ArrayList<>();
        BalanceSubjectVo param;
        for (BalanceSubjectAuxiliaryItemVo item : auxiliaryItemVoList) {
            param = new BalanceSubjectVo();
            param.setAccountBookId(item.getAccountBookId());
            param.setPeriodYear(item.getPeriodYear());
            balanceSubjectList.add(param);
        }
        List<AccountBookPeriodVo> periodAreaList = accountBookPeriodService.findPeriodAreaByYear(balanceSubjectList);
        periodAreaList = periodAreaList.stream().distinct().collect(Collectors.toList());
        //设置辅助核算余额信息的期间区间
        setPeriodInfo(balanceSubjectQueryDto, beyondYearFlag, auxiliaryVoList, periodAreaList);
        //查询auxiliaryVoList所有数据的启用期间对应的balance_auxiliary_id
        List<BalanceSubjectAuxiliaryItemVo> startAuxiliaryItemVoList = balanceSubjectAuxiliaryItemMapper.findStartIdListByParam(auxiliaryVoList);
        //循环auxiliaryVoList和startAuxiliaryItemVoList,设置balance_auxiliary_id
        setBalanceAuxiliaryId(auxiliaryVoList, startAuxiliaryItemVoList);

        //查询auxiliaryVoList所有数据启用期间的金额
        List<BalanceSubjectAuxiliaryVo> startPeriodBalanceList = balanceSubjectAuxiliaryMapper.findStartPeriodBalance(auxiliaryVoList);
        //循环auxiliaryVoList设置启用期间信息
        setStartPeriod(auxiliaryVoList, startPeriodBalanceList);

        BalanceSubjectAuxiliaryVo auxiliaryVo;
        AccountSubjectVo accountSubjectVo;
        BalanceSubjectAuxiliaryVo auxiliaryInfo;
        for (int i = 0; i < subjectList.size(); i++) {
            accountSubjectVo = subjectList.get(i);
            for (int j = 0; j < auxiliaryVoList.size(); j++) {
                auxiliaryVo = auxiliaryVoList.get(j);
                if (auxiliaryVo.getAccountSubjectId().equals(accountSubjectVo.getId())) {
                    auxiliaryInfo = new BalanceSubjectAuxiliaryVo();
                    //组装辅助核算余额信息
                    initAuxiliaryInfo(balanceSubjectQueryDto, allSourceTableList.get(j), auxiliaryVo, accountSubjectVo, auxiliaryInfo);
                    //计算金额信息
                    getBalance(auxiliaryVo, auxiliaryInfo, includeUnbookedFlag, includeProfitAndLossFlag, resultList);
                }
            }
        }
    }

    /**
     * 设置启用期间信息
     *
     * @param auxiliaryVoList        辅助核算余额信息
     * @param startPeriodBalanceList 启用期间信息
     */
    private void setStartPeriod(List<BalanceSubjectAuxiliaryVo> auxiliaryVoList, List<BalanceSubjectAuxiliaryVo> startPeriodBalanceList) {
        MergeUtil.mergeList(auxiliaryVoList, startPeriodBalanceList,
                (auxiliaryVo, startAuxiliaryVo) -> auxiliaryVo.getAuxiliaryIds().contains(startAuxiliaryVo.getId().toString()),
                (auxiliaryVo, balanceList) -> {
                    if (balanceList.size() > 0) {
                        BalanceSubjectAuxiliaryVo startPeriodBalance = new BalanceSubjectAuxiliaryVo();
                        BigDecimal openingBalance = BigDecimal.ZERO;
                        for (BalanceSubjectAuxiliaryVo item : balanceList) {
                            openingBalance = openingBalance.add(item.getOpeningBalance());
                        }
                        startPeriodBalance.setOpeningBalance(openingBalance);
                        startPeriodBalance.setPeriodYearNum(balanceList.get(0).getPeriodYearNum());
                        auxiliaryVo.setStartPeriodBalanceVo(startPeriodBalance);
                    }
                });
    }

    /**
     * 设置启用期间的balance_auxiliary_id
     *
     * @param auxiliaryVoList          辅助核算余额信息
     * @param startAuxiliaryItemVoList 所有启用期间的balance_auxiliary_id
     */
    private void setBalanceAuxiliaryId(List<BalanceSubjectAuxiliaryVo> auxiliaryVoList, List<BalanceSubjectAuxiliaryItemVo> startAuxiliaryItemVoList) {
        MergeUtil.mergeList(auxiliaryVoList, startAuxiliaryItemVoList,
                (auxiliaryVo, startAuxiliaryItemVo) -> auxiliaryVo.getAccountBookId().equals(startAuxiliaryItemVo.getAccountBookId())
                        && auxiliaryVo.getAccountBookEntityId().equals(startAuxiliaryItemVo.getAccountBookEntityId())
                        && auxiliaryVo.getAccountSubjectId().equals(startAuxiliaryItemVo.getAccountSubjectId())
                        && auxiliaryVo.getSourceTableList().contains(startAuxiliaryItemVo.getSourceTable())
                        && auxiliaryVo.getItemValueIdList().contains(startAuxiliaryItemVo.getItemValueId().toString()),
                (auxiliaryVo, balanceList) -> {
                    if (balanceList.size() > 0) {
                        StringBuilder startId = new StringBuilder();
                        List<String> auxiliaryIdList = new LinkedList<>();
                        for (BalanceSubjectAuxiliaryItemVo startVo : balanceList) {
                            if (StringUtil.isNotEmpty(startId.toString())) {
                                startId.append(",");
                            }
                            startId.append(startVo.getBalanceAuxiliaryId());
                            auxiliaryIdList.add(startVo.getBalanceAuxiliaryId().toString());
                        }
                        auxiliaryVo.setIds(startId.toString());
                        auxiliaryVo.setAuxiliaryIds(auxiliaryIdList);
                    }
                });
    }

    /**
     * 设置辅助核算余额信息的期间区间
     *
     * @param balanceSubjectQueryDto 查询条件
     * @param beyondYearFlag         是否跨年标识
     * @param auxiliaryVoList        辅助核算余额信息
     * @param periodAreaList         期间信息
     */
    private void setPeriodInfo(BalanceSubjectAuxiliaryItemQueryDto balanceSubjectQueryDto, Boolean beyondYearFlag, List<BalanceSubjectAuxiliaryVo> auxiliaryVoList, List<AccountBookPeriodVo> periodAreaList) {
        MergeUtil.mergeList(periodAreaList, auxiliaryVoList,
                periodArea -> periodArea.getAccountBookId() + Constant.Character.UNDER_LINE + periodArea.getPeriodYear(),
                auxiliaryVo -> auxiliaryVo.getAccountBookId() + Constant.Character.UNDER_LINE + auxiliaryVo.getPeriodYear(),
                (periodArea, auxiliaryList) -> {
                    if (CollectionUtils.isNotEmpty(auxiliaryList)) {
                        BalanceSubjectAuxiliaryVo balanceSubjectVo = auxiliaryList.get(0);
                        Byte beginNumber = balanceSubjectQueryDto.getPeriodNumbers().get(0);
                        Byte endNumber = balanceSubjectQueryDto.getPeriodNumbers().get(1);
                        if (beyondYearFlag) {
                            if (balanceSubjectVo.getPeriodYear().equals(balanceSubjectQueryDto.getPeriodYears().get(0))) {
                                beginNumber = balanceSubjectQueryDto.getPeriodNumbers().get(0);
                                endNumber = periodArea.getEndNumber();
                            } else if (balanceSubjectVo.getPeriodYear().equals(balanceSubjectQueryDto.getPeriodYears().get(1))) {
                                beginNumber = periodArea.getBeginNumber();
                                endNumber = balanceSubjectQueryDto.getPeriodNumbers().get(1);
                            } else {
                                beginNumber = periodArea.getBeginNumber();
                                endNumber = periodArea.getEndNumber();
                            }
                        }
                        //设置查询开始期间后最近的已结账期间和结束期间前最近的已结账期间的参数信息
                        AccountBookPeriodDto accountBookPeriodDto = new AccountBookPeriodDto();
                        accountBookPeriodDto.setAccountBookId(balanceSubjectVo.getAccountBookId());
                        accountBookPeriodDto.setPeriodYear(balanceSubjectVo.getPeriodYear());
                        accountBookPeriodDto.setIsSettle(Constant.Is.YES);
                        accountBookPeriodDto.setPeriodNum(beginNumber);
                        accountBookPeriodDto.setType(LedgerConstant.FindPeriodType.BEGIN_PERIOD);
                        AccountBookPeriodVo beginSettledPeriodVo = accountBookPeriodService.findPeriodByAccBookIdAndSystemSign(accountBookPeriodDto);
                        accountBookPeriodDto.setPeriodNum(endNumber);
                        accountBookPeriodDto.setType(LedgerConstant.FindPeriodType.END_PERIOD);
                        AccountBookPeriodVo endSettledPeriodVo = accountBookPeriodService.findPeriodByAccBookIdAndSystemSign(accountBookPeriodDto);
                        //查询每个账簿的启用期间
                        AccountBookPeriodVo startPeriodVo = accountBookPeriodService.findStartPeriodByAccountBook(accountBookPeriodDto);
                        for (BalanceSubjectAuxiliaryVo balanceVo : auxiliaryList) {
                            //设置当前数据的期间区间
                            balanceVo.setBeginNumber(beginNumber);
                            balanceVo.setEndNumber(endNumber);
                            //设置当前数据的开始期间后最近的已结账期间和结束期间前最近的已结账期间
                            balanceVo.setBeginSettledPeriodVo(beginSettledPeriodVo);
                            balanceVo.setEndSettledPeriodVo(endSettledPeriodVo);
                            //获取当前数据对应的期间范围
                            String beginPeriodNum = String.format("%02d", beginNumber);
                            Integer beginPeriod = Integer.valueOf(balanceVo.getPeriodYear() + beginPeriodNum);
                            String endPeriodNum = String.format("%02d", endNumber);
                            Integer endPeriod = Integer.valueOf(balanceVo.getPeriodYear() + endPeriodNum);
                            Integer endSettledPeriod = 0;
                            if (endSettledPeriodVo != null) {
                                String endSettledPeriodYear = endSettledPeriodVo.getPeriodYear().toString();
                                String endSettledPeriodNum = String.format("%02d", endSettledPeriodVo.getPeriodNum());
                                endSettledPeriod = Integer.valueOf(endSettledPeriodYear + endSettledPeriodNum);
                            }
                            //设置当前数据对应的期间范围
                            balanceVo.setBeginPeriod(beginPeriod);
                            balanceVo.setEndPeriod(endPeriod);
                            balanceVo.setEndSettledPeriod(endSettledPeriod);
                            //设置账簿的启用期间
                            balanceVo.setStartPeriod(startPeriodVo.getPeriodYearNum());
                        }
                    }
                });
    }

    /**
     * 设置辅助核算余额列表信息
     *
     * @param auxiliaryVoList      组装辅助核算余额信息
     * @param auxiliaryItemVoList  辅助核算组合列表
     * @param auxiliaryBalanceList 辅助核算余额信息
     */
    private void setAuxiliaryVoList(List<BalanceSubjectAuxiliaryVo> auxiliaryVoList, List<BalanceSubjectAuxiliaryItemVo> auxiliaryItemVoList, List<BalanceSubjectAuxiliaryVo> auxiliaryBalanceList) {
        MergeUtil.mergeList(auxiliaryItemVoList, auxiliaryBalanceList,
                (auxiliaryItemVo, auxiliaryBalance) -> auxiliaryItemVo.getAuxiliaryIds().contains(auxiliaryBalance.getId().toString()),
                (auxiliaryItemVo, balanceList) -> {
                    if (balanceList.size() > 0) {
                        BalanceSubjectAuxiliaryVo auxiliaryVo = new BalanceSubjectAuxiliaryVo();
                        BigDecimal debitAmount = BigDecimal.ZERO;
                        BigDecimal creditAmount = BigDecimal.ZERO;
                        BigDecimal totalDebitAmount = BigDecimal.ZERO;
                        BigDecimal totalCreditAmount = BigDecimal.ZERO;
                        BigDecimal postDebitAmount = BigDecimal.ZERO;
                        BigDecimal postCreditAmount = BigDecimal.ZERO;
                        BigDecimal postTotalDebitAmount = BigDecimal.ZERO;
                        BigDecimal postTotalCreditAmount = BigDecimal.ZERO;
                        BigDecimal syDebitAmount = BigDecimal.ZERO;
                        BigDecimal syCreditAmount = BigDecimal.ZERO;
                        BigDecimal syTotalDebitAmount = BigDecimal.ZERO;
                        BigDecimal syTotalCreditAmount = BigDecimal.ZERO;
                        BigDecimal postSyDebitAmount = BigDecimal.ZERO;
                        BigDecimal postSyCreditAmount = BigDecimal.ZERO;
                        BigDecimal postSyTotalDebitAmount = BigDecimal.ZERO;
                        BigDecimal postSyTotalCreditAmount = BigDecimal.ZERO;
                        for (BalanceSubjectAuxiliaryVo item : balanceList) {
                            debitAmount = debitAmount.add(item.getDebitAmount());
                            creditAmount = creditAmount.add(item.getCreditAmount());
                            totalDebitAmount = totalDebitAmount.add(item.getTotalDebitAmount());
                            totalCreditAmount = totalCreditAmount.add(item.getTotalCreditAmount());
                            postDebitAmount = postDebitAmount.add(item.getPostDebitAmount());
                            postCreditAmount = postCreditAmount.add(item.getPostCreditAmount());
                            postTotalDebitAmount = postTotalDebitAmount.add(item.getPostTotalDebitAmount());
                            postTotalCreditAmount = postTotalCreditAmount.add(item.getPostTotalCreditAmount());
                            syDebitAmount = syDebitAmount.add(item.getSyDebitAmount());
                            syCreditAmount = syCreditAmount.add(item.getSyCreditAmount());
                            syTotalDebitAmount = syTotalDebitAmount.add(item.getSyTotalDebitAmount());
                            syTotalCreditAmount = syTotalCreditAmount.add(item.getSyTotalCreditAmount());
                            postSyDebitAmount = postSyDebitAmount.add(item.getPostSyDebitAmount());
                            postSyCreditAmount = postSyCreditAmount.add(item.getPostSyCreditAmount());
                            postSyTotalDebitAmount = postSyTotalDebitAmount.add(item.getPostSyTotalDebitAmount());
                            postSyTotalCreditAmount = postSyTotalCreditAmount.add(item.getPostSyTotalCreditAmount());
                        }
                        auxiliaryVo.setIds(auxiliaryItemVo.getBalanceAuxiliaryIds());
                        auxiliaryVo.setAuxiliaryIds(auxiliaryItemVo.getAuxiliaryIds());
                        auxiliaryVo.setSourceTables(auxiliaryItemVo.getSourceTables());
                        auxiliaryVo.setSourceTableList(auxiliaryItemVo.getSourceTableList());
                        auxiliaryVo.setItemValueIds(auxiliaryItemVo.getItemValueIds());
                        auxiliaryVo.setItemValueIdList(auxiliaryItemVo.getItemValueIdList());
                        auxiliaryVo.setAccountBookId(auxiliaryItemVo.getAccountBookId());
                        auxiliaryVo.setAccountBookEntityId(auxiliaryItemVo.getAccountBookEntityId());
                        auxiliaryVo.setAccountSubjectId(auxiliaryItemVo.getAccountSubjectId());
                        auxiliaryVo.setPeriodYear(auxiliaryItemVo.getPeriodYear());
                        auxiliaryVo.setDebitAmount(debitAmount);
                        auxiliaryVo.setCreditAmount(creditAmount);
                        auxiliaryVo.setTotalDebitAmount(totalDebitAmount);
                        auxiliaryVo.setTotalCreditAmount(totalCreditAmount);
                        auxiliaryVo.setPostDebitAmount(postDebitAmount);
                        auxiliaryVo.setPostCreditAmount(postCreditAmount);
                        auxiliaryVo.setPostTotalDebitAmount(postTotalDebitAmount);
                        auxiliaryVo.setPostTotalCreditAmount(postTotalCreditAmount);
                        auxiliaryVo.setSyDebitAmount(syDebitAmount);
                        auxiliaryVo.setSyCreditAmount(syCreditAmount);
                        auxiliaryVo.setSyTotalDebitAmount(syTotalDebitAmount);
                        auxiliaryVo.setSyTotalCreditAmount(syTotalCreditAmount);
                        auxiliaryVo.setPostSyDebitAmount(postSyDebitAmount);
                        auxiliaryVo.setPostSyCreditAmount(postSyCreditAmount);
                        auxiliaryVo.setPostSyTotalDebitAmount(postSyTotalDebitAmount);
                        auxiliaryVo.setPostSyTotalCreditAmount(postSyTotalCreditAmount);
                        auxiliaryVoList.add(auxiliaryVo);
                    }
                });
    }

    /**
     * 组装辅助核算余额信息
     *
     * @param balanceSubjectQueryDto 页面查询条件
     * @param sourceTableList        辅助核算项详情信息
     * @param accountSubjectVo       会计科目信息
     * @param auxiliaryInfo          辅助核算余额信息
     */
    private void initAuxiliaryInfo(BalanceSubjectAuxiliaryItemQueryDto balanceSubjectQueryDto, List<Map<String, Object>> sourceTableList,
                                   BalanceSubjectAuxiliaryVo auxiliaryVo, AccountSubjectVo accountSubjectVo, BalanceSubjectAuxiliaryVo auxiliaryInfo) {
        auxiliaryVo.setBalanceDirection(accountSubjectVo.getBalanceDirection());
        auxiliaryInfo.setAccountBookId(auxiliaryVo.getAccountBookId());
        auxiliaryInfo.setAccountBookName(auxiliaryVo.getAccountBookName());
        auxiliaryInfo.setAccountBookEntityId(auxiliaryVo.getAccountBookEntityId());
        auxiliaryInfo.setAccountBookEntityName(auxiliaryVo.getAccountBookEntityName());
        auxiliaryInfo.setAccountSubjectId(accountSubjectVo.getId());
        auxiliaryInfo.setPeriodYear(auxiliaryVo.getPeriodYear());
        auxiliaryInfo.setBalanceDirection(accountSubjectVo.getBalanceDirection());
        auxiliaryInfo.setIsEnable(accountSubjectVo.getIsEnable());
        auxiliaryInfo.setCode(accountSubjectVo.getCode());
        if (balanceSubjectQueryDto.getIsShowFullName().equals(Constant.Is.NO)) {
            auxiliaryInfo.setName(accountSubjectVo.getName());
        } else if (balanceSubjectQueryDto.getIsShowFullName().equals(Constant.Is.YES)) {
            auxiliaryInfo.setName(accountSubjectVo.getFullName());
        }
        //组装辅助核算信息
        StringBuilder sourceTable = new StringBuilder();
        StringBuilder itemValueId = new StringBuilder();
        StringBuilder auxiliaryCode = new StringBuilder();
        StringBuilder auxiliaryName = new StringBuilder();
        balanceSubjectService.initAuxiliaryNameAndCode(sourceTableList, sourceTable, itemValueId, auxiliaryCode, auxiliaryName);
        auxiliaryInfo.setSourceTable(sourceTable.toString());
        auxiliaryInfo.setItemValueIds(itemValueId.toString());
        auxiliaryInfo.setAuxiliaryCode(auxiliaryCode.toString());
        auxiliaryInfo.setAuxiliaryName(auxiliaryName.toString());
    }

    /**
     * 查询余额信息
     *
     * @param auxiliaryInfo            组装辅助核算信息
     * @param auxiliaryVo              辅助核算余额信息
     * @param includeUnbookedFlag      是否包含未过账凭证标识
     * @param includeProfitAndLossFlag 是否包含损益结转凭证标识
     * @param resultList               处理结果集
     */
    private void getBalance(BalanceSubjectAuxiliaryVo auxiliaryVo, BalanceSubjectAuxiliaryVo auxiliaryInfo,
                            Boolean includeUnbookedFlag, Boolean includeProfitAndLossFlag, List<BalanceSubjectAuxiliaryVo> resultList) {
        //查询期初和期末
        BalanceSubjectAuxiliaryItemQueryDto queryDto = new BalanceSubjectAuxiliaryItemQueryDto();
        queryDto.setAccountBookEntityId(auxiliaryVo.getAccountBookEntityId());
        queryDto.setAccountSubjectId(auxiliaryVo.getAccountSubjectId());
        queryDto.setItemValueIds(auxiliaryVo.getItemValueIds());
        BalanceSubjectAuxiliaryVo beginSubjectVo = new BalanceSubjectAuxiliaryVo();
        if (auxiliaryVo.getBeginSettledPeriodVo() != null) {
            queryDto.setPeriodYear(auxiliaryVo.getBeginSettledPeriodVo().getPeriodYear());
            queryDto.setPeriodNum(auxiliaryVo.getBeginSettledPeriodVo().getPeriodNum());
            //查询此开始期间后最近的已结账期间的期初和期末
            beginSubjectVo = balanceSubjectAuxiliaryMapper.findInfoByPeriod(queryDto);
        }
        BalanceSubjectAuxiliaryVo endSubjectVo = new BalanceSubjectAuxiliaryVo();
        if (auxiliaryVo.getEndSettledPeriodVo() != null) {
            queryDto.setPeriodYear(auxiliaryVo.getEndSettledPeriodVo().getPeriodYear());
            queryDto.setPeriodNum(auxiliaryVo.getEndSettledPeriodVo().getPeriodNum());
            //查询此结束期间前最近的已结账期间的期初和期末
            endSubjectVo = balanceSubjectAuxiliaryMapper.findInfoByPeriod(queryDto);
        }
        if (auxiliaryVo.getEndSettledPeriod() >= auxiliaryVo.getBeginPeriod() && auxiliaryVo.getEndSettledPeriod() <= auxiliaryVo.getEndPeriod()) {
            //结束期间前最近的已结账期间在当前数据所对应的会计期间内,直接设置期初余额为开始期间后最近的已结账期间的期初
            auxiliaryInfo.setOpeningBalance(beginSubjectVo == null || beginSubjectVo.getOpeningBalance() == null ? BigDecimal.ZERO : beginSubjectVo.getOpeningBalance());
        }
        //查询从结束期间前最近的已结账期间（不包含此期间）到结束期间的余额信息
        List<Integer> periodYears = new LinkedList<>();
        List<Byte> periodNumbers = new LinkedList<>();
        if (auxiliaryVo.getEndSettledPeriodVo() == null) {
            //如果此账簿未结账,则从此账簿的启用期间开始查金额
            periodYears.add(0);
            periodNumbers.add((byte) 0);
        } else {
            periodYears.add(auxiliaryVo.getEndSettledPeriodVo().getPeriodYear());
            periodNumbers.add(auxiliaryVo.getEndSettledPeriodVo().getPeriodNum());
        }
        periodYears.add(auxiliaryVo.getPeriodYear());
        periodNumbers.add(auxiliaryVo.getEndNumber());
        queryDto.setPeriodYears(periodYears);
        queryDto.setPeriodNumbers(periodNumbers);
        List<BalanceSubjectAuxiliaryVo> periodsList = balanceSubjectAuxiliaryMapper.findInfoByPeriods(queryDto);
        //根据页面查询条件计算本期借方、本期贷方、借方累计、贷方累计、期初余额和期末余额
        if (endSubjectVo == null) {
            endSubjectVo = new BalanceSubjectAuxiliaryVo();
        }
        getAuxiliaryBalance(includeUnbookedFlag, includeProfitAndLossFlag, periodsList,
                auxiliaryInfo, endSubjectVo, auxiliaryVo, resultList);
    }

    /**
     * 计算辅助核算期初余额及期末余额
     *
     * @param balanceSubjectVo 辅助核算余额结果信息
     * @param periodBalance    期间余额
     * @param subjectVo        开始期间后最近的已结账期间的余额信息
     * @param debitAmount      本期借方
     * @param creditAmount     本期贷方
     * @param index            下标，用于处理结束期间前最近的已结账期间等于开始期间前一期间的情况
     */
    private void initBalanceAuxiliaryInfo(BalanceSubjectAuxiliaryVo balanceSubjectVo, BalanceSubjectAuxiliaryVo periodBalance, BalanceSubjectAuxiliaryVo subjectVo,
                                          BigDecimal debitAmount, BigDecimal creditAmount, int index) {
        BigDecimal openingBalance = subjectVo.getClosingBalance() == null ? BigDecimal.ZERO : subjectVo.getClosingBalance();
        BigDecimal closingBalance = subjectVo.getClosingBalance() == null ? BigDecimal.ZERO : subjectVo.getClosingBalance();
        String recentPeriodYear = String.format("%02d", periodBalance.getPeriodYear());
        String recentPeriodNum = String.format("%02d", periodBalance.getPeriodNum());
        Integer recentPeriod = Integer.valueOf(recentPeriodYear + recentPeriodNum);
        //如果当前期间是启用期间的话,期初取启用期间的期初,期末取启用期间的期初
        if (balanceSubjectVo.getStartPeriodBalanceVo() != null && recentPeriod.equals(balanceSubjectVo.getStartPeriodBalanceVo().getPeriodYearNum())) {
            openingBalance = balanceSubjectVo.getStartPeriodBalanceVo().getOpeningBalance();
            closingBalance = balanceSubjectVo.getStartPeriodBalanceVo().getOpeningBalance();
            subjectVo.setOpeningBalance(openingBalance);
        }
        if (index == 1) {
            subjectVo.setOpeningBalance(openingBalance);
        }
        if (balanceSubjectVo.getBalanceDirection().equals(Constant.BalanceDirection.DEBIT)) {
            //借方
            if (recentPeriod < balanceSubjectVo.getBeginPeriod()) {
                //计算期末余额直到开始期间的前一期间作为该数据的期初
                openingBalance = openingBalance.add(debitAmount).subtract(creditAmount);
                subjectVo.setOpeningBalance(openingBalance);
            }
            //计算期末余额直到最后一个期间
            closingBalance = closingBalance.add(debitAmount).subtract(creditAmount);
        } else if (balanceSubjectVo.getBalanceDirection().equals(Constant.BalanceDirection.CREDIT)) {
            //贷方
            if (recentPeriod < balanceSubjectVo.getBeginPeriod()) {
                //计算期末余额直到开始期间的前一期间作为该数据的期初
                openingBalance = openingBalance.add(creditAmount).subtract(debitAmount);
                subjectVo.setOpeningBalance(openingBalance);
            }
            //计算期末余额直到最后一个期间
            closingBalance = closingBalance.add(creditAmount).subtract(debitAmount);
        }
        subjectVo.setClosingBalance(closingBalance);
    }

    /**
     * 根据页面查询条件计算本期借方、本期贷方、借方累计、贷方累计、期初余额和期末余额
     *
     * @param includeUnbookedFlag      是否包含未过账凭证标识
     * @param includeProfitAndLossFlag 是否包含损益结转凭证标识
     * @param periodsList              期间列表
     * @param auxiliaryInfo            辅助核算余额结果信息
     * @param endSubjectVo             开始期间后最近的已结账期间的余额信息
     * @param auxiliaryVo              辅助核算余额信息
     * @param resultList               处理结果集
     * @param startPeriodBalance       账簿会计科目的启用期间及金额信息
     */
    private void getAuxiliaryBalance(Boolean includeUnbookedFlag, Boolean includeProfitAndLossFlag, List<BalanceSubjectAuxiliaryVo> periodsList,
                                     BalanceSubjectAuxiliaryVo auxiliaryInfo, BalanceSubjectAuxiliaryVo endSubjectVo,
                                     BalanceSubjectAuxiliaryVo auxiliaryVo, List<BalanceSubjectAuxiliaryVo> resultList) {
        //通过计算期末余额的方式，从此已结账期间开始往后计算出开始期间前一个期间的期末余额,并计算结束期间的期末余额
        BigDecimal debitAmount;
        BigDecimal creditAmount;
        int index = 0;
        if (includeUnbookedFlag) {
            if (includeProfitAndLossFlag) {
                //包含未记账凭证,包含损益结转凭证
                if (CollectionUtils.isNotEmpty(periodsList)) {
                    //计算期初和期末
                    for (BalanceSubjectAuxiliaryVo periodBalance : periodsList) {
                        debitAmount = periodBalance.getDebitAmount();
                        creditAmount = periodBalance.getCreditAmount();
                        index++;
                        initBalanceAuxiliaryInfo(auxiliaryVo, periodBalance, endSubjectVo, debitAmount, creditAmount, index);
                    }
                }
                auxiliaryInfo.setDebitAmount(auxiliaryVo.getDebitAmount());
                auxiliaryInfo.setCreditAmount(auxiliaryVo.getCreditAmount());
                auxiliaryInfo.setTotalDebitAmount(auxiliaryVo.getTotalDebitAmount());
                auxiliaryInfo.setTotalCreditAmount(auxiliaryVo.getTotalCreditAmount());
            } else {
                //包含未记账凭证,不包含损益结转凭证
                if (CollectionUtils.isNotEmpty(periodsList)) {
                    //计算期初和期末
                    for (BalanceSubjectAuxiliaryVo periodBalance : periodsList) {
                        debitAmount = periodBalance.getDebitAmount().subtract(periodBalance.getSyDebitAmount());
                        creditAmount = periodBalance.getCreditAmount().subtract(periodBalance.getSyCreditAmount());
                        index++;
                        initBalanceAuxiliaryInfo(auxiliaryVo, periodBalance, endSubjectVo, debitAmount, creditAmount, index);
                    }
                }
                auxiliaryInfo.setDebitAmount(auxiliaryVo.getDebitAmount().subtract(auxiliaryVo.getSyDebitAmount()));
                auxiliaryInfo.setCreditAmount(auxiliaryVo.getCreditAmount().subtract(auxiliaryVo.getSyCreditAmount()));
                auxiliaryInfo.setTotalDebitAmount(auxiliaryVo.getTotalDebitAmount().subtract(auxiliaryVo.getSyTotalDebitAmount()));
                auxiliaryInfo.setTotalCreditAmount(auxiliaryVo.getTotalCreditAmount().subtract(auxiliaryVo.getSyTotalCreditAmount()));
            }
        }
        if (!includeUnbookedFlag) {
            if (includeProfitAndLossFlag) {
                //不包含未记账凭证,包含损益结转凭证
                if (CollectionUtils.isNotEmpty(periodsList)) {
                    //计算期初和期末
                    for (BalanceSubjectAuxiliaryVo periodBalance : periodsList) {
                        debitAmount = periodBalance.getPostDebitAmount();
                        creditAmount = periodBalance.getPostCreditAmount();
                        index++;
                        initBalanceAuxiliaryInfo(auxiliaryVo, periodBalance, endSubjectVo, debitAmount, creditAmount, index);
                    }
                }
                auxiliaryInfo.setDebitAmount(auxiliaryVo.getPostDebitAmount());
                auxiliaryInfo.setCreditAmount(auxiliaryVo.getPostCreditAmount());
                auxiliaryInfo.setTotalDebitAmount(auxiliaryVo.getPostTotalDebitAmount());
                auxiliaryInfo.setTotalCreditAmount(auxiliaryVo.getPostTotalCreditAmount());
            } else {
                //不包含未记账凭证,不包含损益结转凭证
                if (CollectionUtils.isNotEmpty(periodsList)) {
                    //计算期初和期末
                    for (BalanceSubjectAuxiliaryVo periodBalance : periodsList) {
                        debitAmount = periodBalance.getPostDebitAmount().subtract(periodBalance.getPostSyDebitAmount());
                        creditAmount = periodBalance.getPostCreditAmount().subtract(periodBalance.getPostSyCreditAmount());
                        index++;
                        initBalanceAuxiliaryInfo(auxiliaryVo, periodBalance, endSubjectVo, debitAmount, creditAmount, index);
                    }
                }
                auxiliaryInfo.setDebitAmount(auxiliaryVo.getPostDebitAmount().subtract(auxiliaryVo.getPostSyDebitAmount()));
                auxiliaryInfo.setCreditAmount(auxiliaryVo.getPostCreditAmount().subtract(auxiliaryVo.getPostSyCreditAmount()));
                auxiliaryInfo.setTotalDebitAmount(auxiliaryVo.getPostTotalDebitAmount().subtract(auxiliaryVo.getPostSyTotalDebitAmount()));
                auxiliaryInfo.setTotalCreditAmount(auxiliaryVo.getPostTotalCreditAmount().subtract(auxiliaryVo.getPostSyTotalCreditAmount()));
            }
        }
        //设置期初余额和期末余额
        if (auxiliaryVo.getEndSettledPeriod() < auxiliaryVo.getBeginPeriod()) {
            //结束期间前最近的已结账期间小于当前数据的开始期间或账簿未结账,设置期初余额为开始期间的前一期间的期末余额
            auxiliaryInfo.setOpeningBalance(endSubjectVo.getOpeningBalance() == null ? BigDecimal.ZERO : endSubjectVo.getOpeningBalance());
        }
        auxiliaryInfo.setClosingBalance(endSubjectVo.getClosingBalance() == null ? BigDecimal.ZERO : endSubjectVo.getClosingBalance());
        //根据余额设置方向
        setDirectionName(auxiliaryInfo);
        resultList.add(auxiliaryInfo);
    }

    /**
     * 根据余额设置方向
     *
     * @param auxiliaryInfo
     */
    private void setDirectionName(BalanceSubjectAuxiliaryVo auxiliaryInfo) {
        String openingDirectionName = "";
        String closingDirectionName = "";
        if (null != auxiliaryInfo.getOpeningBalance() && auxiliaryInfo.getOpeningBalance().compareTo(BigDecimal.ZERO) == 0) {
            openingDirectionName = Constant.BalanceDirectionName.FLAT;
        } else if (auxiliaryInfo.getBalanceDirection().equals(Constant.BalanceDirection.DEBIT)) {
            openingDirectionName = Constant.BalanceDirectionName.DEBIT;
        } else if (auxiliaryInfo.getBalanceDirection().equals(Constant.BalanceDirection.CREDIT)) {
            openingDirectionName = Constant.BalanceDirectionName.CREDIT;
        }
        if (null != auxiliaryInfo.getClosingBalance() && auxiliaryInfo.getClosingBalance().compareTo(BigDecimal.ZERO) == 0) {
            closingDirectionName = Constant.BalanceDirectionName.FLAT;
        } else if (auxiliaryInfo.getBalanceDirection().equals(Constant.BalanceDirection.DEBIT)) {
            closingDirectionName = Constant.BalanceDirectionName.DEBIT;
        } else if (auxiliaryInfo.getBalanceDirection().equals(Constant.BalanceDirection.CREDIT)) {
            closingDirectionName = Constant.BalanceDirectionName.CREDIT;
        }
        auxiliaryInfo.setOpeningDirectionName(openingDirectionName);
        auxiliaryInfo.setClosingDirectionName(closingDirectionName);
    }


    /**
     * 计算合计信息并返回最终结果集
     *
     * @param finalResultList      最终结果集
     * @param resultList           处理结果集
     * @param isShowAuxiliaryCount 是否显示辅助核算项小计(0:不显示 1:显示)
     */
    private void countTotalInfo(List<BalanceSubjectAuxiliaryVo> finalResultList, List<BalanceSubjectAuxiliaryVo> resultList, Byte isShowAuxiliaryCount) {
        //根据账簿id分组
        Map<String, List<BalanceSubjectAuxiliaryVo>> accountBookMap = resultList.stream().collect
                (Collectors.groupingBy(e -> e.getAccountBookId().toString()));
        //账簿合计
        BalanceSubjectAuxiliaryVo bookInfo;
        BalanceSubjectAuxiliaryVo bookTotalInfo;
        //核算主体合计
        BalanceSubjectAuxiliaryVo entityInfo;
        BalanceSubjectAuxiliaryVo entityTotalInfo;
        //辅助核算小计
        BalanceSubjectAuxiliaryVo auxiliaryInfo;
        BalanceSubjectAuxiliaryVo auxiliaryTotalInfo;
        List<BalanceSubjectAuxiliaryVo> balanceVoList;
        for (String key : accountBookMap.keySet()) {
            balanceVoList = accountBookMap.get(key);
            //计算账簿下所有合计的金额
            bookInfo = new BalanceSubjectAuxiliaryVo();
            //再根据核算主体id分组
            Map<String, List<BalanceSubjectAuxiliaryVo>> collect = balanceVoList.stream().collect
                    (Collectors.groupingBy(e -> e.getAccountBookEntityId().toString()));
            List<BalanceSubjectAuxiliaryVo> collectVoList;
            for (String item : collect.keySet()) {
                collectVoList = collect.get(item);
                //计算核算主体下所有辅助核算项的金额
                entityInfo = new BalanceSubjectAuxiliaryVo();
                //判断是否需要显示辅助核算项小计
                if (isShowAuxiliaryCount.equals(Constant.Is.YES)) {
                    //根据辅助核算项编码分组
                    Map<String, List<BalanceSubjectAuxiliaryVo>> auxiliaryCollect = collectVoList.stream().collect
                            (Collectors.groupingBy(e -> e.getAuxiliaryCode()));
                    List<BalanceSubjectAuxiliaryVo> auxiliaryList;
                    for (String param : auxiliaryCollect.keySet()) {
                        auxiliaryList = auxiliaryCollect.get(param);
                        Map<String, List<BalanceSubjectAuxiliaryVo>> auxiliaryVoCollect = auxiliaryList.stream().collect
                                (Collectors.groupingBy(e -> e.getPeriodYear().toString()));
                        List<BalanceSubjectAuxiliaryVo> auxiliaryVoList;
                        for (String auxiliaryKey : auxiliaryVoCollect.keySet()) {
                            auxiliaryVoList = auxiliaryVoCollect.get(auxiliaryKey);
                            auxiliaryInfo = new BalanceSubjectAuxiliaryVo();
                            //计算辅助核算小计的金额信息
                            countBalance(auxiliaryVoList, auxiliaryInfo, isShowAuxiliaryCount);
                            //生成一条辅助核算小计信息
                            auxiliaryTotalInfo = new BalanceSubjectAuxiliaryVo();
                            setAuxiliaryTotalInfo(auxiliaryTotalInfo, auxiliaryVoList, auxiliaryInfo);
                            finalResultList.addAll(auxiliaryVoList);
                            finalResultList.add(auxiliaryTotalInfo);
                            //计算核算主体合计金额信息
                            countBookBalance(entityInfo, auxiliaryInfo);
                        }
                    }
                } else {
                    //计算核算主体合计的金额信息
                    countBalance(collectVoList, entityInfo, isShowAuxiliaryCount);
                    finalResultList.addAll(collectVoList);
                }
                //生成一条核算主体合计信息
                entityTotalInfo = new BalanceSubjectAuxiliaryVo();
                setEntityTotalInfo(entityTotalInfo, collectVoList, entityInfo);
                finalResultList.add(entityTotalInfo);
                //计算账簿合计金额信息
                countBookBalance(bookInfo, entityInfo);
            }
            //生成一条账簿合计信息
            bookTotalInfo = new BalanceSubjectAuxiliaryVo();
            setBookTotalInfo(bookTotalInfo, balanceVoList, bookInfo);
            finalResultList.add(bookTotalInfo);
        }
    }

    /**
     * 计算辅助核算项小计和核算主体合计金额信息
     *
     * @param auxiliaryVoList      分组信息
     * @param auxiliaryInfo        合计金额
     * @param isShowAuxiliaryCount 1：计算辅助核算项小计 2：计算核算主体合计
     */
    private void countBalance(List<BalanceSubjectAuxiliaryVo> auxiliaryVoList, BalanceSubjectAuxiliaryVo auxiliaryInfo, Byte isShowAuxiliaryCount) {
        BigDecimal openingBalance = BigDecimal.ZERO;
        BigDecimal closingBalance = BigDecimal.ZERO;
        BigDecimal debitAmount = BigDecimal.ZERO;
        BigDecimal creditAmount = BigDecimal.ZERO;
        BigDecimal totalDebitAmount = BigDecimal.ZERO;
        BigDecimal totalCreditAmount = BigDecimal.ZERO;
        for (BalanceSubjectAuxiliaryVo balance : auxiliaryVoList) {
            //TODO 核算主体合计暂不计算期初和期末
            if (isShowAuxiliaryCount.equals(Constant.Is.YES)) {
                //根据余额方向计算期初和期末
                if (balance.getBalanceDirection().equals(Constant.BalanceDirection.DEBIT)) {
                    //借方
                    openingBalance = openingBalance.add(balance.getOpeningBalance() == null ? BigDecimal.ZERO : balance.getOpeningBalance());
                    closingBalance = closingBalance.add(balance.getClosingBalance() == null ? BigDecimal.ZERO : balance.getClosingBalance());
                } else {
                    //贷方
                    openingBalance = openingBalance.subtract(balance.getOpeningBalance() == null ? BigDecimal.ZERO : balance.getOpeningBalance());
                    closingBalance = closingBalance.subtract(balance.getClosingBalance() == null ? BigDecimal.ZERO : balance.getClosingBalance());
                }
            }
            debitAmount = debitAmount.add(balance.getDebitAmount() == null ? BigDecimal.ZERO : balance.getDebitAmount());
            creditAmount = creditAmount.add(balance.getCreditAmount() == null ? BigDecimal.ZERO : balance.getCreditAmount());
            totalDebitAmount = totalDebitAmount.add(balance.getTotalDebitAmount() == null ? BigDecimal.ZERO : balance.getTotalDebitAmount());
            totalCreditAmount = totalCreditAmount.add(balance.getTotalCreditAmount() == null ? BigDecimal.ZERO : balance.getTotalCreditAmount());
        }
        if (isShowAuxiliaryCount.equals(Constant.Is.YES)) {
            //根据期初和期末设置方向
            setTotalDirectionName(openingBalance, closingBalance, auxiliaryInfo);
            auxiliaryInfo.setOpeningBalance(openingBalance.compareTo(BigDecimal.ZERO) >= 0 ? openingBalance : openingBalance.negate());
            auxiliaryInfo.setClosingBalance(closingBalance.compareTo(BigDecimal.ZERO) >= 0 ? closingBalance : closingBalance.negate());
        }
        auxiliaryInfo.setDebitAmount(debitAmount);
        auxiliaryInfo.setCreditAmount(creditAmount);
        auxiliaryInfo.setTotalDebitAmount(totalDebitAmount);
        auxiliaryInfo.setTotalCreditAmount(totalCreditAmount);
    }

    /**
     * 设置合计信息的方向
     *
     * @param openingBalance 期初
     * @param closingBalance 期末
     * @param balanceInfo    合计信息
     */
    private void setTotalDirectionName(BigDecimal openingBalance, BigDecimal closingBalance, BalanceSubjectAuxiliaryVo balanceInfo) {
        if (openingBalance.compareTo(BigDecimal.ZERO) > 0) {
            balanceInfo.setOpeningDirectionName(Constant.BalanceDirectionName.DEBIT);
        } else if (openingBalance.compareTo(BigDecimal.ZERO) == 0) {
            balanceInfo.setOpeningDirectionName(Constant.BalanceDirectionName.FLAT);
        } else {
            balanceInfo.setOpeningDirectionName(Constant.BalanceDirectionName.CREDIT);
        }
        if (closingBalance.compareTo(BigDecimal.ZERO) > 0) {
            balanceInfo.setClosingDirectionName(Constant.BalanceDirectionName.DEBIT);
        } else if (closingBalance.compareTo(BigDecimal.ZERO) == 0) {
            balanceInfo.setClosingDirectionName(Constant.BalanceDirectionName.FLAT);
        } else {
            balanceInfo.setClosingDirectionName(Constant.BalanceDirectionName.CREDIT);
        }
    }

    /**
     * 计算合计信息
     *
     * @param bookBalanceInfo 合计金额
     * @param balanceInfo     辅助核算项金额
     */
    private void countBookBalance(BalanceSubjectAuxiliaryVo bookBalanceInfo, BalanceSubjectAuxiliaryVo balanceInfo) {
        //根据余额方向计算期初和期末
        /*if (balanceInfo.getOpeningDirectionName().equals(Constant.BalanceDirectionName.DEBIT)) {
            //借方
            bookBalanceInfo.setOpeningBalance(bookBalanceInfo.getOpeningBalance() == null ?
                    balanceInfo.getOpeningBalance() : bookBalanceInfo.getOpeningBalance().add(balanceInfo.getOpeningBalance()));
        } else {
            //贷方或平
            bookBalanceInfo.setOpeningBalance(bookBalanceInfo.getOpeningBalance() == null ?
                    balanceInfo.getOpeningBalance().negate() : bookBalanceInfo.getOpeningBalance().subtract(balanceInfo.getOpeningBalance()));
        }
        if (balanceInfo.getClosingDirectionName().equals(Constant.BalanceDirectionName.DEBIT)) {
            //借方
            bookBalanceInfo.setClosingBalance(bookBalanceInfo.getClosingBalance() == null ?
                    balanceInfo.getClosingBalance() : bookBalanceInfo.getClosingBalance().add(balanceInfo.getClosingBalance()));
        } else {
            //贷方或平
            bookBalanceInfo.setClosingBalance(bookBalanceInfo.getClosingBalance() == null ?
                    balanceInfo.getClosingBalance().negate() : bookBalanceInfo.getClosingBalance().subtract(balanceInfo.getClosingBalance()));
        }*/
        bookBalanceInfo.setDebitAmount(bookBalanceInfo.getDebitAmount() == null ?
                balanceInfo.getDebitAmount() : bookBalanceInfo.getDebitAmount().add(balanceInfo.getDebitAmount()));
        bookBalanceInfo.setCreditAmount(bookBalanceInfo.getCreditAmount() == null ?
                balanceInfo.getCreditAmount() : bookBalanceInfo.getCreditAmount().add(balanceInfo.getCreditAmount()));
        bookBalanceInfo.setTotalDebitAmount(bookBalanceInfo.getTotalDebitAmount() == null ?
                balanceInfo.getTotalDebitAmount() : bookBalanceInfo.getTotalDebitAmount().add(balanceInfo.getTotalDebitAmount()));
        bookBalanceInfo.setTotalCreditAmount(bookBalanceInfo.getTotalCreditAmount() == null ?
                balanceInfo.getTotalCreditAmount() : bookBalanceInfo.getTotalCreditAmount().add(balanceInfo.getTotalCreditAmount()));
    }

    /**
     * 设置辅助核算合计信息
     *
     * @param auxiliaryTotalInfo 合计信息
     * @param auxiliaryVoList    分组信息
     * @param auxiliaryInfo      辅助核算项金额
     */
    private void setAuxiliaryTotalInfo(BalanceSubjectAuxiliaryVo auxiliaryTotalInfo, List<BalanceSubjectAuxiliaryVo> auxiliaryVoList,
                                       BalanceSubjectAuxiliaryVo auxiliaryInfo) {
        auxiliaryTotalInfo.setAccountBookId(auxiliaryVoList.get(0).getAccountBookId());
        auxiliaryTotalInfo.setAccountBookName(auxiliaryVoList.get(0).getAccountBookName());
        auxiliaryTotalInfo.setAccountBookEntityId(auxiliaryVoList.get(0).getAccountBookEntityId());
        auxiliaryTotalInfo.setAccountBookEntityName(auxiliaryVoList.get(0).getAccountBookEntityName());
        auxiliaryTotalInfo.setPeriodYear(auxiliaryVoList.get(0).getPeriodYear());
        auxiliaryTotalInfo.setAuxiliaryCode(auxiliaryVoList.get(0).getAuxiliaryCode());
        auxiliaryTotalInfo.setAuxiliaryName(auxiliaryVoList.get(0).getAuxiliaryName());
        auxiliaryTotalInfo.setOpeningDirectionName(auxiliaryInfo.getOpeningDirectionName());
        auxiliaryTotalInfo.setClosingDirectionName(auxiliaryInfo.getClosingDirectionName());
        auxiliaryTotalInfo.setOpeningBalance(auxiliaryInfo.getOpeningBalance());
        auxiliaryTotalInfo.setClosingBalance(auxiliaryInfo.getClosingBalance());
        auxiliaryTotalInfo.setDebitAmount(auxiliaryInfo.getDebitAmount());
        auxiliaryTotalInfo.setCreditAmount(auxiliaryInfo.getCreditAmount());
        auxiliaryTotalInfo.setTotalDebitAmount(auxiliaryInfo.getTotalDebitAmount());
        auxiliaryTotalInfo.setTotalCreditAmount(auxiliaryInfo.getTotalCreditAmount());
        auxiliaryTotalInfo.setCode(LedgerConstant.FinancialString.SUB_TOTAL);
    }

    /**
     * 设置核算主体合计信息
     *
     * @param entityTotalInfo 合计信息
     * @param collectVoList   分组信息
     * @param entityInfo      辅助核算项金额
     */
    private void setEntityTotalInfo(BalanceSubjectAuxiliaryVo entityTotalInfo, List<BalanceSubjectAuxiliaryVo> collectVoList,
                                    BalanceSubjectAuxiliaryVo entityInfo) {
        entityTotalInfo.setAccountBookId(collectVoList.get(0).getAccountBookId());
        entityTotalInfo.setAccountBookName(collectVoList.get(0).getAccountBookName());
        entityTotalInfo.setAccountBookEntityId(collectVoList.get(0).getAccountBookEntityId());
        entityTotalInfo.setAccountBookEntityName(collectVoList.get(0).getAccountBookEntityName());
        entityTotalInfo.setAuxiliaryCode(LedgerConstant.FinancialString.TOTAL);
        //根据期初和期末设置方向
//        setTotalDirectionName(entityInfo.getOpeningBalance(), entityInfo.getClosingBalance(), entityTotalInfo);
//        entityTotalInfo.setOpeningBalance(entityInfo.getOpeningBalance().compareTo(BigDecimal.ZERO) >= 0 ?
//                entityInfo.getOpeningBalance() : entityInfo.getOpeningBalance().negate());
//        entityTotalInfo.setClosingBalance(entityInfo.getClosingBalance().compareTo(BigDecimal.ZERO) >= 0 ?
//                entityInfo.getClosingBalance() : entityInfo.getClosingBalance().negate());
        entityTotalInfo.setDebitAmount(entityInfo.getDebitAmount());
        entityTotalInfo.setCreditAmount(entityInfo.getCreditAmount());
        entityTotalInfo.setTotalDebitAmount(entityInfo.getTotalDebitAmount());
        entityTotalInfo.setTotalCreditAmount(entityInfo.getTotalCreditAmount());
    }

    /**
     * 设置账簿合计信息
     *
     * @param bookTotalInfo 合计信息
     * @param balanceVoList 分组信息
     * @param bookInfo      辅助核算项金额
     */
    private void setBookTotalInfo(BalanceSubjectAuxiliaryVo bookTotalInfo, List<BalanceSubjectAuxiliaryVo> balanceVoList,
                                  BalanceSubjectAuxiliaryVo bookInfo) {
        bookTotalInfo.setAccountBookId(balanceVoList.get(0).getAccountBookId());
        bookTotalInfo.setAccountBookName(balanceVoList.get(0).getAccountBookName());
        bookTotalInfo.setAccountBookEntityName(LedgerConstant.FinancialString.TOTAL);
        //根据期初和期末设置方向
//        setTotalDirectionName(bookInfo.getOpeningBalance(), bookInfo.getClosingBalance(), bookTotalInfo);
//        bookTotalInfo.setOpeningBalance(bookInfo.getOpeningBalance().compareTo(BigDecimal.ZERO) >= 0 ?
//                bookInfo.getOpeningBalance() : bookInfo.getOpeningBalance().negate());
//        bookTotalInfo.setClosingBalance(bookInfo.getClosingBalance().compareTo(BigDecimal.ZERO) >= 0 ?
//                bookInfo.getClosingBalance() : bookInfo.getClosingBalance().negate());
        bookTotalInfo.setDebitAmount(bookInfo.getDebitAmount());
        bookTotalInfo.setCreditAmount(bookInfo.getCreditAmount());
        bookTotalInfo.setTotalDebitAmount(bookInfo.getTotalDebitAmount());
        bookTotalInfo.setTotalCreditAmount(bookInfo.getTotalCreditAmount());
    }

}

