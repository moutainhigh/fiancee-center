package com.njwd.ledger.excel;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.annotation.ExcelExtend;
import com.njwd.common.Constant;
import com.njwd.common.ExcelDataConstant;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.excel.ExcelCellData;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.basedata.vo.AccountBookVo;
import com.njwd.entity.ledger.BalanceInitExcelContext;
import com.njwd.entity.ledger.BalanceSubjectInitAuxiliaryItem;
import com.njwd.entity.ledger.dto.BalanceSubjectInitAuxiliaryDto;
import com.njwd.entity.ledger.dto.BalanceSubjectInitDto;
import com.njwd.entity.platform.AccountSubjectAuxiliary;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.fileexcel.check.CheckContext;
import com.njwd.fileexcel.check.CheckResult;
import com.njwd.fileexcel.export.DataGetGroup;
import com.njwd.fileexcel.extend.*;
import com.njwd.ledger.cloudclient.AccountBookFeignClient;
import com.njwd.ledger.cloudclient.AccountSubjectFeignClient;
import com.njwd.ledger.service.BalanceSubjectInitService;
import com.njwd.utils.StringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: xdy
 * @create: 2019/10/16 16:53
 */
@Component
@ExcelExtend(type = "balance_subject_init")
public class BalanceSubjectInitExtend extends InitExtend implements DownloadExtend, CheckExtend, AddMultiExtend {

    @Resource
    AccountSubjectFeignClient accountSubjectFeignClient;

    @Resource
    BalanceSubjectInitService balanceSubjectInitService;

    @Resource
    private AccountBookFeignClient accountBookFeignClient;


    @Override
    public void check(CheckContext checkContext) {
        Long accountBookEntityId = checkContext.getLongValue("accountBookEntityId");
        Long accountBookId = checkContext.getLongValue("accountBookId");
        Long accountBookSystemId = checkContext.getLongValue("accountBookSystemId");
        Integer periodYear = checkContext.getIntegerValue("periodYear");
        Byte periodNum = checkContext.getByteValue("periodNum");
        //checkFileName(accountBookEntityId,checkContext.getFileName());
        //查询租户下的所有科目
        List<AccountSubjectVo> accountSubjectVoList = getAccountSubjectVoList();
        Map<String,List<AccountSubjectVo>> subjectMap = accountSubjectVoList.stream().collect(Collectors.groupingBy(AccountSubjectVo::getCode));
        //校验科目是否重复
        List<String> subjectList = new ArrayList<String>();
        //获取所有辅助核算集合
        Map<String,Object> sourceTableInfoMap =getSourceTableInfoMap(accountBookEntityId,accountBookId);

        //excel行数据转为实体
        checkContext.setSheetToEntityHandler(2,getBalanceSubjectInitAuxiliaryEntity());
        //校验
        checkContext.addSheetHandler(1,getBalanceSubjectInitCheck(subjectList,subjectMap,accountBookEntityId,accountBookId,accountBookSystemId,periodYear,periodNum))
                .addSheetHandler(2,getBalanceSubjectInitAuxiliaryCheck(sourceTableInfoMap,subjectMap,accountBookEntityId,accountBookId,accountBookSystemId,periodYear,periodNum));
    }

