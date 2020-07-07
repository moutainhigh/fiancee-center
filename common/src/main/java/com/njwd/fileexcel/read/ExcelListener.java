package com.njwd.fileexcel.read;



import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.njwd.entity.basedata.excel.ExcelCellData;
import com.njwd.entity.basedata.excel.ExcelData;
import com.njwd.entity.basedata.excel.ExcelRowData;
import com.njwd.fileexcel.check.DataCheckManager;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/13 14:33
 */
public class ExcelListener extends AnalysisEventListener<List> {

    private ExcelData excelData;

    private DataCheckManager checkManager;

    public ExcelListener(ExcelData excelData,DataCheckManager checkManager){
        this.excelData = excelData;
        this.checkManager = checkManager;
    }

    BlockingDeque<ExcelRowData> queue = new LinkedBlockingDeque<>(ExcelRead.QUEUE_CAPACITY);

    private boolean isBoot = false;

    @Override
    public void invoke(List array, AnalysisContext context) {
        if(context.getCurrentRowNum().equals(0)){
            if(!isBoot){
                checkManager.boot(context.getTotalCount(),queue);
                isBoot = true;
            }
            List<String> titleList = new ArrayList<>();
            for(Object obj:array){
                titleList.add(Objects.toString(obj));
            }
            excelData.addTitleList(context.getCurrentSheet().getSheetNo(),titleList);
            //多sheet
            if(context.getCurrentSheet().getSheetNo()>1){
                excelData.setMultiSheet(true);
            }
            return;
        }
        ExcelRowData excelRowData = new ExcelRowData();
        excelRowData.setRowNum(context.getCurrentRowNum());
        excelRowData.setSheetNum(context.getCurrentSheet().getSheetNo());
        excelRowData.setSheetName(context.getCurrentSheet().getSheetName());
        excelRowData.setExcelCellDataList(new ArrayList<>());
        for(int i=0;i<array.size();i++){
            ExcelCellData cellData = new ExcelCellData();
            cellData.setCellNum(i);
            cellData.setData(array.get(i));
            excelRowData.getExcelCellDataList().add(cellData);
        }
        try {
            queue.put(excelRowData);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }

}
