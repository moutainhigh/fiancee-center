package com.njwd.fileexcel.add;

import com.njwd.common.Constant;
import com.njwd.entity.basedata.excel.ExcelData;
import com.njwd.entity.basedata.excel.ExcelError;
import com.njwd.entity.basedata.excel.ExcelRowData;
import com.njwd.entity.basedata.excel.ExcelTemplate;
import com.njwd.fileexcel.convert.ConvertContext;
import com.njwd.fileexcel.convert.ConvertUtil;
import com.njwd.fileexcel.extend.AddContext;
import com.njwd.fileexcel.extend.AddMultiExtend;
import com.njwd.fileexcel.extend.ExtendFactory;
import com.njwd.utils.SpringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/6 9:52
 */
public class DataAddManager {

    /**
     * 批次入库的数量
     */
    private int BATCH_INSERT_COUNT = 5000;

    private PlatformTransactionManager transactionManager;

    private ExcelData excelData;

    private ExcelTemplate excelTemplate;

    private Logger logger = LoggerFactory.getLogger(DataAddManager.class);

    private DataAdd dataAdd;

    private AddContext addContext;

    private boolean multiBatch;

    //成功及错误信息
    List<ExcelRowData> successList = new ArrayList<>();
    List<ExcelError> errorList = new ArrayList<>();


    public DataAddManager(ExcelData excelData){
        this.transactionManager = SpringUtils.getBean(PlatformTransactionManager.class);
        this.excelData = excelData;
        this.excelTemplate = excelData.getExcelTemplate();

        //数据导入工具
        this.dataAdd = DataAdds.newDataAdd(excelData);
        multiBatch = dataAdd.isMultiBatch();
        AddMultiExtend addMultiExtend = ExtendFactory.getAddMultiExtend(excelTemplate.getType());
        if(addMultiExtend!=null){
            multiBatch = addMultiExtend.isMultiBatch();
            addContext = new AddContext();
            addContext.setTitleListMap(excelData.getTitleListMap());
            addMultiExtend.add(addContext);
        }

    }
    
    /**
     * @description: 导入数据   可以部分成功部分失败
     * @param: []
     * @return: void 
     * @author: xdy        
     * @create: 2019-06-06 10-57 
     */
    /*public void boot(){
        if(excelData.getExcelRowDataList().isEmpty()) {
            return;
        }
        //工作者数量
        int workerCount = excelData.getExcelRowDataList().size() / BATCH_INSERT_COUNT;
        if (excelData.getExcelRowDataList().size() % BATCH_INSERT_COUNT > 0) {
            workerCount++;
        }
        //计数器
        CountDownLatch end = new CountDownLatch(workerCount);
        //数据导入工具
        DataAdd dataAdd = DataAdds.newDataAdd(excelTemplate.getType(),excelTemplate.getBusinessTable(),excelTemplate.getBusinessColumns());
        //扩展转换
        List<ConvertContext> convertContextList = ConvertUtil.findExtendConverts(excelTemplate.getType(),ConvertContext.CONVERT_BEFORE_ADD);
        final Subject subject = SecurityUtils.getSubject();
        //分多批插入
        logger.debug("启动{}个入库工作者",workerCount);
        long beginTime = System.currentTimeMillis();
        for (int i = 0; i < workerCount; i++) {
            int startIndex = i * BATCH_INSERT_COUNT;
            int endIndex = (i + 1) * BATCH_INSERT_COUNT;
            if (endIndex > excelData.getExcelRowDataList().size()) {
                endIndex = excelData.getExcelRowDataList().size();
            }
            final List<ExcelRowData> dataList = excelData.getExcelRowDataList().subList(startIndex, endIndex);
            final String name = "入库工作者"+i+"号";
            ThreadPool.submit(() -> {
                int totalCount = dataList.size();//处理总数量
                int successCount = 0;//成功处理数量
                try {
                    logger.debug("{}开始运行",name);
                    ThreadContext.bind(subject);
                    boolean isOk;
                    List<ExcelRowData> canUseList = convertData(convertContextList,dataList);
                    TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
                    //先尝试批量插入
                    try {
                        logger.debug("{}开始批量入库",name);
                        dataAdd.addBatch(canUseList);
                        transactionManager.commit(status);
                        isOk = true;
                        successList.addAll(canUseList);
                        successCount = canUseList.size();
                        logger.debug("{}批量入库成功",name);
                    } catch (Exception e) {
                        logger.debug("{}批量入库失败,改为逐条入库。失败原因:{}",name,e.getMessage());
                        transactionManager.rollback(status);
                        isOk = false;
                    }
                    //批量插入未成功，改为逐条插入
                    if (!isOk) {
                        logger.debug("{}开始逐条入库",name);
                        for(ExcelRowData rowData:canUseList){
                            TransactionStatus status1 = transactionManager.getTransaction(new DefaultTransactionDefinition());
                            try {
                                dataAdd.add(rowData);
                                transactionManager.commit(status1);
                                successList.add(rowData);
                                successCount++;
                            } catch (Exception e) {
                                transactionManager.rollback(status1);
                                ExcelError excelError = new ExcelError();
                                excelError.setRowNum(rowData.getRowNum());
                                excelError.setErrorMsg(String.format(Constant.ExcelErrorMsg.ERROR_INSERT, e.getMessage()));
                                errorList.add(excelError);
                            }
                        }
                        logger.debug("{}逐条入库结束",name);
                    }
                } finally {
                    end.countDown();
                    ThreadContext.unbindSubject();
                }
                logger.debug("{}入库成功{}条，失败{}条",name,successCount,totalCount-successCount);
            });
        }
        try {
            end.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        logger.debug("入库完成，使用时间：{}毫秒，入库成功{}条，失败{}条", endTime - beginTime,successList.size() ,errorList.size());
    }*/