    private Map<String,Object> getSourceTableInfoMap(Long accountBookEntityId,Long accountBookId) {
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        List<Long> businessUnitIds = new ArrayList<>();
        businessUnitIds.add(accountBookEntityId);
        List<String> sourceTableList = new ArrayList<String>();
        sourceTableList.add(Constant.TableName.DEPT);
        sourceTableList.add(Constant.TableName.PROJECT);
        sourceTableList.add(Constant.TableName.BANK_ACCOUNT);
        sourceTableList.add(Constant.TableName.CUSTOMER);
        sourceTableList.add(Constant.TableName.SUPPLIER);
        sourceTableList.add(Constant.TableName.STAFF);
        sourceTableList.add(Constant.TableName.ACCOUNTING_ITEM_VALUE);
        sourceTableList.add(Constant.TableName.ACCOUNT_BOOK_ENTITY);
        accountSubjectDto.setSourceTableList(sourceTableList);
        accountSubjectDto.setBusinessUnitIds(businessUnitIds);
        accountSubjectDto.setIfFindUseCompanyDataOnly(Constant.Is.YES);
        AccountBookDto accountBookDto = new AccountBookDto();
        accountBookDto.setId(accountBookId);
        AccountBookVo accountBookVo = accountBookFeignClient.findAccountBookById(accountBookDto).getData();
        if(accountBookVo!=null){
            List<Long> companyIds = new ArrayList<>();
            companyIds.add(accountBookVo.getCompanyId());
            accountSubjectDto.setCompanyIds(companyIds);
        }
        List<List<Map<String, Object>>> allSourceTableInfoList = accountSubjectFeignClient.findAllSourceTableInfo(accountSubjectDto).getData();
        //拼接SourceTable code map key 为SourceTable_code, value为id
        Map<String,Object> sourceTableInfoMap = new HashMap<>();
        for(int i=0;i<allSourceTableInfoList.size();i++){
            List<Map<String, Object>> sourceTableInfoList = allSourceTableInfoList.get(i);
            String sourceTable=sourceTableList.get(i);
            for(Map<String, Object> sourceTableInfo : sourceTableInfoList){
                sourceTableInfoMap.put(sourceTable+Constant.Character.UNDER_LINE+sourceTableInfo.get(Constant.ColumnName.CODE), sourceTableInfo.get(Constant.ColumnName.ID).toString());
                sourceTableInfoMap.put(sourceTable+Constant.Character.UNDER_LINE+sourceTableInfo.get(Constant.ColumnName.CODE)+Constant.Character.UNDER_LINE, sourceTableInfo.get(Constant.ColumnName.NAME));
            }
        }
        return sourceTableInfoMap;
    }

    private List<AccountSubjectVo> getAccountSubjectVoList(){
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        accountSubjectDto.setIsFinal(Constant.Is.YES);
        accountSubjectDto.setIfFindAuxiliary(Constant.Is.YES);
        Page page = new Page();
        page.setSize(10000L);
        page.setSearchCount(false);
        accountSubjectDto.setPage(page);
        Page<AccountSubjectVo> page1 = accountSubjectFeignClient.findPage(accountSubjectDto).getData();
        List<AccountSubjectVo> accountSubjectVoList = page1.getRecords();
        return accountSubjectVoList;
    }

    /**
     * 会计科目待校验
     * @Author lj
     * @Date:14:40 2019/10/23
     * @param subjectList, subjectMap, accountBookEntityId, accountBookId, accountBookSystemId, periodYear, periodNum
     * @return com.njwd.fileexcel.extend.CheckHandler<com.njwd.entity.ledger.dto.BalanceSubjectInitDto>
     **/
    private CheckHandler<BalanceSubjectInitDto> getBalanceSubjectInitCheck( List<String> subjectList,Map<String,List<AccountSubjectVo>> subjectMap,Long accountBookEntityId,
     Long accountBookId, Long accountBookSystemId, Integer periodYear, Byte periodNum) {
        return (data) -> {
            data.setAccountBookEntityId(accountBookEntityId);
            data.setAccountBookId(accountBookId);
            data.setAccountBookSystemId(accountBookSystemId);
            data.setPeriodYear(periodYear);
            data.setPeriodNum(periodNum);
            data.setIsFinal(Constant.Is.YES);
            String message=checkBalanceSubjectInit(subjectList,subjectMap,data);
            subjectList.add(data.getAccountSubjectCode());
            if (StringUtil.isNotEmpty(message)) {
                return CheckResult.error(message);
            }
            //拼接年初余额
            getYearOpeningBalance(subjectMap, data);
            return CheckResult.ok();
        };
    }

