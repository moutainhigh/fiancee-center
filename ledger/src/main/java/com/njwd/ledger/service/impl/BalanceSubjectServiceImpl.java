package com.njwd.ledger.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.njwd.common.Constant;
import com.njwd.common.ExcelColumnConstant;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.basedata.dto.AccountBookEntityDto;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.BalanceSubject;
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
import com.njwd.ledger.mapper.BalanceSubjectMapper;
import com.njwd.ledger.service.AccountBookPeriodService;
import com.njwd.ledger.service.BalanceSubjectService;
import com.njwd.service.FileService;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.MergeUtil;
import com.njwd.utils.StringUtil;
import com.njwd.utils.UserUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 科目余额表实现类
 * @Author 周鹏
 * @Date 2019/8/5
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BalanceSubjectServiceImpl extends ServiceImpl<BalanceSubjectMapper, BalanceSubject> implements BalanceSubjectService {
    @Autowired
    private AccountSubjectFeignClient accountSubjectFeignClient;

    @Autowired
    private AccountBookPeriodService accountBookPeriodService;

    @Autowired
    private FileService fileService;

    @Autowired
    private BalanceSubjectMapper balanceSubjectMapper;

    @Autowired
    private BalanceSubjectAuxiliaryItemMapper balanceSubjectAuxiliaryItemMapper;

    @Autowired
    private BalanceSubjectAuxiliaryMapper balanceSubjectAuxiliaryMapper;

    /**
     * 更新发生额
     *
     * @param balanceSubjects balanceSubjects
     * @param voucherDto      voucherDto
     * @param updateType
     * @return int
     * @author xyyxhcj@qq.com
     * @date 2019/8/13 20:28
     **/
    @Override
    public int updateBatch(Collection<BalanceSubjectDto> balanceSubjects, VoucherDto voucherDto, byte updateType) {
        return balanceSubjectMapper.updateBatch(balanceSubjects, voucherDto, updateType);
    }

    /**
     * 根据条件统计科目余额表
     *
     * @param balanceSubjectQueryDto
     * @return BalanceSubjectVo
     * @author: 周鹏
     * @create: 2019/8/5
     */
    @Override
    public List<BalanceSubjectVo> findListByParam(BalanceSubjectQueryDto balanceSubjectQueryDto) {
        //最终结果集
        List<BalanceSubjectVo> finalResultList = new LinkedList<>();
        //处理结果集
        List<BalanceSubjectVo> resultList = new ArrayList<>();
        //查询科目余额相关信息
        List<BalanceSubjectVo> balanceSubjectList = balanceSubjectMapper.findListByParam(balanceSubjectQueryDto);
        if (balanceSubjectList.size() > 0) {
            //根据核算主体id组装信息
            MergeUtil.merge(balanceSubjectList, balanceSubjectQueryDto.getAccountBookEntityList(),
                    BalanceSubjectVo::getAccountBookEntityId, AccountBookEntityDto::getId,
                    (balanceSubjectVo, accountBookEntity) -> {
                        balanceSubjectVo.setAccountBookName(accountBookEntity.getAccountBookName());
                        balanceSubjectVo.setAccountBookEntityName(accountBookEntity.getEntityName());
                    });
            AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
            FastUtils.copyProperties(balanceSubjectQueryDto, accountSubjectDto);
            accountSubjectDto.setIds(balanceSubjectQueryDto.getSubjectIds());
            //查询所选会计科目区间信息
            Result<List<AccountSubjectVo>> subjectResult = accountSubjectFeignClient.findInfoForLedger(accountSubjectDto);
            List<AccountSubjectVo> subjectList = subjectResult.getData();
            //将科目余额表的会计科目id传过去
            List<Long> accountSubjectIdList = balanceSubjectList.stream().map(BalanceSubjectVo::getAccountSubjectId).collect(Collectors.toList());
            AccountSubjectDto finalAccountSubjectDto = new AccountSubjectDto();
            finalAccountSubjectDto.setIsIncludeEnable(balanceSubjectQueryDto.getIsIncludeEnable());
            finalAccountSubjectDto.setFinalIds(accountSubjectIdList);
            //查询科目余额表的会计科目信息
            Result<List<AccountSubjectVo>> finalSubjectResult = accountSubjectFeignClient.findInfoForLedger(finalAccountSubjectDto);
            List<AccountSubjectVo> finalSubjectList = finalSubjectResult.getData();

            //包含未记账凭证标识
            Boolean includeUnbookedFlag = balanceSubjectQueryDto.getIsIncludeUnbooked().equals(Constant.Is.YES);
            //包含损益结转凭证标识
            Boolean includeProfitAndLossFlag = balanceSubjectQueryDto.getIsIncludeProfitAndLoss().equals(Constant.Is.YES);
            //是否跨年标识
            Boolean beyondYearFlag = balanceSubjectQueryDto.getPeriodYears().get(0) < balanceSubjectQueryDto.getPeriodYears().get(1);
            //根据会计科目id组装数据
            initSubjectData(balanceSubjectList, finalSubjectList, balanceSubjectQueryDto, includeUnbookedFlag, includeProfitAndLossFlag, beyondYearFlag);
            List<BalanceSubjectVo> auxiliaryResultList = new ArrayList<>();
            if (balanceSubjectQueryDto.getIsShowAuxiliaryDetail().equals(Constant.Is.YES)) {
                //查询辅助核算项余额信息
                getAuxiliaryBalanceList(auxiliaryResultList, accountSubjectIdList, balanceSubjectList, balanceSubjectQueryDto, includeUnbookedFlag, includeProfitAndLossFlag);
            }
            //计算每条会计科目的金额信息,并组装科目余额表信息,组装结果集
            dealResultList(subjectList, balanceSubjectList, auxiliaryResultList, balanceSubjectQueryDto, resultList);
            //排序并计算合计
            if (CollectionUtils.isNotEmpty(resultList)) {
                //排序
                resultList.stream().sorted(Comparator.comparing(BalanceSubjectVo::getAccountBookId)
                        .thenComparing(BalanceSubjectVo::getAccountBookEntityId)
                        .thenComparing(BalanceSubjectVo::getPeriodYear)
                        .thenComparing((o1, o2) -> Integer.valueOf(o2.getCode().substring(0, 4)) - Integer.valueOf(o1.getCode().substring(0, 4)))
                        .thenComparing(BalanceSubjectVo::getCode)
                        .thenComparing(BalanceSubjectVo::getAuxiliaryCode, Comparator.nullsFirst(Comparator.naturalOrder()))
                ).collect(Collectors.toList());
                //计算合计并返回最终结果集
                countTotalInfo(finalResultList, resultList);
            }
        }
        return finalResultList;
    }

    /**
     * 根据条件查询凭证号
     *
     * @param balanceSubjectQueryDto
     * @return BalanceSubjectVo
     * @author: 周鹏
     * @create: 2019/8/17
     */
    @Override
    public String findVoucherNumberByParam(BalanceSubjectQueryDto balanceSubjectQueryDto) {
        SysUserVo operator = UserUtils.getUserVo();
        balanceSubjectQueryDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        return balanceSubjectMapper.findVoucherNumberByParam(balanceSubjectQueryDto);
    }

    /**
     * 根据条件统计科目汇总表
     *
     * @param balanceSubjectQueryDto
     * @return BalanceSubjectVo
     * @author: 周鹏
     * @create: 2019/8/16
     */
    @Override
    public List<BalanceSubjectVo> findCollectListByParam(BalanceSubjectQueryDto balanceSubjectQueryDto) {
        //最终结果集
        List<BalanceSubjectVo> finalResultList = new LinkedList<>();
        //处理结果集
        List<BalanceSubjectVo> resultList = new ArrayList<>();
        //step1:查询科目余额表数据或凭证分录表数据
        List<BalanceSubjectVo> balanceSubjectList;
        if (balanceSubjectQueryDto.getVoucherDateOperator() != null) {
            SysUserVo operator = UserUtils.getUserVo();
            balanceSubjectQueryDto.setRootEnterpriseId(operator.getRootEnterpriseId());
            balanceSubjectList = balanceSubjectMapper.findVoucherListByParam(balanceSubjectQueryDto);
        } else {
            balanceSubjectList = balanceSubjectMapper.findCollectListByParam(balanceSubjectQueryDto);
        }
        if (balanceSubjectList.size() > 0) {
            //根据核算主体id组装信息
            MergeUtil.merge(balanceSubjectList, balanceSubjectQueryDto.getAccountBookEntityList(),
                    BalanceSubjectVo::getAccountBookEntityId, AccountBookEntityDto::getId,
                    (balanceSubjectVo, accountBookEntity) -> {
                        balanceSubjectVo.setAccountBookName(accountBookEntity.getAccountBookName());
                        balanceSubjectVo.setAccountBookEntityName(accountBookEntity.getEntityName());
                    });
            //step2:查询会计科目数据
            AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
            FastUtils.copyProperties(balanceSubjectQueryDto, accountSubjectDto);
            accountSubjectDto.setIds(balanceSubjectQueryDto.getSubjectIds());
            //查询所选会计科目区间信息
            Result<List<AccountSubjectVo>> subjectResult = accountSubjectFeignClient.findInfoForLedger(accountSubjectDto);
            List<AccountSubjectVo> subjectList = subjectResult.getData();
            //将科目余额表的会计科目id传过去
            List<Long> accountSubjectIdList = balanceSubjectList.stream().map(BalanceSubjectVo::getAccountSubjectId).collect(Collectors.toList());
            AccountSubjectDto finalAccountSubjectDto = new AccountSubjectDto();
            finalAccountSubjectDto.setIsIncludeEnable(balanceSubjectQueryDto.getIsIncludeEnable());
            finalAccountSubjectDto.setFinalIds(accountSubjectIdList);
            //查询科目余额表的会计科目信息
            Result<List<AccountSubjectVo>> finalSubjectResult = accountSubjectFeignClient.findInfoForLedger(finalAccountSubjectDto);
            List<AccountSubjectVo> finalSubjectList = finalSubjectResult.getData();

            //包含未记账凭证标识
            Boolean includeUnbookedFlag = balanceSubjectQueryDto.getIsIncludeUnbooked().equals(Constant.Is.YES);
            //包含损益结转凭证标识
            Boolean includeProfitAndLossFlag = balanceSubjectQueryDto.getIsIncludeProfitAndLoss().equals(Constant.Is.YES);
            //step3:根据会计科目id组装数据
            initCollectData(balanceSubjectList, finalSubjectList, balanceSubjectQueryDto, includeUnbookedFlag, includeProfitAndLossFlag);
            //step4:计算上级会计科目金额,并将上级会计科目数据放入处理结果集
            dealCollectResultList(subjectList, balanceSubjectList, balanceSubjectQueryDto, resultList);
            //step5:排序并计算合计
            if (CollectionUtils.isNotEmpty(resultList)) {
                //排序
                resultList.stream().sorted(Comparator.comparing(BalanceSubjectVo::getAccountBookId)
                        .thenComparing(BalanceSubjectVo::getAccountBookEntityId)
                        .thenComparing((o1, o2) -> Integer.valueOf(o2.getCode().substring(0, 4)) - Integer.valueOf(o1.getCode().substring(0, 4)))
                        .thenComparing(BalanceSubjectVo::getCode)).collect(Collectors.toList());
                //计算合计并返回最终结果集
                countTotalInfo(finalResultList, resultList);
            }
        }
        return finalResultList;
    }

    /**
     * 根据账簿ID/核算主体ID 查询科目信息
     *
     * @param balanceSubjectQueryDto
     * @return
     */
    @Override
    public List<BalanceSubjectVo> findListByAccountBookIdAndEntityId(BalanceSubjectQueryDto balanceSubjectQueryDto) {
        return balanceSubjectMapper.findListByAccountBookIdAndEntityId(balanceSubjectQueryDto);
    }

    /**
     * 查询所有辅助核算项信息
     *
     * @param auxiliaryItemVoList 辅助核算值来源表及id信息
     * @return
     */
    @Override
    public List<List<Map<String, Object>>> findAllSourceTableInfo(List<BalanceSubjectAuxiliaryItemVo> auxiliaryItemVoList) {
        //组装sourceTable和ids
        AccountSubjectDto param = new AccountSubjectDto();
        List<String> sourceTableList = new LinkedList<>();
        List<List<List<Long>>> idLists = new LinkedList<>();
        List<List<Long>> idList;
        List<Long> ids;
        String[] sourceTables;
        String[] itemValueIds;
        for (BalanceSubjectAuxiliaryItemVo auxiliaryItemVo : auxiliaryItemVoList) {
            sourceTableList.add(auxiliaryItemVo.getSourceTables());
            sourceTables = auxiliaryItemVo.getSourceTables().split(",");
            itemValueIds = auxiliaryItemVo.getItemValueIds().split(",");
            idList = new LinkedList<>();
            for (int i = 0; i < sourceTables.length; i++) {
                ids = new LinkedList<>();
                ids.add(Long.valueOf(itemValueIds[i]));
                idList.add(ids);
            }
            idLists.add(idList);
        }
        param.setSourceTableList(sourceTableList);
        param.setIdLists(idLists);
        //查询所有辅助核算项信息
        Result<List<List<Map<String, Object>>>> allSourceTableInfo = accountSubjectFeignClient.findAllSourceTableInfo(param);
        return allSourceTableInfo.getData();
    }

    /**
     * 根据账簿id/核算主体id/期间 查询科目发生额累计
     *
     * @param balanceSubjectQueryDto
     * @return
     */
    @Override
    public List<BalanceSubjectVo> getAccumulateBalanceByPeriodNum(BalanceSubjectQueryDto balanceSubjectQueryDto) {
        return balanceSubjectMapper.getAccumulateBalanceByPeriodNum(balanceSubjectQueryDto);
    }

    /**
     * 组装辅助核算编码和名称
     *
     * @param auxiliaryList 辅助核算信息
     * @param sourceTable   辅助核算表名
     * @param itemValueId   辅助核算id
     * @param auxiliaryCode 辅助核算编码
     * @param auxiliaryName 辅助核算名称
     */
    @Override
    public void initAuxiliaryNameAndCode(List<Map<String, Object>> auxiliaryList, StringBuilder sourceTable, StringBuilder itemValueId,
                                         StringBuilder auxiliaryCode, StringBuilder auxiliaryName) {
        for (Map<String, Object> map : auxiliaryList) {
            if (StringUtil.isNotEmpty(sourceTable.toString())) {
                sourceTable.append("|");
            }
            sourceTable.append(map.get("sourceTable"));
            if (StringUtil.isNotEmpty(itemValueId.toString())) {
                itemValueId.append("|");
            }
            itemValueId.append(map.get("id"));
            if (StringUtil.isNotEmpty(auxiliaryCode.toString())) {
                auxiliaryCode.append("|");
            }
            auxiliaryCode.append(map.get("code"));
            if (StringUtil.isNotEmpty(auxiliaryName.toString())) {
                auxiliaryName.append("|");
            }
            auxiliaryName.append(map.get("name"));
        }
    }

    /**
     * Excel 导出科目余额表
     *
     * @param balanceSubjectQueryDto
     * @param response
     * @author: 周鹏
     * @create: 2019/8/29
     */
    @Override
    public void exportListExcel(BalanceSubjectQueryDto balanceSubjectQueryDto, HttpServletResponse response) {
        List<BalanceSubjectVo> list = findListByParam(balanceSubjectQueryDto);
        //是否跨年标识
        Boolean beyondYearFlag = balanceSubjectQueryDto.getPeriodYears().get(0) < balanceSubjectQueryDto.getPeriodYears().get(1);
        //是否显示辅助核算明细标识
        Boolean iShowAuxiliaryDetailFlag = balanceSubjectQueryDto.getIsShowAuxiliaryDetail().equals(Constant.Is.YES);
        if (beyondYearFlag) {
            if (iShowAuxiliaryDetailFlag) {
                fileService.exportExcel(response, list,
                        ExcelColumnConstant.BalanceSubject.ACCOUNT_BOOK_NAME, ExcelColumnConstant.BalanceSubject.ACCOUNT_BOOK_ENTITY_NAME,
                        ExcelColumnConstant.BalanceSubject.PERIOD_YEAR, ExcelColumnConstant.BalanceSubject.CODE,
                        ExcelColumnConstant.BalanceSubject.NAME, ExcelColumnConstant.BalanceSubject.AUXILIARY_CODE,
                        ExcelColumnConstant.BalanceSubject.AUXILIARY_NAME, ExcelColumnConstant.BalanceSubject.OPENING_DIRECTION_NAME,
                        ExcelColumnConstant.BalanceSubject.OPENING_BALANCE, ExcelColumnConstant.BalanceSubject.DEBIT_AMOUNT,
                        ExcelColumnConstant.BalanceSubject.CREDIT_AMOUNT, ExcelColumnConstant.BalanceSubject.TOTAL_DEBIT_AMOUNT,
                        ExcelColumnConstant.BalanceSubject.TOTAL_CREDIT_AMOUNT, ExcelColumnConstant.BalanceSubject.CLOSING_DIRECTION_NAME,
                        ExcelColumnConstant.BalanceSubject.CLOSING_BALANCE);
            } else {
                fileService.exportExcel(response, list,
                        ExcelColumnConstant.BalanceSubject.ACCOUNT_BOOK_NAME, ExcelColumnConstant.BalanceSubject.ACCOUNT_BOOK_ENTITY_NAME,
                        ExcelColumnConstant.BalanceSubject.PERIOD_YEAR, ExcelColumnConstant.BalanceSubject.CODE,
                        ExcelColumnConstant.BalanceSubject.NAME, ExcelColumnConstant.BalanceSubject.OPENING_DIRECTION_NAME,
                        ExcelColumnConstant.BalanceSubject.OPENING_BALANCE, ExcelColumnConstant.BalanceSubject.DEBIT_AMOUNT,
                        ExcelColumnConstant.BalanceSubject.CREDIT_AMOUNT, ExcelColumnConstant.BalanceSubject.TOTAL_DEBIT_AMOUNT,
                        ExcelColumnConstant.BalanceSubject.TOTAL_CREDIT_AMOUNT, ExcelColumnConstant.BalanceSubject.CLOSING_DIRECTION_NAME,
                        ExcelColumnConstant.BalanceSubject.CLOSING_BALANCE);
            }
        } else {
            if (iShowAuxiliaryDetailFlag) {
                fileService.exportExcel(response, list,
                        ExcelColumnConstant.BalanceSubject.ACCOUNT_BOOK_NAME, ExcelColumnConstant.BalanceSubject.ACCOUNT_BOOK_ENTITY_NAME,
                        ExcelColumnConstant.BalanceSubject.CODE,
                        ExcelColumnConstant.BalanceSubject.NAME, ExcelColumnConstant.BalanceSubject.AUXILIARY_CODE,
                        ExcelColumnConstant.BalanceSubject.AUXILIARY_NAME, ExcelColumnConstant.BalanceSubject.OPENING_DIRECTION_NAME,
                        ExcelColumnConstant.BalanceSubject.OPENING_BALANCE, ExcelColumnConstant.BalanceSubject.DEBIT_AMOUNT,
                        ExcelColumnConstant.BalanceSubject.CREDIT_AMOUNT, ExcelColumnConstant.BalanceSubject.TOTAL_DEBIT_AMOUNT,
                        ExcelColumnConstant.BalanceSubject.TOTAL_CREDIT_AMOUNT, ExcelColumnConstant.BalanceSubject.CLOSING_DIRECTION_NAME,
                        ExcelColumnConstant.BalanceSubject.CLOSING_BALANCE);
            } else {
                fileService.exportExcel(response, list,
                        ExcelColumnConstant.BalanceSubject.ACCOUNT_BOOK_NAME, ExcelColumnConstant.BalanceSubject.ACCOUNT_BOOK_ENTITY_NAME,
                        ExcelColumnConstant.BalanceSubject.CODE,
                        ExcelColumnConstant.BalanceSubject.NAME, ExcelColumnConstant.BalanceSubject.OPENING_DIRECTION_NAME,
                        ExcelColumnConstant.BalanceSubject.OPENING_BALANCE, ExcelColumnConstant.BalanceSubject.DEBIT_AMOUNT,
                        ExcelColumnConstant.BalanceSubject.CREDIT_AMOUNT, ExcelColumnConstant.BalanceSubject.TOTAL_DEBIT_AMOUNT,
                        ExcelColumnConstant.BalanceSubject.TOTAL_CREDIT_AMOUNT, ExcelColumnConstant.BalanceSubject.CLOSING_DIRECTION_NAME,
                        ExcelColumnConstant.BalanceSubject.CLOSING_BALANCE);
            }
        }

    }

    /**
     * Excel 导出科目汇总表
     *
     * @param balanceSubjectQueryDto
     * @param response
     * @author: 周鹏
     * @create: 2019/8/29
     */
    @Override
    public void exportCollectListExcel(BalanceSubjectQueryDto balanceSubjectQueryDto, HttpServletResponse response) {
        List<BalanceSubjectVo> list = findCollectListByParam(balanceSubjectQueryDto);
        fileService.exportExcel(response, list,
                ExcelColumnConstant.BalanceSubject.ACCOUNT_BOOK_NAME, ExcelColumnConstant.BalanceSubject.ACCOUNT_BOOK_ENTITY_NAME,
                ExcelColumnConstant.BalanceSubject.CODE, ExcelColumnConstant.BalanceSubject.NAME,
                ExcelColumnConstant.BalanceSubject.DEBIT_AMOUNT, ExcelColumnConstant.BalanceSubject.CREDIT_AMOUNT);
    }

    /**
     * @param balanceSubjectQueryDto
     * @return java.util.List<com.njwd.entity.ledger.vo.BalanceSubjectVo>
     * @description: 根据科目id查询指定科目余额信息
     * @Param [balanceSubjectQueryDto]
     * @author LuoY
     * @date 2019/9/18 9:55
     */
    @Override
    public List<BalanceSubject> findBalanceSubjectInfoBySubjectId(BalanceSubjectQueryDto balanceSubjectQueryDto) {
        return balanceSubjectMapper.selectBalanceSubjectBySubjectId(balanceSubjectQueryDto);
    }

    /**
     * @param balanceSubjectQueryDto
     * @return java.util.List<com.njwd.entity.ledger.BalanceSubject>
     * @description: 根据核算账簿, 主体, 期间查询利润表数据
     * @Param [balanceSubjectQueryDto]
     * @author LuoY
     * @date 2019/9/27 16:33
     */
    @Override
    public List<BalanceSubjectVo> findBalanceSubjectInfoByAccountInfo(BalanceSubjectQueryDto balanceSubjectQueryDto) {
        return balanceSubjectMapper.selectBalanceSubjectInfoByAccount(balanceSubjectQueryDto);
    }

    /**
     * 根据会计科目id组装数据(科目余额表)
     *
     * @param balanceSubjectList       科目余额信息列表
     * @param subjectList              会计科目信息列表
     * @param balanceSubjectQueryDto   页面查询条件
     * @param includeUnbookedFlag      是否包含未过账凭证标识
     * @param includeProfitAndLossFlag 是否包含损益结转凭证标识
     * @param beyondYearFlag           是否跨年标识
     */
    private void initSubjectData(List<BalanceSubjectVo> balanceSubjectList, List<AccountSubjectVo> subjectList, BalanceSubjectQueryDto balanceSubjectQueryDto,
                                 Boolean includeUnbookedFlag, Boolean includeProfitAndLossFlag, Boolean beyondYearFlag) {
        //设置会计科目基础信息
        setSubjectInfo(balanceSubjectList, subjectList, balanceSubjectQueryDto);
        //查询所有科目余额信息对应的期间范围
        List<AccountBookPeriodVo> periodAreaList = accountBookPeriodService.findPeriodAreaByYear(balanceSubjectList);
        periodAreaList = periodAreaList.stream().distinct().collect(Collectors.toList());
        //开始期间后最近的已结账期间
        List<BalanceSubjectQueryDto> beginQueryList = new LinkedList<>();
        //结束期间前最近的已结账期间
        List<BalanceSubjectQueryDto> endQueryList = new LinkedList<>();
        //设置科目余额信息的期间区间
        setPeriodInfo(balanceSubjectList, balanceSubjectQueryDto, beyondYearFlag, periodAreaList, beginQueryList, endQueryList);

        //查询账簿的启用期间及当前会计科目在启用期间的金额信息
        List<BalanceSubjectVo> startPeriodBalanceList = balanceSubjectMapper.findStartPeriodBalance(balanceSubjectList);
        //设置科目余额的启用期间的金额信息
        MergeUtil.merge(startPeriodBalanceList, balanceSubjectList,
                startPeriodBalanceVo -> startPeriodBalanceVo.getAccountBookEntityId() + Constant.Character.UNDER_LINE + startPeriodBalanceVo.getAccountSubjectId(),
                balanceSubjectVo -> balanceSubjectVo.getAccountBookEntityId() + Constant.Character.UNDER_LINE + balanceSubjectVo.getAccountSubjectId(),
                (startPeriodBalanceVo, balanceSubjectVo) -> balanceSubjectVo.setStartPeriodBalanceVo(startPeriodBalanceVo));

        //查询每条数据开始期间后最近的已结账期间的期初和期末
        List<BalanceSubjectVo> beginSubjectVoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(beginQueryList)) {
            beginSubjectVoList = balanceSubjectMapper.findInfoByPeriod(beginQueryList);
        }
        //查询每条数据结束期间前最近的已结账期间的期初和期末
        List<BalanceSubjectVo> endSubjectVoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(endQueryList)) {
            endSubjectVoList = balanceSubjectMapper.findInfoByPeriod(endQueryList);
        }
        //设置科目余额信息
        setBalanceSubjectData(balanceSubjectList, includeUnbookedFlag, includeProfitAndLossFlag, beginSubjectVoList, endSubjectVoList);
    }

    /**
     * 设置科目余额信息
     *
     * @param balanceSubjectList       科目余额列表
     * @param includeUnbookedFlag      是否包含未过账凭证标识
     * @param includeProfitAndLossFlag 是否包含损益结转凭证标识
     * @param beginSubjectVoList       开始期间后最近的已结账期间的期初和期末
     * @param endSubjectVoList         结束期间前最近的已结账期间的期初和期末
     */
    private void setBalanceSubjectData(List<BalanceSubjectVo> balanceSubjectList, Boolean includeUnbookedFlag, Boolean includeProfitAndLossFlag, List<BalanceSubjectVo> beginSubjectVoList, List<BalanceSubjectVo> endSubjectVoList) {
        BalanceSubjectVo beginSubjectVo = null;
        for (BalanceSubjectVo balanceSubjectVo : balanceSubjectList) {
            if (StringUtil.isEmpty(balanceSubjectVo.getCode())) {
                //科目编码为空,表示页面选择不包含禁用科目,并且此科目已禁用
                continue;
            }
            if (balanceSubjectVo.getEndSettledPeriod() >= balanceSubjectVo.getBeginPeriod() && balanceSubjectVo.getEndSettledPeriod() <= balanceSubjectVo.getEndPeriod()) {
                //结束期间前最近的已结账期间在当前数据所对应的会计期间内,直接设置期初余额为开始期间后最近的已结账期间的期初
                for (BalanceSubjectVo beginVo : beginSubjectVoList) {
                    if (balanceSubjectVo.getAccountBookEntityId().equals(beginVo.getAccountBookEntityId())
                            && balanceSubjectVo.getAccountSubjectId().equals(beginVo.getAccountSubjectId())
                            && balanceSubjectVo.getBeginPeriodYear().equals(beginVo.getPeriodYear())
                            && balanceSubjectVo.getBeginPeriodNumber().equals(beginVo.getPeriodNum())) {
                        beginSubjectVo = beginVo;
                        break;
                    }
                }
                balanceSubjectVo.setOpeningBalance(beginSubjectVo == null || beginSubjectVo.getOpeningBalance() == null ?
                        BigDecimal.ZERO : beginSubjectVo.getOpeningBalance());
            }
            for (BalanceSubjectVo endVo : endSubjectVoList) {
                if (balanceSubjectVo.getAccountBookEntityId().equals(endVo.getAccountBookEntityId())
                        && balanceSubjectVo.getAccountSubjectId().equals(endVo.getAccountSubjectId())
                        && balanceSubjectVo.getEndPeriodYear().equals(endVo.getPeriodYear())
                        && balanceSubjectVo.getEndPeriodNumber().equals(endVo.getPeriodNum())) {
                    balanceSubjectVo.setEndSubjectVo(endVo);
                    break;
                }
            }
            //查询从结束期间前最近的已结账期间（不包含此期间）到结束期间的余额信息
            List<Integer> periodYears = new LinkedList<>();
            List<Byte> periodNumbers = new LinkedList<>();
            if (balanceSubjectVo.getEndSettledPeriodVo() == null) {
                //如果此账簿未结账,则从此账簿的启用期间开始查金额
                periodYears.add(0);
                periodNumbers.add((byte) 0);
            } else {
                periodYears.add(balanceSubjectVo.getEndSettledPeriodVo().getPeriodYear());
                periodNumbers.add(balanceSubjectVo.getEndSettledPeriodVo().getPeriodNum());
            }
            periodYears.add(balanceSubjectVo.getPeriodYear());
            periodNumbers.add(balanceSubjectVo.getEndNumber());
            BalanceSubjectQueryDto queryDto = new BalanceSubjectQueryDto();
            queryDto.setAccountBookEntityId(balanceSubjectVo.getAccountBookEntityId());
            queryDto.setAccountSubjectId(balanceSubjectVo.getAccountSubjectId());
            queryDto.setPeriodYears(periodYears);
            queryDto.setPeriodNumbers(periodNumbers);
            List<BalanceSubjectVo> periodsList = balanceSubjectMapper.findInfoByPeriods(queryDto);
            if (balanceSubjectVo.getEndSubjectVo() == null) {
                balanceSubjectVo.setEndSubjectVo(new BalanceSubjectVo());
            }
            getSubjectBalance(includeUnbookedFlag, includeProfitAndLossFlag, periodsList, balanceSubjectVo);
            //设置期初余额和期末余额
            if (balanceSubjectVo.getEndSettledPeriod() < balanceSubjectVo.getBeginPeriod()) {
                //结束期间前最近的已结账期间小于当前数据的开始期间或账簿未结账,设置期初余额为开始期间的前一期间的期末余额
                balanceSubjectVo.setOpeningBalance(balanceSubjectVo.getEndSubjectVo().getOpeningBalance() == null ? BigDecimal.ZERO : balanceSubjectVo.getEndSubjectVo().getOpeningBalance());
            }
            balanceSubjectVo.setClosingBalance(balanceSubjectVo.getEndSubjectVo().getClosingBalance() == null ? BigDecimal.ZERO : balanceSubjectVo.getEndSubjectVo().getClosingBalance());
            //根据余额设置方向
            setDirectionName(balanceSubjectVo);
        }
    }

    /**
     * 设置科目余额信息的期间区间
     *
     * @param balanceSubjectList     科目余额信息
     * @param balanceSubjectQueryDto 查询条件
     * @param beyondYearFlag         是否跨年标识
     * @param periodAreaList         科目余额信息对应的期间范围
     * @param beginQueryList         开始期间后最近的已结账期间
     * @param endQueryList           结束期间前最近的已结账期间
     */
    private void setPeriodInfo(List<BalanceSubjectVo> balanceSubjectList, BalanceSubjectQueryDto balanceSubjectQueryDto, Boolean beyondYearFlag, List<AccountBookPeriodVo> periodAreaList, List<BalanceSubjectQueryDto> beginQueryList, List<BalanceSubjectQueryDto> endQueryList) {
        MergeUtil.mergeList(periodAreaList, balanceSubjectList,
                periodArea -> periodArea.getAccountBookId() + Constant.Character.UNDER_LINE + periodArea.getPeriodYear(),
                balanceSubject -> balanceSubject.getAccountBookId() + Constant.Character.UNDER_LINE + balanceSubject.getPeriodYear(),
                (periodArea, balanceSubjectVoList) -> {
                    if (CollectionUtils.isNotEmpty(balanceSubjectVoList)) {
                        BalanceSubjectVo balanceSubjectVo = balanceSubjectVoList.get(0);
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
                        BalanceSubjectQueryDto queryDto;
                        for (BalanceSubjectVo balanceVo : balanceSubjectVoList) {
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
                            if (beginSettledPeriodVo != null) {
                                balanceVo.setBeginPeriodYear(beginSettledPeriodVo.getPeriodYear());
                                balanceVo.setBeginPeriodNumber(beginSettledPeriodVo.getPeriodNum());
                                queryDto = new BalanceSubjectQueryDto();
                                queryDto.setAccountBookEntityId(balanceVo.getAccountBookEntityId());
                                queryDto.setAccountSubjectId(balanceVo.getAccountSubjectId());
                                queryDto.setPeriodYear(beginSettledPeriodVo.getPeriodYear());
                                queryDto.setPeriodNum(beginSettledPeriodVo.getPeriodNum());
                                beginQueryList.add(queryDto);
                            }
                            if (endSettledPeriodVo != null) {
                                balanceVo.setEndPeriodYear(endSettledPeriodVo.getPeriodYear());
                                balanceVo.setEndPeriodNumber(endSettledPeriodVo.getPeriodNum());
                                queryDto = new BalanceSubjectQueryDto();
                                queryDto.setAccountBookEntityId(balanceVo.getAccountBookEntityId());
                                queryDto.setAccountSubjectId(balanceVo.getAccountSubjectId());
                                queryDto.setPeriodYear(endSettledPeriodVo.getPeriodYear());
                                queryDto.setPeriodNum(endSettledPeriodVo.getPeriodNum());
                                endQueryList.add(queryDto);
                            }
                            //设置账簿的启用期间
                            balanceVo.setStartPeriod(startPeriodVo.getPeriodYearNum());
                        }
                    }
                });
    }

    /**
     * 设置会计科目基础信息
     *
     * @param balanceSubjectList     科目余额信息
     * @param subjectList            科目信息
     * @param balanceSubjectQueryDto 查询条件
     */
    private void setSubjectInfo(List<BalanceSubjectVo> balanceSubjectList, List<AccountSubjectVo> subjectList, BalanceSubjectQueryDto balanceSubjectQueryDto) {
        MergeUtil.merge(balanceSubjectList, subjectList,
                BalanceSubjectVo::getAccountSubjectId, AccountSubjectVo::getId,
                (balanceSubjectVo, accountSubjectVo) -> {
                    balanceSubjectVo.setAccountSubjectId(accountSubjectVo.getId());
                    balanceSubjectVo.setSubjectId(accountSubjectVo.getSubjectId());
                    balanceSubjectVo.setBalanceDirection(accountSubjectVo.getBalanceDirection());
                    balanceSubjectVo.setIsEnable(accountSubjectVo.getIsEnable());
                    balanceSubjectVo.setIsFinal(accountSubjectVo.getIsFinal());
                    balanceSubjectVo.setCode(accountSubjectVo.getCode());
                    if (balanceSubjectQueryDto.getIsShowFullName().equals(Constant.Is.NO)) {
                        balanceSubjectVo.setName(accountSubjectVo.getName());
                    } else if (balanceSubjectQueryDto.getIsShowFullName().equals(Constant.Is.YES)) {
                        balanceSubjectVo.setName(accountSubjectVo.getFullName());
                    }
                });
    }

    /**
     * 设置查询余额信息的期间范围
     *
     * @param balanceSubjectQueryDto   页面查询条件
     * @param beyondYearFlag           是否跨年标识
     * @param balanceSubjectVo         科目余额信息
     * @param includeUnbookedFlag      是否包含未过账凭证标识
     * @param includeProfitAndLossFlag 是否包含损益结转凭证标识
     */
    /*private void setPeriodsInfo(BalanceSubjectQueryDto balanceSubjectQueryDto, Boolean beyondYearFlag, BalanceSubjectVo balanceSubjectVo,
                                Boolean includeUnbookedFlag, Boolean includeProfitAndLossFlag) {
        Byte beginNumber = balanceSubjectQueryDto.getPeriodNumbers().get(0);
        Byte endNumber = balanceSubjectQueryDto.getPeriodNumbers().get(1);
        if (beyondYearFlag) {
            //根据账簿id和年度查询期间范围
            AccountBookPeriodDto param = new AccountBookPeriodDto();
            param.setAccountBookId(balanceSubjectVo.getAccountBookId());
            param.setPeriodYear(balanceSubjectVo.getPeriodYear());
            AccountBookPeriodVo periodArea = accountBookPeriodService.findPeriodAreaByYear(param);
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
        //设置当前数据的期间区间
        balanceSubjectVo.setBeginNumber(beginNumber);
        balanceSubjectVo.setEndNumber(endNumber);
        //查询开始期间后最近的已结账期间和结束期间前最近的已结账期间
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
        //设置当前数据的开始期间后最近的已结账期间和结束期间前最近的已结账期间
        balanceSubjectVo.setBeginSettledPeriodVo(beginSettledPeriodVo);
        balanceSubjectVo.setEndSettledPeriodVo(endSettledPeriodVo);
        //获取当前数据对应的期间范围
        String beginPeriodNum = String.format("%02d", beginNumber);
        Integer beginPeriod = Integer.valueOf(balanceSubjectVo.getPeriodYear() + beginPeriodNum);
        String endPeriodNum = String.format("%02d", endNumber);
        Integer endPeriod = Integer.valueOf(balanceSubjectVo.getPeriodYear() + endPeriodNum);
        Integer endSettledPeriod = 0;
        if (endSettledPeriodVo != null) {
            String endSettledPeriodYear = endSettledPeriodVo.getPeriodYear().toString();
            String endSettledPeriodNum = String.format("%02d", endSettledPeriodVo.getPeriodNum());
            endSettledPeriod = Integer.valueOf(endSettledPeriodYear + endSettledPeriodNum);
        }
        //设置当前数据对应的期间范围
        balanceSubjectVo.setBeginPeriod(beginPeriod);
        balanceSubjectVo.setEndPeriod(endPeriod);
        balanceSubjectVo.setEndSettledPeriod(endSettledPeriod);
        //查询余额信息
        getBalanceInfo(balanceSubjectVo, beginSettledPeriodVo, endSettledPeriodVo, endSettledPeriod, beginPeriod, endPeriod,
                endNumber, includeUnbookedFlag, includeProfitAndLossFlag);
    }*/

    /**
     * 查询余额信息
     *
     * @param balanceSubjectVo         科目余额信息
     * @param beginSettledPeriodVo     开始期间后最近的已结账期间信息
     * @param endSettledPeriodVo       结束期间前最近的已结账期间信息
     * @param endSettledPeriod         结束期间前最近的已结账期间
     * @param beginPeriod              开始期间
     * @param endPeriod                结束期间
     * @param endNumber                结束期间号
     * @param includeUnbookedFlag      是否包含未过账凭证标识
     * @param includeProfitAndLossFlag 是否包含损益结转凭证标识
     */
    /*private void getBalanceInfo(BalanceSubjectVo balanceSubjectVo, AccountBookPeriodVo beginSettledPeriodVo, AccountBookPeriodVo endSettledPeriodVo,
                                Integer endSettledPeriod, Integer beginPeriod, Integer endPeriod, Byte endNumber,
                                Boolean includeUnbookedFlag, Boolean includeProfitAndLossFlag) {
        //查询期初和期末
        BalanceSubjectQueryDto queryDto = new BalanceSubjectQueryDto();
        queryDto.setAccountBookEntityId(balanceSubjectVo.getAccountBookEntityId());
        queryDto.setAccountSubjectId(balanceSubjectVo.getAccountSubjectId());
        BalanceSubjectVo beginSubjectVo = new BalanceSubjectVo();
        if (beginSettledPeriodVo != null) {
            queryDto.setPeriodYear(beginSettledPeriodVo.getPeriodYear());
            queryDto.setPeriodNum(beginSettledPeriodVo.getPeriodNum());
            //查询此开始期间后最近的已结账期间的期初和期末
            beginSubjectVo = balanceSubjectMapper.findInfoByPeriod(queryDto);
        }
        BalanceSubjectVo endSubjectVo = new BalanceSubjectVo();
        if (endSettledPeriodVo != null) {
            queryDto.setPeriodYear(endSettledPeriodVo.getPeriodYear());
            queryDto.setPeriodNum(endSettledPeriodVo.getPeriodNum());
            //查询此结束期间前最近的已结账期间的期初和期末
            endSubjectVo = balanceSubjectMapper.findInfoByPeriod(queryDto);
        }
        if (endSettledPeriod >= beginPeriod && endSettledPeriod <= endPeriod) {
            //结束期间前最近的已结账期间在当前数据所对应的会计期间内,直接设置期初余额为开始期间后最近的已结账期间的期初
            balanceSubjectVo.setOpeningBalance(beginSubjectVo == null || beginSubjectVo.getOpeningBalance() == null ? BigDecimal.ZERO : beginSubjectVo.getOpeningBalance());
        }
        //查询从结束期间前最近的已结账期间（不包含此期间）到结束期间的余额信息
        List<Integer> periodYears = new LinkedList<>();
        List<Byte> periodNumbers = new LinkedList<>();
        if (endSettledPeriodVo == null) {
            //如果此账簿未结账,则从此账簿的启用期间开始查金额
            periodYears.add(0);
            periodNumbers.add((byte) 0);
        } else {
            periodYears.add(endSettledPeriodVo.getPeriodYear());
            periodNumbers.add(endSettledPeriodVo.getPeriodNum());
        }
        periodYears.add(balanceSubjectVo.getPeriodYear());
        periodNumbers.add(endNumber);
        queryDto.setPeriodYears(periodYears);
        queryDto.setPeriodNumbers(periodNumbers);
        List<BalanceSubjectVo> periodsList = balanceSubjectMapper.findInfoByPeriods(queryDto);
        //查询账簿的启用期间及当前会计科目在启用期间的金额信息
        BalanceSubjectQueryDto param = new BalanceSubjectQueryDto();
        param.setAccountBookId(balanceSubjectVo.getAccountBookId());
        param.setAccountBookEntityId(balanceSubjectVo.getAccountBookEntityId());
        param.setAccountSubjectId(balanceSubjectVo.getAccountSubjectId());
        BalanceSubjectVo startPeriodBalance = balanceSubjectMapper.findStartPeriodBalance(param);
        //根据页面查询条件计算本期借方、本期贷方、借方累计、贷方累计、期初余额和期末余额
        if (endSubjectVo == null) {
            endSubjectVo = new BalanceSubjectVo();
        }
        getSubjectBalance(includeUnbookedFlag, includeProfitAndLossFlag, periodsList, balanceSubjectVo, endSubjectVo, beginPeriod, startPeriodBalance);
        //设置期初余额和期末余额
        if (endSettledPeriod < beginPeriod) {
            //结束期间前最近的已结账期间小于当前数据的开始期间或账簿未结账,设置期初余额为开始期间的前一期间的期末余额
            balanceSubjectVo.setOpeningBalance(endSubjectVo.getOpeningBalance() == null ? BigDecimal.ZERO : endSubjectVo.getOpeningBalance());
        }
        balanceSubjectVo.setClosingBalance(endSubjectVo.getClosingBalance() == null ? BigDecimal.ZERO : endSubjectVo.getClosingBalance());
        //根据余额设置方向
        setDirectionName(balanceSubjectVo);
    }*/

    /**
     * 根据余额设置方向
     *
     * @param balanceSubjectVo
     */
    private void setDirectionName(BalanceSubjectVo balanceSubjectVo) {
        String openingDirectionName = "";
        String closingDirectionName = "";
        if (null != balanceSubjectVo.getOpeningBalance() && balanceSubjectVo.getOpeningBalance().compareTo(BigDecimal.ZERO) == 0) {
            openingDirectionName = Constant.BalanceDirectionName.FLAT;
        } else if (balanceSubjectVo.getBalanceDirection().equals(Constant.BalanceDirection.DEBIT)) {
            openingDirectionName = Constant.BalanceDirectionName.DEBIT;
        } else if (balanceSubjectVo.getBalanceDirection().equals(Constant.BalanceDirection.CREDIT)) {
            openingDirectionName = Constant.BalanceDirectionName.CREDIT;
        }
        if (null != balanceSubjectVo.getClosingBalance() && balanceSubjectVo.getClosingBalance().compareTo(BigDecimal.ZERO) == 0) {
            closingDirectionName = Constant.BalanceDirectionName.FLAT;
        } else if (balanceSubjectVo.getBalanceDirection().equals(Constant.BalanceDirection.DEBIT)) {
            closingDirectionName = Constant.BalanceDirectionName.DEBIT;
        } else if (balanceSubjectVo.getBalanceDirection().equals(Constant.BalanceDirection.CREDIT)) {
            closingDirectionName = Constant.BalanceDirectionName.CREDIT;
        }
        balanceSubjectVo.setOpeningDirectionName(openingDirectionName);
        balanceSubjectVo.setClosingDirectionName(closingDirectionName);
    }

    /**
     * 根据页面查询条件计算本期借方、本期贷方、借方累计、贷方累计、期初余额和期末余额(会计科目)
     *
     * @param includeUnbookedFlag      是否包含未过账凭证标识
     * @param includeProfitAndLossFlag 是否包含损益结转凭证标识
     * @param periodsList              从已结账期间（不包含已结账期间）到结束期间的余额信息
     * @param balanceSubjectVo         科目余额表信息
     */
    private void getSubjectBalance(Boolean includeUnbookedFlag, Boolean includeProfitAndLossFlag, List<BalanceSubjectVo> periodsList,
                                   BalanceSubjectVo balanceSubjectVo) {
        //通过计算期末余额的方式，从此已结账期间开始往后计算出开始期间前一个期间的期末余额,并计算结束期间的期末余额
        BigDecimal debitAmount;
        BigDecimal creditAmount;
        int index = 0;
        if (includeUnbookedFlag) {
            if (includeProfitAndLossFlag) {
                //包含未记账凭证,包含损益结转凭证
                if (CollectionUtils.isNotEmpty(periodsList)) {
                    //计算期初和期末
                    for (BalanceSubjectVo periodBalance : periodsList) {
                        debitAmount = periodBalance.getDebitAmount();
                        creditAmount = periodBalance.getCreditAmount();
                        index++;
                        initBalanceInfo(balanceSubjectVo, periodBalance, debitAmount, creditAmount, index);
                    }
                }
            } else {
                //包含未记账凭证,不包含损益结转凭证
                if (CollectionUtils.isNotEmpty(periodsList)) {
                    //计算期初和期末
                    for (BalanceSubjectVo periodBalance : periodsList) {
                        debitAmount = periodBalance.getDebitAmount().subtract(periodBalance.getSyDebitAmount());
                        creditAmount = periodBalance.getCreditAmount().subtract(periodBalance.getSyCreditAmount());
                        index++;
                        initBalanceInfo(balanceSubjectVo, periodBalance, debitAmount, creditAmount, index);
                    }
                }
                balanceSubjectVo.setDebitAmount(balanceSubjectVo.getDebitAmount().subtract(balanceSubjectVo.getSyDebitAmount()));
                balanceSubjectVo.setCreditAmount(balanceSubjectVo.getCreditAmount().subtract(balanceSubjectVo.getSyCreditAmount()));
                balanceSubjectVo.setTotalDebitAmount(balanceSubjectVo.getTotalDebitAmount().subtract(balanceSubjectVo.getSyTotalDebitAmount()));
                balanceSubjectVo.setTotalCreditAmount(balanceSubjectVo.getTotalCreditAmount().subtract(balanceSubjectVo.getSyTotalCreditAmount()));
            }
        }
        if (!includeUnbookedFlag) {
            if (includeProfitAndLossFlag) {
                //不包含未记账凭证,包含损益结转凭证
                if (CollectionUtils.isNotEmpty(periodsList)) {
                    //计算期初和期末
                    for (BalanceSubjectVo periodBalance : periodsList) {
                        debitAmount = periodBalance.getPostDebitAmount();
                        creditAmount = periodBalance.getPostCreditAmount();
                        index++;
                        initBalanceInfo(balanceSubjectVo, periodBalance, debitAmount, creditAmount, index);
                    }
                }
                balanceSubjectVo.setDebitAmount(balanceSubjectVo.getPostDebitAmount());
                balanceSubjectVo.setCreditAmount(balanceSubjectVo.getPostCreditAmount());
                balanceSubjectVo.setTotalDebitAmount(balanceSubjectVo.getPostTotalDebitAmount());
                balanceSubjectVo.setTotalCreditAmount(balanceSubjectVo.getPostTotalCreditAmount());
            } else {
                //不包含未记账凭证,不包含损益结转凭证
                if (CollectionUtils.isNotEmpty(periodsList)) {
                    //计算期初和期末
                    for (BalanceSubjectVo periodBalance : periodsList) {
                        debitAmount = periodBalance.getPostDebitAmount().subtract(periodBalance.getPostSyDebitAmount());
                        creditAmount = periodBalance.getPostCreditAmount().subtract(periodBalance.getPostSyCreditAmount());
                        index++;
                        initBalanceInfo(balanceSubjectVo, periodBalance, debitAmount, creditAmount, index);
                    }
                }
                balanceSubjectVo.setDebitAmount(balanceSubjectVo.getPostDebitAmount().subtract(balanceSubjectVo.getPostSyDebitAmount()));
                balanceSubjectVo.setCreditAmount(balanceSubjectVo.getPostCreditAmount().subtract(balanceSubjectVo.getPostSyCreditAmount()));
                balanceSubjectVo.setTotalDebitAmount(balanceSubjectVo.getPostTotalDebitAmount().subtract(balanceSubjectVo.getPostSyTotalDebitAmount()));
                balanceSubjectVo.setTotalCreditAmount(balanceSubjectVo.getPostTotalCreditAmount().subtract(balanceSubjectVo.getPostSyTotalCreditAmount()));
            }
        }
    }

    /**
     * 处理科目余额表结果集
     *
     * @param subjectList            会计科目信息列表
     * @param balanceSubjectList     科目余额信息列表
     * @param balanceSubjectQueryDto 页面查询条件
     * @param resultList             处理结果集
     */
    private void dealResultList(List<AccountSubjectVo> subjectList, List<BalanceSubjectVo> balanceSubjectList, List<BalanceSubjectVo> auxiliaryResultList,
                                BalanceSubjectQueryDto balanceSubjectQueryDto, List<BalanceSubjectVo> resultList) {
        MergeUtil.mergeList(subjectList, balanceSubjectList,
                (subject, balanceSubject) -> balanceSubject.getCode() != null && balanceSubject.getCode().indexOf(subject.getCode()) == 0
                        && subject.getSubjectId().equals(balanceSubject.getSubjectId()),
                (subject, balanceList) -> {
                    if (balanceList.size() > 0) {
                        //判断会计科目是否是最上级（计算核算主体合计时用）
                        setSubjectIsFirst(subjectList, subject);
                        //根据核算主体id分组
                        Map<String, List<BalanceSubjectVo>> collectMap = balanceList.stream().collect
                                (Collectors.groupingBy(e -> e.getAccountBookEntityId().toString()));
                        List<BalanceSubjectVo> balanceVoList;
                        Map<String, List<BalanceSubjectVo>> collect;
                        BalanceSubjectVo balanceInfo;
                        List<BalanceSubjectVo> collectList;
                        BalanceSubjectVo parentInfo;
                        for (String key : collectMap.keySet()) {
                            balanceVoList = collectMap.get(key);
                            //再根据期间年份分组
                            collect = balanceVoList.stream().collect
                                    (Collectors.groupingBy(e -> e.getPeriodYear().toString()));
                            for (String item : collect.keySet()) {
                                collectList = collect.get(item);
                                //非末级科目,计算非末级科目所有下级末级科目的金额,并赋值给上级
                                balanceInfo = new BalanceSubjectVo();
                                balanceInfo.setBalanceDirection(subject.getBalanceDirection());
                                countParentBalance(collectList, balanceInfo, Constant.Is.YES);
                                //组装上级科目余额信息
                                parentInfo = new BalanceSubjectVo();
                                initParentInfo(subject, balanceInfo, collectList, parentInfo, balanceSubjectQueryDto);
                                //根据余额设置方向
                                setDirectionName(parentInfo);
                                resultList.add(parentInfo);
                                if (balanceSubjectQueryDto.getIsShowAuxiliaryDetail().equals(Constant.Is.YES) && subject.getIsFinal().equals(Constant.Is.YES)) {
                                    //设置辅助核算项余额列表信息
                                    MergeUtil.mergeList(collectList, auxiliaryResultList,
                                            subjectVo -> subjectVo.getPeriodYear() + Constant.Character.UNDER_LINE +
                                                    subjectVo.getAccountBookId() + Constant.Character.UNDER_LINE +
                                                    subjectVo.getAccountBookEntityId() + Constant.Character.UNDER_LINE +
                                                    subjectVo.getAccountSubjectId(),
                                            auxiliaryVo -> auxiliaryVo.getPeriodYear() + Constant.Character.UNDER_LINE +
                                                    auxiliaryVo.getAccountBookId() + Constant.Character.UNDER_LINE +
                                                    auxiliaryVo.getAccountBookEntityId() + Constant.Character.UNDER_LINE +
                                                    auxiliaryVo.getAccountSubjectId(),
                                            (subjectVo, auxiliaryVoList) -> {
                                                if (auxiliaryVoList.size() > 0) {
                                                    resultList.addAll(auxiliaryVoList);
                                                }
                                            });
                                }
                            }
                        }
                    }
                });
        //根据条件过滤数据
        initResultList(balanceSubjectQueryDto, resultList);
    }

    /**
     * 根据条件过滤数据
     *
     * @param balanceSubjectQueryDto 页面查询条件
     * @param resultList             处理结果集
     */
    private void initResultList(BalanceSubjectQueryDto balanceSubjectQueryDto, List<BalanceSubjectVo> resultList) {
        //根据显示条件过滤数据
        if (balanceSubjectQueryDto.getShowCondition() != null) {
            List<BalanceSubjectVo> removeList = new LinkedList<>();
            if (balanceSubjectQueryDto.getShowCondition().equals(LedgerConstant.ReportShowCondition.HAPPEN_NO)) {
                for (BalanceSubjectVo info : resultList) {
                    if (info.getDebitAmount().compareTo(BigDecimal.ZERO) == 0 && info.getCreditAmount().compareTo(BigDecimal.ZERO) == 0) {
                        removeList.add(info);
                    }
                }
            } else if (balanceSubjectQueryDto.getShowCondition().equals(LedgerConstant.ReportShowCondition.BALANCE_NO)) {
                for (BalanceSubjectVo info : resultList) {
                    if (info.getClosingBalance().compareTo(BigDecimal.ZERO) == 0) {
                        removeList.add(info);
                    }
                }
            } else if (balanceSubjectQueryDto.getShowCondition().equals(LedgerConstant.ReportShowCondition.HAPPEN_BALANCE_NO)) {
                for (BalanceSubjectVo info : resultList) {
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
     * 组装上级科目的数据
     *
     * @param subject                会计科目信息
     * @param balanceInfo            余额信息
     * @param collectList            分组数据集合
     * @param parentInfo             上级科目数据
     * @param balanceSubjectQueryDto 页面查询条件
     */
    private void initParentInfo(AccountSubjectVo subject, BalanceSubjectVo balanceInfo, List<BalanceSubjectVo> collectList, BalanceSubjectVo parentInfo, BalanceSubjectQueryDto balanceSubjectQueryDto) {
        parentInfo.setAccountBookId(collectList.get(0).getAccountBookId());
        parentInfo.setAccountBookName(collectList.get(0).getAccountBookName());
        parentInfo.setAccountBookEntityId(collectList.get(0).getAccountBookEntityId());
        parentInfo.setAccountBookEntityName(collectList.get(0).getAccountBookEntityName());
        parentInfo.setAccountSubjectId(subject.getId());
        parentInfo.setPeriodYear(collectList.get(0).getPeriodYear());
        parentInfo.setOpeningBalance(balanceInfo.getOpeningBalance());
        parentInfo.setClosingBalance(balanceInfo.getClosingBalance());
        parentInfo.setDebitAmount(balanceInfo.getDebitAmount());
        parentInfo.setCreditAmount(balanceInfo.getCreditAmount());
        parentInfo.setTotalDebitAmount(balanceInfo.getTotalDebitAmount());
        parentInfo.setTotalCreditAmount(balanceInfo.getTotalCreditAmount());
        parentInfo.setCode(subject.getCode());
        if (balanceSubjectQueryDto.getIsShowFullName().equals(Constant.Is.NO)) {
            parentInfo.setName(subject.getName());
        } else if (balanceSubjectQueryDto.getIsShowFullName().equals(Constant.Is.YES)) {
            parentInfo.setName(subject.getFullName());
        }
        parentInfo.setBalanceDirection(subject.getBalanceDirection());
        parentInfo.setIsEnable(subject.getIsEnable());
        parentInfo.setIsFinal(subject.getIsFinal());
        parentInfo.setIsFirst(subject.getIsFirst());
    }

    /**
     * 计算上级科目或核算主体合计的金额信息
     *
     * @param collectList 分组数据集合
     * @param balanceInfo 余额信息
     * @param isFinal     计算末级或非末级数据（0：非末级 1：末级）
     */
    private void countParentBalance(List<BalanceSubjectVo> collectList, BalanceSubjectVo balanceInfo, Byte isFinal) {
        BigDecimal openingBalance = BigDecimal.ZERO;
        BigDecimal closingBalance = BigDecimal.ZERO;
        BigDecimal debitAmount = BigDecimal.ZERO;
        BigDecimal creditAmount = BigDecimal.ZERO;
        BigDecimal totalDebitAmount = BigDecimal.ZERO;
        BigDecimal totalCreditAmount = BigDecimal.ZERO;
        Boolean flag;
        for (BalanceSubjectVo balance : collectList) {
            if (isFinal.equals(Constant.Is.YES)) {
                //计算上级科目
                flag = null != balance.getIsFinal() && balance.getIsFinal().equals(Constant.Is.YES);
            } else {
                //计算核算主体(只计算信息列表中的第一级)
                flag = null != balance.getIsFirst() && balance.getIsFirst().equals(Constant.Is.YES);
            }
            if (flag) {
                //TODO 核算主体合计暂不计算期初和期末
                if (isFinal.equals(Constant.Is.YES)) {
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
        }

        if (isFinal.equals(Constant.Is.YES)) {
            //计算上级科目金额信息时,根据方向设置期初和期末
            setParentBalance(openingBalance, closingBalance, balanceInfo);
        } else {
            //计算核算主体合计金额信息时,根据期初和期末设置方向
//            setTotalDirectionName(openingBalance, closingBalance, balanceInfo);
//            balanceInfo.setOpeningBalance(openingBalance.compareTo(BigDecimal.ZERO) >= 0 ? openingBalance : openingBalance.negate());
//            balanceInfo.setClosingBalance(closingBalance.compareTo(BigDecimal.ZERO) >= 0 ? closingBalance : closingBalance.negate());
        }
        balanceInfo.setDebitAmount(debitAmount);
        balanceInfo.setCreditAmount(creditAmount);
        balanceInfo.setTotalDebitAmount(totalDebitAmount);
        balanceInfo.setTotalCreditAmount(totalCreditAmount);
    }

    /**
     * 获取辅助核算项余额列表信息
     *
     * @param accountSubjectIdList     末级会计科目id集合
     * @param balanceSubjectList       科目余额信息
     * @param balanceSubjectQueryDto   页面查询条件
     * @param includeUnbookedFlag      是否包含未过账凭证标识
     * @param includeProfitAndLossFlag 是否包含损益结转凭证标识
     */
    private void getAuxiliaryBalanceList(List<BalanceSubjectVo> auxiliaryResultList, List<Long> accountSubjectIdList, List<BalanceSubjectVo> balanceSubjectList,
                                         BalanceSubjectQueryDto balanceSubjectQueryDto, Boolean includeUnbookedFlag, Boolean includeProfitAndLossFlag) {
        //查询辅助核算组合列表
        BalanceSubjectAuxiliaryItemQueryDto auxiliaryItemQueryDto = new BalanceSubjectAuxiliaryItemQueryDto();
        FastUtils.copyProperties(balanceSubjectQueryDto, auxiliaryItemQueryDto);
        auxiliaryItemQueryDto.setSubjectIds(accountSubjectIdList);
        List<BalanceSubjectAuxiliaryItemVo> auxiliaryItemVoList = balanceSubjectAuxiliaryItemMapper.findListByParam(auxiliaryItemQueryDto);
        if (CollectionUtils.isNotEmpty(auxiliaryItemVoList)) {
            //组装查询辅助核算余额信息参数
            initAuxiliaryItemQuery(auxiliaryItemQueryDto, auxiliaryItemVoList);
            //查询辅助核算余额信息
            List<BalanceSubjectAuxiliaryVo> auxiliaryBalanceList = balanceSubjectAuxiliaryMapper.findInfoByAuxiliaryItem(auxiliaryItemQueryDto);
            //循环auxiliaryItemVoList和auxiliaryBalanceList设置辅助核算余额信息auxiliaryVoList
            List<BalanceSubjectAuxiliaryVo> auxiliaryVoList = new ArrayList<>();
            setAuxiliaryVoList(auxiliaryVoList, auxiliaryItemVoList, auxiliaryBalanceList);

            //循环auxiliaryVoList和balanceSubjectList设置账簿名称信息和启用期间
            MergeUtil.merge(auxiliaryVoList, balanceSubjectList,
                    auxiliaryVo -> auxiliaryVo.getAccountBookId() + Constant.Character.UNDER_LINE + auxiliaryVo.getAccountBookEntityId(),
                    balanceSubject -> balanceSubject.getAccountBookId() + Constant.Character.UNDER_LINE + balanceSubject.getAccountBookEntityId(),
                    (auxiliaryVo, balanceSubject) -> {
                        auxiliaryVo.setAccountBookName(balanceSubject.getAccountBookName());
                        auxiliaryVo.setAccountBookEntityName(balanceSubject.getAccountBookEntityName());
                        auxiliaryVo.setStartPeriod(balanceSubject.getStartPeriod());
                    });
            //查询auxiliaryVoList所有数据的启用期间对应的balance_auxiliary_id
            List<BalanceSubjectAuxiliaryItemVo> startAuxiliaryItemVoList = balanceSubjectAuxiliaryItemMapper.findStartIdListByParam(auxiliaryVoList);
            //循环auxiliaryVoList和startAuxiliaryItemVoList,设置启用期间的balance_auxiliary_id
            setBalanceAuxiliaryId(auxiliaryVoList, startAuxiliaryItemVoList);

            //查询auxiliaryVoList所有数据启用期间的金额
            List<BalanceSubjectAuxiliaryVo> startPeriodBalanceList = balanceSubjectAuxiliaryMapper.findStartPeriodBalance(auxiliaryVoList);
            //循环auxiliaryVoList设置启用期间信息
            setStartPeriod(auxiliaryVoList, startPeriodBalanceList);
            //设置每条辅助核算余额对应的科目余额信息
            setAuxiliarySubjectInfo(balanceSubjectList, auxiliaryVoList);

            //查询所有辅助核算项信息
            List<List<Map<String, Object>>> allSourceTableList = findAllSourceTableInfo(auxiliaryItemVoList);
            StringBuilder sourceTable;
            StringBuilder itemValueId;
            StringBuilder auxiliaryCode;
            StringBuilder auxiliaryName;
            for (int i = 0; i < auxiliaryVoList.size(); i++) {
                if (StringUtil.isEmpty(auxiliaryVoList.get(i).getCode())) {
                    //科目编码为空,表示页面选择不包含禁用科目,并且此科目已禁用
                    continue;
                }
                //组装辅助核算信息
                sourceTable = new StringBuilder();
                itemValueId = new StringBuilder();
                auxiliaryCode = new StringBuilder();
                auxiliaryName = new StringBuilder();
                initAuxiliaryNameAndCode(allSourceTableList.get(i), sourceTable, itemValueId, auxiliaryCode, auxiliaryName);
                //查询期初和期末
                findOpeningAndClosingBalance(auxiliaryResultList, auxiliaryVoList.get(i), includeUnbookedFlag, includeProfitAndLossFlag,
                        auxiliaryCode, auxiliaryName, sourceTable, itemValueId);
            }
        }
    }

    /**
     * 设置每条辅助核算余额对应的科目余额信息
     *
     * @param balanceSubjectList 科目余额信息
     * @param auxiliaryVoList    辅助核算余额信息
     */
    private void setAuxiliarySubjectInfo(List<BalanceSubjectVo> balanceSubjectList, List<BalanceSubjectAuxiliaryVo> auxiliaryVoList) {
        MergeUtil.merge(auxiliaryVoList, balanceSubjectList,
                auxiliaryVo -> auxiliaryVo.getAccountBookId() + Constant.Character.UNDER_LINE +
                        auxiliaryVo.getAccountBookEntityId() + Constant.Character.UNDER_LINE +
                        auxiliaryVo.getAccountSubjectId() + Constant.Character.UNDER_LINE +
                        auxiliaryVo.getPeriodYear(),
                balanceSubjectVo -> balanceSubjectVo.getAccountBookId() + Constant.Character.UNDER_LINE +
                        balanceSubjectVo.getAccountBookEntityId() + Constant.Character.UNDER_LINE +
                        balanceSubjectVo.getAccountSubjectId() + Constant.Character.UNDER_LINE +
                        balanceSubjectVo.getPeriodYear(),
                (auxiliaryVo, balanceSubjectVo) -> {
                    auxiliaryVo.setBalanceSubjectVo(balanceSubjectVo);
                    auxiliaryVo.setCode(balanceSubjectVo.getCode());
                    auxiliaryVo.setName(balanceSubjectVo.getName());
                    auxiliaryVo.setBalanceDirection(balanceSubjectVo.getBalanceDirection());
                    auxiliaryVo.setIsEnable(balanceSubjectVo.getIsEnable());
                });
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
     * 查询期初和期末
     *
     * @param auxiliaryVo              辅助核算余额信息
     * @param includeUnbookedFlag      是否包含未过账凭证标识
     * @param includeProfitAndLossFlag 是否包含损益结转凭证标识
     * @param auxiliaryCode            辅助核算编码
     * @param auxiliaryName            辅助核算名称
     * @param sourceTable              辅助核算表名
     * @param itemValueId              辅助核算id
     */
    private void findOpeningAndClosingBalance(List<BalanceSubjectVo> auxiliaryResultList, BalanceSubjectAuxiliaryVo auxiliaryVo, Boolean includeUnbookedFlag,
                                              Boolean includeProfitAndLossFlag,
                                              StringBuilder auxiliaryCode, StringBuilder auxiliaryName, StringBuilder sourceTable, StringBuilder itemValueId) {
        //开始期间后最近的已结账余额信息
        BalanceSubjectAuxiliaryVo beginSubjectVo = new BalanceSubjectAuxiliaryVo();
        //结束期间前最近的已结账余额信息
        BalanceSubjectAuxiliaryVo endSubjectVo = new BalanceSubjectAuxiliaryVo();
        //科目余额信息
        BalanceSubjectVo enableBalanceVo = auxiliaryVo.getBalanceSubjectVo();
        AccountBookPeriodVo beginSettledPeriodVo = enableBalanceVo.getBeginSettledPeriodVo();
        AccountBookPeriodVo endSettledPeriodVo = enableBalanceVo.getEndSettledPeriodVo();
        Byte endNumber = enableBalanceVo.getEndNumber();
        Integer beginPeriod = enableBalanceVo.getBeginPeriod();
        Integer endPeriod = enableBalanceVo.getEndPeriod();
        Integer endSettledPeriod = enableBalanceVo.getEndSettledPeriod();
        BalanceSubjectAuxiliaryItemQueryDto queryDto = new BalanceSubjectAuxiliaryItemQueryDto();
        queryDto.setAccountBookEntityId(enableBalanceVo.getAccountBookEntityId());
        queryDto.setAccountSubjectId(enableBalanceVo.getAccountSubjectId());
        queryDto.setItemValueIds(auxiliaryVo.getItemValueIds());
        if (beginSettledPeriodVo != null) {
            queryDto.setPeriodYear(beginSettledPeriodVo.getPeriodYear());
            queryDto.setPeriodNum(beginSettledPeriodVo.getPeriodNum());
            //查询此开始期间后最近的已结账期间的期初和期末
            beginSubjectVo = balanceSubjectAuxiliaryMapper.findInfoByPeriod(queryDto);
        }
        if (endSettledPeriodVo != null) {
            queryDto.setPeriodYear(endSettledPeriodVo.getPeriodYear());
            queryDto.setPeriodNum(endSettledPeriodVo.getPeriodNum());
            //查询此结束期间前最近的已结账期间的期初和期末
            endSubjectVo = balanceSubjectAuxiliaryMapper.findInfoByPeriod(queryDto);
        }
        if (endSettledPeriod >= beginPeriod && endSettledPeriod <= endPeriod) {
            //结束期间前最近的已结账期间在所选会计期间内,直接设置期初余额为开始期间后最近的已结账期间的期初
            auxiliaryVo.setOpeningBalance(beginSubjectVo == null || beginSubjectVo.getOpeningBalance() == null ? BigDecimal.ZERO : beginSubjectVo.getOpeningBalance());
        }
        //查询从已结账期间（不包含已结账期间）到结束期间的余额信息
        List<Integer> periodYears = new LinkedList<>();
        List<Byte> periodNumbers = new LinkedList<>();
        if (endSettledPeriodVo == null) {
            periodYears.add(0);
            periodNumbers.add((byte) 0);
        } else {
            periodYears.add(endSettledPeriodVo.getPeriodYear());
            periodNumbers.add(endSettledPeriodVo.getPeriodNum());
        }
        periodYears.add(auxiliaryVo.getPeriodYear());
        periodNumbers.add(endNumber);
        queryDto.setPeriodYears(periodYears);
        queryDto.setPeriodNumbers(periodNumbers);
        List<BalanceSubjectAuxiliaryVo> periodsList = balanceSubjectAuxiliaryMapper.findInfoByPeriods(queryDto);
        //根据页面查询条件计算本期借方、本期贷方、借方累计、贷方累计、期初余额和期末余额
        if (endSubjectVo == null) {
            endSubjectVo = new BalanceSubjectAuxiliaryVo();
        }
        getAuxiliaryBalance(auxiliaryResultList, includeUnbookedFlag, includeProfitAndLossFlag, periodsList,
                enableBalanceVo, auxiliaryVo, endSubjectVo, beginPeriod, endSettledPeriod, auxiliaryCode, auxiliaryName,
                sourceTable, itemValueId);
    }

    /**
     * 组装查询辅助核算余额信息参数
     *
     * @param auxiliaryItemQueryDto 查询辅助核算余额信息参数
     * @param auxiliaryItemVoList   辅助核算项目信息
     */
    @Override
    public BalanceSubjectAuxiliaryItemQueryDto initAuxiliaryItemQuery(BalanceSubjectAuxiliaryItemQueryDto auxiliaryItemQueryDto,
                                                                      List<BalanceSubjectAuxiliaryItemVo> auxiliaryItemVoList) {
        StringBuilder balanceAuxiliaryIds = new StringBuilder();
        List<String> auxiliaryIdList;
        List<String> sourceTableList;
        List<String> itemValueIdList;
        for (BalanceSubjectAuxiliaryItemVo item : auxiliaryItemVoList) {
            if (StringUtil.isNotEmpty(balanceAuxiliaryIds.toString())) {
                balanceAuxiliaryIds.append(",");
            }
            balanceAuxiliaryIds.append(item.getBalanceAuxiliaryIds());
            String[] auxiliaryIds = item.getBalanceAuxiliaryIds().split(",");
            auxiliaryIdList = new LinkedList<>();
            Collections.addAll(auxiliaryIdList, auxiliaryIds);
            item.setAuxiliaryIds(auxiliaryIdList);
            String[] sourceTables = item.getSourceTables().split(",");
            sourceTableList = new LinkedList<>();
            Collections.addAll(sourceTableList, sourceTables);
            item.setSourceTableList(sourceTableList);
            String[] itemValueIds = item.getItemValueIds().split(",");
            itemValueIdList = new LinkedList<>();
            Collections.addAll(itemValueIdList, itemValueIds);
            item.setItemValueIdList(itemValueIdList);
        }
        auxiliaryItemQueryDto.setBalanceAuxiliaryIds(balanceAuxiliaryIds.toString());
        return auxiliaryItemQueryDto;
    }

    /**
     * 根据页面查询条件计算本期借方、本期贷方、借方累计、贷方累计、期初余额和期末余额(辅助核算项)
     *
     * @param includeUnbookedFlag      是否包含未过账凭证标识
     * @param includeProfitAndLossFlag 是否包含损益结转凭证标识
     * @param periodsList              期间列表
     * @param enableBalanceVo          科目余额信息
     * @param auxiliaryVo              辅助核算余额信息
     * @param endSubjectVo             结束期间前最近的已结账期间的余额信息
     * @param beginPeriod              开始期间
     * @param endSettledPeriod         结束期间前最近的已结账期间
     * @param auxiliaryCode            辅助核算项编码
     * @param auxiliaryName            辅助核算项名称
     * @param sourceTable              辅助核算表名
     * @param itemValueId              辅助核算id
     */
    private void getAuxiliaryBalance(List<BalanceSubjectVo> auxiliaryResultList, Boolean includeUnbookedFlag, Boolean includeProfitAndLossFlag,
                                     List<BalanceSubjectAuxiliaryVo> periodsList, BalanceSubjectVo enableBalanceVo, BalanceSubjectAuxiliaryVo auxiliaryVo,
                                     BalanceSubjectAuxiliaryVo endSubjectVo, Integer beginPeriod, Integer endSettledPeriod, StringBuilder auxiliaryCode,
                                     StringBuilder auxiliaryName, StringBuilder sourceTable, StringBuilder itemValueId) {
        BigDecimal debitAmount;
        BigDecimal creditAmount;
        int index = 0;
        //通过计算期末余额的方式，从此已结账期间开始往后计算出开始期间前一个期间的期末余额,并计算结束期间的期末余额
        if (includeUnbookedFlag) {
            if (includeProfitAndLossFlag) {
                //包含未记账凭证,包含损益结转凭证
                if (CollectionUtils.isNotEmpty(periodsList)) {
                    //计算期初和期末
                    for (BalanceSubjectAuxiliaryVo periodBalance : periodsList) {
                        debitAmount = periodBalance.getDebitAmount();
                        creditAmount = periodBalance.getCreditAmount();
                        index++;
                        initBalanceAuxiliaryInfo(enableBalanceVo, periodBalance, endSubjectVo, beginPeriod, debitAmount, creditAmount, auxiliaryVo.getStartPeriodBalanceVo(), index);
                    }
                }
            } else {
                //包含未记账凭证,不包含损益结转凭证
                if (CollectionUtils.isNotEmpty(periodsList)) {
                    //计算期初和期末
                    for (BalanceSubjectAuxiliaryVo periodBalance : periodsList) {
                        debitAmount = periodBalance.getDebitAmount().subtract(periodBalance.getSyDebitAmount());
                        creditAmount = periodBalance.getCreditAmount().subtract(periodBalance.getSyCreditAmount());
                        index++;
                        initBalanceAuxiliaryInfo(enableBalanceVo, periodBalance, endSubjectVo, beginPeriod, debitAmount, creditAmount, auxiliaryVo.getStartPeriodBalanceVo(), index);
                    }
                }
                auxiliaryVo.setDebitAmount(auxiliaryVo.getDebitAmount().subtract(auxiliaryVo.getSyDebitAmount()));
                auxiliaryVo.setCreditAmount(auxiliaryVo.getCreditAmount().subtract(auxiliaryVo.getSyCreditAmount()));
                auxiliaryVo.setTotalDebitAmount(auxiliaryVo.getTotalDebitAmount().subtract(auxiliaryVo.getSyTotalDebitAmount()));
                auxiliaryVo.setTotalCreditAmount(auxiliaryVo.getTotalCreditAmount().subtract(auxiliaryVo.getSyTotalCreditAmount()));
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
                        initBalanceAuxiliaryInfo(enableBalanceVo, periodBalance, endSubjectVo, beginPeriod, debitAmount, creditAmount, auxiliaryVo.getStartPeriodBalanceVo(), index);
                    }
                }
                auxiliaryVo.setDebitAmount(auxiliaryVo.getPostDebitAmount());
                auxiliaryVo.setCreditAmount(auxiliaryVo.getPostCreditAmount());
                auxiliaryVo.setTotalDebitAmount(auxiliaryVo.getPostTotalDebitAmount());
                auxiliaryVo.setTotalCreditAmount(auxiliaryVo.getPostTotalCreditAmount());
            } else {
                //不包含未记账凭证,不包含损益结转凭证
                if (CollectionUtils.isNotEmpty(periodsList)) {
                    //计算期初和期末
                    for (BalanceSubjectAuxiliaryVo periodBalance : periodsList) {
                        debitAmount = periodBalance.getPostDebitAmount().subtract(periodBalance.getPostSyDebitAmount());
                        creditAmount = periodBalance.getPostCreditAmount().subtract(periodBalance.getPostSyCreditAmount());
                        index++;
                        initBalanceAuxiliaryInfo(enableBalanceVo, periodBalance, endSubjectVo, beginPeriod, debitAmount, creditAmount, auxiliaryVo.getStartPeriodBalanceVo(), index);
                    }
                }
                auxiliaryVo.setDebitAmount(auxiliaryVo.getPostDebitAmount().subtract(auxiliaryVo.getPostSyDebitAmount()));
                auxiliaryVo.setCreditAmount(auxiliaryVo.getPostCreditAmount().subtract(auxiliaryVo.getPostSyCreditAmount()));
                auxiliaryVo.setTotalDebitAmount(auxiliaryVo.getPostTotalDebitAmount().subtract(auxiliaryVo.getPostSyTotalDebitAmount()));
                auxiliaryVo.setTotalCreditAmount(auxiliaryVo.getPostTotalCreditAmount().subtract(auxiliaryVo.getPostSyTotalCreditAmount()));
            }
        }
        //设置期初余额和期末余额
        if (endSettledPeriod < beginPeriod) {
            //结束期间前最近的已结账期间小于当前数据的开始期间,设置期初余额为开始期间的前一期间的期末余额
            auxiliaryVo.setOpeningBalance(endSubjectVo.getOpeningBalance() == null ? BigDecimal.ZERO : endSubjectVo.getOpeningBalance());
        }
        auxiliaryVo.setClosingBalance(endSubjectVo.getClosingBalance() == null ? BigDecimal.ZERO : endSubjectVo.getClosingBalance());
        //组装科目余额-辅助核算项信息
        BalanceSubjectVo auxiliaryInfo = new BalanceSubjectVo();
        //组装辅助核算余额信息
        initAuxiliaryInfo(auxiliaryVo, auxiliaryCode, auxiliaryName, sourceTable, itemValueId, auxiliaryInfo);
        //根据余额设置方向
        setDirectionName(auxiliaryInfo);
        auxiliaryResultList.add(auxiliaryInfo);
    }

    /**
     * 组装辅助核算余额信息
     *
     * @param auxiliaryVo   辅助核算余额信息
     * @param auxiliaryCode 辅助核算编码
     * @param auxiliaryName 辅助核算名称
     * @param auxiliaryInfo 组装的辅助核算余额信息
     * @param sourceTable   辅助核算表名
     * @param itemValueId   辅助核算id
     */
    private void initAuxiliaryInfo(BalanceSubjectAuxiliaryVo auxiliaryVo,
                                   StringBuilder auxiliaryCode, StringBuilder auxiliaryName, StringBuilder sourceTable,
                                   StringBuilder itemValueId, BalanceSubjectVo auxiliaryInfo) {
        auxiliaryInfo.setSourceTable(sourceTable.toString());
        auxiliaryInfo.setItemValueIds(itemValueId.toString());
        auxiliaryInfo.setAuxiliaryCode(auxiliaryCode.toString());
        auxiliaryInfo.setAuxiliaryName(auxiliaryName.toString());
        auxiliaryInfo.setAccountBookId(auxiliaryVo.getAccountBookId());
        auxiliaryInfo.setAccountBookName(auxiliaryVo.getAccountBookName());
        auxiliaryInfo.setAccountBookEntityId(auxiliaryVo.getAccountBookEntityId());
        auxiliaryInfo.setAccountBookEntityName(auxiliaryVo.getAccountBookEntityName());
        auxiliaryInfo.setAccountSubjectId(auxiliaryVo.getAccountSubjectId());
        auxiliaryInfo.setPeriodYear(auxiliaryVo.getPeriodYear());
        auxiliaryInfo.setOpeningBalance(auxiliaryVo.getOpeningBalance());
        auxiliaryInfo.setClosingBalance(auxiliaryVo.getClosingBalance());
        auxiliaryInfo.setDebitAmount(auxiliaryVo.getDebitAmount());
        auxiliaryInfo.setCreditAmount(auxiliaryVo.getCreditAmount());
        auxiliaryInfo.setTotalDebitAmount(auxiliaryVo.getTotalDebitAmount());
        auxiliaryInfo.setTotalCreditAmount(auxiliaryVo.getTotalCreditAmount());
        auxiliaryInfo.setCode(auxiliaryVo.getCode());
        auxiliaryInfo.setName(auxiliaryVo.getName());
        auxiliaryInfo.setBalanceDirection(auxiliaryVo.getBalanceDirection());
        auxiliaryInfo.setIsEnable(auxiliaryVo.getIsEnable());
    }

    /**
     * 根据科目id组装数据(科目汇总表)
     *
     * @param balanceSubjectList       科目余额信息列表
     * @param subjectList              会计科目信息列表
     * @param balanceSubjectQueryDto   页面查询条件
     * @param includeUnbookedFlag      是否包含未过账凭证标识
     * @param includeProfitAndLossFlag 是否包含损益结转凭证标识
     */
    private void initCollectData(List<BalanceSubjectVo> balanceSubjectList, List<AccountSubjectVo> subjectList, BalanceSubjectQueryDto balanceSubjectQueryDto,
                                 Boolean includeUnbookedFlag, Boolean includeProfitAndLossFlag) {
        MergeUtil.merge(balanceSubjectList, subjectList,
                balanceSubjectVo -> balanceSubjectVo.getAccountSubjectId(),
                accountSubjectVo -> accountSubjectVo.getId(),
                (balanceSubjectVo, accountSubjectVo) -> {
                    balanceSubjectVo.setAccountSubjectId(accountSubjectVo.getId());
                    balanceSubjectVo.setSubjectId(accountSubjectVo.getSubjectId());
                    balanceSubjectVo.setBalanceDirection(accountSubjectVo.getBalanceDirection());
                    balanceSubjectVo.setIsEnable(accountSubjectVo.getIsEnable());
                    balanceSubjectVo.setIsFinal(accountSubjectVo.getIsFinal());
                    balanceSubjectVo.setCode(accountSubjectVo.getCode());
                    if (balanceSubjectQueryDto.getIsShowFullName().equals(Constant.Is.NO)) {
                        balanceSubjectVo.setName(accountSubjectVo.getName());
                    } else if (balanceSubjectQueryDto.getIsShowFullName().equals(Constant.Is.YES)) {
                        balanceSubjectVo.setName(accountSubjectVo.getFullName());
                    }
                    if (includeUnbookedFlag) {
                        if (includeProfitAndLossFlag) {
                            //包含未记账凭证,包含损益结转凭证
                        } else {
                            //包含未记账凭证,不包含损益结转凭证
                            balanceSubjectVo.setDebitAmount(balanceSubjectVo.getDebitAmount().subtract(balanceSubjectVo.getSyDebitAmount()));
                            balanceSubjectVo.setCreditAmount(balanceSubjectVo.getCreditAmount().subtract(balanceSubjectVo.getSyCreditAmount()));
                        }
                    }
                    if (!includeUnbookedFlag) {
                        if (includeProfitAndLossFlag) {
                            //不包含未记账凭证,包含损益结转凭证
                            balanceSubjectVo.setDebitAmount(balanceSubjectVo.getPostDebitAmount());
                            balanceSubjectVo.setCreditAmount(balanceSubjectVo.getPostCreditAmount());
                        } else {
                            //不包含未记账凭证,不包含损益结转凭证
                            balanceSubjectVo.setDebitAmount(balanceSubjectVo.getPostDebitAmount().subtract(balanceSubjectVo.getPostSyDebitAmount()));
                            balanceSubjectVo.setCreditAmount(balanceSubjectVo.getPostCreditAmount().subtract(balanceSubjectVo.getPostSyCreditAmount()));
                        }
                    }
                });
    }

    /**
     * 处理科目汇总表结果集
     *
     * @param subjectList            会计科目信息列表
     * @param balanceSubjectList     科目余额信息列表
     * @param balanceSubjectQueryDto 页面查询条件
     * @param resultList             处理结果集
     */
    private void dealCollectResultList(List<AccountSubjectVo> subjectList, List<BalanceSubjectVo> balanceSubjectList, BalanceSubjectQueryDto balanceSubjectQueryDto,
                                       List<BalanceSubjectVo> resultList) {
        MergeUtil.mergeList(subjectList, balanceSubjectList,
                (subject, balanceSubject) -> balanceSubject.getCode() != null && balanceSubject.getCode().indexOf(subject.getCode()) == 0
                        && subject.getSubjectId().equals(balanceSubject.getSubjectId()),
                (subject, balanceList) -> {
                    if (balanceList.size() > 0) {
                        //判断会计科目是否是最上级（计算核算主体合计时用）
                        setSubjectIsFirst(subjectList, subject);
                        //根据核算主体id分组
                        Map<String, List<BalanceSubjectVo>> collectMap = balanceList.stream().collect
                                (Collectors.groupingBy(e -> e.getAccountBookEntityId().toString()));
                        List<BalanceSubjectVo> balanceVoList;
                        BalanceSubjectVo parentInfo;
                        BigDecimal debitAmount;
                        BigDecimal creditAmount;
                        for (String key : collectMap.keySet()) {
                            balanceVoList = collectMap.get(key);
                            //计算非末级科目所有下级末级科目的金额,并赋值给上级
                            debitAmount = BigDecimal.ZERO;
                            creditAmount = BigDecimal.ZERO;
                            for (BalanceSubjectVo balance : balanceVoList) {
                                debitAmount = debitAmount.add(balance.getDebitAmount());
                                creditAmount = creditAmount.add(balance.getCreditAmount());
                            }
                            //组装上级科目余额信息
                            parentInfo = new BalanceSubjectVo();
                            initParentBalanceSubject(subject, balanceVoList, debitAmount, creditAmount, parentInfo, balanceSubjectQueryDto);
                            resultList.add(parentInfo);
                        }
                    }
                });
    }

    /**
     * 判断会计科目是否是最上级（计算核算主体合计时用）
     *
     * @param subjectList
     * @param subject
     */
    private void setSubjectIsFirst(List<AccountSubjectVo> subjectList, AccountSubjectVo subject) {
        List<String> codeList = subjectList.stream().map(AccountSubjectVo::getCode).collect(Collectors.toList());
        Boolean flag = true;
        if (StringUtil.isNotEmpty(subject.getUpCode())) {
            if (codeList.contains(subject.getUpCode())) {
                flag = false;
            } else {
                for (AccountSubjectVo accountSubjectVo : subjectList) {
                    if (subject.getUpCode().indexOf(accountSubjectVo.getCode()) == 0) {
                        flag = false;
                        break;
                    }
                }
            }
        }
        if (flag) {
            //上级编码为空或者在所有编码信息中不存在上级编码和它的所有上级,则认为这条科目是最上级科目
            subject.setIsFirst(Constant.Is.YES);
        } else {
            subject.setIsFirst(Constant.Is.NO);
        }
    }

    /**
     * 组装上级科目汇总余额信息
     *
     * @param subject                会计科目信息
     * @param balanceVoList          分组信息
     * @param debitAmount            本期借方
     * @param creditAmount           本期贷方
     * @param parentInfo             上级科目汇总信息
     * @param balanceSubjectQueryDto 页面查询条件
     */
    private void initParentBalanceSubject(AccountSubjectVo subject, List<BalanceSubjectVo> balanceVoList, BigDecimal debitAmount,
                                          BigDecimal creditAmount, BalanceSubjectVo parentInfo, BalanceSubjectQueryDto balanceSubjectQueryDto) {
        parentInfo.setAccountBookId(balanceVoList.get(0).getAccountBookId());
        parentInfo.setAccountBookName(balanceVoList.get(0).getAccountBookName());
        parentInfo.setAccountBookEntityId(balanceVoList.get(0).getAccountBookEntityId());
        parentInfo.setAccountBookEntityName(balanceVoList.get(0).getAccountBookEntityName());
        parentInfo.setAccountSubjectId(subject.getId());
        parentInfo.setPeriodYear(balanceVoList.get(0).getPeriodYear());
        parentInfo.setDebitAmount(debitAmount);
        parentInfo.setCreditAmount(creditAmount);
        parentInfo.setCode(subject.getCode());
        if (balanceSubjectQueryDto.getIsShowFullName().equals(Constant.Is.NO)) {
            parentInfo.setName(subject.getName());
        } else if (balanceSubjectQueryDto.getIsShowFullName().equals(Constant.Is.YES)) {
            parentInfo.setName(subject.getFullName());
        }
        parentInfo.setBalanceDirection(subject.getBalanceDirection());
        parentInfo.setIsEnable(subject.getIsEnable());
        parentInfo.setIsFinal(subject.getIsFinal());
        parentInfo.setIsFirst(subject.getIsFirst());
    }

    /**
     * 计算科目期初余额及期末余额
     *
     * @param balanceSubjectVo 科目余额表信息
     * @param periodBalance    当前期间余额信息
     * @param debitAmount      本期借方
     * @param creditAmount     本期贷方
     * @param index            下标，用于处理结束期间前最近的已结账期间等于开始期间前一期间的情况
     */
    private void initBalanceInfo(BalanceSubjectVo balanceSubjectVo, BalanceSubjectVo periodBalance,
                                 BigDecimal debitAmount, BigDecimal creditAmount, int index) {
        BigDecimal openingBalance = balanceSubjectVo.getEndSubjectVo().getClosingBalance() == null ? BigDecimal.ZERO : balanceSubjectVo.getEndSubjectVo().getClosingBalance();
        BigDecimal closingBalance = balanceSubjectVo.getEndSubjectVo().getClosingBalance() == null ? BigDecimal.ZERO : balanceSubjectVo.getEndSubjectVo().getClosingBalance();
        String recentPeriodYear = String.format("%02d", periodBalance.getPeriodYear());
        String recentPeriodNum = String.format("%02d", periodBalance.getPeriodNum());
        Integer recentPeriod = Integer.valueOf(recentPeriodYear + recentPeriodNum);
        //如果当前期间是启用期间的话,期初取启用期间的期初,期末取启用期间的期初
        if (balanceSubjectVo.getStartPeriodBalanceVo() != null && recentPeriod.equals(balanceSubjectVo.getStartPeriodBalanceVo().getPeriodYearNum())) {
            openingBalance = balanceSubjectVo.getStartPeriodBalanceVo().getOpeningBalance();
            closingBalance = balanceSubjectVo.getStartPeriodBalanceVo().getOpeningBalance();
            balanceSubjectVo.getEndSubjectVo().setOpeningBalance(openingBalance);
        }
        if (index == 1) {
            balanceSubjectVo.getEndSubjectVo().setOpeningBalance(openingBalance);
        }
        if (balanceSubjectVo.getBalanceDirection().equals(Constant.BalanceDirection.DEBIT)) {
            //借方
            if (recentPeriod < balanceSubjectVo.getBeginPeriod()) {
                //计算期末余额直到开始期间的前一期间作为该数据的期初
                openingBalance = openingBalance.add(debitAmount).subtract(creditAmount);
                balanceSubjectVo.getEndSubjectVo().setOpeningBalance(openingBalance);
            }
            //计算期末余额直到最后一个期间
            closingBalance = closingBalance.add(debitAmount).subtract(creditAmount);
        } else if (balanceSubjectVo.getBalanceDirection().equals(Constant.BalanceDirection.CREDIT)) {
            //贷方
            if (recentPeriod < balanceSubjectVo.getBeginPeriod()) {
                //计算期末余额直到开始期间的前一期间作为该数据的期初
                openingBalance = openingBalance.add(creditAmount).subtract(debitAmount);
                balanceSubjectVo.getEndSubjectVo().setOpeningBalance(openingBalance);
            }
            //计算期末余额直到最后一个期间
            closingBalance = closingBalance.add(creditAmount).subtract(debitAmount);
        }
        balanceSubjectVo.getEndSubjectVo().setClosingBalance(closingBalance);
    }

    /**
     * 计算辅助核算期初余额及期末余额
     *
     * @param balanceSubjectVo   科目余额信息
     * @param periodBalance      期间余额信息
     * @param subjectVo          结束期间前最近的已结账期间的余额信息
     * @param beginPeriod        开始期间
     * @param debitAmount        本期借方
     * @param creditAmount       本期贷方
     * @param startPeriodBalance 账簿会计科目的启用期间及金额信息
     * @param index              下标，用于处理结束期间前最近的已结账期间等于开始期间前一期间的情况
     */
    private void initBalanceAuxiliaryInfo(BalanceSubjectVo balanceSubjectVo, BalanceSubjectAuxiliaryVo periodBalance, BalanceSubjectAuxiliaryVo subjectVo,
                                          Integer beginPeriod, BigDecimal debitAmount, BigDecimal creditAmount, BalanceSubjectAuxiliaryVo startPeriodBalance, int index) {
        BigDecimal openingBalance = subjectVo.getClosingBalance() == null ? BigDecimal.ZERO : subjectVo.getClosingBalance();
        BigDecimal closingBalance = subjectVo.getClosingBalance() == null ? BigDecimal.ZERO : subjectVo.getClosingBalance();
        String recentPeriodYear = String.format("%02d", periodBalance.getPeriodYear());
        String recentPeriodNum = String.format("%02d", periodBalance.getPeriodNum());
        Integer recentPeriod = Integer.valueOf(recentPeriodYear + recentPeriodNum);
        //如果当前期间是启用期间的话,期初取启用期间的期初,期末取启用期间的期初
        if (startPeriodBalance != null && recentPeriod.equals(startPeriodBalance.getPeriodYearNum())) {
            openingBalance = startPeriodBalance.getOpeningBalance();
            closingBalance = startPeriodBalance.getOpeningBalance();
            subjectVo.setOpeningBalance(openingBalance);
        }
        if (index == 1) {
            subjectVo.setOpeningBalance(openingBalance);
        }
        if (balanceSubjectVo.getBalanceDirection().equals(Constant.BalanceDirection.DEBIT)) {
            //借方
            if (recentPeriod < beginPeriod) {
                //计算期末余额直到开始期间的前一期间作为该数据的期初
                openingBalance = openingBalance.add(debitAmount).subtract(creditAmount);
                subjectVo.setOpeningBalance(openingBalance);
            }
            //计算期末余额直到最后一个期间
            closingBalance = closingBalance.add(debitAmount).subtract(creditAmount);
        } else if (balanceSubjectVo.getBalanceDirection().equals(Constant.BalanceDirection.CREDIT)) {
            //贷方
            if (recentPeriod < beginPeriod) {
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
     * 计算合计信息并返回最终结果集
     *
     * @param finalResultList 最终结果集
     * @param resultList      处理结果集
     */
    private void countTotalInfo(List<BalanceSubjectVo> finalResultList, List<BalanceSubjectVo> resultList) {
        //根据账簿id分组
        Map<String, List<BalanceSubjectVo>> accountBookMap = resultList.stream().collect
                (Collectors.groupingBy(e -> e.getAccountBookId().toString()));
        //账簿合计
        BalanceSubjectVo bookTotalInfo;
        //核算主体合计
        BalanceSubjectVo entityTotalInfo;
        List<BalanceSubjectVo> balanceVoList;
        Map<String, List<BalanceSubjectVo>> collect;
        BalanceSubjectVo balanceInfo;
        BalanceSubjectVo bookBalanceInfo;
        for (String key : accountBookMap.keySet()) {
            balanceVoList = accountBookMap.get(key);
            //计算账簿下所有合计的金额
            bookBalanceInfo = new BalanceSubjectVo();
            //再根据核算主体id分组
            collect = balanceVoList.stream().collect
                    (Collectors.groupingBy(e -> e.getAccountBookEntityId().toString()));
            for (String item : collect.keySet()) {
                balanceVoList = collect.get(item);
                //计算核算主体下所有下级非末级科目的金额
                balanceInfo = new BalanceSubjectVo();
                countParentBalance(balanceVoList, balanceInfo, Constant.Is.NO);
                //生成一条核算主体合计信息
                entityTotalInfo = new BalanceSubjectVo();
                initEntityTotalInfo(balanceInfo, entityTotalInfo, balanceVoList);
                finalResultList.addAll(balanceVoList);
                finalResultList.add(entityTotalInfo);
                //计算账簿金额合计信息
                countBookBalance(bookBalanceInfo, balanceInfo);
            }
            //生成一条账簿合计信息
            bookTotalInfo = new BalanceSubjectVo();
            initBookTotalInfo(bookBalanceInfo, bookTotalInfo, balanceVoList);
            finalResultList.add(bookTotalInfo);
        }
    }

    /**
     * 计算合计信息
     *
     * @param bookBalanceInfo 账簿合计信息
     * @param balanceInfo     核算主体合计信息
     */
    private void countBookBalance(BalanceSubjectVo bookBalanceInfo, BalanceSubjectVo balanceInfo) {
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
     * 组装账簿合计信息
     *
     * @param bookBalanceInfo 账簿合计余额信息
     * @param bookTotalInfo   账簿合计信息
     * @param balanceVoList   分组信息
     */
    private void initBookTotalInfo(BalanceSubjectVo bookBalanceInfo, BalanceSubjectVo bookTotalInfo, List<BalanceSubjectVo> balanceVoList) {
        bookTotalInfo.setAccountBookId(balanceVoList.get(0).getAccountBookId());
        bookTotalInfo.setAccountBookName(balanceVoList.get(0).getAccountBookName());
        bookTotalInfo.setAccountBookEntityName(LedgerConstant.FinancialString.TOTAL);
        //根据期初和期末设置方向
//        setTotalDirectionName(bookBalanceInfo.getOpeningBalance(), bookBalanceInfo.getClosingBalance(), bookTotalInfo);
//        bookTotalInfo.setOpeningBalance(bookBalanceInfo.getOpeningBalance().compareTo(BigDecimal.ZERO) >= 0 ?
//                bookBalanceInfo.getOpeningBalance() : bookBalanceInfo.getOpeningBalance().negate());
//        bookTotalInfo.setClosingBalance(bookBalanceInfo.getClosingBalance().compareTo(BigDecimal.ZERO) >= 0 ?
//                bookBalanceInfo.getClosingBalance() : bookBalanceInfo.getClosingBalance().negate());
        bookTotalInfo.setDebitAmount(bookBalanceInfo.getDebitAmount());
        bookTotalInfo.setCreditAmount(bookBalanceInfo.getCreditAmount());
        bookTotalInfo.setTotalDebitAmount(bookBalanceInfo.getTotalDebitAmount());
        bookTotalInfo.setTotalCreditAmount(bookBalanceInfo.getTotalCreditAmount());
    }

    /**
     * 设置上级科目信息的期初和期末
     *
     * @param openingBalance 期初
     * @param closingBalance 期末
     * @param balanceInfo    上级科目信息
     */
    private void setParentBalance(BigDecimal openingBalance, BigDecimal closingBalance, BalanceSubjectVo balanceInfo) {
        //因为期初和期末是以借方为标准来计算的,所以当科目方向为贷方时,期初期末应取相反数
        if (balanceInfo.getBalanceDirection().equals(Constant.BalanceDirection.DEBIT)) {
            balanceInfo.setOpeningBalance(openingBalance);
            balanceInfo.setClosingBalance(closingBalance);
        } else {
            balanceInfo.setOpeningBalance(openingBalance.negate());
            balanceInfo.setClosingBalance(closingBalance.negate());
        }
    }

    /**
     * 设置合计信息的方向
     *
     * @param openingBalance 期初
     * @param closingBalance 期末
     * @param balanceInfo    合计信息
     */
    private void setTotalDirectionName(BigDecimal openingBalance, BigDecimal closingBalance, BalanceSubjectVo balanceInfo) {
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
     * 组装核算主体合计信息
     *
     * @param balanceInfo     余额信息
     * @param entityTotalInfo 核算主体合计信息
     * @param balanceVoList   分组信息
     */
    private void initEntityTotalInfo(BalanceSubjectVo balanceInfo, BalanceSubjectVo entityTotalInfo, List<BalanceSubjectVo> balanceVoList) {
        entityTotalInfo.setAccountBookId(balanceVoList.get(0).getAccountBookId());
        entityTotalInfo.setAccountBookName(balanceVoList.get(0).getAccountBookName());
        entityTotalInfo.setAccountBookEntityId(balanceVoList.get(0).getAccountBookEntityId());
        entityTotalInfo.setAccountBookEntityName(balanceVoList.get(0).getAccountBookEntityName());
//        entityTotalInfo.setOpeningDirectionName(balanceInfo.getOpeningDirectionName());
//        entityTotalInfo.setClosingDirectionName(balanceInfo.getClosingDirectionName());
//        entityTotalInfo.setOpeningBalance(balanceInfo.getOpeningBalance());
//        entityTotalInfo.setClosingBalance(balanceInfo.getClosingBalance());
        entityTotalInfo.setDebitAmount(balanceInfo.getDebitAmount());
        entityTotalInfo.setCreditAmount(balanceInfo.getCreditAmount());
        entityTotalInfo.setTotalDebitAmount(balanceInfo.getTotalDebitAmount());
        entityTotalInfo.setTotalCreditAmount(balanceInfo.getTotalCreditAmount());
        entityTotalInfo.setCode(LedgerConstant.FinancialString.TOTAL);
    }

}
