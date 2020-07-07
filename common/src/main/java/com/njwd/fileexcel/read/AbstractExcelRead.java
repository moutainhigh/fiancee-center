package com.njwd.fileexcel.read;

import com.njwd.entity.basedata.excel.*;
import com.njwd.fileexcel.check.DataCheckManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/26 14:05
 */
public abstract class AbstractExcelRead implements ExcelRead{

    protected DataCheckManager checkManager;
    protected Logger logger = LoggerFactory.getLogger(AbstractExcelRead.class);

    protected abstract void read0(InputStream inputStream, ExcelData excelData);

    @Override
    public void read(InputStream inputStream, ExcelData excelData) {
        logger.debug("读取开始");
        long beginTime = System.currentTimeMillis();
        read0(inputStream,excelData);
        long endTime = System.currentTimeMillis();
        int successCount=0,errorCount=0;
        if(checkManager!=null){
            successCount = checkManager.getRowDataList().size();
            errorCount = checkManager.getErrorList().size();
        }
        logger.debug("校验完成，使用时间：{}毫秒，校验成功{}条，失败{}条", endTime - beginTime,successCount ,errorCount);
    }

    @Override
    public List<ExcelRowData> getRowDataList() {
        if(checkManager==null) {
            return new ArrayList<>();
        }
        return checkManager.getRowDataList();
    }

    @Override
    public List<ExcelError> getErrorList() {
        if(checkManager==null) {
            return new ArrayList<>();
        }
        return checkManager.getErrorList();
    }
}