    /**
     * 拼接年初余额
     * @Author lj
     * @Date:16:40 2019/10/22
     * @param subjectMap, data
     * @return void
     **/
    private void getYearOpeningBalance(Map<String, List<AccountSubjectVo>> subjectMap, BalanceSubjectInitDto data) {
        Byte balanceDirection = subjectMap.get(data.getAccountSubjectCode()).get(Constant.Number.ZERO).getBalanceDirection();
        Long accountSubjectId = subjectMap.get(data.getAccountSubjectCode()).get(Constant.Number.ZERO).getId();
        BigDecimal openingBalance = data.getOpeningBalance();
        BigDecimal thisYearDebitAmount = data.getThisYearDebitAmount();
        BigDecimal thisYearCreditAmount = data.getThisYearCreditAmount();
        if(openingBalance==null){
            openingBalance=new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
        }
        if(thisYearDebitAmount==null){
            thisYearDebitAmount=new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
        }
        if(thisYearCreditAmount==null){
            thisYearCreditAmount=new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
        }
        BigDecimal yearOpeningBalance = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
        //借，年初余额 = 期初余额-本年借方+本年贷方 贷，年初余额=期初余额+本年借方-本年贷方
        if(Constant.Is.NO.equals(balanceDirection)){
            yearOpeningBalance = yearOpeningBalance.add(openingBalance).subtract(thisYearDebitAmount).add(thisYearCreditAmount);
        }else {
            yearOpeningBalance = yearOpeningBalance.add(openingBalance).add(thisYearDebitAmount).subtract(thisYearCreditAmount);
        }
        data.setAccountSubjectId(accountSubjectId);
        data.setYearOpeningBalance(yearOpeningBalance);
    }

    /**
     * 拼接年初余额
     * @Author lj
     * @Date:16:40 2019/10/22
     * @param subjectMap, data
     * @return void
     **/
    private void getYearOpeningBalanceTwo(Map<String, List<AccountSubjectVo>> subjectMap, BalanceSubjectInitAuxiliaryDto temp) {
        Byte balanceDirection = subjectMap.get(temp.getAccountSubjectCode()).get(Constant.Number.ZERO).getBalanceDirection();
        Long accountSubjectId = subjectMap.get(temp.getAccountSubjectCode()).get(Constant.Number.ZERO).getId();
        BigDecimal openingBalance = temp.getOpeningBalance();
        BigDecimal thisYearDebitAmount = temp.getThisYearDebitAmount();
        BigDecimal thisYearCreditAmount = temp.getThisYearCreditAmount();
        if(openingBalance==null){
            openingBalance=new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
        }
        if(thisYearDebitAmount==null){
            thisYearDebitAmount=new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
        }
        if(thisYearCreditAmount==null){
            thisYearCreditAmount=new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
        }
        BigDecimal yearOpeningBalance = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
        //借，年初余额 = 期初余额-本年借方+本年贷方 贷，年初余额=期初余额+本年借方-本年贷方
        if(Constant.Is.NO.equals(balanceDirection)){
            yearOpeningBalance = yearOpeningBalance.add(openingBalance).subtract(thisYearDebitAmount).add(thisYearCreditAmount);
        }else {
            yearOpeningBalance = yearOpeningBalance.add(openingBalance).add(thisYearDebitAmount).subtract(thisYearCreditAmount);
        }
        temp.setAccountSubjectId(accountSubjectId);
        temp.setYearOpeningBalance(yearOpeningBalance);
    }

    private String checkBalanceSubjectInit(List<String> subjectList,Map<String,List<AccountSubjectVo>> subjectMap,BalanceSubjectInitDto data) {
        String message="";
        //记录表名：@表名，记录行号：@行号，记录异常：科目编码 @科目编码 不存在。
        if(!subjectMap.containsKey(data.getAccountSubjectCode())){
            message = "科目编码 不存在";
            return message;
        }
        //toDo//如果期初余额、本年借方、本年贷方数值无效(通用逻辑：数据有效性校验)，记录表名：@表名，记录行号：@行号，记录异常：期初余额(、本年借方、本年贷方)数据格式异常。

        //如果科目编码在系统科目编码中为非末级科目，记录表名：@表名，记录行号：@行号，记录异常：科目编码 @科目编码 非末级科目。
        if(Constant.Is.NO.equals(subjectMap.get(data.getAccountSubjectCode()).get(Constant.Number.ZERO).getIsFinal())){
            message = "科目编码 非末级科目";
            return message;
        }

        //如果存在相同科目编码行，记录表名：@表名，记录行号：@行号1，@行号2，…(以最小行值填表)，记录异常：存在相同科目编码 @科目编码 于行 @行号1，行 @行号2。
        if(subjectList.contains(data.getAccountSubjectCode())){
            message = "存在相同科目编码 "+data.getAccountSubjectCode();
            return message;
        }
        return message;
    }

