package com.njwd.ledger.service.impl;

import com.alibaba.excel.util.CollectionUtils;
import com.njwd.common.Constant;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.basedata.dto.AccountBookEntityDto;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.ledger.CommonAuxiliary;
import com.njwd.entity.ledger.dto.AuxiliaryAccountingQueryDto;
import com.njwd.entity.ledger.vo.GeneralReturnAuxiliaryVo;
import com.njwd.entity.platform.AccountSubject;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.ledger.cloudclient.AccountSubjectFeignClient;
import com.njwd.ledger.mapper.AuxiliaryItemAccountMapper;
import com.njwd.ledger.service.AuxiliaryAccountingBackService;
import com.njwd.service.FileService;
import com.njwd.utils.FastUtils;
import com.njwd.utils.StringUtil;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

@Service
public class AuxiliaryAccountingBackServiceImpl implements AuxiliaryAccountingBackService {
    @Resource
    private AuxiliaryItemAccountMapper auxiliaryItemAccountMapper;
    @Resource
    private AccountSubjectFeignClient accountSubjectFeignClient;
    @Resource
    private FileService fileService;

    /**
     * 查询所有的辅助核算明细账数据
     * @param auxiliaryAccountingQueryDto
     * @return
     */
    @Override
    public List<GeneralReturnAuxiliaryVo> findAuxiliaryDetailList(AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto) {
        //从基础资料获取 会计科目数据
        List<AccountSubjectVo> accountSubjectList = getAccountSubjectVos(auxiliaryAccountingQueryDto);
        if (CollectionUtils.isEmpty(accountSubjectList)) {
            return new ArrayList<>();
        }
        //获取会计科目id
        List<Long> subjectIdList = new LinkedList<>();
        if(!FastUtils.checkNullOrEmpty(accountSubjectList)){
            for(AccountSubjectVo accountSubject : accountSubjectList){
                subjectIdList.add(accountSubject.getId());
            }
        }
        //设置科目id
        auxiliaryAccountingQueryDto.setSubjectIdList(subjectIdList);
        //设置租户id
        auxiliaryAccountingQueryDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        //获取辅助核算项
        List<CommonAuxiliary> auxiliaryAccounting = getAuxiliaryAccounting(auxiliaryAccountingQueryDto);
        //查询符合条件的制单日期内的数据 最大最小区间
        List<Date> voucherDates = auxiliaryAccountingQueryDto.getVoucherDates();
        if(null != voucherDates && voucherDates.size() > 0){
            if (!setParamByVoucherDate(auxiliaryAccountingQueryDto)) {
                return new ArrayList<>();
            }
        }
        List<GeneralReturnAuxiliaryVo> dataList = null;
        if(null != auxiliaryAccounting && auxiliaryAccounting.size() > 0){
            //根据条件,有辅助核算项的,查 辅助核算余额表 关联到辅助核算项表
            dataList = auxiliaryItemAccountMapper.getGeneralReturnAuxiliaryList(auxiliaryAccountingQueryDto,"wd_balance_subject_auxiliary");
        }else {
            //根据条件获取数据 默认查科目余额表
            dataList = auxiliaryItemAccountMapper.getGeneralReturnAuxiliaryList(auxiliaryAccountingQueryDto,null);
        }
        //如果没数据  直接返回
        if (CollectionUtils.isEmpty(dataList)) {
            return new ArrayList<>();
        }
        //生成以code为key的计算后数据
        HashMap<String, List<GeneralReturnAuxiliaryVo>> formatListHashMap = formatCodeMap(dataList, accountSubjectList, auxiliaryAccountingQueryDto);
        //输出账簿格式
        List<GeneralReturnAuxiliaryVo> formatData = formatReportForms(formatListHashMap, auxiliaryAccountingQueryDto, accountSubjectList, Constant.SortNum.DETAIL);
        //数据过滤
        filterDetailData(formatData, auxiliaryAccountingQueryDto);
        //日期过滤
        filterDataByDate(formatData, auxiliaryAccountingQueryDto);
        //有辅助核算项的 把辅助核算项添加进来
        StringBuilder sb;
        if(null != auxiliaryAccounting && auxiliaryAccounting.size() > 0){
           for(GeneralReturnAuxiliaryVo g:formatData){
             g.setCommonAuxiliaryList(auxiliaryAccounting);
             //拼接树形标识
               List<CommonAuxiliary> commonAuxiliaryList = g.getCommonAuxiliaryList();
               if(null != commonAuxiliaryList && commonAuxiliaryList.size() > 0){
                   sb = new StringBuilder();
                   for(CommonAuxiliary auxiliary : commonAuxiliaryList){
                       sb.append(auxiliary.getSourceTable()).append(auxiliary.getId()).append("_");
                   }
                   g.setKeySign(sb);
               }
           }
        }
        return formatData;
    }

