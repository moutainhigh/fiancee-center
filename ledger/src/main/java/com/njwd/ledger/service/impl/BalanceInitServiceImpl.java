package com.njwd.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.njwd.common.Constant;
import com.njwd.common.ExcelColumnConstant;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.AccountBookSystemDto;
import com.njwd.entity.basedata.vo.AccountBookSystemVo;
import com.njwd.entity.basedata.vo.AccountBookVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.*;
import com.njwd.entity.ledger.dto.BalanceCashFlowInitDto;
import com.njwd.entity.ledger.dto.BalanceInitDto;
import com.njwd.entity.ledger.dto.BalanceSubjectInitAuxiliaryDto;
import com.njwd.entity.ledger.dto.BalanceSubjectInitDto;
import com.njwd.entity.ledger.vo.BalanceInitCheckVo;
import com.njwd.entity.ledger.vo.BalanceInitVo;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.dto.CashFlowItemDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.entity.platform.vo.CashFlowItemVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.ledger.cloudclient.AccountBookFeignClient;
import com.njwd.ledger.cloudclient.AccountBookSystemFeignClient;
import com.njwd.ledger.cloudclient.AccountSubjectFeignClient;
import com.njwd.ledger.cloudclient.CashFlowReportClient;
import com.njwd.ledger.mapper.*;
import com.njwd.ledger.service.BalanceInitService;
import com.njwd.service.FileService;
import com.njwd.utils.FastUtils;
import com.njwd.utils.MergeUtil;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author lj
 * @Description 总账初始化
 * @Date:11:15 2019/8/12
 **/
@Service
public class BalanceInitServiceImpl implements BalanceInitService {

    @Resource
    private AccountBookPeriodMapper accountBookPeriodMapper;

    @Resource
    private BalanceSubjectMapper balanceSubjectMapper;

    @Resource
    private BalanceSubjectAuxiliaryMapper balanceSubjectAuxiliaryMapper;

    @Resource
    private BalanceSubjectAuxiliaryItemMapper balanceSubjectAuxiliaryItemMapper;

    @Resource
    private BalanceCashFlowMapper balanceCashFlowMapper;

    @Resource
    private BalanceSubjectInitMapper balanceSubjectInitMapper;

    @Resource
    private BalanceSubjectInitAuxiliaryMapper balanceSubInitAuxiliaryMapper;

    @Resource
    private BalanceSubjectInitAuxiliaryItemMapper balanceSubInitAuxItemMapper;

    @Resource
    private BalanceCashFlowInitMapper balanceCashFlowInitMapper;

    @Resource
    private AccountBookSystemFeignClient accountBookSystemFeignClient;

    @Resource
    private AccountSubjectFeignClient accountSubjectFeignClient;

    @Resource
    private CashFlowReportClient cashFlowReportClient;

    @Resource
    private AccountBookFeignClient accountBookFeignClient;

    @Resource
    private BalanceInitRecordMapper balanceInitRecordMapper;

    @Resource
    private FileService fileService;