    /**
     * 核算待校验
     * @Author lj
     * @Date:14:40 2019/10/23
     * @param sourceTableInfoMap, subjectMap, accountBookEntityId, accountBookId, accountBookSystemId, periodYear, periodNum
     * @return com.njwd.fileexcel.extend.CheckHandler<com.njwd.entity.ledger.dto.BalanceSubjectInitAuxiliaryDto>
     **/
    private CheckHandler<BalanceSubjectInitAuxiliaryDto> getBalanceSubjectInitAuxiliaryCheck(Map<String,Object> sourceTableInfoMap,Map<String,List<AccountSubjectVo>> subjectMap,Long accountBookEntityId,
     Long accountBookId, Long accountBookSystemId, Integer periodYear, Byte periodNum) {
        Map<String,String> auxiliaryIndexMap = new HashMap<String,String>();
        auxiliaryIndexMap.put(Constant.TableName.DEPT,Constant.Auxiliary.DEPT_CODE);
        auxiliaryIndexMap.put(Constant.TableName.PROJECT,Constant.Auxiliary.PROJECT_CODE);
        auxiliaryIndexMap.put(Constant.TableName.BANK_ACCOUNT,Constant.Auxiliary.BANK_ACCOUNT_CODE);
        auxiliaryIndexMap.put(Constant.TableName.CUSTOMER,Constant.Auxiliary.CUSTOMER_CODE);
        auxiliaryIndexMap.put(Constant.TableName.SUPPLIER,Constant.Auxiliary.SUPPLIER_CODE);
        auxiliaryIndexMap.put(Constant.TableName.STAFF,Constant.Auxiliary.STAFF_CODE);
        auxiliaryIndexMap.put(Constant.TableName.ACCOUNTING_ITEM_VALUE,Constant.Auxiliary.ACCOUNTING_ITEM_VALUE_CODE);
        auxiliaryIndexMap.put(Constant.TableName.ACCOUNT_BOOK_ENTITY,Constant.Auxiliary.ACCOUNT_BOOK_ENTITY_CODE);
        //定义keySign list 来校验是否有重复的keySign
        List<String> keySign = new ArrayList<>();
        return (data) -> {
            data.setAccountBookEntityId(accountBookEntityId);
            data.setAccountBookId(accountBookId);
            data.setAccountBookSystemId(accountBookSystemId);
            data.setPeriodYear(periodYear);
            data.setPeriodNum(periodNum);
            String message=checkBalanceSubjectInitAuxiliary(keySign,auxiliaryIndexMap,sourceTableInfoMap,subjectMap,data);
            if (StringUtil.isNotEmpty(message)) {
                return CheckResult.error(message);
            }
            //拼接年初余额
            getYearOpeningBalanceTwo(subjectMap, data);

            //重复校验
            System.err.println("BalanceSubjectInitAuxiliaryDto"+data.getAccountSubjectCode()+data.getOpeningBalance());
            return CheckResult.ok();
        };
    }

