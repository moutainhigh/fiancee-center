package com.njwd.fileexcel.check;

import com.njwd.common.Constant;
import com.njwd.entity.basedata.excel.*;
import com.njwd.fileexcel.convert.ConvertContext;
import com.njwd.fileexcel.convert.ConvertResult;
import com.njwd.fileexcel.convert.ConvertUtil;
import com.njwd.fileexcel.convert.DataConvertGroup;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/5 11:18
 */
public class DataCheckWorker implements Runnable {

    private BlockingDeque<ExcelRowData> queue;
    private List<ExcelRule> rules;
    private CountDownLatch end;
    private boolean isReadEnd = false;
    List<ExcelRowData> rowDataList;
    List<ExcelError> errorList;
    private String name;
    private Logger logger = LoggerFactory.getLogger(DataCheckWorker.class);
    private DataCheckGroup dataCheckGroup;
    private List<ConvertContext> convertContexts;
    private DataConvertGroup dataConvertGroup;
    private List<ConvertContext> extendConvertContexts;
    private Subject subject;
    private ExcelData excelData;


    DataCheckWorker(BlockingDeque<ExcelRowData> queue, CountDownLatch end, int index, ExcelData excelData, List<ConvertContext> convertContexts, DataConvertGroup dataConvertGroup, List<ConvertContext> extendConvertContexts, Subject subject) {
        this.excelData = excelData;
        this.subject = subject;
        this.queue = queue;
        this.rules = excelData.getExcelRuleList();
        this.end = end;
        this.name = "校验工作者" + index + "号";
        this.rowDataList = new ArrayList<>();
        this.errorList = new ArrayList<>();
        this.dataCheckGroup = new DataCheckGroup(excelData);
        this.convertContexts = convertContexts;
        this.dataConvertGroup = dataConvertGroup;
        this.extendConvertContexts = extendConvertContexts;
    }