    /**
     * 账簿初始化
     * i.	核算主体试算平衡：获取账簿各核算主体试算平衡情况（核算主体平衡结果在科目期初录入中获取），所有核算主体试算平衡，则通过，否则为不通过
     * ii.	内部往来科目期初校验：校验账簿科目表中1999科目，校验逻辑：核算主体A-1999科目（辅助核算：核算主体 B）+ 核算主体B-1999科目（辅助核算：核算主体 B）=0，符合则通过，不符合为不通过
     * iii.	现金流量和资金科目期初校验，校验逻辑：现金流量流入-现金流量流出=科目期初现金类科目借方发生额-贷方发生额，符合则通过，不符合为警告
     * iv.	内部往来现金流量，校验账簿现金流量表中1999现金流量项目，校验逻辑：1999现金流量项目金额合计为0，符合为通过，不符合为不通过
     *
     * @param balanceInitDto
     * @return com.njwd.support.BatchResult
     * @Author lj
     * @Date:11:27 2019/8/12
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BalanceInitVo balanceInit(BalanceInitDto balanceInitDto) {
        List<BalanceInitCheckVo> balanceInitCheckVos = new ArrayList<BalanceInitCheckVo>();
        BalanceInitVo balanceInitVo = new BalanceInitVo();
        Boolean flag=true;
        //查询科目期初数据
        List<BalanceSubjectInit> balanceSubjectInits = balanceSubjectInitMapper.selectSubjectInitList(balanceInitDto);
        List<BalanceSubjectInitAuxiliary> balanceSubjectInitAuxiliaries = balanceSubInitAuxiliaryMapper.selectSubjectInitAuxiliaryList(balanceInitDto);
        List<BalanceSubjectInitAuxiliaryItem> subjectInitAuxiliaryItems = balanceSubInitAuxItemMapper.selectBalSubInitAuxItemList(balanceInitDto);
        //查询现金流量期初数据
        List<BalanceCashFlowInit> balanceCashFlowInits = balanceCashFlowInitMapper.selectBalanceCashFlowInitList(balanceInitDto);
        AccountBookVo accountBookVo = getAccountBook(balanceInitDto);
        //查询数据是否已经初始化,如果已初始化跳出校验，加入忽略异常
        List<BalanceInitRecord> balanceInitRecordList = getBalanceInitRecords(balanceInitDto);
        //获取账簿初始化状态
        Byte isInitalized = getIsInitalized(balanceInitDto);
        if(Constant.Is.YES.equals(isInitalized)){
            throw new ServiceException(ResultCode.BALANCE_IGNORE);
        }
        //查询帐簿是否开启现金流量
        Byte cashFlowStatus = getCashFlowStatus(balanceInitDto);
        //开始校验
        flag = doCheck(balanceInitRecordList,balanceInitCheckVos, flag, balanceSubjectInits, balanceSubjectInitAuxiliaries, subjectInitAuxiliaryItems, balanceCashFlowInits, accountBookVo,cashFlowStatus);
        //校验通过
        initDataBatachInster(balanceInitDto, flag, balanceSubjectInits, balanceSubjectInitAuxiliaries, subjectInitAuxiliaryItems, balanceCashFlowInits);

        balanceInitVo.setBalanceInitCheckVos(balanceInitCheckVos);
        balanceInitVo.setCheckFlag(flag);
        return balanceInitVo;
    }

    private List<BalanceInitRecord> getBalanceInitRecords(BalanceInitDto balanceInitDto) {
        List<BalanceInitRecord> balanceInitRecordList = balanceInitRecordMapper.selectList(new LambdaQueryWrapper<BalanceInitRecord>()
                .eq(BalanceInitRecord::getAccountBookId, balanceInitDto.getAccountBookId())
                .eq(BalanceInitRecord::getPeriodYear, balanceInitDto.getPeriodYear())
                .eq(BalanceInitRecord::getPeriodNum, balanceInitDto.getPeriodNum()));
        for (BalanceInitRecord balanceInitRecord : balanceInitRecordList) {
            if (LedgerConstant.SubjectStatus.INIT_ED.equals(balanceInitRecord.getSubjectStatus())) {
                throw new ServiceException(ResultCode.BALANCE_IGNORE);
            }
        }
        return balanceInitRecordList;
    }

    /**
     * 开始校验
     * @Author lj
     * @Date:14:05 2019/8/29
     * @return java.lang.Boolean
     **/
    private Boolean doCheck(List<BalanceInitRecord> balanceInitRecordList,List<BalanceInitCheckVo> balanceInitCheckVos, Boolean flag, List<BalanceSubjectInit> balanceSubjectInits, List<BalanceSubjectInitAuxiliary> balanceSubjectInitAuxiliaries, List<BalanceSubjectInitAuxiliaryItem> subjectInitAuxiliaryItems, List<BalanceCashFlowInit> balanceCashFlowInits, AccountBookVo accountBookVo,Byte cashFlowStatus) {
        if (accountBookVo != null) {
            AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
            accountSubjectDto.setSubjectId(accountBookVo.getSubjectId());
            List<AccountSubjectVo> accountSubjectVos = accountSubjectFeignClient.findInfoForLedger(accountSubjectDto).getData();
            List<BalanceSubjectInitDto> balanceSubjectInitDtos = new ArrayList<BalanceSubjectInitDto>();
            BalanceSubjectInitDto balanceSubjectInitDto = null;
            for (BalanceSubjectInit balanceSubjectInit : balanceSubjectInits) {
                balanceSubjectInitDto = new BalanceSubjectInitDto();
                FastUtils.copyProperties(balanceSubjectInit, balanceSubjectInitDto);
                balanceSubjectInitDtos.add(balanceSubjectInitDto);
            }
            //复制科目属性
            MergeUtil.merge(balanceSubjectInitDtos,
                    accountSubjectVos, accSubjectDto -> accSubjectDto.getAccountSubjectId(),accountSubjectVo->accountSubjectVo.getId(),
                    (accSubjectDto, accountSubjectVo) -> {
                        accSubjectDto.setBalanceDirection(accountSubjectVo.getBalanceDirection());
                        accSubjectDto.setAccountCategory(accountSubjectVo.getAccountCategory());
                        accSubjectDto.setAccountSubjectCode(accountSubjectVo.getCode());
                    });
            //复制核算主体名称
            MergeUtil.merge(balanceSubjectInitDtos,
                    balanceInitRecordList, accSubjectDto -> accSubjectDto.getAccountBookEntityId(),balanceInitRecord->balanceInitRecord.getEntityId(),
                    (accSubjectDto, balanceInitRecord) -> {
                        accSubjectDto.setAccountBookEntityName(balanceInitRecord.getEntityName());
                        accSubjectDto.setAccountBookEntityCode(balanceInitRecord.getEntityCode());
                    });

            List<BalanceSubjectInitAuxiliaryDto> balSubInitAuxDtos = new ArrayList<BalanceSubjectInitAuxiliaryDto>();
            BalanceSubjectInitAuxiliaryDto balSubInitAuxiliaryDto = null;
            for (BalanceSubjectInitAuxiliary balanceSubjectInitAuxiliary : balanceSubjectInitAuxiliaries) {
                balSubInitAuxiliaryDto = new BalanceSubjectInitAuxiliaryDto();
                FastUtils.copyProperties(balanceSubjectInitAuxiliary, balSubInitAuxiliaryDto);
                balSubInitAuxDtos.add(balSubInitAuxiliaryDto);
            }

            //复制辅助核算属性
            MergeUtil.merge(balSubInitAuxDtos,
                    accountSubjectVos, balSubInitAuxDto -> balSubInitAuxDto.getAccountSubjectId(),accountSubjectVo->accountSubjectVo.getId(),
                    (balSubInitAuxDto, accountSubjectVo) -> {
                        balSubInitAuxDto.setBalanceDirection(accountSubjectVo.getBalanceDirection());
                        balSubInitAuxDto.setAccountSubjectCode(accountSubjectVo.getCode());
                    });

            //复制核算主体名称
            MergeUtil.merge(balSubInitAuxDtos,
                    balanceInitRecordList,balSubInitAuxDto -> balSubInitAuxDto.getAccountBookEntityId(),balanceInitRecord->balanceInitRecord.getEntityId(),
                    (balSubInitAuxDto, balanceInitRecord) -> {
                        balSubInitAuxDto.setAccountBookEntityName(balanceInitRecord.getEntityName());
                        balSubInitAuxDto.setAccountBookEntityCode(balanceInitRecord.getEntityCode());
                    });

            CashFlowItemDto cashFlowItemDto = new CashFlowItemDto();
            cashFlowItemDto.setCashFlowId(accountBookVo.getCashFlowItemId());
            List<CashFlowItemVo> cashFlowItemVoList = cashFlowReportClient.findCashFlowItemForReport(cashFlowItemDto).getData();
            List<BalanceCashFlowInitDto> balanceCashFlowInitDtos = new ArrayList<BalanceCashFlowInitDto>();
            BalanceCashFlowInitDto balanceCashFlowInitDto = null;
            for (BalanceCashFlowInit balanceCashFlowInit : balanceCashFlowInits) {
                balanceCashFlowInitDto = new BalanceCashFlowInitDto();
                FastUtils.copyProperties(balanceCashFlowInit, balanceCashFlowInitDto);
                balanceCashFlowInitDtos.add(balanceCashFlowInitDto);
            }
            //复制现金流量属性
            MergeUtil.merge(balanceCashFlowInitDtos,
                    cashFlowItemVoList, balCashFlowInitDto-> balCashFlowInitDto.getItemId(),cashFlowItemVo->cashFlowItemVo.getId(),
                    (balCashFlowInitDto, cashFlowItemVo) -> {
                        balCashFlowInitDto.setCashFlowDirection(cashFlowItemVo.getCashFlowDirection());
                        balCashFlowInitDto.setIsInteriorContact(cashFlowItemVo.getIsInteriorContact());
                    });

            //复制核算主体名称
            MergeUtil.merge(balanceCashFlowInitDtos,
                    balanceInitRecordList,balCashFlowInitDto -> balCashFlowInitDto.getAccountBookEntityId(),balanceInitRecord->balanceInitRecord.getEntityId(),
                    (balCashFlowInitDto, balanceInitRecord) -> {
                        balCashFlowInitDto.setAccountBookEntityName(balanceInitRecord.getEntityName());
                        balCashFlowInitDto.setAccountBookEntityCode(balanceInitRecord.getEntityCode());
                    });
            //根据核算主体分组数据
            Map<Long, List<BalanceSubjectInitDto>> subMap = balanceSubjectInitDtos.stream().collect(Collectors.groupingBy(BalanceSubjectInitDto::getAccountBookEntityId));
            Map<Long, List<BalanceSubjectInitAuxiliaryDto>> balAuxMap = balSubInitAuxDtos.stream().collect(Collectors.groupingBy(BalanceSubjectInitAuxiliaryDto::getAccountBookEntityId));
            Map<Long, List<BalanceCashFlowInitDto>> balCashMap = balanceCashFlowInitDtos.stream().collect(Collectors.groupingBy(BalanceCashFlowInitDto::getAccountBookEntityId));
            //核算主体试算平衡
            BalanceInitCheckVo balanceInitCheckVo = new BalanceInitCheckVo();
            balanceInitCheckVo.setCheckContext(LedgerConstant.BalanceInit.ENTRY_CHECK_CONTEXT);
            flag = checkEntity(balanceInitCheckVos, flag, subMap, balanceInitCheckVo);

            //内部往来科目期初校验
            BalanceInitCheckVo balanceInitCheckVoTWO = new BalanceInitCheckVo();
            balanceInitCheckVoTWO.setCheckContext(LedgerConstant.BalanceInit.INTERIOR_SUBJECT_CHECK_CONTEXT);
            flag = checkInertSubject(balanceInitCheckVos, flag, subjectInitAuxiliaryItems, balAuxMap, balanceInitCheckVoTWO);

            /*如果：账簿启用子系统记录表-现金流量启用状态=是 校验
            *否则：
            * 不进行校验。
            */
            //现金流量和资金科目期初
            if(Constant.Is.YES.equals(cashFlowStatus)){
                BalanceInitCheckVo balanceInitCheckVoThr = new BalanceInitCheckVo();
                balanceInitCheckVoThr.setCheckContext(LedgerConstant.BalanceInit.ACC_CHECK_CONTEXT);
                flag = checkCashFlowSubject(balanceInitCheckVos, flag, balanceCashFlowInitDtos, subMap, balCashMap, balanceInitCheckVoThr,cashFlowStatus);

//                //内部往来现金流量校验
//                BalanceInitCheckVo balanceInitCheckVoFou = new BalanceInitCheckVo();
//                balanceInitCheckVoFou.setCheckContext(LedgerConstant.BalanceInit.INTERIOR_CASH_CHECK_CONTEXT);
//                balanceInitCheckVoFou.setDescription(LedgerConstant.BalanceInit.INTERIOR_CASH_DESCRIPTION);
//                flag = checkInterCashFlow(balanceInitCheckVos, flag, balanceCashFlowInitDtos, balanceInitCheckVoFou);
            }
        }
        return flag;
    }

