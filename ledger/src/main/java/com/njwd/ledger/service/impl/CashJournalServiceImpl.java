package com.njwd.ledger.service.impl;

import com.njwd.basedata.api.AccountSubjectApi;
import com.njwd.common.Constant;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.basedata.dto.AccountBookEntityDto;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.ledger.dto.BalanceSubjectCashJournalQueryDto;
import com.njwd.entity.ledger.dto.BalanceSubjectQueryDto;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import com.njwd.entity.ledger.vo.BalanceSubjectCashJournalVo;
import com.njwd.entity.ledger.vo.BalanceSubjectVo;
import com.njwd.entity.ledger.vo.VoucherEntryAuxiliaryVo;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.vo.AccountSubjectAuxiliaryVo;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.ledger.cloudclient.AccountSubjectFeignClient;
import com.njwd.ledger.mapper.AccountBookPeriodMapper;
import com.njwd.ledger.mapper.BalanceSubjectMapper;
import com.njwd.ledger.mapper.CashJournalMapper;
import com.njwd.ledger.service.CashJournalService;
import com.njwd.service.FileService;
import com.njwd.support.Result;
import com.njwd.utils.MergeUtil;
import com.njwd.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wuweiming
 * @since 2019/08/13
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CashJournalServiceImpl implements CashJournalService {
    @Resource
    private CashJournalMapper cashJournalMapper;
    @Resource
    private AccountSubjectApi accountSubjectApi;
    @Resource
    private BalanceSubjectMapper balanceSubjectMapper;
    @Resource
    private AccountBookPeriodMapper accountBookPeriodMapper;
    @Autowired
    private FileService fileService;
    @Resource
    private AccountSubjectFeignClient accountSubjectFeignClient;

    /**
     * 根据条件查询科目信息
     *
     * @param dto
     * @return Result<AccountSubjectVo>
     * @author: wuweiming
     * @create: 2019/09/18
     */
    @Override
    public AccountSubjectVo findSubjectInfoByParamWithData(BalanceSubjectCashJournalQueryDto dto) {
        AccountSubjectVo accountSubjectVo = new AccountSubjectVo();
        List<Long> itemValueIdList = new LinkedList<>();
        List<String> sourceTableList = new LinkedList<>();
        for (AccountSubjectAuxiliaryVo vo : dto.getItemValueInfos()) {
            itemValueIdList.addAll(vo.getItemValueIdList());
            sourceTableList.addAll(vo.getSourceTableList());
        }
        if (itemValueIdList != null && itemValueIdList.size() > 0) {
            dto.setItemValueIds(itemValueIdList);
        }
        if (sourceTableList != null && sourceTableList.size() > 0) {
            dto.setSourceTables(sourceTableList);
        }
        /*** 查询所有末级科目信息 ***/
        List<AccountSubjectVo> lastSubjectList = findAllChildSubjecList(dto);
        if (lastSubjectList != null && lastSubjectList.size() > 0) {
            List<Long> subjectList = new LinkedList<>();
            //遍历
            for (AccountSubjectVo vo : lastSubjectList) {
                subjectList.add(vo.getId());
            }
            //将查询的所有（去重后的）科目id添加到过滤条件中
            dto.setSubjectList(subjectList.stream().distinct().collect(Collectors.toList()));
        }

        List<Long> list = cashJournalMapper.findVocherListByParams(dto);
        if (list != null && list.size() > 0) {
            AccountSubjectDto queryDto = new AccountSubjectDto();
            queryDto.setSubjectId(dto.getSubjectId());
            queryDto.setIsFinal(dto.getIsFinal());
            queryDto.setIsIncludeEnable(dto.getIsConfigEnable());
            queryDto.setIds(list);
            queryDto.setSubjectLevels(dto.getSubjectLevels());
            Result<AccountSubjectVo> result = accountSubjectFeignClient.findSubjectInfoByParamWithCodes(queryDto);
            if (result != null && result.getData() != null) {
                accountSubjectVo = result.getData();
            }
        }
        return accountSubjectVo;
    }

    /**
     * @return com.njwd.ledger.entity.vo.BalanceSubjectCashJournalVo
     * @Description 查询 现金日记账/银行日记账
     * @Author wuweiming
     * @Data 2019/7/29 13:37
     * @Param BalanceSubjectCashJournalQueryDto
     */
    @Override
    public List<BalanceSubjectCashJournalVo> findCashJournalList(BalanceSubjectCashJournalQueryDto dto) {
        List<BalanceSubjectCashJournalVo> balanceSubjectCashJournalVoList = new LinkedList<>();
        /*** step1:查询账簿期间 & 最后结账账簿期间 ***/
        //查询账簿的（已结账已启用）最后结账账簿期间信息
        AccountBookPeriodDto abpDto = new AccountBookPeriodDto();
        abpDto.setAccountBookId(dto.getAccountBookId());
        abpDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        AccountBookPeriodVo abpVo = accountBookPeriodMapper.findLastPostingByAccountBookId(abpDto);

        //查询 区间内的期间集合
        List<AccountBookPeriodVo> accountBookPeriodList = findAccountBookPeriodList(dto, abpDto);
        if (accountBookPeriodList == null || accountBookPeriodList.size() == 0){
            //期间集合为空，则直接返回空数据
            return balanceSubjectCashJournalVoList;
        }

        /*** 2:查询所有末级科目信息 ***/
        List<AccountSubjectVo> lastSubjectList = findAllChildSubjecList(dto);
        if (lastSubjectList != null && lastSubjectList.size() > 0) {
            List<Long> subjectList = new LinkedList<>();
            //遍历
            for (AccountSubjectVo vo : lastSubjectList) {
                subjectList.add(vo.getId());
            }
            //将查询的所有（去重后的）科目id添加到过滤条件中
            dto.setSubjectList(subjectList.stream().distinct().collect(Collectors.toList()));
        }

        /*** step3:查询凭证记录 ***/
        List<BalanceSubjectCashJournalVo> vocherList = findAllVocherList(dto);

        if (vocherList == null || vocherList.size() == 0)
            return balanceSubjectCashJournalVoList;

        /*** step4:合并账簿期间与凭证 ***/
        balanceSubjectCashJournalVoList = mergeList(dto, accountBookPeriodList, vocherList, abpVo, lastSubjectList);

        return balanceSubjectCashJournalVoList;
    }

    /**
     * Excel 导出
     *
     * @param dto
     * @param response
     * @author: wuweiming
     * @create: 2019/8/29
     */
    @Override
    public void exportExcel(BalanceSubjectCashJournalQueryDto dto, HttpServletResponse response) {
        List<BalanceSubjectCashJournalVo> balanceSubjectCashJournalVoList = new LinkedList<>();
        /*** step1:查询账簿期间 & 最后结账账簿期间 ***/
        //查询账簿的（已结账已启用）最后结账账簿期间信息
        AccountBookPeriodDto abpDto = new AccountBookPeriodDto();
        abpDto.setAccountBookId(dto.getAccountBookId());
        abpDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        AccountBookPeriodVo abpVo = accountBookPeriodMapper.findLastPostingByAccountBookId(abpDto);

        //查询 区间内的期间集合
        List<AccountBookPeriodVo> accountBookPeriodList = findAccountBookPeriodList(dto, abpDto);
        if (accountBookPeriodList != null && accountBookPeriodList.size() > 0) {
            /*** 2:查询所有末级科目信息 ***/
            List<AccountSubjectVo> lastSubjectList = findAllChildSubjecList(dto);
            if (lastSubjectList != null && lastSubjectList.size() > 0) {
                List<Long> subjectList = new LinkedList<>();
                //遍历
                for (AccountSubjectVo vo : lastSubjectList) {
                    subjectList.add(vo.getId());
                }
                //将查询的所有（去重后的）科目id添加到过滤条件中
                dto.setSubjectList(subjectList.stream().distinct().collect(Collectors.toList()));
            }

            /*** step3:查询凭证记录 ***/
            List<BalanceSubjectCashJournalVo> vocherList = findAllVocherList(dto);
            if (vocherList != null && vocherList.size() > 0) {
                /*** step4:合并账簿期间与凭证 ***/
                balanceSubjectCashJournalVoList = mergeList(dto, accountBookPeriodList, vocherList, abpVo, lastSubjectList);
                /*** step5:导出 ***/
                doExportExcel(response, dto, balanceSubjectCashJournalVoList);
            } else {
                /*** 导出 ***/
                doExportExcel(response, dto, balanceSubjectCashJournalVoList);
            }
        } else {
            /*** 导出 ***/
            doExportExcel(response, dto, balanceSubjectCashJournalVoList);
        }

    }

    /**
     * Excel 导出
     *
     * @param dto
     * @param response
     * @author: wuweiming
     * @create: 2019/9/30
     */
    @Override
    public void exportExcelAll(BalanceSubjectCashJournalQueryDto dto, HttpServletResponse response) {
        List<BalanceSubjectCashJournalVo> balanceSubjectCashJournalVoList = new LinkedList<>();
        //查询科目树
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();

        /*** step1:查询账簿期间 & 最后结账账簿期间 ***/
        //查询账簿的（已结账已启用）最后结账账簿期间信息
        AccountBookPeriodDto abpDto = new AccountBookPeriodDto();
        abpDto.setAccountBookId(dto.getAccountBookId());
        abpDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        AccountBookPeriodVo abpVo = accountBookPeriodMapper.findLastPostingByAccountBookId(abpDto);

        //查询 区间内的期间集合
        List<AccountBookPeriodVo> accountBookPeriodList = findAccountBookPeriodList(dto, abpDto);

        if (accountBookPeriodList != null && accountBookPeriodList.size() > 0) {
            accountSubjectDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
            accountSubjectDto.setSubjectId(dto.getSubjectId());
            if (Constant.Is.NO.equals(dto.getIsFinal())) {
                accountSubjectDto.setLevel(dto.getSubjectLevels().get(0));
            } else {
                accountSubjectDto.setLevel(dto.getSubjectLevels().get(1));
            }
            accountSubjectDto.setSubjectCodeOperator(dto.getSubjectOperator());
            accountSubjectDto.setIsFinal(dto.getIsFinal());
            accountSubjectDto.setCodes(dto.getCodeList());

            Result<AccountSubjectVo> accountSubject = accountSubjectApi.findSubjectInfoByParam(accountSubjectDto);

            if (accountSubject != null && accountSubject.getData() != null && accountSubject.getData().getAccountSubjectList() != null) {
                for (AccountSubjectVo accountSubjectVo : accountSubject.getData().getAccountSubjectList()) {
                    dto.setAccountSubjectId(accountSubjectVo.getId());
                    dto.setSubjectName(accountSubjectVo.getName());
                    dto.setSubjectCode(accountSubjectVo.getCode());
                    dto.setBalanceDirection(accountSubjectVo.getBalanceDirection());

                    /*** 2:查询所有末级科目信息 ***/
                    List<AccountSubjectVo> lastSubjectList = findAllChildSubjecList(dto);
                    if (lastSubjectList != null && lastSubjectList.size() > 0) {
                        List<Long> subjectList = new LinkedList<>();
                        //遍历
                        for (AccountSubjectVo vo : lastSubjectList) {
                            subjectList.add(vo.getId());
                        }
                        //将查询的所有（去重后的）科目id添加到过滤条件中
                        dto.setSubjectList(subjectList.stream().distinct().collect(Collectors.toList()));
                    }

                    /*** step3:查询凭证记录 ***/
                    List<BalanceSubjectCashJournalVo> vocherList = findAllVocherList(dto);
                    if (vocherList != null && vocherList.size() > 0) {
                        /*** step4:合并账簿期间与凭证 ***/
                        balanceSubjectCashJournalVoList.addAll(mergeList(dto, accountBookPeriodList, vocherList, abpVo, lastSubjectList));
                    }
                }
            }
        }
        /*** step5:导出 ***/
        doExportExcel(response, dto, balanceSubjectCashJournalVoList);
    }

    /**
     * 导出
     * wuweiming
     *
     * @param response
     * @param list
     */
    public void doExportExcel(HttpServletResponse response, BalanceSubjectCashJournalQueryDto dto, List<BalanceSubjectCashJournalVo> list) {
        Boolean searchFlag = false;
        if (dto.getItemValueList() != null && dto.getItemValueList().size() > 0) {
            for (BalanceSubjectCashJournalQueryDto balanceSubjectCashJournalQueryDto : dto.getItemValueList()) {
                if (balanceSubjectCashJournalQueryDto.getItemValueInfos() != null && balanceSubjectCashJournalQueryDto.getItemValueInfos().size() > 0) {
                    searchFlag = true;
                }
            }
        }

        //判断辅助核算id是否为空
        if (searchFlag) {
            //导出  显示辅助核算
            fileService.exportExcel(response, list, LedgerConstant.LedgerExportName.LEDGER_CASH_JOURNAL_DETAIL,
                    new ExcelColumn("subjectCode", "科目编码"),
                    new ExcelColumn("subjectName", "科目名称"),
                    new ExcelColumn("accountBookEntityName", "核算主体"),
                    new ExcelColumn("voucherDate", "日期"),
                    new ExcelColumn("postingPeriodYear", "会计年度"),
                    new ExcelColumn("postingPeriodNum", "期间"),
                    new ExcelColumn("wordAndCod", "凭证字号"),
                    new ExcelColumn("firstAbstract", "摘要"),
                    new ExcelColumn("debitAmount", "借方"),
                    new ExcelColumn("creditAmount", "贷方"),
                    new ExcelColumn("balanceDirectionName", "方向"),
                    new ExcelColumn("balance", "余额"));
        } else {
            fileService.exportExcel(response, list, LedgerConstant.LedgerExportName.LEDGER_CASH_JOURNAL_DETAIL,
                    new ExcelColumn("subjectCode", "科目编码"),
                    new ExcelColumn("subjectName", "科目名称"),
                    new ExcelColumn("accountBookEntityName", "核算主体"),
                    new ExcelColumn("voucherDate", "日期"),
                    new ExcelColumn("postingPeriodYear", "会计年度"),
                    new ExcelColumn("postingPeriodNum", "期间"),
                    new ExcelColumn("wordAndCod", "凭证字号"),
                    new ExcelColumn("firstAbstract", "摘要"),
                    new ExcelColumn("debitAmount", "借方"),
                    new ExcelColumn("creditAmount", "贷方"),
                    new ExcelColumn("balanceDirectionName", "方向"),
                    new ExcelColumn("balance", "余额"));
        }

    }

    /**
     * 合并账簿期间与凭证
     * wuweiming
     *
     * @param dto
     * @param accountBookPeriodList
     * @param vocherList
     * @return
     */
    public List<BalanceSubjectCashJournalVo> mergeList(BalanceSubjectCashJournalQueryDto dto, List<AccountBookPeriodVo> accountBookPeriodList,
                                                       List<BalanceSubjectCashJournalVo> vocherList, AccountBookPeriodVo abpVo, List<AccountSubjectVo> lastSubjectList) {
        List<BalanceSubjectCashJournalVo> mergeList = new LinkedList<>();
        List<BalanceSubjectCashJournalVo> newList;
        Map<String, BalanceSubjectVo> map = new HashMap<>();
        //遍历核算主体
        for (AccountBookEntityDto accountBookEntity : dto.getAccountBookEntityList()) {
            //遍历账簿期间
            for (int i = 0; i < accountBookPeriodList.size(); i++) {
                //获取每期、每个核算主体的凭证
                newList = getCurrVocherList(accountBookEntity, accountBookPeriodList.get(i), vocherList);
                //当前核算主体、期间有凭证记录
                if (i == 0) {
                    //获取当前期间、核算主体的期初余额
                    getBalanceInfo(map, dto, accountBookPeriodList.get(i), accountBookEntity, abpVo);
                    mergeList.addAll(doTotal(dto, accountBookEntity, accountBookPeriodList.get(i), accountBookPeriodList.get(i), newList, map));
                } else {
                    map.get(accountBookEntity.getId() + Constant.Character.UNDER_LINE + accountBookPeriodList.get(i - 1).getPeriodYear() + Constant.Character.UNDER_LINE + accountBookPeriodList.get(i - 1).getPeriodNum());
                    mergeList.addAll(doTotal(dto, accountBookEntity, accountBookPeriodList.get(i), accountBookPeriodList.get(i - 1), newList, map));
                }
            }
        }
        return mergeList;
    }

    /**
     * 每期数据 & 本日合计 & 本期合计 & 本年累计
     *
     * @param dto
     * @param accountBookEntity
     * @param ab
     * @param lastAb
     * @param vocherList
     * @param map
     * @return
     */
    public List<BalanceSubjectCashJournalVo> doTotal(BalanceSubjectCashJournalQueryDto dto, AccountBookEntityDto accountBookEntity, AccountBookPeriodVo ab,
                                                     AccountBookPeriodVo lastAb, List<BalanceSubjectCashJournalVo> vocherList, Map<String, BalanceSubjectVo> map) {
        //0期借方金额
        BigDecimal zeroDebitAmount = BigDecimal.ZERO;
        //0期贷方金额
        BigDecimal zeroCreditAmount = BigDecimal.ZERO;
        List<BalanceSubjectCashJournalVo> list = new LinkedList<>();
        Map<String, BalanceSubjectCashJournalVo> dataMap = new HashMap<>();
        //本日合计借方金额
        BigDecimal sumDayDebitAmount = BigDecimal.ZERO;
        //本期合计借方金额
        BigDecimal sumIssueDebitAmount = BigDecimal.ZERO;
        //本年合计借方金额
        BigDecimal sumYesrDebitAmount = BigDecimal.ZERO;

        //本日合计贷方金额
        BigDecimal sumDayCreditaAmount = BigDecimal.ZERO;
        //本期合计贷方金额
        BigDecimal sumIssueCreditaAmount = BigDecimal.ZERO;
        //本年合计贷方金额
        BigDecimal sumYearCreditaAmount = BigDecimal.ZERO;

        //判断本年累计是否已存在数据
        if (map.get(accountBookEntity.getId() + Constant.Character.UNDER_LINE + ab.getPeriodYear()) != null) {
            sumYesrDebitAmount = map.get(accountBookEntity.getId() + Constant.Character.UNDER_LINE + ab.getPeriodYear()).getDebitAmount();
            sumYearCreditaAmount = map.get(accountBookEntity.getId() + Constant.Character.UNDER_LINE + ab.getPeriodYear()).getCreditAmount();
        } else {
            if (map.get(accountBookEntity.getId() + Constant.Character.UNDER_LINE + ab.getPeriodYear() + Constant.Character.UNDER_LINE_ZERO) != null &&
                    map.get(accountBookEntity.getId() + Constant.Character.UNDER_LINE + ab.getPeriodYear() + Constant.Character.UNDER_LINE_ZERO).getDebitAmount() != null) {
                zeroDebitAmount = map.get(accountBookEntity.getId() + Constant.Character.UNDER_LINE + ab.getPeriodYear() + Constant.Character.UNDER_LINE_ZERO).getDebitAmount();
            }
            if (map.get(accountBookEntity.getId() + Constant.Character.UNDER_LINE + ab.getPeriodYear() + Constant.Character.UNDER_LINE_ZERO) != null &&
                    map.get(accountBookEntity.getId() + Constant.Character.UNDER_LINE + ab.getPeriodYear() + Constant.Character.UNDER_LINE_ZERO).getCreditAmount() != null) {
                zeroCreditAmount = map.get(accountBookEntity.getId() + Constant.Character.UNDER_LINE + ab.getPeriodYear() + Constant.Character.UNDER_LINE_ZERO).getCreditAmount();
            }
        }
        //加上0期数据
        sumYesrDebitAmount = sumYesrDebitAmount.add(zeroDebitAmount);
        //加上0期数据
        sumYearCreditaAmount = sumYearCreditaAmount.add(zeroCreditAmount);

        //期初余额
        //获取期初余额
        BalanceSubjectCashJournalVo initVo = new BalanceSubjectCashJournalVo();
        initVo.setSubjectId(dto.getSubjectId());
        initVo.setSubjectName(dto.getSubjectName());
        initVo.setSubjectCode(dto.getSubjectCode());
        initVo.setAccountBookEntityId(accountBookEntity.getId());
        initVo.setAccountBookEntityName(accountBookEntity.getEntityName());
        initVo.setPostingPeriodYear(ab.getPeriodYear());
        initVo.setPostingPeriodNum(ab.getPeriodNum());
        initVo.setFirstAbstract("期初余额");
        if (map.get(accountBookEntity.getId() + Constant.Character.UNDER_LINE + ab.getPeriodYear() + Constant.Character.UNDER_LINE + ab.getPeriodNum()) != null) {
            //当前期初余额不为空
            initVo.setBalanceDirection(map.get(accountBookEntity.getId() + Constant.Character.UNDER_LINE + ab.getPeriodYear() + Constant.Character.UNDER_LINE + ab.getPeriodNum()).getBalanceDirection());
            initVo.setOpeningBalance(map.get(accountBookEntity.getId() + Constant.Character.UNDER_LINE + ab.getPeriodYear() + Constant.Character.UNDER_LINE + ab.getPeriodNum()).getOpeningBalance());
            initVo.setBalance(map.get(accountBookEntity.getId() + Constant.Character.UNDER_LINE + ab.getPeriodYear() + Constant.Character.UNDER_LINE + ab.getPeriodNum()).getOpeningBalance());
        } else if (map.get(accountBookEntity.getId() + Constant.Character.UNDER_LINE + lastAb.getPeriodYear() + Constant.Character.UNDER_LINE + lastAb.getPeriodNum()) != null) {
            //上期期末不为空
            initVo.setBalanceDirection(map.get(accountBookEntity.getId() + Constant.Character.UNDER_LINE + lastAb.getPeriodYear() + Constant.Character.UNDER_LINE + lastAb.getPeriodNum()).getBalanceDirection());
            initVo.setOpeningBalance(map.get(accountBookEntity.getId() + Constant.Character.UNDER_LINE + lastAb.getPeriodYear() + Constant.Character.UNDER_LINE + lastAb.getPeriodNum()).getOpeningBalance());
            initVo.setBalance(map.get(accountBookEntity.getId() + Constant.Character.UNDER_LINE + lastAb.getPeriodYear() + Constant.Character.UNDER_LINE + lastAb.getPeriodNum()).getClosingBalance());
        } else {
            //默认为0
            initVo.setBalance(BigDecimal.ZERO);
            initVo.setOpeningBalance(BigDecimal.ZERO);
            initVo.setBalanceDirection(dto.getBalanceDirection());
        }
        initVo.setSumBalance(initVo.getBalance());
        //将期初余额添加到list中
        list.add(initVo);

        //遍历 凭证记录
        for (BalanceSubjectCashJournalVo vo : vocherList) {
            if (dataMap.get(vo.getVoucherDate()) != null) {
                if (initVo != null && initVo.getBalanceDirection() == 0) {
                    //借方余额 = 期初余额 + 借方金额 - 贷方金额
                    vo.setBalance(initVo.getSumBalance().add(vo.getDebitAmount().subtract(vo.getCreditAmount())));
                    initVo.setSumBalance(vo.getBalance());
                } else {
                    //贷方余额 = 期初余额 + 贷方金额 - 借方金额
                    vo.setBalance(initVo.getSumBalance().add(vo.getCreditAmount().subtract(vo.getDebitAmount())));
                    initVo.setSumBalance(vo.getBalance());
                }
                list.add(vo);
                //累计
                sumDayDebitAmount = sumDayDebitAmount.add(vo.getDebitAmount());
                sumDayCreditaAmount = sumDayCreditaAmount.add(vo.getCreditAmount());
                sumIssueDebitAmount = sumIssueDebitAmount.add(vo.getDebitAmount());
                sumIssueCreditaAmount = sumIssueCreditaAmount.add(vo.getCreditAmount());
                if (vo == vocherList.get(vocherList.size() - 1)) {
                    //本日合计
                    BalanceSubjectCashJournalVo dayVo = getBalanceSubjectCashJournalVo(0, accountBookEntity, dto, ab, sumDayDebitAmount, sumDayCreditaAmount, initVo);
                    list.add(dayVo);
                    dataMap.put(vo.getVoucherDate(), vo);
                }
            } else {
                if (dataMap.size() > 0) {
                    //本日合计
                    BalanceSubjectCashJournalVo newVo = getBalanceSubjectCashJournalVo(0, accountBookEntity, dto, ab, sumDayDebitAmount, sumDayCreditaAmount, initVo);
                    list.add(newVo);
                    //清零
                    sumDayDebitAmount = BigDecimal.ZERO;
                    sumDayCreditaAmount = BigDecimal.ZERO;
                }
                if (initVo != null && initVo.getBalanceDirection() == 0) {
                    //借方余额 = 期初余额 + 借方金额 - 贷方金额
                    vo.setBalance(initVo.getSumBalance().add(vo.getDebitAmount().subtract(vo.getCreditAmount())));
                    initVo.setSumBalance(vo.getBalance());
                } else {
                    //贷方余额 = 期初余额 + 贷方金额 - 借方金额
                    vo.setBalance(initVo.getSumBalance().add(vo.getCreditAmount().subtract(vo.getDebitAmount())));
                    initVo.setSumBalance(vo.getBalance());
                }
                list.add(vo);
                sumDayDebitAmount = sumDayDebitAmount.add(vo.getDebitAmount());
                sumDayCreditaAmount = sumDayCreditaAmount.add(vo.getCreditAmount());
                sumIssueDebitAmount = sumIssueDebitAmount.add(vo.getDebitAmount());
                sumIssueCreditaAmount = sumIssueCreditaAmount.add(vo.getCreditAmount());
                dataMap.put(vo.getVoucherDate(), vo);
            }
        }
        //本期合计
        BalanceSubjectCashJournalVo newVo = getBalanceSubjectCashJournalVo(1, accountBookEntity, dto, ab, sumIssueDebitAmount, sumIssueCreditaAmount, initVo);
        //本年累计
        sumYesrDebitAmount = sumYesrDebitAmount.add(sumIssueDebitAmount);
        sumYearCreditaAmount = sumYearCreditaAmount.add(sumIssueCreditaAmount);
        BalanceSubjectCashJournalVo yearVo = getBalanceSubjectCashJournalVo(2, accountBookEntity, dto, ab, sumYesrDebitAmount, sumYearCreditaAmount, initVo);
        list.add(newVo);
        list.add(yearVo);

        //将当前期末余额存到map中
        BalanceSubjectVo bsVo = new BalanceSubjectVo();
        bsVo.setAccountBookEntityId(accountBookEntity.getId());
        bsVo.setAccountBookEntityName(accountBookEntity.getEntityName());
        bsVo.setPeriodYear(ab.getPeriodYear());
        bsVo.setPeriodNum(ab.getPeriodNum());
        bsVo.setOpeningBalance(initVo.getOpeningBalance());
        bsVo.setClosingBalance(newVo.getBalance());
        bsVo.setBalanceDirection(initVo.getBalanceDirection());
        map.put(accountBookEntity.getId() + Constant.Character.UNDER_LINE + ab.getPeriodYear() + Constant.Character.UNDER_LINE + ab.getPeriodNum(), bsVo);
        bsVo.setDebitAmount(sumYesrDebitAmount);
        bsVo.setCreditAmount(sumYearCreditaAmount);
        map.put(accountBookEntity.getId() + Constant.Character.UNDER_LINE + ab.getPeriodYear(), bsVo);
        return list;
    }

    /**
     * 处理合计
     *
     * @param type
     * @param accountBookEntity
     * @param dto
     * @param ab
     * @param debitAmount
     * @param creditaAmount
     * @param initVo
     * @return
     */
    public BalanceSubjectCashJournalVo getBalanceSubjectCashJournalVo(int type, AccountBookEntityDto accountBookEntity, BalanceSubjectCashJournalQueryDto dto,
                                                                      AccountBookPeriodVo ab, BigDecimal debitAmount, BigDecimal creditaAmount,
                                                                      BalanceSubjectCashJournalVo initVo) {
        BalanceSubjectCashJournalVo newVo = new BalanceSubjectCashJournalVo();
        newVo.setSubjectId(dto.getSubjectId());
        newVo.setSubjectName(dto.getSubjectName());
        newVo.setSubjectCode(dto.getSubjectCode());
        newVo.setAccountBookEntityId(accountBookEntity.getId());
        newVo.setAccountBookEntityName(accountBookEntity.getEntityName());
        newVo.setPostingPeriodYear(ab.getPeriodYear());
        newVo.setPostingPeriodNum(ab.getPeriodNum());
        if (debitAmount == null) {
            debitAmount = BigDecimal.ZERO;
        }
        if (creditaAmount == null) {
            creditaAmount = BigDecimal.ZERO;
        }
        newVo.setDebitAmount(debitAmount);
        newVo.setCreditAmount(creditaAmount);
        if (type == 0) {
            newVo.setFirstAbstract(LedgerConstant.Ledger.TOTAL_NAME_DAY);
        } else if (type == 1) {
            newVo.setFirstAbstract(LedgerConstant.Ledger.TOTAL_NAME_PERIOD);
        } else if (type == 2) {
            newVo.setFirstAbstract(LedgerConstant.Ledger.TOTAL_NAME_YEAR);

        }
        newVo.setBalance(initVo.getSumBalance());
        newVo.setBalanceDirection(dto.getBalanceDirection());
        return newVo;
    }

    /**
     * 获取每个期间每个核算主体的期初余额
     * wuweiming
     *
     * @param map
     * @param dto
     * @param ab
     * @param accountBookEntity
     * @return
     */
    public void getBalanceInfo(Map<String, BalanceSubjectVo> map, BalanceSubjectCashJournalQueryDto dto, AccountBookPeriodVo ab,
                               AccountBookEntityDto accountBookEntity, AccountBookPeriodVo abpVo) {
        BalanceSubjectVo vo = new BalanceSubjectVo();
        if (map.get(accountBookEntity.getId() + Constant.Character.UNDER_LINE + ab.getPeriodYear() + Constant.Character.UNDER_LINE + ab.getPeriodNum()) != null) {
            //当前期间、核算主体的期初余额不为空
            vo = map.get(accountBookEntity.getId() + Constant.Character.UNDER_LINE + ab.getPeriodYear() + Constant.Character.UNDER_LINE + ab.getPeriodNum());
        } else {
            //当前期间、核算主体的期初余额为空
            //最后结账期间不为空
            if (abpVo != null) {
                //获取每个核算主体的各期间的期初余额
                getBalanceSubjectInfo(map, dto, ab, accountBookEntity);
            } else {
                //查询第一期
                //查询账簿的第一次期间信息
                AccountBookPeriodDto abpDto = new AccountBookPeriodDto();
                abpDto.setAccountBookId(dto.getAccountBookId());
                abpDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
                AccountBookPeriodVo firstVo = accountBookPeriodMapper.findFirstPostingByAccountBookId(abpDto);
                if (firstVo != null) {
                    getBalanceSubjectInfo(map, dto, ab, accountBookEntity);
                } else {
                    //test
                    vo.setAccountBookId(dto.getAccountBookId());
                    vo.setAccountBookEntityId(accountBookEntity.getId());
                    vo.setAccountBookEntityName(accountBookEntity.getEntityName());
                    vo.setPeriodYear(ab.getPeriodYear());
                    vo.setPeriodNum(ab.getPeriodNum());
                    vo.setBalanceDirection(Constant.BalanceDirection.DEBIT);
                    vo.setOpeningBalance(BigDecimal.ZERO);
                    vo.setClosingBalance(BigDecimal.ZERO);
                }
            }
            //查询开始期间年份的0期
            BalanceSubjectQueryDto bqDto = new BalanceSubjectQueryDto();
            bqDto.setPeriodYear(ab.getPeriodYear());
            bqDto.setPeriodNum(Constant.Number.ANTI_INITLIZED);
            bqDto.setAccountBookEntityId(accountBookEntity.getId());
            bqDto.setAccountBookId(dto.getAccountBookId());
            bqDto.setSubjectIds(dto.getSubjectList());
            List<BalanceSubjectVo> list = balanceSubjectMapper.findBalanceSubjectByParams(bqDto);
            //校验是否为空
            if (list != null && list.size() > 0) {
                BigDecimal debitAmount = BigDecimal.ZERO;
                BigDecimal creditAmount = BigDecimal.ZERO;
                BalanceSubjectVo balanceSubjectVo = new BalanceSubjectVo();
                for(BalanceSubjectVo balSub:list){
                    debitAmount= debitAmount.add(balSub.getDebitAmount());
                    creditAmount= creditAmount.add(balSub.getCreditAmount());
                }
                balanceSubjectVo.setDebitAmount(debitAmount);
                balanceSubjectVo.setCreditAmount(creditAmount);
                map.put(accountBookEntity.getId() + Constant.Character.UNDER_LINE + ab.getPeriodYear() + Constant.Character.UNDER_LINE_ZERO, balanceSubjectVo);
            }
        }
    }

    /**
     * 获取每个核算主体的各期间的期初余额
     * wuweiming
     *
     * @param map
     * @param dto
     * @param ab
     * @param accountBookEntity
     * @return
     */
    public void getBalanceSubjectInfo(Map<String, BalanceSubjectVo> map, BalanceSubjectCashJournalQueryDto dto,
                                      AccountBookPeriodVo ab, AccountBookEntityDto accountBookEntity) {
        BalanceSubjectVo balanceSubjectVo = new BalanceSubjectVo();
        BigDecimal sum = BigDecimal.ZERO;
        //查询所有末级科目的期初余额
        BalanceSubjectQueryDto bqDto = new BalanceSubjectQueryDto();
        bqDto.setPeriodYear(ab.getPeriodYear());
        bqDto.setPeriodNum(ab.getPeriodNum());
        bqDto.setAccountBookEntityId(accountBookEntity.getId());
        bqDto.setAccountBookId(dto.getAccountBookId());
        bqDto.setSubjectIds(dto.getSubjectList());
        List<BalanceSubjectVo> list = balanceSubjectMapper.findBalanceSubjectByParams(bqDto);
        //获取帐簿信息
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        accountSubjectDto.setSubjectId(dto.getSubjectId());
        List<AccountSubjectVo> accountSubjectVos = accountSubjectFeignClient.findInfoForLedger(accountSubjectDto).getData();
        //复制科目属性
        MergeUtil.merge(list,
                accountSubjectVos, balanceSubjVo -> balanceSubjVo.getAccountSubjectId(),accountSubjectVo->accountSubjectVo.getId(),
                (balanceSubjVo, accountSubjectVo) -> {
                    balanceSubjVo.setBalanceDirection(accountSubjectVo.getBalanceDirection());
                });

        for (BalanceSubjectVo vo : list) {
            if (dto.getBalanceDirection().equals(vo.getBalanceDirection())){
                //方向相同
                sum = sum.add(vo.getOpeningBalance());
            }else {
                //方向相反
                sum = sum.subtract(vo.getOpeningBalance());
            }
        }
        //遍历
//        for (BalanceSubjectVo vo : list){
//            //判断 是否包含未记账凭证(0:不包含 1:包含)
//            if (Constant.Is.NO.equals(dto.getIsIncludeUnbooked())){
//                vo.setDebitAmount(vo.getPostDebitAmount());
//                vo.setCreditAmount(vo.getPostCreditAmount());
//                //判断 是否包含损益结转凭证(0:不包含 1:包含)
//                if (Constant.Is.NO.equals(dto.getIsIncludeProfitAndLoss())){
//                    vo.setDebitAmount(vo.getDebitAmount().subtract(vo.getPostSyDebitAmount()));
//                    vo.setCreditAmount(vo.getCreditAmount().subtract(vo.getPostSyCreditAmount()));
//                }
//            }else {
//                //判断 是否包含损益结转凭证(0:不包含 1:包含)
//                if (Constant.Is.NO.equals(dto.getIsIncludeProfitAndLoss())){
//                    vo.setDebitAmount(vo.getDebitAmount().subtract(vo.getSyDebitAmount()));
//                    vo.setCreditAmount(vo.getCreditAmount().subtract(vo.getSyCreditAmount()));
//                }
//            }
//            //判断科目余额方向(0：借方、1：贷方)
//            if (dto.getBalanceDirection().equals("0")){
//                balanceSubjectVo.setBalanceDirection(Constant.BalanceDirection.DEBIT);
//                //借方余额 = 期末余额 + 借方金额 - 贷方金额
//                sum = sum.add(vo.getDebitAmount().subtract(vo.getCreditAmount()));
//            }else {
//                balanceSubjectVo.setBalanceDirection(Constant.BalanceDirection.CREDIT);
//                //贷方余额 = 期末余额 + 贷方金额 - 借方金额
//                sum = sum.add(vo.getCreditAmount().subtract(vo.getDebitAmount()));
//            }
//        }

        if (dto.getVoucherDate() != null && dto.getVoucherDate().size() > 0) {
            //制单日期不为空
            BalanceSubjectCashJournalQueryDto newDto = new BalanceSubjectCashJournalQueryDto();
            newDto.setAccountBookId(dto.getAccountBookId());
            newDto.setAccountBookEntityId(accountBookEntity.getId());
            newDto.setSubjectList(dto.getSubjectList());
            newDto.setPeriodYear(ab.getPeriodYear());
            newDto.setPeriodNumber(ab.getPeriodNum());
            newDto.setVocherTime(dto.getVoucherDate().get(0));
            newDto.setItemValueIds(dto.getItemValueIds());
            newDto.setIsIncludeProfitAndLoss(dto.getIsIncludeProfitAndLoss());
            newDto.setIsIncludeUnbooked(dto.getIsIncludeUnbooked());
            List<BalanceSubjectCashJournalVo> bcList = cashJournalMapper.findCashJournalList(newDto);

            for (BalanceSubjectCashJournalVo bc : bcList) {
                //判断科目余额方向(0：借方、1：贷方)
                if (Constant.BalanceDirection.DEBIT.equals(dto.getBalanceDirection())) {
                    balanceSubjectVo.setBalanceDirection(Constant.BalanceDirection.DEBIT);
                    //借方余额 = 期末余额 + 借方金额 - 贷方金额
                    sum = sum.add(bc.getDebitAmount().subtract(bc.getCreditAmount()));
                } else {
                    balanceSubjectVo.setBalanceDirection(Constant.BalanceDirection.CREDIT);
                    //贷方余额 = 期末余额 + 贷方金额 - 借方金额
                    sum = sum.add(bc.getCreditAmount().subtract(bc.getDebitAmount()));
                }
            }
        }
        balanceSubjectVo.setAccountSubjectId(dto.getAccountSubjectId());
        balanceSubjectVo.setAccountBookId(dto.getAccountBookId());
        balanceSubjectVo.setAccountBookEntityId(accountBookEntity.getId());
        balanceSubjectVo.setAccountBookEntityName(accountBookEntity.getEntityName());
        balanceSubjectVo.setSubjectId(dto.getSubjectId());
        balanceSubjectVo.setPeriodYear(ab.getPeriodYear());
        balanceSubjectVo.setPeriodNum(ab.getPeriodNum());
        balanceSubjectVo.setOpeningBalance(sum);
        balanceSubjectVo.setBalanceDirection(dto.getBalanceDirection());
        map.put(accountBookEntity.getId() + Constant.Character.UNDER_LINE + balanceSubjectVo.getPeriodYear() + Constant.Character.UNDER_LINE + balanceSubjectVo.getPeriodNum(), balanceSubjectVo);
    }

    /**
     * 获取每一期账簿期间的凭证记录
     * wuweiming
     *
     * @param accountBookEntity
     * @param ab
     * @param vocherList
     * @return
     */
    public List<BalanceSubjectCashJournalVo> getCurrVocherList(AccountBookEntityDto accountBookEntity, AccountBookPeriodVo ab, List<BalanceSubjectCashJournalVo> vocherList) {
        List<BalanceSubjectCashJournalVo> list = new ArrayList<>();
        //遍历凭证记录
        for (BalanceSubjectCashJournalVo vo : vocherList) {
            if (accountBookEntity.getId().equals(vo.getAccountBookEntityId()) && ab.getPeriodYear().equals(vo.getPostingPeriodYear()) && ab.getPeriodNum().equals(vo.getPostingPeriodNum())) {
                list.add(vo);
            }
        }
        return list;
    }

    /**
     * wuweiming
     * 查询所有期间的凭证记录
     *
     * @param dto
     * @return
     */
    public List<BalanceSubjectCashJournalVo> findAllVocherList(BalanceSubjectCashJournalQueryDto dto) {
        List<Long> accountBookEntityIds = new LinkedList<>();
        for (AccountBookEntityDto accountBookEntity : dto.getAccountBookEntityList()) {
            accountBookEntityIds.add(accountBookEntity.getId());
        }
        dto.setAccountBookEntityIds(accountBookEntityIds);
        List<BalanceSubjectCashJournalVo> allVocherList = new ArrayList<>();
        Boolean searchFlag = false;
        if (dto.getItemValueList() != null && dto.getItemValueList().size() > 0) {
            for (BalanceSubjectCashJournalQueryDto balanceSubjectCashJournalQueryDto : dto.getItemValueList()) {
                if (balanceSubjectCashJournalQueryDto.getItemValueInfos() != null && balanceSubjectCashJournalQueryDto.getItemValueInfos().size() > 0) {
                    searchFlag = true;
                }
            }
        }
        //判断辅助核算id是否为空
        if (searchFlag) {
            List<VoucherEntryAuxiliaryVo> voucherEntryAuxiliaryVos = cashJournalMapper.findVoucherEntryForAuxiary(dto);
            Map<Long, List<VoucherEntryAuxiliaryVo>> voucherEntryAuxiliaryVosMap
                    = voucherEntryAuxiliaryVos.parallelStream().sorted(Comparator.comparing(VoucherEntryAuxiliaryVo::getSourceTable))
                    .collect(Collectors.groupingBy(VoucherEntryAuxiliaryVo::getEntryId));
            List<String> sourceTableList = dto.getItemValueList().parallelStream().filter(e -> e.getItemValueInfos() != null && e.getItemValueInfos().size() > 0).map(BalanceSubjectCashJournalQueryDto::getSourceTable).sorted().collect(Collectors.toList());
            Map<String, List<Long>> sourceTableMap = new HashMap<>();
            dto.getItemValueList().parallelStream().forEach(e -> sourceTableMap.put(e.getSourceTable(),
                    e.getItemValueInfos().parallelStream().map(AccountSubjectAuxiliaryVo::getId).collect(Collectors.toList())));
            Set<Long> voucherEntryAuxiliaryIds = new HashSet<>();
            voucherEntryAuxiliaryVosMap.forEach((key, value) -> {
                List<String> findSourceTableList = value.parallelStream().map(VoucherEntryAuxiliaryVo::getSourceTable).collect(Collectors.toList());
                if (value.size() >= sourceTableList.size() && findSourceTableList.containsAll(sourceTableList)) {
                    Boolean flag = true;
                    for (VoucherEntryAuxiliaryVo voucherEntryAuxiliaryVo : value) {
                        if (!(sourceTableMap.get(voucherEntryAuxiliaryVo.getSourceTable()) != null
                                && sourceTableMap.get(voucherEntryAuxiliaryVo.getSourceTable()).contains(voucherEntryAuxiliaryVo.getItemValueId()))) {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        voucherEntryAuxiliaryIds.add(key);
                    }
                }
            });
            dto.setIds(voucherEntryAuxiliaryIds);
            if (voucherEntryAuxiliaryIds.size() > 0) {
                allVocherList = cashJournalMapper.findCashJournalAuxiliaryList(dto);
                List<VoucherEntryAuxiliaryVo> itemList = cashJournalMapper.selectAuxiliaryIteam(voucherEntryAuxiliaryIds);
                MergeUtil.merge(itemList, dto.getItemValueList()
                        , (des, source) -> des.getSourceTable().equals(source.getSourceTable())
                        , (des, source) -> {
                            source.getItemValueInfos().forEach(e -> {
                                        if (e.getId().equals(des.getItemValueId())) {
                                            des.setProjectCode(e.getAuxiliaryCode());
                                            des.setProjectName(e.getAuxiliaryName());
                                        }
                                    }
                            );
                        });
                MergeUtil.mergeList(allVocherList, itemList,
                        (bs, ts) -> bs.getId().equals(ts.getEntryId()),
                        (bs, ts) -> {
                            bs.setVoucherEntryAuxiliaryVoList(ts);
                        });
            }
        } else {
            //辅助核算id为空，则查询科目余额
            allVocherList = cashJournalMapper.findCashJournalList(dto);
        }

        return allVocherList;
    }

    /**
     * wuweiming
     * 查询所有末级科目
     *
     * @param dto
     * @return
     */
    public List<AccountSubjectVo> findAllChildSubjecList(BalanceSubjectCashJournalQueryDto dto) {
        List<AccountSubjectVo> list = new ArrayList<>();
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        accountSubjectDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        accountSubjectDto.setSubjectId(dto.getSubjectId());
        accountSubjectDto.setCode(dto.getSubjectCode());
        accountSubjectDto.setIsIncludeEnable(dto.getIsConfigEnable());
        accountSubjectDto.setIsFinal(new Byte("1"));
        //查询所选科目的所有未删除的下级科目
        Result<List<AccountSubjectVo>> result = accountSubjectApi.findAllChildInfo(accountSubjectDto);
        if (result != null && result.getData() != null) {
            list = result.getData();
        }
        return list;
    }

    /**
     * wuweiming
     * 查询 区间内的期间集合
     *
     * @param dto
     * @param abpDto
     * @return
     */
    public List<AccountBookPeriodVo> findAccountBookPeriodList(BalanceSubjectCashJournalQueryDto dto, AccountBookPeriodDto abpDto) {
        abpDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        //判断选择的日期是否为空
        if (dto.getVoucherDate() != null && dto.getVoucherDate().size() > 0) {
            //制单日期不为空
            abpDto.setDateList(dto.getVoucherDate());
        } else if (dto.getPeriodYears() != null && dto.getPeriodNumbers() != null) {
            abpDto.setPeriodYears(dto.getPeriodYears());
            abpDto.setPeriodNums(dto.getPeriodNumbers());
        }
        //查询 区间内的期间集合
        return accountBookPeriodMapper.findAccountBookPeriodListByParams(abpDto);
    }
}