    /**
     * @description: 校验数据
     * @param: []
     * @return: void
     * @author: xdy
     * @create: 2019-06-05 17-42
     */
    @Override
    public void run() {
        try {
            log("{}开始运行", this.name);
            ThreadContext.bind(subject);
            while (true) {
                ExcelRowData excelRowData = null;
                try {
                    excelRowData = queue.poll(10, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //值校验
                if (excelRowData != null) {
                    //系统配置 值转换
                    if(this.excelData.isSystemCheck())
                        excelRowData = convertData(excelRowData);
                    //扩展值转换
                    excelRowData = ConvertUtil.convertData(this.extendConvertContexts,excelRowData,this.errorList);
                    if(excelRowData==null) {
                        continue;
                    }

                    boolean isOk = true;
                    String errorMsg ="";
                    int errorCol = -1;
                    try {
                        //系统检验
                        if(this.excelData.isSystemCheck()){
                            for (int i = 0; i < rules.size(); i++) {
                                ExcelRule rule = rules.get(i);
                                ExcelCellData cellData = excelRowData.getExcelCellDataList().get(i);
                                //获取导入值
                                Object value = cellData.getImportData();
                                //log("规则:{},数据：{}",rule,value);
                                //校验规则
                                CheckResult checkResult = dataCheckGroup.check(value, rule);
                                if (!checkResult.isOk()) {
                                    errorMsg = checkResult.getErrorMsg();
                                    errorCol = i;
                                    isOk = false;
                                    break;
                                }
                            }
                        }
                        //自定义校验
                        if(isOk){
                            CheckResult checkResult =dataCheckGroup.check(excelRowData);
                            if(!checkResult.isOk()){
                                errorMsg = checkResult.getErrorMsg();
                                errorCol = checkResult.getErrorCol();
                                isOk = false;
                            }
                        }
                    }catch (Exception e){
                        isOk = false;
                        errorMsg = e.getMessage();
                        if(errorMsg==null)
                            errorMsg = e.toString();
                        e.printStackTrace();
                    }
                    if (isOk) {
                        rowDataList.add(excelRowData);
                    }else {
                        ExcelError excelError = new ExcelError();
                        excelError.setSheetNum(excelRowData.getSheetNum());
                        excelError.setSheetName(excelRowData.getSheetName());
                        excelError.setRowNum(excelRowData.getRowNum());
                        excelError.setCellNum(errorCol);
                        if(errorCol>=0&&errorCol<excelRowData.getExcelCellDataList().size()){
                            excelError.setData(excelRowData.getExcelCellDataList().get(errorCol).getImportData());
                        }
                        excelError.setErrorMsg(errorMsg);
                        errorList.add(excelError);
                    }
                    log("{}处理完第{}行数据", this.name, excelRowData.getRowNum());
                } else if (isReadEnd) {
                    break;
                }

            }
        } finally {
            end.countDown();
            ThreadContext.unbindSubject();
        }
        log("{}运行结束，共处理{}条数据", this.name, rowDataList.size() + errorList.size());
    }

    /**
     * @description: 通知读取结束
     * @param: []
     * @return: void
     * @author: xdy
     * @create: 2019-06-05 17-43
     */
    public void readEnd() {
        this.isReadEnd = true;
    }

    /**
     * @description: 获取成功数据
     * @param: []
     * @return: java.util.List<com.njwd.entity.basedata.excel.ExcelRowData>
     * @author: xdy
     * @create: 2019-06-05 17-44
     */
    public List<ExcelRowData> getRowDataList() {
        return this.rowDataList;
    }

    /**
     * @description: 获取错误数据
     * @param: []
     * @return: java.util.List<com.njwd.entity.basedata.excel.ExcelError>
     * @author: xdy
     * @create: 2019-06-05 17-44
     */
    public List<ExcelError> getErrorList() {
        return this.errorList;
    }

    private void log(String format, Object... arguments) {
        try {
            //if (IS_DEBUG)
            logger.debug(format, arguments);
        } catch (Exception e) {

        }
    }


    /**
     * 值转换
     * @param excelRowData
     * @return
     */
    public ExcelRowData convertData(ExcelRowData excelRowData) {
        if(excelRowData==null) {
            return null;
        }
        //数据转换
        boolean isOk = true;
        String errorMsg ="";
        ExcelCellData currentCell=null;
        try {
            if (!convertContexts.isEmpty()) {
                for (ConvertContext convertContext : convertContexts) {
                    currentCell = null;
                    ExcelCellData excelCellData = excelRowData.getExcelCellDataList().get(convertContext.getIndex());
                    currentCell = excelCellData;
                    ConvertResult convertResult = dataConvertGroup.convert(convertContext.getType(), excelCellData.getData());
                    if (convertResult.isOk()) {
                        excelCellData.setOldData(excelCellData.getData());
                        excelCellData.setData(convertResult.getTarget());
                        excelCellData.setConverted(true);
                        //存在冗余字段
                        if (convertContext.isRedundancy()) {
                            ExcelCellData redundancyCell = new ExcelCellData();
                            redundancyCell.setCellNum(-1);
                            redundancyCell.setData(excelCellData.getOldData());
                            excelRowData.getExcelCellDataList().add(redundancyCell);
                        }
                    } else {
                        errorMsg = convertResult.getErrorMsg();
                        isOk = false;
                        break;
                    }
                }
            }
        }catch (Exception e){
            isOk = false;
            errorMsg = e.getMessage();
        }
        if(!isOk){
            ExcelError excelError = new ExcelError();
            excelError.setRowNum(excelRowData.getRowNum());
            if(currentCell!=null){
                excelError.setCellNum(currentCell.getCellNum());
                excelError.setData(currentCell.getData());
            }
            excelError.setErrorMsg(String.format(Constant.ExcelErrorMsg.ERROR_CONVERT, errorMsg));
            errorList.add(excelError);
            return null;
        }else {
            return excelRowData;
        }
    }

}