    /**
     * 导出数据
     * @param auxiliaryAccountingQueryDto
     * @param response
     */
    @Override
    public void exportExcel(AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto, HttpServletResponse response) {
        //获取辅助核算明细数据
        List<GeneralReturnAuxiliaryVo> auxiliaryDetailList = findAuxiliaryDetailList(auxiliaryAccountingQueryDto);
        if(null != auxiliaryDetailList && auxiliaryDetailList.size() > 0){
            //遍历数据
            for(GeneralReturnAuxiliaryVo vo : auxiliaryDetailList){
                //设置借贷方向
                vo.setDirection(convertDirection(vo.getBalanceDirection()));
            }
            //导出是否显示科目全名(0:不显示 1:显示)
            if (Constant.Is.YES.equals(auxiliaryAccountingQueryDto.getIsShowFullName())){
                fileService.exportExcel(response, auxiliaryDetailList,"AuxiliaryAccountingQueryResult",
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
                //导出科目不是全名
                fileService.exportExcel(response, auxiliaryDetailList,"AuxiliaryAccountingQueryResult",
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
     * 辅助核算项
     * @param auxiliaryAccountingQueryDto
     * @return
     */
    private List<CommonAuxiliary> getAuxiliaryAccounting(AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto){
        //获取参数
        Map<String, List<Long>> sourceTableAndValueList = auxiliaryAccountingQueryDto.getSourceTableAndValueList();
        // 辅助核算项
        List<CommonAuxiliary> commonAuxiliaryList = new LinkedList<>();
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
            //把表名称集合设置到参数里 供查询sql使用
            auxiliaryAccountingQueryDto.setSourceTableList(sourceTableList);
            //根据条件查询所有辅助核算来源表信息
            List<List<Map<String, Object>>> allSourceTableInfo = accountSubjectFeignClient.findAllSourceTableInfo(param).getData();
            //循环来源表信息 把数据添加到 辅助核算项里面
            if(null != allSourceTableInfo && !allSourceTableInfo.isEmpty()){
                for(int i = 0; i < allSourceTableInfo.size(); i++){
                    if(!FastUtils.checkNullOrEmpty(allSourceTableInfo.get(i)) && null != sourceTableList.get(i)){
                        commonAuxiliaryList.addAll(getCommonAuxiliaries(allSourceTableInfo.get(i),sourceTableList.get(i)));
                    }
                }
            }
            //设置辅助核算项参数  key与value 1对1
            auxiliaryAccountingQueryDto.setSourceTableAndValue(commonAuxiliaryList);
        }
        return commonAuxiliaryList;
    }

    /**
     * 组装code为key的HasMap数据
     * @param dataList
     * @param accountSubjectList
     * @param param
     * @return
     */
    private HashMap<String, List<GeneralReturnAuxiliaryVo>> formatCodeMap(List<GeneralReturnAuxiliaryVo> dataList, List<AccountSubjectVo> accountSubjectList, AuxiliaryAccountingQueryDto param) {
        HashMap<String, List<GeneralReturnAuxiliaryVo>> returnMap = new HashMap<>();
        //上一条数据的科目编码 账簿信息
        AccountSubjectVo accountSubject = null;
        AccountBookEntityDto accountBookEntity = null;
        //循环数据
        for (GeneralReturnAuxiliaryVo gl : dataList) {
            //判断是否是新的账簿 赋值新的账簿信息
            if (accountBookEntity == null || !accountBookEntity.getEntityId().equals(gl.getAccountBookEntityId())) {
                //获取核算账簿主体
                accountBookEntity = getAccountBookEntity(gl.getAccountBookEntityId(), param.getAccountBookEntityList());
                if (accountBookEntity == null) {
                    continue;
                }
            }
            //判断是否是新的科目 赋值新的科目信息
            if (accountSubject == null || !accountSubject.getId().equals(Long.valueOf(gl.getSubjectId()))) {
                //获取对应科目信息
                accountSubject = getAccountSubject(accountSubjectList, Long.valueOf(gl.getSubjectId()), null, null, param.getIsShowFullName());
                if (accountSubject == null) {
                    continue;
                }
                returnMap.put(accountSubject.getCode(), new ArrayList<>());
            }
            //为对象赋值账簿信息
            gl.setAccountBookName(accountBookEntity.getAccountBookName());
            gl.setAccountBookEntityName(accountBookEntity.getEntityName());
            //为对象赋值科目信息
            gl.setSubjectCode(accountSubject.getCode());
            gl.setSubjectName(accountSubject.getName());
            gl.setUpCode(accountSubject.getUpCode());
            gl.setLevel(accountSubject.getLevel());
            gl.setBalanceDirection(accountSubject.getBalanceDirection());
            returnMap.get(accountSubject.getCode()).add(gl);
            //向父节点 添加该数据
            setDataToParentCode(returnMap, gl, gl.getUpCode(), accountSubjectList, param);
        }
        return returnMap;
    }

    /**
     * 设置会计科目 查询条件
     * @param auxiliaryAccountingQueryDto
     * @return
     */
    private List<AccountSubjectVo> getAccountSubjectVos(AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto) {
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        //设置租户id
        accountSubjectDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        //会计科目查询类型 0等于 1期间 2包含
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
     * 辅助核算项目  具体内容
     * @param auxiliaryProject
     * @param sourceTable
     * @return
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
     * 获取对应账簿信息
     * @param accountBookEntityId
     * @param accountBookEntityList
     * @return
     */
    private AccountBookEntityDto getAccountBookEntity(Long accountBookEntityId, List<AccountBookEntityDto> accountBookEntityList) {
        for (AccountBookEntityDto accountBookEntity : accountBookEntityList) {
            if (accountBookEntityId.equals(accountBookEntity.getEntityId())) {
                return accountBookEntity;
            }
        }
        return null;
    }
    /**
     * 获取对应科目信息
     * @param accountSubjectList
     * @param id
     * @param code
     * @param codeMap
     * @param fullNameFlag
     * @return
     */
    private AccountSubjectVo getAccountSubject(List<AccountSubjectVo> accountSubjectList, Long id, String code, HashMap<String, List<GeneralReturnAuxiliaryVo>> codeMap, byte fullNameFlag) {
        //如果不是空的 先从codeMap里面获取，如果没有再去循环获取
        if (code != null) {
            List<GeneralReturnAuxiliaryVo> codeList = codeMap.get(code);
            if (!CollectionUtils.isEmpty(codeList)) {
                GeneralReturnAuxiliaryVo nowDate = codeList.get(0);
                AccountSubjectVo accountSubject = new AccountSubjectDto<>();
                accountSubject.setId(Long.valueOf(nowDate.getSubjectId()));
                accountSubject.setCode(nowDate.getSubjectCode());
                accountSubject.setSubjectName(nowDate.getSubjectName());
                accountSubject.setUpCode(nowDate.getUpCode());
                accountSubject.setLevel(nowDate.getLevel());
                accountSubject.setBalanceDirection((byte) nowDate.getBalanceDirection());
                return accountSubject;
            }
        }
        //如果有ID则根据ID查询 没有则根据CODE查询
        boolean idEquals;
        boolean codeEquals;
        for (AccountSubjectVo accountSubject : accountSubjectList) {
            idEquals = id != null && id.equals(accountSubject.getId());
            codeEquals = code != null && code.equals(accountSubject.getCode());
            if (idEquals || codeEquals) {
                //科目全名
                if (fullNameFlag == Constant.Number.ONEB) {
                    accountSubject.setName(accountSubject.getFullName());
                }
                return accountSubject;
            }
        }
        return null;
    }
    /**
     * 向父节点添加数据
     * @param codeMap
     * @param childData
     * @param upCode
     * @param accountSubjectList
     * @param param
     */
    private void setDataToParentCode(HashMap<String, List<GeneralReturnAuxiliaryVo>> codeMap, GeneralReturnAuxiliaryVo childData, String upCode, List<AccountSubjectVo> accountSubjectList, AuxiliaryAccountingQueryDto param) {
        GeneralReturnAuxiliaryVo nowData;
        //NOT NULL
        if (childData == null || StringUtil.isBlank(upCode)) {
            return;
        }
        //查询父级科目信息
        AccountSubject accountSubject = getAccountSubject(accountSubjectList, null, upCode, codeMap, param.getIsShowFullName());
        if (accountSubject == null) {
            return;
        }
        //如果为空则新生成 否则重新计算原有数据
        List<GeneralReturnAuxiliaryVo> parentList = codeMap.get(upCode);
        if (CollectionUtils.isEmpty(parentList)) {
            //新的map key
            parentList = new ArrayList<>();
            nowData = cloneGeneralLedgerVo(childData, accountSubject, null, null);
            nowData.setDebit(childData.getSelfDebit());
            nowData.setCredit(childData.getSelfCredit());
            parentList.add(nowData);
            codeMap.put(upCode, parentList);
        } else {
            //添加计算父类数据
            nowData = addParentCodeData(codeMap.get(upCode), childData);
        }
        //继续
        setDataToParentCode(codeMap, childData, nowData.getUpCode(), accountSubjectList, param);
    }
    /**
     * 数据克隆，防止污染原有数据
     * @param sourceData
     * @param accountSubject
     * @param parentData
     * @param type
     * @return
     */
    private GeneralReturnAuxiliaryVo cloneGeneralLedgerVo(GeneralReturnAuxiliaryVo sourceData, AccountSubject accountSubject, GeneralReturnAuxiliaryVo parentData, Integer type) {
        GeneralReturnAuxiliaryVo targetData = new GeneralReturnAuxiliaryVo();
        //判断科目信息继承方式
        if (accountSubject != null) {
            targetData.setSubjectId(String.valueOf(accountSubject.getId()));
            targetData.setSubjectCode(accountSubject.getCode());
            targetData.setSubjectName(accountSubject.getName());
            targetData.setUpCode(accountSubject.getUpCode());
            targetData.setLevel(accountSubject.getLevel());
            targetData.setBalanceDirection(accountSubject.getBalanceDirection());
        } else if (parentData != null) {
            targetData.setSubjectId(parentData.getSubjectId());
            targetData.setSubjectCode(parentData.getSubjectCode());
            targetData.setSubjectName(parentData.getSubjectName());
            targetData.setUpCode(parentData.getUpCode());
            targetData.setLevel(parentData.getLevel());
            targetData.setBalanceDirection(parentData.getBalanceDirection());
        } else {
            targetData.setSubjectId(sourceData.getSubjectId());
            targetData.setSubjectCode(sourceData.getSubjectCode());
            targetData.setSubjectName(sourceData.getSubjectName());
            targetData.setUpCode(sourceData.getUpCode());
            targetData.setLevel(sourceData.getLevel());
            targetData.setBalanceDirection(sourceData.getBalanceDirection());
        }
        //克隆原有信息
        targetData.setAccountBookId(sourceData.getAccountBookId());
        targetData.setAccountBookName(sourceData.getAccountBookName());
        targetData.setAccountBookEntityId(sourceData.getAccountBookEntityId());
        targetData.setAccountBookEntityName(sourceData.getAccountBookEntityName());
        targetData.setPeriodYear(sourceData.getPeriodYear());
        targetData.setPeriodNum(sourceData.getPeriodNum());
        targetData.setPeriodYearNum(sourceData.getPeriodYearNum());
        targetData.setCredentialWordCode(sourceData.getCredentialWordCode());
        //明细用
        if (sourceData.getVoucherId() != null) {
            targetData.setVoucherId(sourceData.getVoucherId());
            targetData.setVoucherDate(sourceData.getVoucherDate());
            targetData.setCredentialWord(sourceData.getCredentialWord());
            targetData.setAbstractContent(sourceData.getAbstractContent());
            targetData.setMainCode(sourceData.getMainCode());
        }
        //根据类型赋值
        if (type != null) {
            targetData.setDebit(0);
            targetData.setCredit(0);
            targetData.setCredentialWord(null);
            targetData.setMainCode(null);
            //期初数据　期末 年末
            if (type.equals(Constant.SortNum.OPENING)) {
                targetData.setSortNum(Constant.SortNum.OPENING);
                targetData.setAbstractContent(Constant.ReportFormRowType.OPENING);
                targetData.setBalance(0);
                targetData.setBalanceDirection(sourceData.getOpeningBalance() == 0 ? Constant.BalanceDirection.FLAT : sourceData.getBalanceDirection());
                if (targetData.getVoucherDate() != null) {
                    targetData.setVoucherDate(sourceData.getVoucherDate());
                }
                targetData.setCredentialWordCode(null);
            } else if (type.equals(Constant.SortNum.PERIOD_NUM)) {
                targetData.setSortNum(Constant.SortNum.PERIOD_NUM);
                targetData.setAbstractContent(Constant.ReportFormRowType.PERIOD_NUM);
                targetData.setOpeningBalance(targetData.getOpeningBalance());
                targetData.setVoucherDate(null);
                targetData.setCredentialWordCode(null);
            } else if (type.equals(Constant.SortNum.PERIOD_YEAR)) {
                targetData.setSortNum(Constant.SortNum.PERIOD_YEAR);
                targetData.setAbstractContent(Constant.ReportFormRowType.PERIOD_YEAR);
                targetData.setVoucherDate(null);
                targetData.setCredentialWordCode(null);
            }
        } else {
            targetData.setOpeningBalance(sourceData.getOpeningBalance());
            targetData.setSortNum(sourceData.getSortNum());
            targetData.setAbstractContent(sourceData.getAbstractContent());
            targetData.setDebit(sourceData.getDebit());
            targetData.setCredit(sourceData.getCredit());
            targetData.setBalance(sourceData.getBalance());
        }
        return targetData;
    }

    /**
     * 判断账簿年月为父级数据赋值
     * @param parentList
     * @param childGl
     * @return
     */
    private GeneralReturnAuxiliaryVo addParentCodeData(List<GeneralReturnAuxiliaryVo> parentList, GeneralReturnAuxiliaryVo childGl) {
        //添加的数据，返回使用
        GeneralReturnAuxiliaryVo returnData;

        //判断添加位置
        for (int i = 0; i < parentList.size(); i++) {
            GeneralReturnAuxiliaryVo parentGl = parentList.get(i);
            //判断 总账账簿
            if (parentGl.getAccountBookId() > childGl.getAccountBookId()) {
                returnData = childDataToParentData(childGl, parentGl, true);
                parentList.add(i, returnData);
                return returnData;
            } else if (parentGl.getAccountBookId() < childGl.getAccountBookId()) {
                continue;
            }
            //判断 账簿主体
            if (parentGl.getAccountBookEntityId() > childGl.getAccountBookEntityId()) {
                returnData = childDataToParentData(childGl, parentGl, true);
                parentList.add(i, returnData);
                return returnData;
            } else if (parentGl.getAccountBookEntityId() < childGl.getAccountBookEntityId()) {
                continue;
            }
            //判断年份
            if (parentGl.getPeriodYear() > childGl.getPeriodYear()) {
                returnData = childDataToParentData(childGl, parentGl, true);
                parentList.add(i, returnData);
                return returnData;
            } else if (parentGl.getPeriodYear() < childGl.getPeriodYear()) {
                continue;
            }
            //判断月份
            if (parentGl.getPeriodNum() > childGl.getPeriodNum()) {
                returnData = childDataToParentData(childGl, parentGl, true);
                parentList.add(i, returnData);
                return returnData;
            } else if (parentGl.getPeriodNum() < childGl.getPeriodNum()) {
                continue;
            }
            //判断凭证
            if (childGl.getVoucherId() == null && parentGl.getVoucherId() != null) {
                returnData = childDataToParentData(childGl, parentGl, true);
                parentList.add(i, returnData);
                return returnData;
            } else if (childGl.getVoucherId() != null && parentGl.getVoucherId() == null) {
                continue;
            } else if (childGl.getVoucherId() != null && parentGl.getVoucherId() != null) {
                //判断凭证时间
                if (parentGl.getVoucherId() > childGl.getVoucherId()) {
                    returnData = childDataToParentData(childGl, parentGl, true);
                    parentList.add(i, returnData);
                    return returnData;
                } else if (parentGl.getVoucherId() < childGl.getVoucherId()) {
                    continue;
                }
                //判断凭证时间
               /* if (parentGl.getVoucherDateTime() > childGl.getVoucherDateTime()) {
                    returnData = childDataToParentData(childGl, parentGl, true);
                    parentList.add(i, returnData);
                    return returnData;
                } else if (parentGl.getVoucherDateTime() < childGl.getVoucherDateTime()) {
                    continue;
                }
                //判断凭证id
                if (parentGl.getVoucherId() > childGl.getVoucherId()) {
                    returnData = childDataToParentData(childGl, parentGl, true);
                    parentList.add(i, returnData);
                    return returnData;
                } else {
                    continue;
                }*/
            }
            //如果都相同则累加
            returnData = childDataToParentData(childGl, parentGl, false);
            parentList.set(i, returnData);
            return returnData;
        }
        //否则添加到最后
        returnData = childDataToParentData(childGl, parentList.get(0), true);
        parentList.add(returnData);
        return returnData;
    }
    /**
     * 子类复制给父类
     * @param childData
     * @param parentData
     * @param isNew
     * @return
     */
    private GeneralReturnAuxiliaryVo childDataToParentData(GeneralReturnAuxiliaryVo childData, GeneralReturnAuxiliaryVo parentData, Boolean isNew) {
        GeneralReturnAuxiliaryVo returnData;
        //判断是克隆 还是 累加
        if (isNew) {
            returnData = cloneGeneralLedgerVo(childData, null, parentData, null);
            //期初还是子集的
            returnData.setOpeningBalance(0);
            returnData.setDebit(0);
            returnData.setCredit(0);
        } else {
            returnData = parentData;
        }
        //如果借贷方向不同
            BigDecimal tempReturnDataOpeningBalance = BigDecimal.valueOf(returnData.getOpeningBalance());
            BigDecimal tempChildDataOpeningBalance = BigDecimal.valueOf(childData.getOpeningBalance());
            BigDecimal tempReturnDataDebit = BigDecimal.valueOf(returnData.getDebit());
            BigDecimal tempChildDataSelfCredit = BigDecimal.valueOf(childData.getSelfCredit());
            BigDecimal tempReturnDataCredit = BigDecimal.valueOf(returnData.getCredit());
            BigDecimal tempChildDataSelfDebit = BigDecimal.valueOf(childData.getSelfDebit());
        if (parentData.getBalanceDirection() != childData.getBalanceDirection()) {
            //设置期初余额
            returnData.setOpeningBalance(tempReturnDataOpeningBalance.subtract(tempChildDataOpeningBalance).doubleValue());
            //设置借方
            returnData.setDebit(tempReturnDataDebit.add(tempChildDataSelfDebit).doubleValue());
            //设置贷方
            returnData.setCredit(tempReturnDataCredit.add(tempChildDataSelfCredit).doubleValue());
        } else {
            //设置期初余额
            returnData.setOpeningBalance(tempReturnDataOpeningBalance.add(tempChildDataOpeningBalance).doubleValue());
            //设置借方
            returnData.setDebit(tempReturnDataDebit.add(tempChildDataSelfDebit).doubleValue());
            //设置贷方
            returnData.setCredit(tempReturnDataCredit.add(tempChildDataSelfCredit).doubleValue());
        }
        return returnData;
    }
    /**
     * 格式化输出账簿
     * @param hashMap
     * @param param
     * @param accountSubjectList
     * @param type
     * @return
     */
    private List<GeneralReturnAuxiliaryVo> formatReportForms(HashMap<String, List<GeneralReturnAuxiliaryVo>> hashMap, AuxiliaryAccountingQueryDto param, List<AccountSubjectVo> accountSubjectList, Integer type) {
        List<GeneralReturnAuxiliaryVo> returnData = new LinkedList<>();
        //是否需要 期初 期末
        boolean needOpen;
        boolean needClose;
        boolean nnedCode;
        //本期合计 本年累计
        double[] middleList = new double[]{0, 0, 0};
        double[] finishList = new double[]{0, 0};

        //根据accountSubject排序 计算 生成期初期中期末
        GeneralReturnAuxiliaryVo openGv;
        GeneralReturnAuxiliaryVo nowData;
        for (AccountSubjectVo accountSubject : accountSubjectList) {
            //查询到末尾code则不再继续
            Byte operator = param.getAccountSubjectOperator();
            if(operator.equals(LedgerConstant.QueryScheme.OPERATOR_EQUAL)){
                List<Long> codes = param.getSubjectCodes();
                if (codes.size() > 1 && StringUtil.isNotBlank(String.valueOf(codes.get(1))) && String.valueOf(codes.get(1)).equals(accountSubject.getCode())) {
                    break;
                }
            }else {
                if (StringUtil.isNotBlank(param.getAccountCode2()) && param.getAccountCode2().equals(accountSubject.getCode())) {
                    break;
                }
            }

            //如果Map中不存在对应code则跳过
            if (hashMap.get(accountSubject.getCode()) == null) {
                continue;
            }

            List<GeneralReturnAuxiliaryVo> nowList = hashMap.get(accountSubject.getCode());
            for (int i = 0; i < nowList.size(); i++) {
                nowData = nowList.get(i);
                //加上自身值 设置借方
                BigDecimal nowDataSelfDebit = BigDecimal.valueOf(nowData.getSelfDebit());
                BigDecimal nowDataDebit = BigDecimal.valueOf(nowData.getDebit());
                nowData.setDebit(nowDataSelfDebit.add(nowDataDebit).doubleValue());
                //设置贷方
                BigDecimal nowDataSelfCredit = BigDecimal.valueOf(nowData.getSelfCredit());
                BigDecimal nowDataCredit = BigDecimal.valueOf(nowData.getCredit());
                nowData.setCredit(nowDataSelfCredit.add(nowDataCredit).doubleValue());
                nowData.setSelfDebit(Constant.Number.ZERO);
                nowData.setSelfCredit(Constant.Number.ZERO);
                //判断是否需要期初余额
                needOpen = needOpen(nowData, nowList, i);
                //判断是否需要期中期末
                needClose = needClose(nowData, nowList, i, type);

                //计算
                if (needOpen) {
                    middleList = new double[]{0, 0, sumOpeningBalance(nowData, i, nowList)};
                    finishList = new double[]{0, 0, 0};
                }
                calc(nowData);

                //本期合计 本年累计
                BigDecimal middleOne = BigDecimal.valueOf(middleList[0]);
                BigDecimal nowDataBigDebit = BigDecimal.valueOf(nowData.getDebit());
                middleList[0] = middleOne .add(nowDataBigDebit).doubleValue();
                BigDecimal middleTwo = BigDecimal.valueOf(middleList[1]);
                BigDecimal nowDataBigCebit = BigDecimal.valueOf(nowData.getCredit());
                middleList[1] = middleTwo.add(nowDataBigCebit).doubleValue();
                BigDecimal finishOne = BigDecimal.valueOf(finishList[0]);
                finishList[0] = finishOne.add(nowDataBigDebit).doubleValue();
                BigDecimal finishTwo = BigDecimal.valueOf(finishList[1]);
                finishList[1] = finishTwo.add(nowDataBigCebit).doubleValue();

                //期初余额
                if (needOpen) {
                    openGv = cloneGeneralLedgerVo(nowData, null, null, Constant.SortNum.OPENING);
                    openGv.setBalance(middleList[2]);
                    returnData.add(openGv);
                }
                //明细
                if (type.equals(Constant.SortNum.DETAIL)) {
                    nowData.setOpeningBalance(middleList[2]);
                    nowData.setSortNum(Constant.SortNum.DETAIL);
                    calc(nowData);
                    if (nowData.getVoucherId() != null) {
                        returnData.add(nowData);
                    }
                    middleList[2] = nowData.getBalance();
                    nowData.setBalanceDirection(nowData.getBalance() == 0 ? Constant.BalanceDirection.FLAT : nowData.getBalanceDirection());
                }
                if (needClose) {
                    if (type.equals(Constant.SortNum.PERIOD_NUM)) {
                        nowData.setSubjectCode(null);
                        nowData.setSubjectName(null);
                    }
                    //期末
                    GeneralReturnAuxiliaryVo gl;
                    gl = cloneGeneralLedgerVo(nowData, null, null, Constant.SortNum.PERIOD_NUM);
                    gl.setDebit(middleList[0]);
                    gl.setCredit(middleList[1]);
                    if (type.equals(Constant.SortNum.PERIOD_NUM)) {
                        gl.setOpeningBalance(middleList[2]);
                        calc(gl);
                    } else if (type.equals(Constant.SortNum.DETAIL)) {
                        gl.setBalance(middleList[2]);
                    }
                    gl.setBalanceDirection(gl.getBalance() == 0 ? Constant.BalanceDirection.FLAT : gl.getBalanceDirection());
                    returnData.add(gl);

                    middleList[0] = 0;
                    middleList[1] = 0;
                    middleList[2] = gl.getBalance();
                    finishList[2] = gl.getBalance();
                    //年末
                    gl = cloneGeneralLedgerVo(nowData, null, null, Constant.SortNum.PERIOD_YEAR);
                    gl.setDebit(finishList[0]);
                    gl.setCredit(finishList[1]);
                    gl.setBalance(finishList[2]);
                    gl.setBalanceDirection(gl.getBalance() == 0 ? Constant.BalanceDirection.FLAT : gl.getBalanceDirection());
                    returnData.add(gl);
                }
            }
        }
        return returnData;
    }

    /**
     * 是否需要期初
     * @param nowData
     * @param dataList
     * @param index
     * @return
     */
    private boolean needOpen(GeneralReturnAuxiliaryVo nowData, List<GeneralReturnAuxiliaryVo> dataList, int index) {
        if (index == 0) {
            return true;
        }
        GeneralReturnAuxiliaryVo prevData = dataList.get(index - Constant.Number.ONE);
        return !(nowData.getAccountBookEntityId().equals(prevData.getAccountBookEntityId())
                && nowData.getPeriodYear() == prevData.getPeriodYear());
    }
    /**
     * 是否需要期末年末
     * @param nowData
     * @param dataList
     * @param index
     * @param type
     * @return
     */
    private boolean needClose(GeneralReturnAuxiliaryVo nowData, List<GeneralReturnAuxiliaryVo> dataList, int index, Integer type) {
        if (index == dataList.size() - Constant.Number.ONE) {
            return true;
        }
        GeneralReturnAuxiliaryVo nextData = dataList.get(index + Constant.Number.ONE);
        return !type.equals(Constant.SortNum.DETAIL)
                || !nowData.getAccountBookEntityId().equals(nextData.getAccountBookEntityId())
                || nowData.getPeriodYear() != nextData.getPeriodYear()
                || nowData.getPeriodNum() != nextData.getPeriodNum();
    }
    /**
     * 计算期初值
     * @param nowData
     * @param index
     * @param dataList
     * @return
     */
    private double sumOpeningBalance(GeneralReturnAuxiliaryVo nowData, int index, List<GeneralReturnAuxiliaryVo> dataList) {
        if (index == dataList.size() - 1) {
            return nowData.getOpeningBalance();
        }
        double returnDouble = 0;
        GeneralReturnAuxiliaryVo gv;
        for (; index < dataList.size(); index++) {
            gv = dataList.get(index);
            if (gv.getAccountBookEntityId().equals(nowData.getAccountBookEntityId()) && gv.getPeriodYear() == nowData.getPeriodYear() && gv.getPeriodNum() == nowData.getPeriodNum()) {
                returnDouble += gv.getOpeningBalance();
            } else {
                return returnDouble;
            }
        }
        return returnDouble;
    }
    /**
     * 计算
     * @param gv
     */
    private void calc(GeneralReturnAuxiliaryVo gv) {
            BigDecimal openingBalance = BigDecimal.valueOf(gv.getOpeningBalance());
            BigDecimal selfDebit = BigDecimal.valueOf(gv.getSelfDebit());
            BigDecimal debit = BigDecimal.valueOf(gv.getDebit());
            BigDecimal selfCredit = BigDecimal.valueOf(gv.getSelfCredit());
            BigDecimal credit = BigDecimal.valueOf(gv.getCredit());
        if (gv.getBalanceDirection() == Constant.BalanceDirection.DEBIT) {
            gv.setBalance(openingBalance.add(selfDebit).add(debit).subtract(selfCredit).subtract(credit).doubleValue());
        } else if (gv.getBalanceDirection() == Constant.BalanceDirection.CREDIT) {
            gv.setBalance(openingBalance.add(selfCredit).add(credit).subtract(selfDebit).subtract(debit).doubleValue());
        }
    }

    /**
     * 是否需要科目编码
     * @param nowData
     * @param dataList
     * @param index
     * @param type
     * @return
     */
    private boolean needCode(GeneralReturnAuxiliaryVo nowData, List<GeneralReturnAuxiliaryVo> dataList, int index, Integer type) {
        if (index == 0) {
            return true;
        }
        GeneralReturnAuxiliaryVo prevData = dataList.get(index - Constant.Number.ONE);
        return type.equals(Constant.SortNum.PERIOD_NUM) && nowData.getAccountBookEntityId().equals(prevData.getAccountBookEntityId());
    }

    /**
     * 通过制单日期设置查询范围
     * @param param
     * @return
     */
    private boolean setParamByVoucherDate(AuxiliaryAccountingQueryDto param) {
        //根据制单日期查出数据
        List<GeneralReturnAuxiliaryVo> voList = auxiliaryItemAccountMapper.findDetailInDataIds(param);
        if (CollectionUtils.isEmpty(voList) || voList.get(0) == null) {
            return false;
        }
        param.setVoucherIdList(new ArrayList<>());
        List<Long> num = null;
        //循环数据
        for (GeneralReturnAuxiliaryVo nowData : voList) {
            //设置范围内的voucherId
            param.getVoucherIdList().add(nowData.getVoucherId());
            num = new LinkedList<>();
            //设置最小日期 最大日期
            List<Long> periodYearNum = param.getPeriodYearNum();
            if(periodYearNum == null || periodYearNum.size() == 0|| periodYearNum.get(0) == null  || periodYearNum.get(0) > nowData.getPeriodYearNum1()){
                num.add(nowData.getPeriodYearNum1());
               // periodYearNum.add(nowData.getPeriodYearNum1());
            }
            if(periodYearNum == null || periodYearNum.size() == 0||periodYearNum.get(1) == 0 || periodYearNum.get(1) < nowData.getPeriodYearNum2()){
                num.add(nowData.getPeriodYearNum2());
              //  periodYearNum.add(nowData.getPeriodYearNum2());
            }
        }
        if(null != num && num.size() > 0){
            param.setPeriodYearNum(num);
        }
        return true;
    }

    /**
     * 过滤详细数据
     * @param formatData
     * @param param
     */
    private void filterDetailData(List<GeneralReturnAuxiliaryVo> formatData, AuxiliaryAccountingQueryDto param) {
        //上一个期初的位置
        int prevOpen = 0;
        int prevYear = 0;
        int i = 0;
        GeneralReturnAuxiliaryVo nowData;
        GeneralReturnAuxiliaryVo nextData;
        String subjectCode = null;
        String subjectName = null;
        while (i < formatData.size()) {
            nowData = formatData.get(i);

            if (StringUtil.isNotBlank(nowData.getSubjectCode())) {
                subjectCode = nowData.getSubjectCode();
                subjectName = nowData.getSubjectName();
            }

            //如果是第零期
            if (formatData.get(i).getPeriodNum() == 0) {
                //如果是最后三笔数据
                if (i + Constant.Number.THREE >= formatData.size()) {
                    formatData.remove(i);
                    formatData.remove(i);
                    formatData.remove(i);
                    break;
                }

                //如果不是零期的年末累计则删除
                if (formatData.get(i).getSortNum() != Constant.SortNum.PERIOD_YEAR) {
                    formatData.remove(i);
                    continue;
                }
                //否则把年末变为期初
                nextData = formatData.get(i + Constant.Number.ONE);
                nowData.setSubjectCode(subjectCode);
                nowData.setSubjectName(subjectName);
                changeToOpeningData(nowData, nextData);
                continue;
            }

            //如果是期初则记录期初位置
            if (nowData.getSortNum() == Constant.SortNum.OPENING) {
                prevOpen = i;
                prevYear = prevOpen;
                //如果这是最后一笔数据
                if (i + 1 == formatData.size()) {
                    formatData.remove(i);
                    return;
                }
                //如果只有三笔数据
                if (formatData.size() < Constant.Number.THREE) {
                    formatData.clear();
                    return;
                }
                //如果下一笔数据也是期初
                if (formatData.get(i + 1).getSortNum() == Constant.SortNum.OPENING) {
                    formatData.remove(i);
                    continue;
                }
                i++;
                continue;
            }
            //记录上一个年末位置
            if (nowData.getSortNum() == Constant.SortNum.PERIOD_YEAR) {
                prevYear = i;
            }

            //如果本期无发生
            boolean happenNo = param.getShowCondition().equals(LedgerConstant.ReportShowCondition.HAPPEN_NO) &&
                    nowData.getSortNum() == Constant.SortNum.PERIOD_NUM &&
                    nowData.getVoucherCount() == Constant.Number.ZERO;
            //余额为零且本期无发生不显示
            boolean happenBalanceNo = param.getShowCondition().equals(LedgerConstant.ReportShowCondition.HAPPEN_BALANCE_NO) &&
                    nowData.getSortNum() == Constant.SortNum.PERIOD_NUM &&
                    nowData.getVoucherCount() == Constant.Number.ZERO &&
                    nowData.getOpeningBalance() == Constant.Number.ZERO;
            if (happenNo || happenBalanceNo) {
                if (nowData.getCredit() == Constant.Number.ZERO
                        && nowData.getDebit() == Constant.Number.ZERO) {
                    for (int j = 0; j < (i - prevYear) + 1; j++) {
                        formatData.remove(prevYear + 1);
                    }
                    //回到期初
                    i = prevYear;
                    continue;
                }
            } else if (param.getShowCondition().equals(LedgerConstant.ReportShowCondition.BALANCE_NO)
                    && nowData.getSortNum() == Constant.SortNum.PERIOD_YEAR
                    && nowData.getBalance() == Constant.Number.ZERO) {
                //余额为零不显示
                for (int j = 0; j < (i - prevOpen); j++) {
                    formatData.remove(prevOpen + 1);
                }
                //回到期初
                i = prevOpen;
                continue;
            }
            i++;
        }
    }

    private void filterDataByDate(List<GeneralReturnAuxiliaryVo> formatData, AuxiliaryAccountingQueryDto param) {
        //日期过滤
        int i = 0;
        String subjectCode = null;
        String subjectName = null;
        GeneralReturnAuxiliaryVo nowData;
        GeneralReturnAuxiliaryVo nextData;
        //如果空则判断日期，否则判断id是否存在
        if (CollectionUtils.isEmpty(param.getVoucherIdList())) {
            while (i < formatData.size()) {
                nowData = formatData.get(i);
                if (StringUtil.isNotBlank(nowData.getSubjectCode())) {
                    subjectCode = nowData.getSubjectCode();
                    subjectName = nowData.getSubjectName();
                }
                if (nowData.getPeriodYearNum() < param.getPeriodYearNum().get(0) || nowData.getPeriodYearNum() > param.getPeriodYearNum().get(1)) {
                    if (nowData.getSortNum() == Constant.SortNum.PERIOD_YEAR && (i + 1) < formatData.size()) {
                        nextData = formatData.get(i + 1);
                        nowData.setSubjectCode(subjectCode);
                        nowData.setSubjectName(subjectName);
                        nowData.setDebit(0);
                        nowData.setCredit(0);
                        nowData.setPeriodYearNum(nextData.getPeriodYearNum());
                        nowData.setPeriodNum(nextData.getPeriodNum());
                        nowData.setSortNum(Constant.SortNum.OPENING);
                        nowData.setAbstractContent(Constant.ReportFormRowType.OPENING);
                        nowData.setBalanceDirection(nowData.getOpeningBalance() == 0 ? Constant.BalanceDirection.FLAT : nowData.getBalanceDirection());
                    } else {
                        formatData.remove(nowData);
                        continue;
                    }
                }
                i++;
            }
        } else {
            boolean isExits;
            while (i < formatData.size()) {
                nowData = formatData.get(i);
                isExits = false;
                param.getVoucherIdList().add(0L);
                for (Long id : param.getVoucherIdList()) {
                    //如果凭证ID存在并且在列表中
                    if (nowData.getVoucherId() != null && nowData.getVoucherId().equals(id)) {
                        isExits = true;
                    }
                }
                if (!isExits) {
                    formatData.remove(nowData);
                    continue;
                }
                i++;
            }
        }

    }
    /**
     * 借贷方向转换
     * @param balanceDirection
     * @return
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

    /**
     * @Description 将数据改为年初数据
     * @Author 刘遵通
     * @Data 2019/10/24 11:42
     * @Param [generalLedgerVo]
     */
    private void changeToOpeningData(GeneralReturnAuxiliaryVo nowData, GeneralReturnAuxiliaryVo nextData) {
        nowData.setDebit(0);
        nowData.setCredit(0);
        nowData.setPeriodYearNum(nextData.getPeriodYearNum());
        nowData.setPeriodNum(nextData.getPeriodNum());
        nowData.setSortNum(Constant.SortNum.OPENING);
        nowData.setAbstractContent(Constant.ReportFormRowType.OPENING);
        nowData.setBalanceDirection(nowData.getBalance() == Constant.Number.ZERO ? Constant.BalanceDirection.FLAT : nowData.getBalanceDirection());
    }
}
