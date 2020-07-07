package com.njwd.fileexcel.read;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.metadata.Sheet;
import com.njwd.entity.basedata.excel.ExcelData;
import com.njwd.entity.basedata.excel.ExcelRule;
import com.njwd.entity.basedata.excel.ExcelTemplate;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.fileexcel.check.DataCheckManager;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * @description: 流式读取excel,防止内存溢出
 * @author: xdy
 * @create: 2019/6/26 12:00
 */
public class SaxExcelRead extends AbstractExcelRead {

    @Override
    public void read0(InputStream inputStream, ExcelData excelData) {
        checkManager = new DataCheckManager(excelData);
        //数据回调处理
        ExcelListener excelListener = new ExcelListener(excelData,checkManager);
        //读取数据
        EasyExcelFactory.readBySax(new BufferedInputStream(inputStream), null, excelListener);
        //读取结束
        checkManager.readEnd();
    }

}