    private String checkBalanceSubjectInitAuxiliary(List<String> keySign,Map<String,String> auxiliaryIndexMap,Map<String,Object> sourceTableInfoMap,Map<String,List<AccountSubjectVo>> subjectMap,BalanceSubjectInitAuxiliaryDto data) {
        String message="";
        //如果核算编码不存在于系统核算编码中，记录表名：@表名，记录行号：@行号，记录异常：客户核算编码 @核算编码 不存在。
        List<BalanceSubjectInitAuxiliaryItem> balanceSubjectInitAuxiliaryItemList=data.getBalanceSubjectInitAuxItemList();
        for(BalanceSubjectInitAuxiliaryItem item : balanceSubjectInitAuxiliaryItemList){
            if(!sourceTableInfoMap.containsKey(item.getSourceTable()+Constant.Character.UNDER_LINE+item.getItemValueCode())){
                message = auxiliaryIndexMap.get(item.getSourceTable())+" "+item.getItemValueCode()+" 不存在";
                return message;
            }else {
                item.setItemValueId(Long.valueOf(sourceTableInfoMap.get(item.getSourceTable()+Constant.Character.UNDER_LINE+item.getItemValueCode()).toString()));
                item.setItemValueName(String.valueOf(sourceTableInfoMap.get(item.getSourceTable()+Constant.Character.UNDER_LINE+item.getItemValueCode()+Constant.Character.UNDER_LINE)));
            }
        }

        //toDo//如果期初余额、本年借方、本年贷方数值无效(通用逻辑：数据有效性校验)，记录表名：@表名，记录行号：@行号，记录异常：期初余额(、本年借方、本年贷方)数据格式异常。

        //如果存在相同 科目编码+核算编码，记录表名：@表名：@表名，记录行号：@行号1，@行号2，
        //拼接keySign
        Set<BalanceSubjectInitAuxiliaryItem> itemSet = new HashSet<>(balanceSubjectInitAuxiliaryItemList);
        String initKeySignStr = getKeySignTwo(data.getAccountSubjectCode(), itemSet);
        if(keySign.contains(initKeySignStr)){
            message = "存在相同科目编码 "+data.getAccountSubjectCode()+" 核算编码 "+initKeySignStr;
            return message;
        }
        keySign.add(initKeySignStr);

        //如果科目所启用核算项目与填表项目不一致 @科目编码 启用核算与填写项目不一致。
        List<AccountSubjectVo> accountSubjectVos = subjectMap.get(data.getAccountSubjectCode());
        Set<String> sourceTables = new HashSet<>();
        Set<String> sourceTablesTwo = new HashSet<>();
        for(AccountSubjectVo accountSubjectVo:accountSubjectVos){
            if(accountSubjectVo.getAccountSubjectAuxiliaryList()!=null){
                for(AccountSubjectAuxiliary accountSubjectAuxiliary:accountSubjectVo.getAccountSubjectAuxiliaryList()){
                    sourceTables.add(accountSubjectAuxiliary.getAuxiliarySourceTable());
                }
            }
        }

        for(BalanceSubjectInitAuxiliaryItem item : balanceSubjectInitAuxiliaryItemList){
            sourceTablesTwo.add(item.getSourceTable());
            if(!sourceTables.contains(item.getSourceTable())){
                message = "科目编码 "+data.getAccountSubjectCode()+" 启用核算与填写项目不一致";
                return message;
            }
        }

        if(sourceTables.size() != sourceTablesTwo.size()){
            message = "科目编码 "+data.getAccountSubjectCode()+" 启用核算与填写项目不一致";
            return message;
        }

        return message;
    }

    /**
     * 拼接辅助核算余额的keySign
     * @param auxiliaryItems   auxiliaryItems
     * @return java.lang.StringBuilder
     * @author xyyxhcj@qq.com
     * @date 2019/9/27 17:05
     **/
    private String getKeySignTwo(String subjectCode, Set<BalanceSubjectInitAuxiliaryItem> auxiliaryItems) {
        return concatKeySignTwo(subjectCode, auxiliaryItems);
    }

    /**
     * 拼接辅助核算维度标识
     *
     * @param subjectCode      subjectCode
     * @param auxiliaryItems auxiliaryItems
     * @return java.lang.StringBuilder
     * @author xyyxhcj@qq.com
     * @date 2019/9/27 18:19
     **/
    private String concatKeySignTwo(String subjectCode, Set<BalanceSubjectInitAuxiliaryItem> auxiliaryItems) {
        StringBuilder keySign = new StringBuilder();
        keySign.append(subjectCode).append(Constant.Character.UNDER_LINE);
        for (BalanceSubjectInitAuxiliaryItem auxiliaryItem : auxiliaryItems) {
            keySign.append(auxiliaryItem.getSign()).append(Constant.Character.UNDER_LINE);
        }
        return keySign.toString();
    }

    @Override
    public void add(AddContext addContext) {
        addContext.addAddExtend(1,getBalanceSubjectInitAdd());
        addContext.addAddExtend(2,getBalanceSubjectInitAuxiliaryAdd());
    }

