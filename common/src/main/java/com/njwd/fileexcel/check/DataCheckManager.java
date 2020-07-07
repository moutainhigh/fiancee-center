package com.njwd.fileexcel.check;

import com.njwd.entity.basedata.excel.*;
import com.njwd.fileexcel.ThreadPool;
import com.njwd.fileexcel.convert.ConvertContext;
import com.njwd.fileexcel.convert.ConvertUtil;
import com.njwd.fileexcel.convert.DataConvertGroup;
import com.njwd.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/5 11:24
 */
public class DataCheckManager {

    private BlockingDeque<ExcelRowData> queue;
    private CountDownLatch end;
    private DataCheckWorker[] workers;
    private Logger logger = LoggerFactory.getLogger(DataCheckWorker.class);
    private List<ConvertContext> convertContexts;
    private DataConvertGroup dataConvertGroup;
    private List<ConvertContext> extendConvertContexts;
    private ExcelData excelData;

    /**
     * 线程最少校验数量
     */
    private static final int WORKER_CHECK_COUNT = 500;

    public DataCheckManager(ExcelData excelData) {
        this.excelData = excelData;
        //需转换数据
        if(excelData.isSystemCheck()){
            this.convertContexts = new ArrayList<>();
            List<ExcelRule> rules = excelData.getExcelRuleList();
            for(int i=0;i<rules.size();i++){
                ExcelRule rule = rules.get(i);
                if(StringUtil.isNotEmpty(rule.getConvertType())){
                    ConvertContext convertContext = new ConvertContext(i,rule.getConvertType());
                    //冗余字段
                    if(StringUtils.isNoneBlank(rule.getRedundancyColumn())){
                        excelData.getExcelTemplate().getBusinessColumns().add(rule.getRedundancyColumn());
                        convertContext.setRedundancy(true);
                    }
                    convertContexts.add(convertContext);
                }
            }
        }
        //数据转换
        this.dataConvertGroup = new DataConvertGroup();
        //扩展转换
        this.extendConvertContexts = ConvertUtil.findExtendConverts(excelData.getExcelTemplate().getType(),ConvertContext.CONVERT_BEFORE_CHECK);

    }
    
    /**
     * @description: 启动工作者
     * @param: [workerCount]
     * @return: void 
     * @author: xdy        
     * @create: 2019-06-05 17-39 
     */
    public void boot(int rowCount,BlockingDeque<ExcelRowData> queue) {
        this.queue = queue;
        int workerCount = Math.max(rowCount / WORKER_CHECK_COUNT, 1);
        workerCount = Math.min(workerCount, ThreadPool.getThreadCount());
        log("启动{}个校验工作者", workerCount);
        //计数器
        this.end = new CountDownLatch(workerCount);
        //启动校验工作者
        workers = new DataCheckWorker[workerCount];
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        RequestContextHolder.setRequestAttributes(sra, true);
        for (int i = 0; i < workerCount; i++) {
            workers[i] = new DataCheckWorker(this.queue, this.end, i,this.excelData,this.convertContexts,this.dataConvertGroup,this.extendConvertContexts, SecurityUtils.getSubject());
            ThreadPool.submit(workers[i]);
        }
    }
    
    /**
     * @description: 通知工作者并等待
     * @param: []
     * @return: void 
     * @author: xdy        
     * @create: 2019-06-05 17-40 
     */
    public void readEnd() {
        for (int i = 0; i < workers.length; i++) {
            workers[i].readEnd();
        }
        //等待校验结束
        try {
            end.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        excelData.setExcelRowDataList(getRowDataList());
        excelData.setExcelErrorList(getErrorList());
    }

    /**
     * @description: 获取成功数据
     * @param: []
     * @return: java.util.List<com.njwd.entity.basedata.excel.ExcelRowData>
     * @author: xdy        
     * @create: 2019-06-05 17-40 
     */
    public List<ExcelRowData> getRowDataList() {
        List<ExcelRowData> rowDataList = new ArrayList<>();
        for (int i = 0; i < workers.length; i++) {
            rowDataList.addAll(workers[i].getRowDataList());
        }
        return rowDataList;
    }

    /**
     * @description: 获取错误数据
     * @param: []
     * @return: java.util.List<com.njwd.entity.basedata.excel.ExcelError>
     * @author: xdy        
     * @create: 2019-06-05 17-41 
     */
    public List<ExcelError> getErrorList() {
        List<ExcelError> errorList = new ArrayList<>();
        for (int i = 0; i < workers.length; i++) {
            errorList.addAll(workers[i].getErrorList());
        }
        return errorList;
    }

    private void log(String format, Object... arguments) {
        try {
            //if (IS_DEBUG)
            logger.debug(format, arguments);
        } catch (Exception e) {

        }
    }
}
