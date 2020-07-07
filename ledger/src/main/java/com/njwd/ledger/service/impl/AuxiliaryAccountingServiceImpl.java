package com.njwd.ledger.service.impl;

import com.njwd.common.Constant;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.basedata.dto.AccountingItemValueDto;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.basedata.vo.AccountingItemValueVo;
import com.njwd.entity.basedata.vo.AccountingItemVo;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.CommonAuxiliary;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.ledger.dto.AuxiliaryAccountingQueryDto;
import com.njwd.entity.ledger.dto.BalanceSubjectAuxiliaryItemQueryDto;
import com.njwd.entity.ledger.dto.VoucherEntryAuxiliaryDto;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import com.njwd.entity.ledger.vo.BalanceSubjectAuxiliaryItemVo;
import com.njwd.entity.ledger.vo.GeneralReturnAuxiliaryVo;
import com.njwd.entity.ledger.vo.VoucherEntryAuxiliaryVo;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.entity.platform.vo.AuxiliaryItemVo;
import com.njwd.ledger.cloudclient.AccountSubjectFeignClient;
import com.njwd.ledger.cloudclient.AccountingItemClient;
import com.njwd.ledger.cloudclient.AccountingItemValueClient;
import com.njwd.ledger.cloudclient.AuxiliaryItemClient;
import com.njwd.ledger.mapper.AuxiliaryItemAccountMapper;
import com.njwd.ledger.mapper.BalanceSubjectAuxiliaryItemMapper;
import com.njwd.ledger.mapper.VoucherEntryAuxiliaryMapper;
import com.njwd.ledger.service.AccountBookPeriodService;
import com.njwd.ledger.service.AuxiliaryAccountingService;
import com.njwd.service.FileService;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.MergeUtil;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author: ZhuHC
 * @Date: 2019/7/30 11:21
 */
@Service
public class AuxiliaryAccountingServiceImpl implements AuxiliaryAccountingService {

    @Resource
    private AuxiliaryItemAccountMapper auxiliaryItemAccountMapper;

    @Resource
    private AuxiliaryItemClient auxiliaryItemClient;

    @Resource
    private AccountingItemClient accountingItemClient;

    @Resource
    private AccountingItemValueClient accountingItemValueClient;

    @Resource
    private AccountSubjectFeignClient accountSubjectFeignClient;

    @Resource
    private AccountBookPeriodService accountBookPeriodService;

    @Resource
    private FileService fileService;

    @Resource
    private BalanceSubjectAuxiliaryItemMapper balanceSubjectAuxiliaryItemMapper;

    @Resource
    private VoucherEntryAuxiliaryMapper voucherEntryAuxiliaryMapper;
    /**
     * @Author ZhuHC
     * @Date  2019/7/30 11:24
     * @Param
     * @return
     * @Description 根据辅助核算项目  查询明细
     */
    @Override
    public List<GeneralReturnAuxiliaryVo> findAuxiliaryDetails(AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto){
        //TODO 排列出所有 辅助核算项组合；后期优化
        Map<String,List<Long>> sourceTableAndValueList = auxiliaryAccountingQueryDto.getSourceTableAndValueList();
        List<Map<String,List<Long>>> mapList = new ArrayList<>();
        if(null != sourceTableAndValueList && sourceTableAndValueList.size() > 0){
            List<List<Long>> lists = new ArrayList<>();
            List<String> keyList = new ArrayList<>();
            for (Map.Entry<String,List<Long>> entry : sourceTableAndValueList.entrySet()) {
                keyList.add(entry.getKey());
                lists.add(entry.getValue());
            }
            List<List<Long>> returnLists = cartesianProduct(lists);
            Map<String,List<Long>> stringListMap = null;
            for(List itemIdList : returnLists){
                stringListMap = new HashMap<>();
                for(int i = 0; i < itemIdList.size(); i++){
                    stringListMap.put(keyList.get(i),Arrays.asList(Long.valueOf(itemIdList.get(i).toString())));
                }
                mapList.add(stringListMap);
            }
        }
        List<GeneralReturnAuxiliaryVo> voList = new LinkedList<>();
        if(!FastUtils.checkNullOrEmpty(mapList)){
            List<GeneralReturnAuxiliaryVo> auxiliaryVos;
            for(Map<String,List<Long>> map : mapList){
                auxiliaryAccountingQueryDto.setSourceTableAndValueList(map);
                auxiliaryVos = getAuxiliaryVos(auxiliaryAccountingQueryDto);
                voList.addAll(auxiliaryVos);
            }
            //拼接keySign
            List<CommonAuxiliary> commonAuxiliaryList;
            StringBuilder sb;
            for(GeneralReturnAuxiliaryVo returnAuxiliaryVo : voList){
                commonAuxiliaryList = returnAuxiliaryVo.getCommonAuxiliaryList();
                sb = new StringBuilder();
                for(CommonAuxiliary auxiliary : commonAuxiliaryList){
                    sb.append(auxiliary.getSourceTable()).append(auxiliary.getId()).append("_");
                }
                returnAuxiliaryVo.setKeySign(sb);
            }
        }else {
            voList = getAuxiliaryVos(auxiliaryAccountingQueryDto);
        }
        // 获得 明细账
        for(GeneralReturnAuxiliaryVo vo : voList) {
            if (Constant.SortNum.DETAIL.equals(vo.getSortNum())) {
                vo.setCredentialWordCode(convertData(vo.getCredentialWord()) + "-" + vo.getMainCode());
            }
        }

        return voList;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/10/7 12:23
     * @Param [lists]
     * @return java.util.List<java.util.List<T>>
     * @Description 多个集合的笛卡尔积
     */
    protected <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
        List<List<T>> resultLists = new ArrayList<List<T>>();
        if (lists.size() == 0) {
            resultLists.add(new ArrayList<T>());
            return resultLists;
        } else {
            List<T> firstList = lists.get(0);
            List<List<T>> remainingLists = cartesianProduct(lists.subList(1, lists.size()));
            for (T condition : firstList) {
                for (List<T> remainingList : remainingLists) {
                    ArrayList<T> resultList = new ArrayList<T>();
                    resultList.add(condition);
                    resultList.addAll(remainingList);
                    resultLists.add(resultList);
                }
            }
        }
        return resultLists;
    }