    /**
     * 会计科目新增
     * @Author lj
     * @Date:14:41 2019/10/23
     * @param
     * @return com.njwd.fileexcel.extend.AddExtend<com.njwd.entity.ledger.dto.BalanceSubjectInitDto>
     **/
    private AddExtend<BalanceSubjectInitDto> getBalanceSubjectInitAdd(){
        return new AddExtend<BalanceSubjectInitDto>() {
            @Override
            public int addBatch(List<BalanceSubjectInitDto> datas) {
                int result = 0;
                if (CollectionUtils.isNotEmpty(datas)) {
                    BalanceSubjectInitDto balanceSubjectInitDto = new BalanceSubjectInitDto();
                    balanceSubjectInitDto.setBalanceSubjectInitList(datas);
                    balanceSubjectInitService.addSubjectInitBatch(balanceSubjectInitDto);
                }
                return result;
            }

            @Override
            public int add(BalanceSubjectInitDto data) {
                return 0;
            }
        };
    }

    /**
     * 核算新增
     * @Author lj
     * @Date:14:41 2019/10/23
     * @param
     * @return com.njwd.fileexcel.extend.AddExtend<com.njwd.entity.ledger.dto.BalanceSubjectInitAuxiliaryDto>
     **/
    private AddExtend<BalanceSubjectInitAuxiliaryDto> getBalanceSubjectInitAuxiliaryAdd(){
        return new AddExtend<BalanceSubjectInitAuxiliaryDto>() {
            @Override
            public int addBatch(List<BalanceSubjectInitAuxiliaryDto> datas) {
                int result = 0;
                if (CollectionUtils.isNotEmpty(datas)) {
                    BalanceSubjectInitDto balanceSubjectInitDto = new BalanceSubjectInitDto();
                    balanceSubjectInitDto.setBalanceSubInitAuxiliaryList(datas);
                    balanceSubjectInitService.addSubjectInitBatch(balanceSubjectInitDto);
                }
                return result;
            }

            @Override
            public int add(BalanceSubjectInitAuxiliaryDto data) {
                return 0;
            }
        };
    }

    /**
     * @description: excel行转为实体对象
     * @param: []
     * @return: com.njwd.fileexcel.extend.ToEntityHandler 
     * @author: xdy        
     * @create: 2019-10-18 11:36 
     */
    private ToEntityHandler getBalanceSubjectInitAuxiliaryEntity(){
        BalanceInitExcelContext balanceInitExcelContext = new BalanceInitExcelContext();
        return (excelRowData,titleList)->{
            if(!balanceInitExcelContext.isParse()){
                Map<String,Integer> auxiliaryIndexMap = new HashMap<>();
                for(int i=0;i<titleList.size();i++){
                    switch (titleList.get(i)){
                        case Constant.Auxiliary.DEPT_CODE:
                            auxiliaryIndexMap.put(Constant.TableName.DEPT,i);
                            break;
                        case Constant.Auxiliary.PROJECT_CODE:
                            auxiliaryIndexMap.put(Constant.TableName.PROJECT,i);
                            break;
                        case Constant.Auxiliary.BANK_ACCOUNT_CODE:
                            auxiliaryIndexMap.put(Constant.TableName.BANK_ACCOUNT,i);
                            break;
                        case Constant.Auxiliary.CUSTOMER_CODE:
                            auxiliaryIndexMap.put(Constant.TableName.CUSTOMER,i);
                            break;
                        case Constant.Auxiliary.SUPPLIER_CODE:
                            auxiliaryIndexMap.put(Constant.TableName.SUPPLIER,i);
                            break;
                        case Constant.Auxiliary.STAFF_CODE:
                            auxiliaryIndexMap.put(Constant.TableName.STAFF,i);
                            break;
                        case Constant.Auxiliary.ACCOUNTING_ITEM_VALUE_CODE:
                            auxiliaryIndexMap.put(Constant.TableName.ACCOUNTING_ITEM_VALUE,i);
                            break;
                        case Constant.Auxiliary.ACCOUNT_BOOK_ENTITY_CODE:
                            auxiliaryIndexMap.put(Constant.TableName.ACCOUNT_BOOK_ENTITY,i);
                            break;
                        case "期初余额":
                            balanceInitExcelContext.setOpeningBalanceIndex(i);
                            break;
                        case "本年借方":
                            balanceInitExcelContext.setThisYearDebitAmountIndex(i);
                            break;
                        case "本年贷方":
                            balanceInitExcelContext.setThisYearCreditAmountIndex(i);
                    }
                }
                balanceInitExcelContext.setAuxiliaryIndexMap(auxiliaryIndexMap);
                balanceInitExcelContext.setParse(true);
            }
            BalanceSubjectInitAuxiliaryDto balanceSubjectInitAuxiliaryDto = new BalanceSubjectInitAuxiliaryDto();
            List<ExcelCellData> excelCellDataList = excelRowData.getExcelCellDataList();
            balanceSubjectInitAuxiliaryDto.setAccountSubjectCode(Objects.toString(excelCellDataList.get(0).getData()));
            Object openingBalance = excelCellDataList.get(balanceInitExcelContext.getOpeningBalanceIndex()).getData();
            balanceSubjectInitAuxiliaryDto.setOpeningBalance(objectToString(openingBalance));
            Object thisYearCreditAmount = excelCellDataList.get(balanceInitExcelContext.getThisYearCreditAmountIndex()).getData();
            balanceSubjectInitAuxiliaryDto.setThisYearCreditAmount(objectToString(thisYearCreditAmount));
            Object thisYearDebitAmount = excelCellDataList.get(balanceInitExcelContext.getThisYearDebitAmountIndex()).getData();
            balanceSubjectInitAuxiliaryDto.setThisYearDebitAmount(objectToString(thisYearDebitAmount));
            List<BalanceSubjectInitAuxiliaryItem> items = new ArrayList<>();
            for(String key:balanceInitExcelContext.getAuxiliaryIndexMap().keySet()){
                Integer index = balanceInitExcelContext.getAuxiliaryIndexMap().get(key);
                Object code = excelCellDataList.get(index).getData();
                if(code!=null){
                    BalanceSubjectInitAuxiliaryItem item = new BalanceSubjectInitAuxiliaryItem();
                    item.setSourceTable(key);
                    item.setItemValueCode(code.toString());
                    items.add(item);
                }
            }
            balanceSubjectInitAuxiliaryDto.setBalanceSubjectInitAuxItemList(items);
            return balanceSubjectInitAuxiliaryDto;
        };
    }