    /**
     * 内部往来现金流量校验
     * @Author lj
     * @Date:14:06 2019/8/29
     * @param balanceInitCheckVos, flag, balanceCashFlowInitDtos, balanceInitCheckVoFou
     * @return java.lang.Boolean
     **/
    private Boolean checkInterCashFlow(List<BalanceInitCheckVo> balanceInitCheckVos, Boolean flag, List<BalanceCashFlowInitDto> balanceCashFlowInitDtos, BalanceInitCheckVo balanceInitCheckVoFou) {
        BigDecimal flowInAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
        BigDecimal flowOutAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
        for (BalanceCashFlowInitDto balanceCashFlowInitDto : balanceCashFlowInitDtos) {
            if (Constant.Is.YES.equals(balanceCashFlowInitDto.getIsInteriorContact())) {
                if (Constant.Is.NO.equals(balanceCashFlowInitDto.getCashFlowDirection())) {
                    flowInAmountSum = flowInAmountSum.add(balanceCashFlowInitDto.getOpeningBalance());
                } else {
                    flowOutAmountSum = flowOutAmountSum.add(balanceCashFlowInitDto.getOpeningBalance());
                }
            }
        }

        if (flowInAmountSum.compareTo(flowOutAmountSum) != Constant.Number.ZERO) {
            balanceInitCheckVoFou.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_UN_PASS);
            flag = false;
        } else {
            balanceInitCheckVoFou.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_PASS);
        }
        balanceInitCheckVos.add(balanceInitCheckVoFou);
        return flag;
    }

    /**
     * 内部往来科目期初校验
     * @Author lj
     * @Date:14:07 2019/8/29
     * @param balanceInitCheckVos, flag, subjectInitAuxiliaryItems, balAuxMap, balanceInitCheckVoTWO
     * @return java.lang.Boolean
     **/
    private Boolean checkInertSubject(List<BalanceInitCheckVo> balanceInitCheckVos, Boolean flag, List<BalanceSubjectInitAuxiliaryItem> subjectInitAuxiliaryItems, Map<Long, List<BalanceSubjectInitAuxiliaryDto>> balAuxMap, BalanceInitCheckVo balanceInitCheckVoTWO) {
        List<String> descriptionList= new ArrayList<String>();
        if(CollectionUtils.isEmpty(balAuxMap)){
            balanceInitCheckVoTWO.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_PASS);
            balanceInitCheckVoTWO.setDescription(descriptionList);
        }else {
            for (Long key : balAuxMap.keySet()) {
                //试算平衡
                BigDecimal dOpeningBalanceSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
                BigDecimal cOpeningBalanceSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);

                List<BalanceSubjectInitAuxiliaryDto> subAuxList = balAuxMap.get(key);
                //内部往来的科目
                Lab:
                for (BalanceSubjectInitAuxiliaryDto balanceSubjectInitAuxiliary : subAuxList) {
                    if (LedgerConstant.BalanceInit.INTERIOR_SUBJECT_CODE.equals(balanceSubjectInitAuxiliary.getAccountSubjectCode())) {
                        dOpeningBalanceSum = dOpeningBalanceSum.add(balanceSubjectInitAuxiliary.getOpeningBalance());
                        //查找核算主体A 下的辅助核算B
                        List<BalanceSubjectInitAuxiliaryItem> tempAuxiliaryItem = subjectInitAuxiliaryItems.stream().filter(
                                t -> t.getBalanceAuxiliaryId().equals(balanceSubjectInitAuxiliary.getId())).collect(Collectors.toList());
                        if (!CollectionUtils.isEmpty(tempAuxiliaryItem)) {
                            for(BalanceSubjectInitAuxiliaryItem item: tempAuxiliaryItem){

                                BalanceSubjectInitAuxiliaryItem itemB = item;
                                Long entryB = itemB.getItemValueId();
                                //查找辅助核算B
                                List<BalanceSubjectInitAuxiliaryDto> subAuxBList = balAuxMap.get(entryB);
                                if(CollectionUtils.isEmpty(subAuxBList)){
                                    balanceInitCheckVoTWO.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_UN_PASS);
                                    descriptionList.add(balanceSubjectInitAuxiliary.getAccountBookEntityCode()+Constant.Character.THROUGH_LINE+
                                            balanceSubjectInitAuxiliary.getAccountBookEntityName()+LedgerConstant.BalanceInit.INTERIOR_SUBJECT_DESCRIPTION);
                                    flag = false;
                                    break Lab;
                                }
                                //过滤辅助核算B内部往来的数据
                                List<BalanceSubjectInitAuxiliaryDto> balSubInitAuxList = subAuxBList.stream().filter(
                                        t -> t.getAccountSubjectCode().equals(LedgerConstant.BalanceInit.INTERIOR_SUBJECT_CODE)).collect(Collectors.toList());
                                if (!CollectionUtils.isEmpty(balSubInitAuxList)) {
                                    Boolean tFlag=true;
                                    BalanceSubjectInitAuxiliaryDto tempEntryBAux = null;
                                    for(BalanceSubjectInitAuxiliaryDto tempEntryAux:balSubInitAuxList){
                                         tempEntryBAux = tempEntryAux;
                                        //查找核算主体B 下的辅助核算A
                                        List<BalanceSubjectInitAuxiliaryItem> tempAuxiliaryItemB = subjectInitAuxiliaryItems.stream().filter(
                                                t -> t.getBalanceAuxiliaryId().equals(tempEntryAux.getId())).collect(Collectors.toList());
                                        //如果算主体B 下的辅助核算不是A直接不通过
                                        if(CollectionUtils.isEmpty(tempAuxiliaryItemB)){
                                            balanceInitCheckVoTWO.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_UN_PASS);
                                            descriptionList.add(balanceSubjectInitAuxiliary.getAccountBookEntityCode()+Constant.Character.THROUGH_LINE+
                                                    balanceSubjectInitAuxiliary.getAccountBookEntityName()+Constant.Character.AND_CN+tempEntryBAux.getAccountBookEntityCode()+Constant.Character.THROUGH_LINE+
                                                    tempEntryBAux.getAccountBookEntityName()+LedgerConstant.BalanceInit.INTERIOR_SUBJECT_DESCRIPTION);
                                            flag = false;
                                            break Lab;
                                        }else {
                                            for(BalanceSubjectInitAuxiliaryItem tempAuxiliaryInitItem : tempAuxiliaryItemB){
                                                if (balanceSubjectInitAuxiliary.getAccountBookEntityId().equals(tempAuxiliaryInitItem.getItemValueId())) {
                                                    dOpeningBalanceSum = dOpeningBalanceSum.add(tempEntryBAux.getOpeningBalance());
                                                    tFlag=false;
                                                }
                                            }
                                        }
                                    }
                                    //如果算主体B 下的辅助核算不是A直接不通过
                                    if(tFlag){
                                        balanceInitCheckVoTWO.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_UN_PASS);
                                        descriptionList.add(balanceSubjectInitAuxiliary.getAccountBookEntityCode()+Constant.Character.THROUGH_LINE+
                                                balanceSubjectInitAuxiliary.getAccountBookEntityName()+Constant.Character.AND_CN+tempEntryBAux.getAccountBookEntityCode()+Constant.Character.THROUGH_LINE+
                                                tempEntryBAux.getAccountBookEntityName()+LedgerConstant.BalanceInit.INTERIOR_SUBJECT_DESCRIPTION);
                                        flag = false;
                                        break Lab;
                                    }
                                } else {
                                    balanceInitCheckVoTWO.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_UN_PASS);
                                    descriptionList.add(balanceSubjectInitAuxiliary.getAccountBookEntityCode()+Constant.Character.THROUGH_LINE+
                                            balanceSubjectInitAuxiliary.getAccountBookEntityName()+Constant.Character.AND_CN+subAuxBList.get(Constant.Number.ZERO).getAccountBookEntityCode()+Constant.Character.THROUGH_LINE+
                                            subAuxBList.get(Constant.Number.ZERO).getAccountBookEntityName()+LedgerConstant.BalanceInit.INTERIOR_SUBJECT_DESCRIPTION);
                                    flag = false;
                                    break Lab;
                                }

                            }

                        }

                    }
                }

                //试算平衡不平
                if (dOpeningBalanceSum.compareTo(cOpeningBalanceSum) != Constant.Number.ZERO) {
                    balanceInitCheckVoTWO.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_UN_PASS);
                    descriptionList.add(subAuxList.get(Constant.Number.ZERO).getAccountBookEntityCode()+Constant.Character.THROUGH_LINE+
                            subAuxList.get(Constant.Number.ZERO).getAccountBookEntityName()+LedgerConstant.BalanceInit.INTERIOR_SUBJECT_DESCRIPTION);
                    flag = false;
                } else {
                    balanceInitCheckVoTWO.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_PASS);
                }
            }
            balanceInitCheckVoTWO.setDescription(descriptionList);
        }

        balanceInitCheckVos.add(balanceInitCheckVoTWO);
        return flag;
    }

    /**
     * 现金流量和资金科目期初
     * @Author lj
     * @Date:14:08 2019/8/29
     * @param balanceInitCheckVos, flag, balanceCashFlowInitDtos, subMap, balCashMap, balanceInitCheckVoThr, cashFlowStatus
     * @return java.lang.Boolean
     **/
    private Boolean checkCashFlowSubject(List<BalanceInitCheckVo> balanceInitCheckVos, Boolean flag, List<BalanceCashFlowInitDto> balanceCashFlowInitDtos, Map<Long, List<BalanceSubjectInitDto>> subMap, Map<Long, List<BalanceCashFlowInitDto>> balCashMap, BalanceInitCheckVo balanceInitCheckVoThr,Byte cashFlowStatus) {
        List<String> descriptionList= new ArrayList<String>();
        if(CollectionUtils.isEmpty(subMap)){
            if(CollectionUtils.isEmpty(balCashMap)){
                balanceInitCheckVoThr.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_PASS);
                balanceInitCheckVoThr.setDescription(descriptionList);
            }else{
                for (Long key : balCashMap.keySet()) {
                    BigDecimal tempA = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
                    //现金流量流入-现金流量流出
                    BigDecimal flowInAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
                    BigDecimal flowOutAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
                    BigDecimal tempB = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
                    List<BalanceCashFlowInitDto> balCashList = balCashMap.get(key);
                    for (BalanceCashFlowInitDto balanceCashFlowInitDto : balCashList) {
                        if (Constant.Is.NO.equals(balanceCashFlowInitDto.getCashFlowDirection())) {
                            flowOutAmountSum = flowOutAmountSum.add(balanceCashFlowInitDto.getOpeningBalance());
                        } else {
                            flowInAmountSum = flowInAmountSum.add(balanceCashFlowInitDto.getOpeningBalance());
                        }
                    }

                    tempB = tempB.add(flowInAmountSum).subtract(flowOutAmountSum);

                    if (tempA.compareTo(tempB) != Constant.Number.ZERO) {
                        if(Constant.Is.YES.equals(cashFlowStatus)){
                            balanceInitCheckVoThr.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_UN_PASS);
                            flag = false;
                        }else {
                            balanceInitCheckVoThr.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_WARN);
                        }
                        descriptionList.add(balCashList.get(Constant.Number.ZERO).getAccountBookEntityCode()+Constant.Character.THROUGH_LINE+
                                balCashList.get(Constant.Number.ZERO).getAccountBookEntityName()+LedgerConstant.BalanceInit.ACC_DESCRIPTION);
                    } else {
                        balanceInitCheckVoThr.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_PASS);
                    }
                }
                balanceInitCheckVoThr.setDescription(descriptionList);
            }
        }else {
            for (Long key : subMap.keySet()) {
                List<BalanceSubjectInitDto> subList = subMap.get(key);

                //现金类科目发生额
                BigDecimal thisYearDebitAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
                BigDecimal thisYearCreditAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
                BigDecimal tempA = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
                for (BalanceSubjectInitDto balanceSubjectInit : subList) {
                    //只计算现金类科目
                    if (balanceSubjectInit.getAccountCategory().startsWith(Constant.AccountCategory.A)) {
                            thisYearDebitAmountSum = thisYearDebitAmountSum.add(balanceSubjectInit.getThisYearDebitAmount());
                            thisYearCreditAmountSum = thisYearCreditAmountSum.add(balanceSubjectInit.getThisYearCreditAmount());
                    }
                }
                tempA = tempA.add(thisYearDebitAmountSum).subtract(thisYearCreditAmountSum);

                //现金流量流入-现金流量流出
                BigDecimal flowInAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
                BigDecimal flowOutAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
                BigDecimal tempB = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
                List<BalanceCashFlowInitDto> balCashList = balCashMap.get(key);
                if(balCashList!=null){
                    for (BalanceCashFlowInitDto balanceCashFlowInitDto : balCashList) {

                        if (Constant.Is.NO.equals(balanceCashFlowInitDto.getCashFlowDirection())) {
                            flowOutAmountSum = flowOutAmountSum.add(balanceCashFlowInitDto.getOpeningBalance());
                        } else {
                            flowInAmountSum = flowInAmountSum.add(balanceCashFlowInitDto.getOpeningBalance());
                        }
                    }
                }

                tempB = tempB.add(flowInAmountSum).subtract(flowOutAmountSum);

                if (tempA.compareTo(tempB) != Constant.Number.ZERO) {
                    if(Constant.Is.YES.equals(cashFlowStatus)){
                        balanceInitCheckVoThr.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_UN_PASS);
                        flag = false;
                    }else {
                        balanceInitCheckVoThr.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_WARN);
                    }
                    descriptionList.add(subList.get(Constant.Number.ZERO).getAccountBookEntityCode()+Constant.Character.THROUGH_LINE+
                            subList.get(Constant.Number.ZERO).getAccountBookEntityName()+LedgerConstant.BalanceInit.ACC_DESCRIPTION);
                } else {
                    balanceInitCheckVoThr.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_PASS);
                }
            }
            balanceInitCheckVoThr.setDescription(descriptionList);
        }

        balanceInitCheckVos.add(balanceInitCheckVoThr);
        return flag;
    }

    /**
     * 批量初始化数据
     * @Author lj
     * @Date:14:11 2019/8/29
     * @param balanceInitDto, flag, balanceSubjectInits, balanceSubjectInitAuxiliaries, subjectInitAuxiliaryItems, balanceCashFlowInits
     * @return void
     **/
    private void initDataBatachInster(BalanceInitDto balanceInitDto, Boolean flag, List<BalanceSubjectInit> balanceSubjectInits, List<BalanceSubjectInitAuxiliary> balanceSubjectInitAuxiliaries, List<BalanceSubjectInitAuxiliaryItem> subjectInitAuxiliaryItems, List<BalanceCashFlowInit> balanceCashFlowInits) {
        //校验通过
        if(flag){
            SysUserVo user = UserUtils.getUserVo();
            //更新核算主体记录表现金流量状态
            BalanceInitRecord balanceInitRecord = new BalanceInitRecord();
            balanceInitRecord.setCashStatus(LedgerConstant.CashStatus.INIT_ED);
            balanceInitRecord.setSubjectStatus(LedgerConstant.SubjectStatus.INIT_ED);
            balanceInitRecordMapper.update(balanceInitRecord,new LambdaQueryWrapper<BalanceInitRecord>()
                    .eq(BalanceInitRecord::getAccountBookId,balanceInitDto.getAccountBookId())
                    .eq(BalanceInitRecord::getPeriodYear, balanceInitDto.getPeriodYear())
                    .eq(BalanceInitRecord::getRootEnterpriseId, user.getRootEnterpriseId())
                    .eq(BalanceInitRecord::getPeriodNum, balanceInitDto.getPeriodNum()));
            if(!CollectionUtils.isEmpty(balanceSubjectInits)){
                BalanceSubject balanceSubject;
                //获取余额表数据
                List<BalanceSubject> balanceSubjectList= balanceSubjectMapper.selectList(new LambdaQueryWrapper<BalanceSubject>()
                        .eq(BalanceSubject::getAccountBookId,balanceInitDto.getAccountBookId())
                        .eq(BalanceSubject::getPeriodYear,balanceInitDto.getPeriodYear())
                        .eq(BalanceSubject::getPeriodNum,balanceInitDto.getPeriodNum()));
                Map<String,BalanceSubject> balanceSubjectMap = balanceSubjectList.stream().collect(Collectors.toMap(t->t.getAccountBookEntityId()+Constant.Character.COMMA+
                        t.getAccountSubjectId(),t->t));
                for(BalanceSubjectInit subjectInit :balanceSubjectInits){
                    balanceSubject = balanceSubjectMap.get(subjectInit.getAccountBookEntityId()+Constant.Character.COMMA+
                            subjectInit.getAccountSubjectId());
                    //判断当前期间是否是第一期，不是第一期则生成第0期数据
                    if(!Constant.Number.INITIAL.equals(subjectInit.getPeriodNum())){
                        //不为空则更新数据
                        if(balanceSubject!=null){
                            balanceSubjectMapper.updateSubjectAdd(subjectInit);
                        }else {
                            //添加数据
                            balanceSubjectMapper.addSubject(subjectInit);
                        }
                        balanceSubjectMapper.addSubjectZero(subjectInit);

                    }else {
                        if(balanceSubject!=null){
                            balanceSubjectMapper.updateSubjectOne(subjectInit);
                        }else {
                            balanceSubjectMapper.addSubjectOne(subjectInit);
                        }
                        balanceSubjectMapper.addSubjectZero(subjectInit);
                    }
                }
            }
            //分核算主体去更新
            if(!CollectionUtils.isEmpty(balanceSubjectInitAuxiliaries)){
                List<BalanceSubjectInitAuxiliaryItem> items;
                List<BalanceSubjectAuxiliaryItem> BalanceItems;
                Map<Long, List<BalanceSubjectInitAuxiliary>> balAuxMap =  balanceSubjectInitAuxiliaries.stream().collect(Collectors.groupingBy(BalanceSubjectInitAuxiliary::getAccountBookEntityId));
                //获取辅助核算余额表数据
                List<BalanceSubjectAuxiliaryItem> auxiliaryItemList= balanceSubjectAuxiliaryItemMapper.selectList(new LambdaQueryWrapper<BalanceSubjectAuxiliaryItem>()
                        .eq(BalanceSubjectAuxiliaryItem::getAccountBookId,balanceInitDto.getAccountBookId())
                        .eq(BalanceSubjectAuxiliaryItem::getPeriodYear,balanceInitDto.getPeriodYear())
                        .eq(BalanceSubjectAuxiliaryItem::getPeriodNum,balanceInitDto.getPeriodNum()));
                Map<String,List<BalanceSubjectAuxiliaryItem>> auxiliaryItemMap = auxiliaryItemList.stream().collect(Collectors.groupingBy(t->t.getAccountBookEntityId()+Constant.Character.COMMA
                        +t.getAccountSubjectId()));
                for(Map.Entry<Long, List<BalanceSubjectInitAuxiliary>> balanceSubjectInitAuxEntry:balAuxMap.entrySet()){
                    List<BalanceSubjectInitAuxiliary> balanceSubjectInitAuxList = balanceSubjectInitAuxEntry.getValue();
                    for(BalanceSubjectInitAuxiliary subjectInitAuxiliary :balanceSubjectInitAuxList){
                        items = subjectInitAuxiliaryItems.stream().filter(t -> t.getBalanceAuxiliaryId().equals(subjectInitAuxiliary.getId())).collect(Collectors.toList());
                        //查询科目余额表里的核算和期初的辅助核算是否相等
                        Boolean tFlag=true;
                        BalanceItems = auxiliaryItemMap.get(subjectInitAuxiliary.getAccountBookEntityId()+Constant.Character.COMMA+
                                subjectInitAuxiliary.getAccountSubjectId());
                        // 本期的辅助核算keySign字典 key为keySign
                        Map<String, Long> balanceIdkeySignDict = new LinkedHashMap<>();
                        // 以balanceId为key,将BalanceSubjectAuxiliaryItem归类排序
                        Map<Long, Set<BalanceSubjectAuxiliaryItem>> sortAuxiliaryItemMap = new LinkedHashMap<>();
                        Set<BalanceSubjectInitAuxiliaryItem> itemSet = new HashSet<>(items);
                        String initKeySignStr = getKeySignTwo(subjectInitAuxiliary.getAccountSubjectId(), itemSet);

                        if(!CollectionUtils.isEmpty(BalanceItems)){
                            for (BalanceSubjectAuxiliaryItem auxiliaryItem : BalanceItems) {
                                sortAuxiliaryItemMap.computeIfAbsent(auxiliaryItem.getBalanceAuxiliaryId(), k -> new HashSet<>()).add(auxiliaryItem);
                            }

                            for (Map.Entry<Long,Set<BalanceSubjectAuxiliaryItem>> sortAuxiliaryItem : sortAuxiliaryItemMap.entrySet()) {
                                Set<BalanceSubjectAuxiliaryItem> auxiliaryItems = sortAuxiliaryItem.getValue();
                                String keySignStr = getKeySign(subjectInitAuxiliary.getAccountSubjectId(), auxiliaryItems);
                                balanceIdkeySignDict.put(keySignStr, sortAuxiliaryItem.getKey());
                            }
                        }else {
                            tFlag=false;
                        }

                        //通过keySign获取auxiliaryId
                        Long auxiliaryId;
                        if(!CollectionUtils.isEmpty(balanceIdkeySignDict)){
                            auxiliaryId=balanceIdkeySignDict.get(initKeySignStr);
                            if(auxiliaryId!=null){
                                subjectInitAuxiliary.setId(auxiliaryId);
                            }else {
                                tFlag=false;
                            }
                        }


                        //判断当前期间是否是第一期，不是第一期则生成第0期数据
                        if(!Constant.Number.INITIAL.equals(subjectInitAuxiliary.getPeriodNum())){
                            //不为空则更新数据
                            if(tFlag){
                                balanceSubjectAuxiliaryMapper.updateSubjectInitAuxiliaryAdd(subjectInitAuxiliary);
                            }else {
                                //添加数据
                                balanceSubjectAuxiliaryMapper.addSubjectInitAuxiliary(subjectInitAuxiliary);
                                for(BalanceSubjectInitAuxiliaryItem item: items){
                                    item.setBalanceAuxiliaryId(subjectInitAuxiliary.getId());
                                    balanceSubjectAuxiliaryItemMapper.addBalanceSubAuxItem(item);
                                }
                            }

                            balanceSubjectAuxiliaryMapper.addSubjectInitAuxiliaryZero(subjectInitAuxiliary);
                            for(BalanceSubjectInitAuxiliaryItem item: items){
                                item.setBalanceAuxiliaryId(subjectInitAuxiliary.getId());
                                balanceSubjectAuxiliaryItemMapper.addBalanceSubAuxItemZero(item);
                            }
                        }else {
                            if(tFlag){
                                balanceSubjectAuxiliaryMapper.updateSubjectInitAuxiliaryOne(subjectInitAuxiliary);
                            }else {
                                balanceSubjectAuxiliaryMapper.addSubjectInitAuxiliaryOne(subjectInitAuxiliary);
                                for(BalanceSubjectInitAuxiliaryItem item: items){
                                    item.setBalanceAuxiliaryId(subjectInitAuxiliary.getId());
                                    balanceSubjectAuxiliaryItemMapper.addBalanceSubAuxItemOne(item);
                                }
                            }

                            balanceSubjectAuxiliaryMapper.addSubjectInitAuxiliaryZero(subjectInitAuxiliary);
                            for(BalanceSubjectInitAuxiliaryItem item: items){
                                item.setBalanceAuxiliaryId(subjectInitAuxiliary.getId());
                                balanceSubjectAuxiliaryItemMapper.addBalanceSubAuxItemZero(item);
                            }
                        }
                    }
                }

            }

            if(!CollectionUtils.isEmpty(balanceCashFlowInits)){
                BalanceCashFlow balanceCash;
                //获取现金流量余额表数据
                List<BalanceCashFlow> balanceCashList= balanceCashFlowMapper.selectList(new LambdaQueryWrapper<BalanceCashFlow>()
                        .eq(BalanceCashFlow::getAccountBookId,balanceInitDto.getAccountBookId())
                        .eq(BalanceCashFlow::getPeriodYear,balanceInitDto.getPeriodYear())
                        .eq(BalanceCashFlow::getPeriodNum,balanceInitDto.getPeriodNum()));
                Map<String,BalanceCashFlow> balanceCashMap = balanceCashList.stream().collect(Collectors.toMap(t->t.getAccountBookEntityId()+Constant.Character.COMMA+
                        t.getItemId(),t->t));
                for(BalanceCashFlowInit balanceCashFlowInit :balanceCashFlowInits){
                    balanceCash = balanceCashMap.get(balanceCashFlowInit.getAccountBookEntityId()+Constant.Character.COMMA+
                            balanceCashFlowInit.getItemId());
                        //不为空则更新数据
                    if(balanceCash!=null){
                        balanceCashFlowMapper.updateCashFlowAdd(balanceCashFlowInit);
                    }else {
                        //添加数据
                        balanceCashFlowMapper.addCashFlow(balanceCashFlowInit);
                    }
                }
            }
            AccountBookSystemDto accountBookSystemDto = new AccountBookSystemDto();
            accountBookSystemDto.setId(balanceInitDto.getAccountBookSystemId());
            //更新子系统状态
            accountBookSystemFeignClient.initSystemById(accountBookSystemDto);
        }
    }

    /**
     * 拼接辅助核算余额的keySign
     * @param auxiliaryItems   auxiliaryItems
     * @return java.lang.StringBuilder
     * @author xyyxhcj@qq.com
     * @date 2019/9/27 17:05
     **/
    private String getKeySign(Long subjectId, Set<BalanceSubjectAuxiliaryItem> auxiliaryItems) {
        return concatKeySign(subjectId, auxiliaryItems);
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
     * 拼接辅助核算余额的keySign
     * @param auxiliaryItems   auxiliaryItems
     * @return java.lang.StringBuilder
     * @author xyyxhcj@qq.com
     * @date 2019/9/27 17:05
     **/
    private String getKeySignTwo(Long subjectId, Set<BalanceSubjectInitAuxiliaryItem> auxiliaryItems) {
        return concatKeySignTwo(subjectId, auxiliaryItems);
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
    private String concatKeySignTwo(Long subjectId, Set<BalanceSubjectInitAuxiliaryItem> auxiliaryItems) {
        StringBuilder keySign = new StringBuilder();
        keySign.append(subjectId).append(Constant.Character.UNDER_LINE);
        for (BalanceSubjectInitAuxiliaryItem auxiliaryItem : auxiliaryItems) {
            keySign.append(auxiliaryItem.getSign()).append(Constant.Character.UNDER_LINE);
        }
        return keySign.toString();
    }

    /**
     * 获取帐簿信息
     * @Author lj
     * @Date:14:10 2019/8/29
     * @param initDto
     * @return com.njwd.entity.basedata.vo.AccountBookVo
     **/
    private AccountBookVo getAccountBook(BalanceInitDto initDto) {
        AccountBookDto accountBookDto = new AccountBookDto();
        accountBookDto.setId(initDto.getAccountBookId());
        return accountBookFeignClient.selectById(accountBookDto).getData();
    }

    /**
     * 核算主体试算平衡
     * @Author lj
     * @Date:14:09 2019/8/29
     * @param balanceInitCheckVos, flag, subMap, balAuxMap, balanceInitCheckVo
     * @return java.lang.Boolean
     **/
    private Boolean checkEntity(List<BalanceInitCheckVo> balanceInitCheckVos, Boolean flag, Map<Long, List<BalanceSubjectInitDto>> subMap, BalanceInitCheckVo balanceInitCheckVo) {
        List<String> descriptionList= new ArrayList<String>();
        if(CollectionUtils.isEmpty(subMap)){
            balanceInitCheckVo.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_PASS);
            balanceInitCheckVo.setDescription(descriptionList);
        }else{
            //试算平衡
            BigDecimal dOpeningBalanceSum = null;
            BigDecimal cOpeningBalanceSum = null;
            BigDecimal thisYearDebitAmountSum = null;
            BigDecimal thisYearCreditAmountSum = null;
            //分核算主体试算平衡校验
            for (Long key : subMap.keySet()) {
                List<BalanceSubjectInitDto> subList = subMap.get(key);
                //试算平衡
                dOpeningBalanceSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
                cOpeningBalanceSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
                thisYearDebitAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
                thisYearCreditAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
                for (BalanceSubjectInitDto balanceSubjectInit : subList) {
                    //去除内部往来的科目
//                    if (!LedgerConstant.BalanceInit.INTERIOR_SUBJECT_CODE.equals(balanceSubjectInit.getAccountSubjectCode())) {
                        if (Constant.Is.NO.equals(balanceSubjectInit.getBalanceDirection())) {
                            dOpeningBalanceSum = dOpeningBalanceSum.add(balanceSubjectInit.getOpeningBalance());
                        } else {
                            cOpeningBalanceSum = cOpeningBalanceSum.add(balanceSubjectInit.getOpeningBalance());
                        }
                        thisYearDebitAmountSum = thisYearDebitAmountSum.add(balanceSubjectInit.getThisYearDebitAmount());
                        thisYearCreditAmountSum = thisYearCreditAmountSum.add(balanceSubjectInit.getThisYearCreditAmount());
//                    }

                }

                //试算平衡不平
                if (dOpeningBalanceSum.compareTo(cOpeningBalanceSum) != Constant.Number.ZERO || thisYearDebitAmountSum.compareTo(thisYearCreditAmountSum) != Constant.Number.ZERO) {
                    balanceInitCheckVo.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_UN_PASS);
                    descriptionList.add(subList.get(Constant.Number.ZERO).getAccountBookEntityCode()+Constant.Character.THROUGH_LINE+
                            subList.get(Constant.Number.ZERO).getAccountBookEntityName()+LedgerConstant.BalanceInit.ENTRY_DESCRIPTION);
                    flag = false;
                } else {
                    balanceInitCheckVo.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_PASS);
                }
            }
            balanceInitCheckVo.setDescription(descriptionList);
        }
        balanceInitCheckVos.add(balanceInitCheckVo);
        return flag;
    }

    /**
     * 账簿初始化批量校验
     *
     * @param balanceInitDto
     * @return com.njwd.support.BatchResult
     * @Author lj
     * @Date:11:27 2019/8/12
     **/
    @Override
    public BalanceInitVo balanceInitBatchCheck(BalanceInitDto balanceInitDto) {
        BalanceInitVo balanceInitVoR = new BalanceInitVo();
        List<BalanceInitVo> balanceInitVoList = new ArrayList<BalanceInitVo>();
        balanceInitVoR.setBalanceInitVoList(balanceInitVoList);
        BalanceInitVo balanceInitVo = null;
        List<BalanceInitCheckVo> balanceInitCheckVos = null;
        for(BalanceInitDto initDto :balanceInitDto.getBalanceInitList()){
            balanceInitVo = new BalanceInitVo();
            balanceInitVo.setAccountBookName(initDto.getAccountBookName());
            balanceInitVoList.add(balanceInitVo);
            balanceInitCheckVos = new ArrayList<BalanceInitCheckVo>();
            Boolean flag=true;
            //查询科目期初数据
            List<BalanceSubjectInit> balanceSubjectInits = balanceSubjectInitMapper.selectSubjectInitList(initDto);
            List<BalanceSubjectInitAuxiliary> balanceSubjectInitAuxiliaries = balanceSubInitAuxiliaryMapper.selectSubjectInitAuxiliaryList(initDto);
            List<BalanceSubjectInitAuxiliaryItem> subjectInitAuxiliaryItems = balanceSubInitAuxItemMapper.selectBalSubInitAuxItemList(initDto);
            //查询现金流量期初数据
            List<BalanceCashFlowInit> balanceCashFlowInits = balanceCashFlowInitMapper.selectBalanceCashFlowInitList(initDto);
            AccountBookVo accountBookVo = getAccountBook(initDto);
            //查询数据是否已经初始化,如果已初始化跳出校验，加入忽略异常
            List<BalanceInitRecord> balanceInitRecordList = getBalanceInitRecords(initDto);
            //查询帐簿是否开启现金流量
            Byte cashFlowStatus = getCashFlowStatus(initDto);
            //开始校验
            flag = doCheck(balanceInitRecordList,balanceInitCheckVos, flag, balanceSubjectInits, balanceSubjectInitAuxiliaries, subjectInitAuxiliaryItems, balanceCashFlowInits, accountBookVo,cashFlowStatus);

            //校验通过
            if(flag){
                balanceInitVo.setAccountStatus(LedgerConstant.BalanceInit.CHECK_STATUS_PASS);
                balanceInitVoR.getSuccessList().add(initDto);
            }else {
                balanceInitVo.setAccountStatus(LedgerConstant.BalanceInit.CHECK_STATUS_UN_PASS);
                balanceInitVoR.getFailList().add(initDto);
            }

            balanceInitVo.setBalanceInitCheckVos(balanceInitCheckVos);

        }
        return balanceInitVoR;
    }

    /**
     * 获取现金流量状态
     * @Author lj
     * @Date:14:10 2019/8/29
     * @param balanceInitDto
     * @return java.lang.Byte
     **/
    private Byte getCashFlowStatus(BalanceInitDto balanceInitDto) {
        AccountBookSystemDto accountBookSystemDto = new AccountBookSystemDto();
        accountBookSystemDto.setAccountBookId(balanceInitDto.getAccountBookId());
        accountBookSystemDto.setSystemSign(Constant.SystemSignValue.LEDGER);
        List<AccountBookSystemVo> accountBookSystemVos = accountBookSystemFeignClient.findEnableList(accountBookSystemDto).getData();
        Byte cashFlowStatus = null;
        if (!CollectionUtils.isEmpty(accountBookSystemVos)) {
            AccountBookSystemVo accountBookSystemVo = accountBookSystemVos.get(Constant.Number.ZERO);
            cashFlowStatus = accountBookSystemVo.getCashFlowEnableStatus();
        }
        return cashFlowStatus;
    }

    /**
     * 获取账簿初始化状态
     * @Author lj
     * @Date:14:10 2019/8/29
     * @param balanceInitDto
     * @return java.lang.Byte
     **/
    private Byte getIsInitalized(BalanceInitDto balanceInitDto) {
        AccountBookSystemDto accountBookSystemDto = new AccountBookSystemDto();
        accountBookSystemDto.setAccountBookId(balanceInitDto.getAccountBookId());
        accountBookSystemDto.setSystemSign(Constant.SystemSignValue.LEDGER);
        List<AccountBookSystemVo> accountBookSystemVos = accountBookSystemFeignClient.findEnableList(accountBookSystemDto).getData();
        Byte isInitalized = null;
        if (!CollectionUtils.isEmpty(accountBookSystemVos)) {
            AccountBookSystemVo accountBookSystemVo = accountBookSystemVos.get(Constant.Number.ZERO);
            isInitalized = accountBookSystemVo.getIsInitalized();
        }
        return isInitalized;
    }

    /**
     * 账簿初始化批量更新数据
     *
     * @param balanceInitDto
     * @return com.njwd.support.BatchResult
     * @Author lj
     * @Date:11:27 2019/8/12
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BalanceInitVo balanceInitBatchUpdate(BalanceInitDto balanceInitDto) {
        BalanceInitVo balanceInitVo = new BalanceInitVo();
        for(BalanceInitDto initDto :balanceInitDto.getBalanceInitList()){
            List<Long> accountBookIds = balanceInitVo.getAccountBookIds();
            accountBookIds.add(initDto.getAccountBookId());
            Boolean flag=true;
            //查询科目期初数据
            List<BalanceSubjectInit> balanceSubjectInits = balanceSubjectInitMapper.selectSubjectInitList(initDto);
            List<BalanceSubjectInitAuxiliary> balanceSubjectInitAuxiliaries = balanceSubInitAuxiliaryMapper.selectSubjectInitAuxiliaryList(initDto);
            List<BalanceSubjectInitAuxiliaryItem> subjectInitAuxiliaryItems = balanceSubInitAuxItemMapper.selectBalSubInitAuxItemList(initDto);
            //查询现金流量期初数据
            List<BalanceCashFlowInit> balanceCashFlowInits = balanceCashFlowInitMapper.selectBalanceCashFlowInitList(initDto);
            //校验通过
            initDataBatachInster(initDto, flag, balanceSubjectInits, balanceSubjectInitAuxiliaries, subjectInitAuxiliaryItems, balanceCashFlowInits);
        }
        return balanceInitVo;
    }

    /**
     * 账簿反初始化
     *
     * @param balanceInitDto
     * @return com.njwd.support.BatchResult
     * @Author lj
     * @Date:11:27 2019/8/12
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BalanceInitVo balanceDisInit(BalanceInitDto balanceInitDto) {
        List<BalanceInitCheckVo> balanceInitCheckVos = new ArrayList<BalanceInitCheckVo>();
        BalanceInitVo balanceInitVo = new BalanceInitVo();
        BalanceInitCheckVo balanceInitCheckVo = new BalanceInitCheckVo();
        balanceInitCheckVo.setCheckContext(LedgerConstant.BalanceInit.DIS_INIT_CHECK_CONTEXT);
        List<String> description = new ArrayList<String>();
        //校验最近账期是否结账：获取账簿启用期间 期间状态是否为 未结账 ，是则通过，否则为不通过
        AccountBookPeriod accountBookPeriod = accountBookPeriodMapper.selectOne(new LambdaQueryWrapper<AccountBookPeriod>()
                .eq(AccountBookPeriod::getAccountBookId, balanceInitDto.getAccountBookId())
                .eq(AccountBookPeriod::getAccountBookSystemId,balanceInitDto.getAccountBookSystemId())
                .eq(AccountBookPeriod::getPeriodYear, balanceInitDto.getPeriodYear())
                .eq(AccountBookPeriod::getPeriodNum, balanceInitDto.getPeriodNum()));
        //查询数据是否已经初始化,如果已初始化跳出校验，加入忽略异常
        List<BalanceInitRecord> balanceInitRecordList = balanceInitRecordMapper.selectList(new LambdaQueryWrapper<BalanceInitRecord>()
                .eq(BalanceInitRecord::getAccountBookId, balanceInitDto.getAccountBookId())
                .eq(BalanceInitRecord::getPeriodYear, balanceInitDto.getPeriodYear())
                .eq(BalanceInitRecord::getPeriodNum, balanceInitDto.getPeriodNum()));
        for(BalanceInitRecord balanceInitRecord:balanceInitRecordList){
            if(!LedgerConstant.SubjectStatus.INIT_ED.equals(balanceInitRecord.getSubjectStatus())){
                throw new ServiceException(ResultCode.BALANCE_IGNORE);
            }
        }
        //获取账簿初始化状态
        Byte isInitalized = getIsInitalized(balanceInitDto);
        if(Constant.Is.NO.equals(isInitalized)){
            throw new ServiceException(ResultCode.BALANCE_IGNORE);
        }

        //未结账
        if (accountBookPeriod != null && !Constant.Is.NO.equals(accountBookPeriod.getIsSettle())) {
            balanceInitCheckVo.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_UN_PASS);
            description.add(LedgerConstant.BalanceInit.DIS_INIT_DESCRIPTION);
            balanceInitVo.setCheckFlag(false);
        } else {
            balanceInitVo.setCheckFlag(true);
            balanceInitCheckVo.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_PASS);
            disInitData(balanceInitDto);
        }

        balanceInitCheckVo.setDescription(description);
        balanceInitCheckVos.add(balanceInitCheckVo);
        balanceInitVo.setBalanceInitCheckVos(balanceInitCheckVos);
        return balanceInitVo;
    }

    /**
     * 批量反初始化
     * @Author lj
     * @Date:14:11 2019/8/29
     * @param balanceInitDto
     * @return void
     **/
    private void disInitData(BalanceInitDto balanceInitDto) {
        //查询科目期初数据
        List<BalanceSubjectInit> balanceSubjectInits = balanceSubjectInitMapper.selectSubjectInitList(balanceInitDto);
        List<BalanceSubjectInitAuxiliary> balanceSubjectInitAuxiliaries = balanceSubInitAuxiliaryMapper.selectSubjectInitAuxiliaryList(balanceInitDto);
        List<BalanceSubjectInitAuxiliaryItem> subjectInitAuxiliaryItems = balanceSubInitAuxItemMapper.selectBalSubInitAuxItemList(balanceInitDto);
        //查询现金流量期初数据
        List<BalanceCashFlowInit> balanceCashFlowInits = balanceCashFlowInitMapper.selectBalanceCashFlowInitList(balanceInitDto);
        //查询帐簿是否开启现金流量
        Byte cashFlowStatus = getCashFlowStatus(balanceInitDto);
        //更新核算主体记录表现金流量状态
        BalanceInitRecord balanceInitRecord = new BalanceInitRecord();
        BalanceInitRecord balanceInitRecordTwo = new BalanceInitRecord();
        SysUserVo user = UserUtils.getUserVo();
        if(CollectionUtils.isEmpty(balanceSubjectInits)){
            balanceInitRecord.setSubjectStatus(LedgerConstant.SubjectStatus.UN_RECORD);
            balanceInitRecordMapper.update(balanceInitRecord,new LambdaQueryWrapper<BalanceInitRecord>()
                    .eq(BalanceInitRecord::getAccountBookId,balanceInitDto.getAccountBookId())
                    .eq(BalanceInitRecord::getPeriodYear, balanceInitDto.getPeriodYear())
                    .eq(BalanceInitRecord::getRootEnterpriseId, user.getRootEnterpriseId())
                    .eq(BalanceInitRecord::getPeriodNum, balanceInitDto.getPeriodNum()));
        }else {
            balanceInitRecord.setSubjectStatus(LedgerConstant.SubjectStatus.UN_RECORD);
            balanceInitRecordMapper.update(balanceInitRecord,new LambdaQueryWrapper<BalanceInitRecord>()
                    .eq(BalanceInitRecord::getAccountBookId,balanceInitDto.getAccountBookId())
                    .eq(BalanceInitRecord::getPeriodYear, balanceInitDto.getPeriodYear())
                    .eq(BalanceInitRecord::getRootEnterpriseId, user.getRootEnterpriseId())
                    .eq(BalanceInitRecord::getPeriodNum, balanceInitDto.getPeriodNum()));
            for(BalanceSubjectInit balanceSubjectInit:balanceSubjectInits){
                balanceInitRecord.setSubjectStatus(LedgerConstant.SubjectStatus.BALANCED);
                balanceInitRecordMapper.update(balanceInitRecord,new LambdaQueryWrapper<BalanceInitRecord>()
                        .eq(BalanceInitRecord::getAccountBookId,balanceInitDto.getAccountBookId())
                        .eq(BalanceInitRecord::getPeriodYear, balanceInitDto.getPeriodYear())
                        .eq(BalanceInitRecord::getEntityId, balanceSubjectInit.getAccountBookEntityId())
                        .eq(BalanceInitRecord::getRootEnterpriseId, user.getRootEnterpriseId())
                        .eq(BalanceInitRecord::getPeriodNum, balanceInitDto.getPeriodNum()));
            }
        }
        if(!CollectionUtils.isEmpty(balanceCashFlowInits)){
            updateCashStatus(balanceInitDto, cashFlowStatus, balanceInitRecordTwo, user);
            for(BalanceCashFlowInit balanceCashFlowInit:balanceCashFlowInits){
                balanceInitRecordTwo.setCashStatus(LedgerConstant.CashStatus.RECORDED);
                balanceInitRecordMapper.update(balanceInitRecordTwo,new LambdaQueryWrapper<BalanceInitRecord>()
                        .eq(BalanceInitRecord::getAccountBookId,balanceInitDto.getAccountBookId())
                        .eq(BalanceInitRecord::getPeriodYear, balanceInitDto.getPeriodYear())
                        .eq(BalanceInitRecord::getEntityId, balanceCashFlowInit.getAccountBookEntityId())
                        .eq(BalanceInitRecord::getRootEnterpriseId, user.getRootEnterpriseId())
                        .eq(BalanceInitRecord::getPeriodNum, balanceInitDto.getPeriodNum()));
            }
        }else {
            updateCashStatus(balanceInitDto, cashFlowStatus, balanceInitRecordTwo, user);
        }
        //清除科目余额表期初数，清除现金流量期初
        if(!CollectionUtils.isEmpty(balanceSubjectInits)){
            for(BalanceSubjectInit subjectInit :balanceSubjectInits){
                if(!Constant.Number.INITIAL.equals(subjectInit.getPeriodNum())){
                    balanceSubjectMapper.updateSubjectDel(subjectInit);
                }else {
                    balanceSubjectMapper.updateSubjectDelOne(subjectInit);
                }
                //清除第0期数据
                balanceSubjectMapper.delete(new LambdaQueryWrapper<BalanceSubject>()
                        .eq(BalanceSubject::getAccountBookId,subjectInit.getAccountBookId())
                        .eq(BalanceSubject::getAccountBookEntityId,subjectInit.getAccountBookEntityId())
                        .eq(BalanceSubject::getAccountSubjectId,subjectInit.getAccountSubjectId())
                        .eq(BalanceSubject::getPeriodYear,subjectInit.getPeriodYear())
                        .eq(BalanceSubject::getPeriodNum,Constant.Number.ANTI_INITLIZED));
            }
        }
        if(!CollectionUtils.isEmpty(balanceSubjectInitAuxiliaries)){
            List<BalanceSubjectInitAuxiliaryItem> items;
            List<BalanceSubjectAuxiliaryItem> BalanceItems;
            Map<Long, List<BalanceSubjectInitAuxiliary>> balAuxMap =  balanceSubjectInitAuxiliaries.stream().collect(Collectors.groupingBy(BalanceSubjectInitAuxiliary::getAccountBookEntityId));
            for(Map.Entry<Long, List<BalanceSubjectInitAuxiliary>> balanceSubjectInitAuxEntry:balAuxMap.entrySet()) {
                List<BalanceSubjectInitAuxiliary> balanceSubjectInitAuxList = balanceSubjectInitAuxEntry.getValue();
                for (BalanceSubjectInitAuxiliary subjectInitAuxiliary : balanceSubjectInitAuxList) {
                    items = subjectInitAuxiliaryItems.stream().filter(t -> t.getBalanceAuxiliaryId().equals(subjectInitAuxiliary.getId())).collect(Collectors.toList());
                    BalanceItems = balanceSubjectAuxiliaryItemMapper.selectList(new LambdaQueryWrapper<BalanceSubjectAuxiliaryItem>()
                            .eq(BalanceSubjectAuxiliaryItem::getAccountBookId,subjectInitAuxiliary.getAccountBookId())
                            .eq(BalanceSubjectAuxiliaryItem::getAccountBookEntityId,subjectInitAuxiliary.getAccountBookEntityId())
                            .eq(BalanceSubjectAuxiliaryItem::getAccountSubjectId,subjectInitAuxiliary.getAccountSubjectId())
                            .eq(BalanceSubjectAuxiliaryItem::getPeriodYear,subjectInitAuxiliary.getPeriodYear())
                            .eq(BalanceSubjectAuxiliaryItem::getPeriodNum,subjectInitAuxiliary.getPeriodNum()));
                    // 本期的辅助核算keySign字典 key为keySign
                    Map<String, Long> balanceIdkeySignDict = new LinkedHashMap<>();
                    // 以balanceId为key,将BalanceSubjectAuxiliaryItem归类排序
                    Map<Long, Set<BalanceSubjectAuxiliaryItem>> sortAuxiliaryItemMap = new LinkedHashMap<>();
                    Set<BalanceSubjectInitAuxiliaryItem> itemSet = new HashSet<>(items);
                    String initKeySignStr = getKeySignTwo(subjectInitAuxiliary.getAccountSubjectId(), itemSet);

                    if(!CollectionUtils.isEmpty(BalanceItems)){
                        for (BalanceSubjectAuxiliaryItem auxiliaryItem : BalanceItems) {
                            sortAuxiliaryItemMap.computeIfAbsent(auxiliaryItem.getBalanceAuxiliaryId(), k -> new HashSet<>()).add(auxiliaryItem);
                        }

                        for (Map.Entry<Long,Set<BalanceSubjectAuxiliaryItem>> sortAuxiliaryItem : sortAuxiliaryItemMap.entrySet()) {
                            Set<BalanceSubjectAuxiliaryItem> auxiliaryItems = sortAuxiliaryItem.getValue();
                            String keySignStr = getKeySign(subjectInitAuxiliary.getAccountSubjectId(), auxiliaryItems);
                            balanceIdkeySignDict.put(keySignStr, sortAuxiliaryItem.getKey());
                        }
                    }

                    //通过keySign获取auxiliaryId
                    Long auxiliaryId;
                    if(!CollectionUtils.isEmpty(balanceIdkeySignDict)){
                        auxiliaryId=balanceIdkeySignDict.get(initKeySignStr);
                        if(auxiliaryId!=null){
                            subjectInitAuxiliary.setId(auxiliaryId);
                        }
                    }

                    if(!Constant.Number.INITIAL.equals(subjectInitAuxiliary.getPeriodNum())){
                        balanceSubjectAuxiliaryMapper.updateSubjectInitAuxiliaryDel(subjectInitAuxiliary);
                    }else {
                        balanceSubjectAuxiliaryMapper.updateSubjectInitAuxiliaryDelOne(subjectInitAuxiliary);
                    }
                    //清除第0期数据
                    balanceSubjectAuxiliaryMapper.delete(new LambdaQueryWrapper<BalanceSubjectAuxiliary>()
                            .eq(BalanceSubjectAuxiliary::getAccountBookId,subjectInitAuxiliary.getAccountBookId())
                            .eq(BalanceSubjectAuxiliary::getAccountBookEntityId,subjectInitAuxiliary.getAccountBookEntityId())
                            .eq(BalanceSubjectAuxiliary::getAccountSubjectId,subjectInitAuxiliary.getAccountSubjectId())
                            .eq(BalanceSubjectAuxiliary::getPeriodYear,subjectInitAuxiliary.getPeriodYear())
                            .eq(BalanceSubjectAuxiliary::getPeriodNum,Constant.Number.ANTI_INITLIZED));
                    //清除第0期数据
                    balanceSubjectAuxiliaryItemMapper.delete(new LambdaQueryWrapper<BalanceSubjectAuxiliaryItem>()
                            .eq(BalanceSubjectAuxiliaryItem::getAccountBookId,subjectInitAuxiliary.getAccountBookId())
                            .eq(BalanceSubjectAuxiliaryItem::getAccountBookEntityId,subjectInitAuxiliary.getAccountBookEntityId())
                            .eq(BalanceSubjectAuxiliaryItem::getAccountSubjectId,subjectInitAuxiliary.getAccountSubjectId())
                            .eq(BalanceSubjectAuxiliaryItem::getPeriodYear,subjectInitAuxiliary.getPeriodYear())
                            .eq(BalanceSubjectAuxiliaryItem::getPeriodNum,Constant.Number.ANTI_INITLIZED));
                }
            }

        }

        if(!CollectionUtils.isEmpty(balanceCashFlowInits)){
            for(BalanceCashFlowInit balanceCashFlowInit :balanceCashFlowInits){
                balanceCashFlowMapper.updateCashFlowDel(balanceCashFlowInit);
            }
        }

        AccountBookSystemDto accountBookSystemDto = new AccountBookSystemDto();
        accountBookSystemDto.setId(balanceInitDto.getAccountBookSystemId());
        //更新子系统状态
        accountBookSystemFeignClient.antiInitSystemById(accountBookSystemDto);
    }

    private void updateCashStatus(BalanceInitDto balanceInitDto, Byte cashFlowStatus, BalanceInitRecord balanceInitRecordTwo, SysUserVo user) {
        if (Constant.Is.YES.equals(cashFlowStatus)) {
            balanceInitRecordTwo.setCashStatus(LedgerConstant.CashStatus.UN_RECORD);
        } else {
            balanceInitRecordTwo.setCashStatus(LedgerConstant.CashStatus.UNUSED);
        }
        balanceInitRecordMapper.update(balanceInitRecordTwo, new LambdaQueryWrapper<BalanceInitRecord>()
                .eq(BalanceInitRecord::getAccountBookId, balanceInitDto.getAccountBookId())
                .eq(BalanceInitRecord::getPeriodYear, balanceInitDto.getPeriodYear())
                .eq(BalanceInitRecord::getRootEnterpriseId, user.getRootEnterpriseId())
                .eq(BalanceInitRecord::getPeriodNum, balanceInitDto.getPeriodNum()));
    }

    /**
     * 账簿反初始化批量校验
     *
     * @param balanceInitDto
     * @return com.njwd.support.BatchResult
     * @Author lj
     * @Date:11:27 2019/8/12
     **/
    @Override
    public BalanceInitVo balanceDisInitBatchCheck(BalanceInitDto balanceInitDto) {
        BalanceInitVo balanceInitVoR = new BalanceInitVo();
        List<BalanceInitVo> balanceInitVoList = new ArrayList<BalanceInitVo>();
        balanceInitVoR.setBalanceInitVoList(balanceInitVoList);
        BalanceInitVo balanceInitVo = null;
        List<BalanceInitCheckVo> balanceInitCheckVos = null;
        BalanceInitCheckVo balanceInitCheckVo = null;
        for(BalanceInitDto initDto :balanceInitDto.getBalanceInitList()) {
            balanceInitVo = new BalanceInitVo();
            balanceInitVo.setAccountBookName(initDto.getAccountBookName());
            balanceInitVoList.add(balanceInitVo);
            balanceInitCheckVos = new ArrayList<BalanceInitCheckVo>();
            balanceInitCheckVo = new BalanceInitCheckVo();
            balanceInitCheckVo.setCheckContext(LedgerConstant.BalanceInit.DIS_INIT_CHECK_CONTEXT);
//            balanceInitCheckVo.setDescription(LedgerConstant.BalanceInit.DIS_INIT_DESCRIPTION);
            //校验最近账期是否结账：获取账簿启用期间 期间状态是否为 未结账 ，是则通过，否则为不通过
            AccountBookPeriod accountBookPeriod = accountBookPeriodMapper.selectOne(new LambdaQueryWrapper<AccountBookPeriod>()
                    .eq(AccountBookPeriod::getAccountBookId, initDto.getAccountBookId())
                    .eq(AccountBookPeriod::getAccountBookSystemId,initDto.getAccountBookSystemId())
                    .eq(AccountBookPeriod::getPeriodYear, initDto.getPeriodYear())
                    .eq(AccountBookPeriod::getPeriodNum, initDto.getPeriodNum()));
            //未结账
            if (accountBookPeriod != null && !Constant.Is.NO.equals(accountBookPeriod.getIsSettle())) {
                balanceInitVo.setAccountStatus(LedgerConstant.BalanceInit.CHECK_STATUS_UN_PASS);
                balanceInitCheckVo.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_UN_PASS);
                balanceInitVoR.getFailList().add(initDto);
            } else {
                balanceInitCheckVo.setCheckStatus(LedgerConstant.BalanceInit.CHECK_STATUS_PASS);
                balanceInitVo.setAccountStatus(LedgerConstant.BalanceInit.CHECK_STATUS_PASS);
                balanceInitVoR.getSuccessList().add(initDto);
            }
            balanceInitCheckVos.add(balanceInitCheckVo);
            balanceInitVo.setBalanceInitCheckVos(balanceInitCheckVos);
        }

        return balanceInitVoR;
    }

    /**
     * 账簿反初始化批量更新数据
     *
     * @param balanceInitDto
     * @return com.njwd.support.BatchResult
     * @Author lj
     * @Date:11:27 2019/8/12
     **/
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BalanceInitVo balanceDisInitBatchUpdate(BalanceInitDto balanceInitDto) {
        BalanceInitVo balanceInitVo = new BalanceInitVo();
        for(BalanceInitDto initDto :balanceInitDto.getBalanceInitList()){
            List<Long> accountBookIds = balanceInitVo.getAccountBookIds();
            accountBookIds.add(initDto.getAccountBookId());
            disInitData(initDto);
        }
        return balanceInitVo;
    }

    /**
     * @param balanceInitVo
     * @param response
     * @description: 导出校验错误信息excel
     * @param: [balanceInitVo, response]
     * @return: void
     * @author: xdy
     * @create: 2019-10-22 15:19
     */
    @Override
    public void exportExcel(BalanceInitVo balanceInitVo, HttpServletResponse response) {
        List<BalanceInitCheckVo> balanceInitCheckVoList = balanceInitVo.getBalanceInitCheckVos();
        fileService.exportExcel(response,
                balanceInitCheckVoList,
                balanceInitVo.getAccountBookName()+Constant.Character.THROUGH_LINE+ ExcelColumnConstant.BalanceInit.ACCOUNT_BOOK_NAME,
                ExcelColumnConstant.BalanceInit.CHECK_CONTEXT
                ,ExcelColumnConstant.BalanceInit.CHECK_STATUS
                ,ExcelColumnConstant.BalanceInit.CHECK_DESCRIPTION);
    }
}