    /**
     * @description: 导入数据 全部成功或全部失败
     * @param: []
     * @return: void 
     * @author: xdy        
     * @create: 2019-07-29 14-57 
     */
    public void boot0(){
        if(excelData.getExcelRowDataList().isEmpty()) {
            return;
        }
        //
        Map<Integer,List<ExcelRowData>> sheetRowDataMap =  excelData.getExcelRowDataList().stream().collect(Collectors.groupingBy(ExcelRowData::getSheetNum));
        //工作者数量
        Map<Integer,Integer> sheetWorkerCount = new HashMap<>();
        Set<Integer> sheetNums = sheetRowDataMap.keySet();
        for(Integer sheetNum:sheetNums){
            int batchInsertCount = BATCH_INSERT_COUNT;
            if(!multiBatch){
                batchInsertCount = sheetRowDataMap.get(sheetNum).size();
            }
            int workerCount = sheetRowDataMap.get(sheetNum).size() / batchInsertCount;
            if (sheetRowDataMap.get(sheetNum).size() % batchInsertCount > 0) {
                workerCount++;
            }
            sheetWorkerCount.put(sheetNum,workerCount);
        }
        //扩展转换
        List<ConvertContext> convertContextList = ConvertUtil.findExtendConverts(excelTemplate.getType(),ConvertContext.CONVERT_BEFORE_ADD);

        long beginTime = System.currentTimeMillis();
        //分批次批量插入
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        boolean isBatchOk = true;
        List<ExcelRowData> toDebugList = new ArrayList<>();
        logger.debug("开始批量入库");
        try {
            for(Integer sheetNum:sheetNums){
                int workerCount = sheetWorkerCount.get(sheetNum);
                List<ExcelRowData> sheetRowDataList = sheetRowDataMap.get(sheetNum);
                int batchInsertCount = BATCH_INSERT_COUNT;
                if(!multiBatch){
                    batchInsertCount = sheetRowDataList.size();
                }
                DataAdd dataAdd = getDataAdd(sheetNum);
                for (int i = 0; i < workerCount; i++) {
                    int startIndex = i * batchInsertCount;
                    int endIndex = (i + 1) * batchInsertCount;
                    if (endIndex > sheetRowDataList.size()) {
                        endIndex = sheetRowDataList.size();
                    }
                    final List<ExcelRowData> dataList = sheetRowDataList.subList(startIndex, endIndex);
                    List<ExcelRowData> canUseList = convertData(convertContextList,dataList);
                    if(dataList.size()!=canUseList.size()){
                        isBatchOk = false;
                    }
                    try{
                        dataAdd.addBatch(canUseList);
                        successList.addAll(canUseList);
                    }catch (Exception e){
                        isBatchOk = false;
                        String errorMsg = e.getMessage();
                        if(errorMsg==null){
                            errorMsg = e.toString();
                        }
                        errorMsg = String.format(Constant.ExcelErrorMsg.ERROR_INSERT, errorMsg);
                        for(ExcelRowData rowData:canUseList){
                            ExcelError excelError = new ExcelError();
                            excelError.setSheetName(rowData.getSheetName());
                            excelError.setRowNum(rowData.getRowNum());
                            excelError.setCellNum(-1);
                            excelError.setErrorMsg(errorMsg);
                            errorList.add(excelError);
                        }
                        //toDebugList.addAll(canUseList);
                    }
                }
            }
        }catch (Exception e){
            isBatchOk = false;
        }finally {
            if(isBatchOk){
                transactionManager.commit(status);
                logger.debug("批量入库成功");
            }
            else{
                transactionManager.rollback(status);
                logger.debug("批量入库失败");
            }
        }
        //查询插入错误数据
        /*if(!toDebugList.isEmpty()){
            TransactionStatus status1 = transactionManager.getTransaction(new DefaultTransactionDefinition());
            logger.debug("查找入库错误数据");
            try {
                for(ExcelRowData rowData:toDebugList){
                    DataAdd dataAdd = getDataAdd(rowData.getSheetNum());
                    try {
                        dataAdd.add(rowData);
                        successList.add(rowData);
                    }catch (Exception e){
                        ExcelError excelError = new ExcelError();
                        excelError.setSheetName(rowData.getSheetName());
                        excelError.setRowNum(rowData.getRowNum());
                        excelError.setCellNum(-1);
                        excelError.setErrorMsg(String.format(Constant.ExcelErrorMsg.ERROR_INSERT, e.getMessage()));
                        errorList.add(excelError);
                    }
                }
            }finally {
                transactionManager.rollback(status1);
            }
        }*/
        long endTime = System.currentTimeMillis();
        logger.debug("入库结束，使用时间：{}毫秒，入库成功{}条，失败{}条", endTime - beginTime,successList.size() ,errorList.size());
    }