    /**
     * @Author wuweiming
     * @Date  2019/08/07 09:59
     * @Param []
     * @return List<AuxiliaryItemVo>
     * @Description 查询所有辅助核算（包含自定义）
     */
    @Override
    public List<AuxiliaryItemVo> findAuxiliaryItemList(){
        //获取所有未删除的辅助核算
        Result<List<AuxiliaryItemVo>> auxiliaryItemResult = auxiliaryItemClient.findAllAuxiliaryItem();
        List<AuxiliaryItemVo> auxiliaryItem = new ArrayList<>();
        if (auxiliaryItemResult.getData() != null){
            auxiliaryItem = auxiliaryItemResult.getData();
        }
        //获取所有未删除的自定义核算
        Result<List<AccountingItemVo>> accountingItemResult = accountingItemClient.findAllAccountItem();
        List<AccountingItemVo> accountingItem = new ArrayList<>();
        if (accountingItemResult.getData() != null){
            accountingItem = accountingItemResult.getData();
        }
        //遍历自定义核算
        for (AccountingItemVo item : accountingItem){
            AuxiliaryItemVo vo = new AuxiliaryItemVo();
            vo.setId(item.getId());
            vo.setName(item.getName());
            vo.setSourceTable(item.getSourceTable());
            auxiliaryItem.add(vo);
        }
        //辅助核算
        return auxiliaryItem;
    }