    private BigDecimal objectToString(Object object){
        BigDecimal bigDecimal;
        try {
            bigDecimal = new BigDecimal(object.toString());
        }catch (Exception e){
            bigDecimal = new BigDecimal("0");
        }
        return bigDecimal;
    }

    /**
     * @description: 写入excel内容
     * @param: [writer]
     * @return: void 
     * @author: xdy        
     * @create: 2019-10-22 09:34 
     */
    @Override
    protected void writeSheet(ExcelWriter writer) {
        writeSubjectSheet(writer);
        writeAuxiliarySheet(writer);
    }

    /**
     * @description: 期初会计科目
     * @param: [writer]
     * @return: void 
     * @author: xdy        
     * @create: 2019-10-17 14:38 
     */
    private void writeSubjectSheet(ExcelWriter writer) {
        ExcelColumn[] excelColumnArr = new ExcelColumn[]{
                new ExcelColumn("code", "科目编码"),
                new ExcelColumn("name", "科目名称"),
                new ExcelColumn("balanceDirection", "科目方向", ExcelDataConstant.SYSTEM_DATA_BALANCE_DIRECTION),
                new ExcelColumn("", "币种"),
                new ExcelColumn("", "期初余额"),
                new ExcelColumn("", "本年借方"),
                new ExcelColumn("", "本年贷方")
        };
        //标题
        List<List<String>> excelHead = new ArrayList<>();
        for (ExcelColumn excelColumn : excelColumnArr) {
            List<String> list = new ArrayList<>();
            list.add(excelColumn.getTitle());
            excelHead.add(list);
        }
        //数据
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        accountSubjectDto.setIsFinal(Constant.Is.YES);
        accountSubjectDto.setIfFindHasAuxiliaryOnly(Constant.Is.NO);
        Page page = new Page();
        page.setSize(10000L);
        page.setSearchCount(false);
        accountSubjectDto.setPage(page);
        Page<AccountSubjectVo> page1 = accountSubjectFeignClient.findPage(accountSubjectDto).getData();
        List<AccountSubjectVo> accountSubjectVoList = page1.getRecords();
        DataGetGroup dataGetGroup = new DataGetGroup();
        List<List<Object>> excelData = dataGetGroup.get(accountSubjectVoList, excelColumnArr);
        //
        Sheet sheet1 = new Sheet(1, 0);
        sheet1.setSheetName("科目");
        sheet1.setHead(excelHead);
        sheet1.setAutoWidth(Boolean.TRUE);
        //
        writer.write1(excelData,sheet1);
    }

