package com.njwd.fileexcel.read;

import com.njwd.entity.basedata.excel.*;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.fileexcel.check.DataCheckManager;
import com.njwd.utils.ExcelUtil;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/26 13:56
 */
public class GeneralExcelRead extends AbstractExcelRead{

    @Override
    public void read0(InputStream inputStream, ExcelData excelData) {
        Workbook workbook=null;
        try {
            workbook =  WorkbookFactory.create(inputStream);
        } catch (Exception e) {

        }
        if(workbook == null) {
            throw new ServiceException(ResultCode.EXCEL_NOT_CORRECT);
        }
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        Sheet sheet = workbook.getSheetAt(0);
        BlockingDeque<ExcelRowData> queue = new LinkedBlockingDeque<>(QUEUE_CAPACITY);
        //启动校验线程
        checkManager = new DataCheckManager(excelData);
        checkManager.boot(sheet.getLastRowNum()+1,queue);
        //读取excel数据
        int colNum=0;
        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                colNum = row.getLastCellNum();
                continue;
            }
            ExcelRowData excelRowData = new ExcelRowData();
            excelRowData.setRowNum(row.getRowNum());
            excelRowData.setExcelCellDataList(new ArrayList<>());
            for (int i = 0; i < colNum; i++) {
                Object value = null;
                Cell cell = row.getCell(i);
                if (cell != null){
                    evaluator.evaluateInCell(cell);
                    value = ExcelUtil.getCellValue(cell);
                }
                ExcelCellData cellData = new ExcelCellData();
                cellData.setCellNum(i);
                cellData.setData(value);
                excelRowData.getExcelCellDataList().add(cellData);
            }
            try {
                queue.put(excelRowData);
                logger.debug("读取完第{}条数据", excelRowData.getRowNum());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //读取结束
        checkManager.readEnd();
    }

}