    /**
     * @Author wuweiming
     * @Date  2019/08/07 14:19
     * @Param [AccountingItemValueDto]
     * @return List<AccountingItemValueVo>
     * @Description 查询所有辅助核算（包含自定义）
     */
    @Override
    public List<AccountingItemValueVo> findAuxiliaryItemValues(AccountingItemValueDto dto){
        List<AccountingItemValueVo> list = new ArrayList<>();
        //判断数据来源表
        if (dto.getSourceTable().equals(Constant.TableName.ACCOUNTING_ITEM)){
            //来源自定义核算表，则去wd_accounting_item_value表查询数据
            dto.setSourceTable(Constant.TableName.ACCOUNTING_ITEM_VALUE);
            //根据 itemId 查询自定义核算
            Result<List<AccountingItemValueVo>> result = accountingItemValueClient.findAllAccountItemValueByItemId(dto);
            //判断是否有数据
            if (result.getData() != null){
                list = result.getData();
            }
        }else if (!dto.getSourceTable().equals(Constant.TableName.ACCOUNTING_ITEM) && dto.getSourceTable() != null && !dto.getSourceTable().equals("")){
            //来源非自定义表且来源不为空
            //查询辅助核算
            Result<List<AccountingItemValueVo>> result = accountingItemValueClient.findAllAuxiliaryItemValue(dto);
            //判断是否有数据
            if (result.getData() != null){
                list = result.getData();
            }
        }
        return list;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/30 10:16
     * @Param
     * @return
     * @Description 辅助核算明细账 数据导出
     */
    @Override
    public void exportExcel(AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto, HttpServletResponse response) {
        // 获得 明细账
        List<GeneralReturnAuxiliaryVo> returnAuxiliaryVoList = getAuxiliaryVos(auxiliaryAccountingQueryDto);
        if(null != returnAuxiliaryVoList){
            StringBuffer codeStr;
            StringBuffer nameStr;
            for(GeneralReturnAuxiliaryVo vo : returnAuxiliaryVoList){
                if(Constant.SortNum.DETAIL.equals(vo.getSortNum())){
                    vo.setCredentialWordCode(convertData(vo.getCredentialWord())+"-"+vo.getMainCode());
                }
                vo.setDirection(convertDirection(vo.getBalanceDirection()));
                codeStr = new StringBuffer();
                nameStr = new StringBuffer();
                for(CommonAuxiliary common : vo.getCommonAuxiliaryList()){
                    codeStr = codeStr.append(common.getCode()).append(" ");
                    nameStr = nameStr.append(common.getName()).append(" ");
                }
                vo.setAuxiliaryInfoCode(codeStr.toString());
                vo.setAuxiliaryInfoName(nameStr.toString());
            }
            if (Constant.Is.YES.equals(auxiliaryAccountingQueryDto.getIsShowFullName())){
                fileService.exportExcel(response, returnAuxiliaryVoList,"AuxiliaryAccountingQueryResult",
                        new ExcelColumn("auxiliaryInfoCode","辅助编码"),
                        new ExcelColumn("auxiliaryInfoName","辅助名称"),
                        new ExcelColumn("accountBookName","总账账簿"),
                        new ExcelColumn("accountBookEntityName","核算主体"),
                        new ExcelColumn("subjectCode","科目编码"),
                        new ExcelColumn("subjectFullName","科目名称"),
                        new ExcelColumn("voucherDate","日期"),
                        new ExcelColumn("periodYear","会计年度"),
                        new ExcelColumn("periodNum","期间"),
                        new ExcelColumn("credentialWordCode","凭证字号"),
                        new ExcelColumn("abstractContent","摘要"),
                        new ExcelColumn("debit","借方"),
                        new ExcelColumn("credit","贷方"),
                        new ExcelColumn("direction","方向"),
                        new ExcelColumn("balance","余额")
                );
            }else {
                fileService.exportExcel(response, returnAuxiliaryVoList,"AuxiliaryAccountingQueryResult",
                        new ExcelColumn("auxiliaryInfoCode","辅助编码"),
                        new ExcelColumn("auxiliaryInfoName","辅助名称"),
                        new ExcelColumn("accountBookName","总账账簿"),
                        new ExcelColumn("accountBookEntityName","核算主体"),
                        new ExcelColumn("subjectCode","科目编码"),
                        new ExcelColumn("subjectName","科目名称"),
                        new ExcelColumn("voucherDate","日期"),
                        new ExcelColumn("periodYear","会计年度"),
                        new ExcelColumn("periodNum","期间"),
                        new ExcelColumn("credentialWordCode","凭证字号"),
                        new ExcelColumn("abstractContent","摘要"),
                        new ExcelColumn("debit","借方"),
                        new ExcelColumn("credit","贷方"),
                        new ExcelColumn("direction","方向"),
                        new ExcelColumn("balance","余额")
                );
            }

        }
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/29 17:00
     * @Param [credentialWord]
     * @return java.lang.String
     * @Description 凭证字数据转换
     */
    @Override
    public String convertData(Byte credentialWord){
        //凭证字类型 1：记 、2：收、3：付、4：转
        String wordStr = null;
        if(null != credentialWord){
            switch (credentialWord){
                case Constant.CredentialWordType.RECORD:
                    wordStr = LedgerConstant.CredentialWordName.RECORD;
                    break;
                case Constant.CredentialWordType.RECEIVE:
                    wordStr = LedgerConstant.CredentialWordName.RECEIVE ;
                    break;
                case Constant.CredentialWordType.PAY:
                    wordStr = LedgerConstant.CredentialWordName.PAY ;
                    break;
                case Constant.CredentialWordType.TRANSFER:
                    wordStr = LedgerConstant.CredentialWordName.TRANSFER;
                    break;
                default:
            }
        }
        return wordStr;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/29 15:45
     * @Param [auxiliaryAccountingQueryDto]
     * @return java.util.List<com.njwd.entity.ledger.vo.GeneralReturnAuxiliaryVo>
     * @Description  数据获取 数据处理
     */
    private List<GeneralReturnAuxiliaryVo> getAuxiliaryVos(AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto){
        //TODO 将传入的 Map<String,List<Long>> sourceTableAndValueList 转为  List<CommonAuxiliary> sourceTableAndValue 下次迭代前端直接传sourceTableAndValue
        Map<String,List<Long>> sourceTableAndValueList = auxiliaryAccountingQueryDto.getSourceTableAndValueList();
        if(null != sourceTableAndValueList && sourceTableAndValueList.size() > 0){
            //查询所有辅助核算项信息
            AccountSubjectDto param = new AccountSubjectDto();
            List<String> sourceTableList = new ArrayList<>();
            List<List<List<Long>>> idLists = new ArrayList<>();
            List<List<Long>> idList ;
            List<Long> ids ;
            for (Map.Entry<String,List<Long>> entry : sourceTableAndValueList.entrySet()) {
                ids = new ArrayList<>();
                idList = new ArrayList<>();
                sourceTableList.add(entry.getKey());
                ids.addAll(entry.getValue());
                idList.add(ids);
                idLists.add(idList);
            }
            param.setSourceTableList(sourceTableList);
            param.setIdLists(idLists);
            auxiliaryAccountingQueryDto.setSourceTableList(sourceTableList);
            List<List<Map<String, Object>>> allSourceTableInfo = accountSubjectFeignClient.findAllSourceTableInfo(param).getData();
            // 辅助核算项目值 转换
            List<CommonAuxiliary> commonAuxiliaryList = new LinkedList<>();
            if(null != allSourceTableInfo && !allSourceTableInfo.isEmpty()){
                for(int i = 0; i < allSourceTableInfo.size(); i++){
                    if(!FastUtils.checkNullOrEmpty(allSourceTableInfo.get(i)) && null != sourceTableList.get(i)){
                        commonAuxiliaryList.addAll(getCommonAuxiliaries(allSourceTableInfo.get(i),sourceTableList.get(i)));
                    }
                }
            }
            auxiliaryAccountingQueryDto.setSourceTableAndValue(commonAuxiliaryList);
        }
        //从基础资料获取 会计科目数据
        List<AccountSubjectVo> subjectList = getAccountSubjectVos(auxiliaryAccountingQueryDto);
        List<Long> subjectIdList = new ArrayList<>();
        // 获得会计科目 ID
        if(!FastUtils.checkNullOrEmpty(subjectList)){
            for(AccountSubjectVo accountSubjectVo : subjectList){
                subjectIdList.add(accountSubjectVo.getId());
            }
        }
        auxiliaryAccountingQueryDto.setSubjectIdList(subjectIdList);
        auxiliaryAccountingQueryDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        List<GeneralReturnAuxiliaryVo> totalList = getTotalList(auxiliaryAccountingQueryDto,subjectList);
        if(!FastUtils.checkNullOrEmpty(totalList)){
            //合并list
            MergeUtil.merge(totalList,subjectList,
                    (tol, sub) -> tol.getSubjectId().equals(sub.getId().toString()),
                    (tol, sub) -> {
                        tol.setSubjectCode(sub.getCode());
                        tol.setSubjectName(sub.getName());
                        tol.setSubjectFullName(sub.getFullName());
                        tol.setBalanceDirection(sub.getBalanceDirection());
                    });
            //将账簿名称 和 主体名称 拼接到 科目数据上
            MergeUtil.merge(totalList,auxiliaryAccountingQueryDto.getAccountBookEntityList(),
                    (tol, dto) -> tol.getAccountBookId().equals(dto.getAccountBookId()) && tol.getAccountBookEntityId().equals(dto.getEntityId()),
                    (tol, dto) -> {
                        tol.setAccountBookName(dto.getAccountBookName());
                        tol.setAccountBookEntityName(dto.getEntityName());
                    });
            //判断方向 计算余额
            countBalance(totalList,auxiliaryAccountingQueryDto);
            deleteInfoByStatus(totalList,auxiliaryAccountingQueryDto);
        }
        for(GeneralReturnAuxiliaryVo returnAuxiliaryVo : totalList){
            returnAuxiliaryVo.setCommonAuxiliaryList(auxiliaryAccountingQueryDto.getSourceTableAndValue());
        }
        return totalList;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/8 16:20
     * @Param [auxiliaryAccountingQueryDto]
     * @return java.util.List<com.njwd.entity.platform.vo.AccountSubjectVo>
     * @Description 设置会计科目 查询条件
     */
    private List<AccountSubjectVo> getAccountSubjectVos(AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto) {
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        accountSubjectDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        Byte operator = auxiliaryAccountingQueryDto.getAccountSubjectOperator();
        accountSubjectDto.setSubjectCodeOperator(operator);
        if(operator.equals(LedgerConstant.QueryScheme.OPERATOR_EQUAL)){
            accountSubjectDto.setIds(auxiliaryAccountingQueryDto.getSubjectCodes());
        }
        if(operator.equals(LedgerConstant.QueryScheme.OPERATOR_RANGE)){
            accountSubjectDto.setSubjectCodes(auxiliaryAccountingQueryDto.getSubjectCodes());
        }
        return accountSubjectFeignClient.findInfoForLedger(accountSubjectDto).getData();
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/29 15:46
     * @Param [auxiliaryAccountingQueryDto, sourceTableAndValueList, auxiliaryMap]
     * @return java.util.List<com.njwd.entity.ledger.vo.GeneralReturnAuxiliaryVo>
     * @Description 本年数据
     */
    private List<GeneralReturnAuxiliaryVo> getPeriodYearInfos(AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto) {
        if(null != auxiliaryAccountingQueryDto.getSubjectAuxiliaryItemQueryDto()){
            List<BalanceSubjectAuxiliaryItemVo> auxiliaryItemVoList = balanceSubjectAuxiliaryItemMapper.findListByAuxiliariesAndPeriod(auxiliaryAccountingQueryDto.getSubjectAuxiliaryItemQueryDto());
            List<Long> idListByRule = getInfoByRules(auxiliaryAccountingQueryDto, auxiliaryItemVoList);
            auxiliaryAccountingQueryDto.setAuxiliaryIdList(idListByRule);
        }
        //获得 本期 数据
        List<GeneralReturnAuxiliaryVo> periodYearInfo = auxiliaryItemAccountMapper.getPeriodYearAccount(auxiliaryAccountingQueryDto);
        return periodYearInfo;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/29 15:46
     * @Param [auxiliaryAccountingQueryDto, sourceTableAndValueList, auxiliaryMap]
     * @return java.util.List<com.njwd.entity.ledger.vo.GeneralReturnAuxiliaryVo>
     * @Description 本期数据
     */
    private List<GeneralReturnAuxiliaryVo> getPeriodNumInfos(AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto) {
        if(null != auxiliaryAccountingQueryDto.getSubjectAuxiliaryItemQueryDto()){
            List<BalanceSubjectAuxiliaryItemVo> auxiliaryItemVoList = balanceSubjectAuxiliaryItemMapper.findListByAuxiliariesAndPeriod(auxiliaryAccountingQueryDto.getSubjectAuxiliaryItemQueryDto());
            List<Long> idListByRule = getInfoByRules(auxiliaryAccountingQueryDto, auxiliaryItemVoList);
            auxiliaryAccountingQueryDto.setAuxiliaryIdList(idListByRule);
        }
        //获得 本期 数据
        List<GeneralReturnAuxiliaryVo> periodNumInfo = auxiliaryItemAccountMapper.getPeriodNumAccount(auxiliaryAccountingQueryDto);
        return periodNumInfo;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/29 15:46
     * @Param [auxiliaryAccountingQueryDto, sourceTableAndValueList, auxiliaryMap]
     * @return java.util.List<com.njwd.entity.ledger.vo.GeneralReturnAuxiliaryVo>
     * @Description 分录数据
     */
    private List<GeneralReturnAuxiliaryVo> getDetailInfos(AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto) {
        if(null != auxiliaryAccountingQueryDto.getVoucherEntryAuxiliaryDto()){
            List<VoucherEntryAuxiliaryVo> entryAuxiliaryVos = voucherEntryAuxiliaryMapper.findListByAuxiliary(auxiliaryAccountingQueryDto.getVoucherEntryAuxiliaryDto());
            List<Long> idListByRule = getDetailInfosByRules(auxiliaryAccountingQueryDto, entryAuxiliaryVos);
            auxiliaryAccountingQueryDto.setAuxiliaryIdList(idListByRule);
        }
        List<GeneralReturnAuxiliaryVo> detailInfo = auxiliaryItemAccountMapper.getDetailAccount(auxiliaryAccountingQueryDto);
        return detailInfo;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/29 15:46
     * @Param [auxiliaryAccountingQueryDto, sourceTableAndValueList, auxiliaryMap]
     * @return java.util.List<com.njwd.entity.ledger.vo.GeneralReturnAuxiliaryVo>
     * @Description  根据辅助核算明细 删除不符合的 分录数据
     */
    private List<Long> getDetailInfosByRules(AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto,List<VoucherEntryAuxiliaryVo> entryAuxiliaryVos) {
        List<Long> valueIdList = new LinkedList<>();
        List<CommonAuxiliary> sourceTableAndValue = auxiliaryAccountingQueryDto.getSourceTableAndValue();
        StringBuffer sourceTableBuffer = new StringBuffer();
        StringBuffer itemIdBuffer = new StringBuffer();
        CommonAuxiliary commonAuxiliary;
        for(int i = 0 ;i < sourceTableAndValue.size() ; i++){
            commonAuxiliary = sourceTableAndValue.get(i);
            sourceTableBuffer.append(commonAuxiliary.getSourceTable());
            itemIdBuffer.append(commonAuxiliary.getId());
            if(i != sourceTableAndValue.size()-1){
                sourceTableBuffer.append(",");
                itemIdBuffer.append(",");
            }
        }
        String sourceTableSign = sourceTableBuffer.toString();
        String itemIdSign = itemIdBuffer.toString();
        List<String> auxiliaryIds;
        for(VoucherEntryAuxiliaryVo vo : entryAuxiliaryVos){
            if(sourceTableSign.equals(vo.getSourceTables()) && itemIdSign.equals(vo.getItemValueIds())){
                auxiliaryIds = Arrays.asList(vo.getEntryIds().split(","));
                valueIdList.addAll(auxiliaryIds.stream().map(id -> Long.parseLong(id)).collect(Collectors.toList()));
            }
        }
       return valueIdList;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/29 15:48
     * @Param [vo, entry]
     * @return void
     * @Description 设置通用标识
     */
    private void setReturnCommonInfo(GeneralReturnAuxiliaryVo vo, Map.Entry<Long, List<CommonAuxiliary>> entry) {
        List<CommonAuxiliary> commonAuxiliaryList = entry.getValue();
        vo.setCommonAuxiliaryList(commonAuxiliaryList);
        StringBuilder keySign = new StringBuilder();
        keySign.append(vo.getAccountBookId()).append(Constant.Character.UNDER_LINE);
        keySign.append(vo.getAccountBookEntityId()).append(Constant.Character.UNDER_LINE);
        keySign.append(vo.getSubjectId()).append(Constant.Character.UNDER_LINE);
        if(!FastUtils.checkNullOrEmpty(commonAuxiliaryList)){
            for (CommonAuxiliary common : commonAuxiliaryList) {
                keySign.append(common.getSign()).append(Constant.Character.UNDER_LINE);
            }
        }
        vo.setKeySign(keySign);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/26 10:11
     * @Param [periodTotalBySubject, startOpeningInfo]
     * @return java.util.List<com.njwd.entity.ledger.vo.GeneralReturnItemJournalVo>
     * @Description 计算 借贷金额
     */
    private List<GeneralReturnAuxiliaryVo> getOpenInfo(List<GeneralReturnAuxiliaryVo> periodTotalBySubject,List<GeneralReturnAuxiliaryVo> startOpeningInfo,AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto) {
        List<GeneralReturnAuxiliaryVo> voList = new LinkedList<>();
        for(GeneralReturnAuxiliaryVo vo : periodTotalBySubject){
            vo.setSignFlag(vo.getAccountBookId()+"_"+vo.getAccountBookEntityId()+"_"+vo.getSubjectId());
        }
        Map<String, List<GeneralReturnAuxiliaryVo>> resultMap = new HashMap<>();
        for (GeneralReturnAuxiliaryVo vo : periodTotalBySubject) {
            if (resultMap.containsKey(vo.getSignFlag())) {
                //map中存在此flag，将数据存放当前key的map中
                resultMap.get(vo.getSignFlag()).add(vo);
            } else {
                //map中不存在，新建key，用来存放数据
                List<GeneralReturnAuxiliaryVo> tmpList = new ArrayList<>();
                tmpList.add(vo);
                resultMap.put(vo.getSignFlag(), tmpList);
            }
        }
        DecimalFormat format = new DecimalFormat("##0.00");
        // 借
        String debit ;
        // 贷
        String credit ;
        //余额
        String balance = format.format(0);
        int direction;
        GeneralReturnAuxiliaryVo journalVo = null;
        for (Map.Entry<String,List<GeneralReturnAuxiliaryVo>> map : resultMap.entrySet()) {
            for(int i = 0; i< map.getValue().size(); i++){
                GeneralReturnAuxiliaryVo vo = map.getValue().get(i);
                for(GeneralReturnAuxiliaryVo startVo: startOpeningInfo){
                    if(0 == i && vo.getAccountBookId().equals(startVo.getAccountBookId()) && vo.getAccountBookEntityId().equals(startVo.getAccountBookEntityId())){
                        balance = format.format(startVo.getBalance());
                    }
                }
                journalVo = vo;
                direction = vo.getBalanceDirection();
                debit = format.format(vo.getDebit());
                credit = format.format(vo.getCredit());
                Long periodYearNum = vo.getPeriodYearNum();
                List<Long> periodYearNumList = auxiliaryAccountingQueryDto.getPeriodYearNum();
                if(periodYearNumList.get(0) <= periodYearNum && periodYearNum <= periodYearNumList.get(1)){
                    if(null != startOpeningInfo && startOpeningInfo.size() > 0){
                        balance = format.format(new BigDecimal(balance));
                    }else {
                        balance = format.format(new BigDecimal(vo.getBalance()));
                    }
                }else{
                    //发生额 借方：加借减贷  贷方：加贷减借
                    if(Constant.BalanceDirection.DEBIT == direction){
                        balance = format.format(new BigDecimal(balance).add(new BigDecimal(debit)).subtract(new BigDecimal(credit)));
                    }
                    if(Constant.BalanceDirection.CREDIT == direction){
                        balance = format.format(new BigDecimal(balance).add(new BigDecimal(credit)).subtract(new BigDecimal(debit)));
                    }
                }
            }
            if(null != journalVo){
                journalVo.setDebit(0);
                journalVo.setCredit(0);
                journalVo.setBalance(new BigDecimal(balance).doubleValue());
                voList.add(journalVo);
                balance = format.format(0);
                debit = format.format(0);
                credit = format.format(0);
            }
        }
        return voList;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/29 15:49
     * @Param [auxiliaryAccountingQueryDto, sourceTableAndValueList, auxiliaryMap]
     * @return java.util.List<com.njwd.entity.ledger.vo.GeneralReturnAuxiliaryVo>
     * @Description 期初数据
     */
    private List<GeneralReturnAuxiliaryVo> getOpeningInfos(AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto, List<AccountSubjectVo> subjectList) {
        List<GeneralReturnAuxiliaryVo> openingInfo = new ArrayList<>();
        //取得启用期间的期初余额
        if(null != auxiliaryAccountingQueryDto.getSubjectAuxiliaryItemQueryDto()){
            List<BalanceSubjectAuxiliaryItemVo> auxiliaryItemVos = balanceSubjectAuxiliaryItemMapper.findListByAuxiliaries(auxiliaryAccountingQueryDto.getSubjectAuxiliaryItemQueryDto());
            List<Long> idsByRule = getInfoByRules(auxiliaryAccountingQueryDto, auxiliaryItemVos);
            auxiliaryAccountingQueryDto.setAuxiliaryIdList(idsByRule);
            List<BalanceSubjectAuxiliaryItemVo> auxiliaryItemVoList = balanceSubjectAuxiliaryItemMapper.findListByAuxiliariesAndPeriod(auxiliaryAccountingQueryDto.getSubjectAuxiliaryItemQueryDto());
            List<Long> idListByRule = getInfoByRules(auxiliaryAccountingQueryDto, auxiliaryItemVoList);
            auxiliaryAccountingQueryDto.setAuxiliaryIdList(idListByRule);
        }
        //会计期间查询类型 0 制单日期 1 会计区间
        Byte periodOperator = auxiliaryAccountingQueryDto.getPeriodOperator();
        Long tempNum = 0L;
        if(0 == periodOperator){
            //设置制单日期从第0期开始
            List<Date> voucherDates = auxiliaryAccountingQueryDto.getVoucherDates();
            Date d = voucherDates.get(0);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String tempDate = formatter.format(d);
            String[] s = tempDate.split("-");
            tempNum = Long.valueOf(s[0]+"00");
        }else {
            List<Long> periodYearNums = auxiliaryAccountingQueryDto.getPeriodYearNum();
            //设置区间为第0期
            tempNum = periodYearNums.get(0)/ 100 * 100;
        }
        auxiliaryAccountingQueryDto.setPeriodNum(tempNum);
        //获取从第0期开始的数据
        List<GeneralReturnAuxiliaryVo> zeroInfo =auxiliaryItemAccountMapper.getZeroOpeningInfo(auxiliaryAccountingQueryDto);
        //获取最小期间开始的数据
        List<GeneralReturnAuxiliaryVo> startOpeningInfo = auxiliaryItemAccountMapper.getStartOpeningInfo(auxiliaryAccountingQueryDto);
        //拼接数据  条件相同 把第0期的数据赋值到最小期间数据上
        if(!FastUtils.checkNullOrEmpty(zeroInfo)){
            MergeUtil.merge(zeroInfo,startOpeningInfo,
                    (z, s) -> z.getAccountBookId().equals(s.getAccountBookId()) && z.getAccountBookEntityId() .equals(s.getAccountBookEntityId()) && z.getSubjectId() .equals(s.getSubjectId()),
                    (z, s) -> {
                     //   s.setBalance(z.getBalance());
                        s.setDebit(z.getDebit());
                        s.setCredit(z.getCredit());
                    });
        }
        //取得不同 账簿 主体 科目 下，账簿开始期间到 选择期间前一期的 借贷方发生额
        List<GeneralReturnAuxiliaryVo> periodTotal = auxiliaryItemAccountMapper.getOpeningAccount(auxiliaryAccountingQueryDto);
        if(!FastUtils.checkNullOrEmpty(periodTotal)){
            MergeUtil.merge(periodTotal,subjectList,
                    (tol, sub) -> tol.getSubjectId().equals(sub.getId().toString()),
                    (tol, sub) -> {
                        tol.setBalanceDirection(sub.getBalanceDirection());
                    });
            openingInfo = getOpenInfo(periodTotal,startOpeningInfo,auxiliaryAccountingQueryDto);
        }else {
            openingInfo.addAll(startOpeningInfo);
        }
        //查询条件是 制单日期时 将未包含进期初的凭证分录数据  累加进期初中
        if(LedgerConstant.PeriodOperator.VOUCHER_DATE.equals(auxiliaryAccountingQueryDto.getPeriodOperator()) && openingInfo.size() > 0){
            AuxiliaryAccountingQueryDto accountingQueryDto = new AuxiliaryAccountingQueryDto();
            List<AuxiliaryAccountingQueryDto> dtoList = new LinkedList<>();
            AuxiliaryAccountingQueryDto dto;
            for(GeneralReturnAuxiliaryVo vo : openingInfo){
                dto = new AuxiliaryAccountingQueryDto();
                dto.setVoucherDates(auxiliaryAccountingQueryDto.getVoucherDates());
                dto.setIsIncludeProfitAndLoss(auxiliaryAccountingQueryDto.getIsIncludeProfitAndLoss());
                dto.setIsIncludeUnbooked(auxiliaryAccountingQueryDto.getIsIncludeUnbooked());
                dto.setAccountBookId(vo.getAccountBookId());
                dto.setAccountBookEntityId(vo.getAccountBookEntityId());
                dto.setAccountSubjectId(Long.parseLong(vo.getSubjectId()));
                dto.setPeriodYearAndNum(vo.getPeriodYearNum());
                dtoList.add(dto);
            }
            accountingQueryDto.setDtoList(dtoList);
            accountingQueryDto.setAuxiliaryIdList(auxiliaryAccountingQueryDto.getAuxiliaryIdList());
            List<GeneralReturnAuxiliaryVo> openingLossList = auxiliaryItemAccountMapper.getOpeningLossInfo(accountingQueryDto);
            if(!FastUtils.checkNullOrEmpty(openingLossList)){
                //合并list
                MergeUtil.merge(openingLossList,subjectList,
                        (tol, sub) -> tol.getSubjectId().equals(sub.getId().toString()),
                        (tol, sub) -> {
                            tol.setBalanceDirection(sub.getBalanceDirection());
                        });
                DecimalFormat format = new DecimalFormat("##0.00");
                //余额
                MergeUtil.merge(openingInfo,openingLossList,
                        (op, opl) -> op.getSubjectId().equals(opl.getSubjectId())
                                && op.getAccountBookEntityId().equals(opl.getAccountBookEntityId()),
                        (op, opl) -> {
                            int direction = opl.getBalanceDirection();
                            String balance = format.format(0);
                            if(Constant.BalanceDirection.DEBIT == direction){
                                balance = format.format(new BigDecimal(op.getBalance()).add(new BigDecimal(opl.getDebit())).subtract(new BigDecimal(opl.getCredit())));
                            }
                            if(Constant.BalanceDirection.CREDIT == direction){
                                balance = format.format(new BigDecimal(op.getBalance()).add(new BigDecimal(opl.getCredit())).subtract(new BigDecimal(opl.getDebit())));
                            }
                            op.setBalance(new BigDecimal(balance).doubleValue());
                        });
            }
        }
        return openingInfo;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/29 15:49
     * @Param [sourceTableAndValueList, info, auxiliaryMap]
     * @return java.util.List<com.njwd.entity.ledger.vo.GeneralReturnAuxiliaryVo>
     * @Description 根据条件 获取数据
     */
    private List<Long> getInfoByRules(AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto,List<BalanceSubjectAuxiliaryItemVo> auxiliaryItemVos) {
        List<Long> valueIdList = new LinkedList<>();
        List<CommonAuxiliary> sourceTableAndValue = auxiliaryAccountingQueryDto.getSourceTableAndValue();
        StringBuffer sourceTableBuffer = new StringBuffer();
        StringBuffer itemIdBuffer = new StringBuffer();
        CommonAuxiliary commonAuxiliary;
        for(int i = 0 ;i < sourceTableAndValue.size() ; i++){
            commonAuxiliary = sourceTableAndValue.get(i);
            sourceTableBuffer.append(commonAuxiliary.getSourceTable());
            itemIdBuffer.append(commonAuxiliary.getId());
            if(i != sourceTableAndValue.size()-1){
                sourceTableBuffer.append(",");
                itemIdBuffer.append(",");
            }
        }
        String sourceTableSign = sourceTableBuffer.toString();
        String itemIdSign = itemIdBuffer.toString();
        List<String> auxiliaryIds;
        for(BalanceSubjectAuxiliaryItemVo vo : auxiliaryItemVos){
           if(sourceTableSign.equals(vo.getSourceTables()) && itemIdSign.equals(vo.getItemValueIds())){
               auxiliaryIds = Arrays.asList(vo.getBalanceAuxiliaryIds().split(","));
               valueIdList.addAll(auxiliaryIds.stream().map(id -> Long.parseLong(id)).collect(Collectors.toList()));
           }
        }
        return valueIdList;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/12 15:27
     * @Param [auxiliaryAccountingQueryDto, totalList]
     * @return void
     * @Description 期初 明细 本期 本年 数据合集
     */
    private List<GeneralReturnAuxiliaryVo> getTotalList(AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto,List<AccountSubjectVo> subjectList) {
        //辅助核算余额表查询条件
        BalanceSubjectAuxiliaryItemQueryDto subjectAuxiliaryItemQueryDto = new BalanceSubjectAuxiliaryItemQueryDto();
        List<BalanceSubjectAuxiliaryItemQueryDto> dtoList = new LinkedList<>();
        //辅助核算凭证表查询条件
        VoucherEntryAuxiliaryDto voucherEntryAuxiliaryDto = new VoucherEntryAuxiliaryDto();
        List<VoucherEntryAuxiliaryDto> auxiliaryDtoList = new LinkedList<>();
        if(!FastUtils.checkNullOrEmpty(auxiliaryAccountingQueryDto.getSourceTableAndValue())){
            for(CommonAuxiliary commonAuxiliary : auxiliaryAccountingQueryDto.getSourceTableAndValue()){
                subjectAuxiliaryItemQueryDto.setSourceTable(commonAuxiliary.getSourceTable());
                subjectAuxiliaryItemQueryDto.setItemValueId(commonAuxiliary.getId());
                dtoList.add(subjectAuxiliaryItemQueryDto);
                voucherEntryAuxiliaryDto.setSourceTable(commonAuxiliary.getSourceTable());
                voucherEntryAuxiliaryDto.setItemValueId(commonAuxiliary.getId());
                auxiliaryDtoList.add(voucherEntryAuxiliaryDto);
            }
            subjectAuxiliaryItemQueryDto.setSourceTableAndIdList(dtoList);
            subjectAuxiliaryItemQueryDto.setAccountBookIds(auxiliaryAccountingQueryDto.getAccountBookIds());
            voucherEntryAuxiliaryDto.setSourceTableAndIdList(auxiliaryDtoList);
            //查询条件加入 辅助核算项 查询条件
            auxiliaryAccountingQueryDto.setSubjectAuxiliaryItemQueryDto(subjectAuxiliaryItemQueryDto);
            auxiliaryAccountingQueryDto.setVoucherEntryAuxiliaryDto(voucherEntryAuxiliaryDto);
        }
        Long rootEnterpriseId = UserUtils.getUserVo().getRootEnterpriseId();
        auxiliaryAccountingQueryDto.setRootEnterpriseId(rootEnterpriseId);
        subjectAuxiliaryItemQueryDto.setRootEnterpriseId(rootEnterpriseId);
        List<GeneralReturnAuxiliaryVo> totalList = new ArrayList<>();
        //获得 期初数据
        List<GeneralReturnAuxiliaryVo> openingInfo = getOpeningInfos(auxiliaryAccountingQueryDto,subjectList);
        //获得 明细数据
        List<GeneralReturnAuxiliaryVo> detailInfo = getDetailInfos(auxiliaryAccountingQueryDto);
        //获得 本期数据
        List<GeneralReturnAuxiliaryVo> periodNumInfo = getPeriodNumInfos(auxiliaryAccountingQueryDto);
        //获得 本年数据
        List<GeneralReturnAuxiliaryVo> periodYearInfo = getPeriodYearInfos(auxiliaryAccountingQueryDto);
        List<GeneralReturnAuxiliaryVo> usedDetails;
        if(!FastUtils.checkNullOrEmpty(openingInfo)){
            for (GeneralReturnAuxiliaryVo opening : openingInfo) {
                totalList.add(opening);
                if(!FastUtils.checkNullOrEmpty(periodNumInfo)){
                    for(int i = 0;i < periodNumInfo.size();i++){
                        GeneralReturnAuxiliaryVo numInfo = periodNumInfo.get(i);
                        //已经添加进 结果list 的 detail
                        usedDetails = new ArrayList<>();
                        if(opening.getSubjectId().equals(numInfo.getSubjectId()) && opening.getAccountBookEntityId().equals(numInfo.getAccountBookEntityId())
                                && opening.getSubjectId().equals(numInfo.getSubjectId())){
                            if(!FastUtils.checkNullOrEmpty(detailInfo)){
                                for(GeneralReturnAuxiliaryVo detail : detailInfo){
                                    if(numInfo.getSubjectId().equals(detail.getSubjectId()) && numInfo.getAccountBookEntityId().equals(detail.getAccountBookEntityId())
                                            && numInfo.getSubjectId().equals(detail.getSubjectId())
                                            && numInfo.getPeriodYearNum().equals(detail.getPeriodYearNum())){
                                        totalList.add(detail);
                                        usedDetails.add(detail);
                                    }
                                }
                            }
                            totalList.add(numInfo);
                            totalList.add(periodYearInfo.get(i));
                            detailInfo.removeAll(usedDetails);
                        }
                    }
                }
            }
        }
        return totalList;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/6 16:27
     * @Param [totalList]
     * @return void
     * @Description 计算方向 余额
     */
    private void countBalance(List<GeneralReturnAuxiliaryVo> totalList,AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto) {
        if(!FastUtils.checkNullOrEmpty(totalList)){
            DecimalFormat format = new DecimalFormat("##0.00");
            //余额
            String balance = format.format(0);
            // 会计期间查询类型 periodOperator 0 制单日期 1 会计区间
            String debit;
            String credit;
            String periodYearDebit = format.format(0);
            String periodYearCrebit = format.format(0);
            String periodNumDebit = format.format(0);
            String periodNumCrebit = format.format(0);
            GeneralReturnAuxiliaryVo info;
            //借方
            String tempDebit =  format.format(0);
            //贷方
            String tempCredit =  format.format(0);
            int direction;
            for(int i = 0; i< totalList.size();i++){
                info = totalList.get(i);
                direction = info.getBalanceDirection();
                //余额方向  0：借、1：贷 2；平
                if(Constant.SortNum.OPENING.equals(info.getSortNum())){
                    debit = format.format(info.getDebit());
                    credit = format.format(info.getCredit());
                    tempDebit = debit;
                    tempCredit = credit;
                    if(Constant.BalanceDirection.DEBIT == direction){
                        balance = format.format(new BigDecimal(info.getBalance()).add(new BigDecimal(debit)).subtract(new BigDecimal(credit)));
                    }
                    if(Constant.BalanceDirection.CREDIT == direction){
                        balance = format.format(new BigDecimal(info.getBalance()).add(new BigDecimal(credit)).subtract(new BigDecimal(debit)));
                    }
                    info.setDebit(0);
                    info.setCredit(0);
                }else if(Constant.SortNum.DETAIL.equals(info.getSortNum())){
                    debit = format.format(info.getDebit());
                    credit = format.format(info.getCredit());
                    periodNumDebit = format.format(new BigDecimal(periodNumDebit).add(new BigDecimal(debit)));
                    periodNumCrebit = format.format(new BigDecimal(periodNumCrebit).add(new BigDecimal(credit)));
                    periodYearDebit = format.format(new BigDecimal(periodYearDebit).add(new BigDecimal(debit)));
                    periodYearCrebit = format.format(new BigDecimal(periodYearCrebit).add(new BigDecimal(credit)));
                    if(Constant.BalanceDirection.DEBIT == direction){
                        balance = format.format(new BigDecimal(balance).add(new BigDecimal(debit)).subtract(new BigDecimal(credit)));
                    }
                    if(Constant.BalanceDirection.CREDIT == direction){
                        balance = format.format(new BigDecimal(balance).add(new BigDecimal(credit)).subtract(new BigDecimal(debit)));
                    }
                }else if(Constant.SortNum.PERIOD_NUM.equals(info.getSortNum())){
                    info.setDebit(new BigDecimal(periodNumDebit).doubleValue());
                    info.setCredit(new BigDecimal(periodNumCrebit).doubleValue());
                    periodNumDebit = format.format(0);
                    periodNumCrebit = format.format(0);
                }else if(Constant.SortNum.PERIOD_YEAR.equals(info.getSortNum())){
                    periodYearDebit = format.format(new BigDecimal(periodYearDebit).add(new BigDecimal(tempDebit)));
                    periodYearCrebit = format.format(new BigDecimal(periodYearCrebit).add(new BigDecimal(tempCredit)));
                    tempDebit = format.format(0);
                    tempCredit = format.format(0);
                    info.setDebit(new BigDecimal(periodYearDebit).doubleValue());
                    info.setCredit(new BigDecimal(periodYearCrebit).doubleValue());
                    if(i+1 < totalList.size()){
                        GeneralReturnAuxiliaryVo finalTotal = totalList.get(i+1);
                        boolean flag = info.getPeriodYear() != finalTotal.getPeriodYear() || !info.getAccountBookId().equals(finalTotal.getAccountBookId())
                                || !info.getAccountBookEntityId().equals(finalTotal.getAccountBookEntityId())
                                || !info.getSubjectId().equals(finalTotal.getSubjectId());
                        if(flag){
                            periodYearDebit = format.format(0);
                            periodYearCrebit = format.format(0);
                            tempDebit = format.format(0);
                            tempCredit = format.format(0);
                        }
                    }
                }else {
                    continue;
                }
                info.setBalance(new BigDecimal(balance).doubleValue());
                setBalanceDirection(new BigDecimal(balance).doubleValue(), info);
            }
        }
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/8 17:58
     * @Param [auxiliaryProject]
     * @return java.util.List<com.njwd.entity.ledger.CommonAuxiliary>
     * @Description 辅助核算项目  具体内容
     */
    private List<CommonAuxiliary> getCommonAuxiliaries(List<Map<String, Object>> auxiliaryProject,String sourceTable) {
        List<CommonAuxiliary> commonAuxiliaryList = new ArrayList<>();
        CommonAuxiliary commonAuxiliary;
        for (Map<String, Object> item : auxiliaryProject) {
            commonAuxiliary = new CommonAuxiliary();
            // map 的 key 就是 核算項目的ID
            commonAuxiliary.setId(Long.valueOf(item.get("id").toString()));
            //取得核算項目的名称和编码
            if (null != item.get("code")) {
                commonAuxiliary.setCode(String.valueOf(item.get("code")));
            }
            if (null != item.get("name")) {
                commonAuxiliary.setName(String.valueOf(item.get("name")));
            }
            if (null != item.get("fullName")) {
                commonAuxiliary.setFullName(String.valueOf(item.get("fullName")));
            }
            if (null != item.get("auxiliaryName")) {
                commonAuxiliary.setAuxiliaryName(String.valueOf(item.get("auxiliaryName")));
            }
            if (null != item.get("sourceTable")) {
                commonAuxiliary.setSourceTable(String.valueOf(item.get("sourceTable")));
            }
            commonAuxiliaryList.add(commonAuxiliary);
        }
        return commonAuxiliaryList;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/29 14:50
     * @Param [startDate, info]
     * @return com.njwd.entity.ledger.AccountBookPeriod
     * @Description 账簿会计区间 查询条件设置
     */
    private AccountBookPeriodVo getAccountBookPeriodByDate(Date startDate, GeneralReturnAuxiliaryVo info) {
        AccountBookPeriodDto param = new AccountBookPeriodDto();
        param.setAccountBookId(info.getAccountBookId());
        param.setSystemSign(Constant.SystemSignValue.LEDGER);
        param.setStatus(Constant.Status.ON);
        param.setVoucherDate(startDate);
        return accountBookPeriodService.findPeriodByAccBookIdAndSystemSign(param);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/12 15:32
     * @Param [auxiliaryAccountingQueryDto, startDate, info]
     * @return com.njwd.entity.ledger.dto.AuxiliaryAccountingQueryDto
     * @Description 查询条件
     */
    private AuxiliaryAccountingQueryDto getAuxiliaryAccountingQueryDto(AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto, GeneralReturnAuxiliaryVo info,Date startDate) {
        AuxiliaryAccountingQueryDto dto = new AuxiliaryAccountingQueryDto();
        dto.setIsIncludeUnbooked(auxiliaryAccountingQueryDto.getIsIncludeUnbooked());
        dto.setIsIncludeProfitAndLoss(auxiliaryAccountingQueryDto.getIsIncludeProfitAndLoss());
        List<Date> voucherDates = new ArrayList<>();
        AccountBookPeriodDto param = new AccountBookPeriodDto();
        param.setAccountBookId(info.getAccountBookId());
        param.setPeriodYear(info.getPeriodYear());
        param.setPeriodNum(info.getPeriodNum());
        param.setStatus(Constant.Status.ON);
        param.setSystemSign(Constant.SystemSignValue.LEDGER);
        AccountBookPeriodVo period = accountBookPeriodService.findPeriodByAccBookIdAndSystemSign(param);
        voucherDates.add(period.getStartDate());
        voucherDates.add(startDate);
        dto.setVoucherDates(voucherDates);
        dto.setAccountBookId(info.getAccountBookId());
        dto.setAccountBookEntityId(info.getAccountBookEntityId());
        dto.setAccountSubjectId(Long.valueOf(info.getSubjectId()));
        return dto;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/6 16:27
     * @Param [balance, list]
     * @return void
     * @Description 设置余额方向
     */
    private void setBalanceDirection(Double balance, GeneralReturnAuxiliaryVo list) {
        if(Constant.Number.ZEROD.equals(balance)){
            list.setBalanceDirection(Constant.BalanceDirectionType.FLAT);
        }
    }


    /**
     * @Author ZhuHC
     * @Date  2019/8/2 17:23
     * @Param [returnList]
     * @return void
     * @Description 根据条件 删除数据
     */
    private void deleteInfoByStatus(List<GeneralReturnAuxiliaryVo> returnList,AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto) {
        if(!FastUtils.checkNullOrEmpty(returnList)){
            if(null != auxiliaryAccountingQueryDto.getShowCondition()){
                //0：本期无发生 1;余额为零  2； 本期无发生，且余额为零
                List<GeneralReturnAuxiliaryVo> firstList = new ArrayList<>();
                List<GeneralReturnAuxiliaryVo> secondList = new ArrayList<>();
                List<GeneralReturnAuxiliaryVo> thirdList = new ArrayList<>();
                for(GeneralReturnAuxiliaryVo generalReturnAuxiliaryVo : returnList){
                    if(Constant.SortNum.DETAIL.equals(generalReturnAuxiliaryVo.getSortNum())){
                        if(Constant.Number.ZEROD.equals(generalReturnAuxiliaryVo.getCredit()) && Constant.Number.ZEROD.equals(generalReturnAuxiliaryVo.getDebit())){
                            firstList.add(generalReturnAuxiliaryVo);
                        }
                        if(Constant.Number.ZEROD.equals(generalReturnAuxiliaryVo.getBalance())){
                            secondList.add(generalReturnAuxiliaryVo);
                        }
                        if(Constant.Number.ZEROD.equals(generalReturnAuxiliaryVo.getCredit())
                                && Constant.Number.ZEROD.equals(generalReturnAuxiliaryVo.getDebit())
                                && Constant.Number.ZEROD.equals(generalReturnAuxiliaryVo.getBalance())){
                            thirdList.add(generalReturnAuxiliaryVo);
                        }
                    }
                }
                //显示条件(0:本期无发生不显示 1:余额为零显示 2:余额为零且本期无发生不显示)
                Byte condition = auxiliaryAccountingQueryDto.getShowCondition();
                if(LedgerConstant.ReportShowCondition.HAPPEN_NO.equals(condition)){
                    returnList.removeAll(firstList);
                }else if (LedgerConstant.ReportShowCondition.BALANCE_NO.equals(condition)){
                    returnList.removeAll(secondList);
                }else if (LedgerConstant.ReportShowCondition.HAPPEN_BALANCE_NO.equals(condition)){
                    returnList.removeAll(thirdList);
                }
            }
        }
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/29 17:58
     * @Param [balanceDirection]
     * @return java.lang.String
     * @Description
     */
    private String convertDirection(int balanceDirection){
        //余额方向类型 0：借 、1：贷、2：平
        String wordStr = "";
        switch (balanceDirection){
            case Constant.BalanceDirectionType.DEBIT:
                wordStr = Constant.BalanceDirectionName.CREDIT ;
                break;
            case Constant.BalanceDirectionType.CREDIT:
                wordStr = Constant.BalanceDirectionName.DEBIT ;
                break;
            case Constant.BalanceDirectionType.FLAT:
                wordStr = Constant.BalanceDirectionName.FLAT ;
                break;
            default:
        }
        return wordStr;
    }
}