    /**
     * @description: 期初辅助核算
     * @param: [writer]
     * @return: void 
     * @author: xdy        
     * @create: 2019-10-17 14:38 
     */
    private void writeAuxiliarySheet(ExcelWriter writer) {
        List<String> titleArr = new ArrayList<>();
        titleArr.addAll(Arrays.asList("科目编码","科目名称","科目方向","币种"));
        List<AccountSubjectVo> accountSubjectVoList = getAccountSubjectVoList();
        Set<String> sourceTables = new HashSet<>();
        for(AccountSubjectVo accountSubjectVo:accountSubjectVoList){
            if(accountSubjectVo.getAccountSubjectAuxiliaryList()!=null){
                for(AccountSubjectAuxiliary accountSubjectAuxiliary:accountSubjectVo.getAccountSubjectAuxiliaryList()){
                    sourceTables.add(accountSubjectAuxiliary.getAuxiliarySourceTable());
                }
            }
        }
        for(String sourceTable:sourceTables){
            switch (sourceTable){
                case Constant.TableName.PROJECT:
                    titleArr.add(Constant.Auxiliary.PROJECT_CODE);
                    titleArr.add(Constant.Auxiliary.PROJECT_NAME);
                    break;
                case Constant.TableName.BANK_ACCOUNT:
                    titleArr.add(Constant.Auxiliary.BANK_ACCOUNT_CODE);
                    titleArr.add(Constant.Auxiliary.BANK_ACCOUNT_NAME);
                    break;
                case Constant.TableName.CUSTOMER:
                    titleArr.add(Constant.Auxiliary.CUSTOMER_CODE);
                    titleArr.add(Constant.Auxiliary.CUSTOMER_NAME);
                    break;
                case Constant.TableName.SUPPLIER:
                    titleArr.add(Constant.Auxiliary.SUPPLIER_CODE);
                    titleArr.add(Constant.Auxiliary.SUPPLIER_NAME);
                    break;
                case Constant.TableName.STAFF:
                    titleArr.add(Constant.Auxiliary.STAFF_CODE);
                    titleArr.add(Constant.Auxiliary.STAFF_NAME);
                    break;
                case Constant.TableName.DEPT:
                    titleArr.add(Constant.Auxiliary.DEPT_CODE);
                    titleArr.add(Constant.Auxiliary.DEPT_NAME);
                    break;
                case Constant.TableName.ACCOUNTING_ITEM_VALUE:
                    titleArr.add(Constant.Auxiliary.ACCOUNTING_ITEM_VALUE_CODE);
                    titleArr.add(Constant.Auxiliary.ACCOUNTING_ITEM_VALUE_NAME);
                    break;
                case Constant.TableName.ACCOUNT_BOOK_ENTITY:
                    titleArr.add(Constant.Auxiliary.ACCOUNT_BOOK_ENTITY_CODE);
                    titleArr.add(Constant.Auxiliary.ACCOUNT_BOOK_ENTITY_NAME);
                    break;
            }
        }
        titleArr.addAll(Arrays.asList("期初余额","本年借方","本年贷方"));

        //标题
        List<List<String>> excelHead = new ArrayList<>();
        for (String title : titleArr) {
            List<String> list = new ArrayList<>();
            list.add(title);
            excelHead.add(list);
        }
        Sheet sheet1 = new Sheet(2, 0);
        sheet1.setSheetName("核算");
        sheet1.setHead(excelHead);
        sheet1.setAutoWidth(Boolean.TRUE);
        //
        writer.write1(null,sheet1);
    }


    /**
     * @description: 取消系统校验
     * @param: []
     * @return: boolean 
     * @author: xdy        
     * @create: 2019-10-22 10:08 
     */
    @Override
    public boolean isSystemCheck(){
        return false;
    }

    /**
     * @description: 是否分多批次，false全部一次性录入
     * @param: []
     * @return: boolean
     * @author: xdy
     * @create: 2019-10-23 10:49
     */
    @Override
    public boolean isMultiBatch(){return false;}

}