    private DataAdd getDataAdd(Integer sheetNum){
        if(this.addContext!=null){
            return this.addContext.getAddExtendProxy(sheetNum);
        }
        return this.dataAdd;
    }

    public List<ExcelRowData> convertData(List<ConvertContext> convertContextList,List<ExcelRowData> dataList){
        List<ExcelRowData> canUseList = new ArrayList<>();
        for(ExcelRowData excelRowData:dataList){
            excelRowData = ConvertUtil.convertData(convertContextList,excelRowData,this.errorList);
            if(excelRowData!=null) {
                canUseList.add(excelRowData);
            }
        }
        return canUseList;
    }
    
    /**
     * @description: 成功导入的数据
     * @param: []
     * @return: java.util.List<com.njwd.entity.basedata.excel.ExcelRowData>
     * @author: xdy        
     * @create: 2019-06-06 11-00 
     */
    public List<ExcelRowData> getSuccessList(){
        return this.successList;
    }
    
    /**
     * @description: 校验错误，转换错误，导入错误数据
     * @param: []
     * @return: java.util.List<com.njwd.entity.basedata.excel.ExcelError>
     * @author: xdy        
     * @create: 2019-06-06 11-00 
     */
    public List<ExcelError> getErrorList(){
        //合并校验错误，转换错误，导入错误
        List<ExcelError> errorList = new ArrayList<>();
        errorList.addAll(this.errorList);
        errorList.addAll(excelData.getExcelErrorList());
        return errorList;
    }

    public boolean isAddOk(){
        return this.errorList.size()==0;
    }

}
