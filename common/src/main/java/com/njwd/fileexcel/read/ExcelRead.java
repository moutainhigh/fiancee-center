package com.njwd.fileexcel.read;

import com.njwd.entity.basedata.excel.*;

import java.io.InputStream;
import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/26 11:56
 */
public interface ExcelRead {

    int QUEUE_CAPACITY = 1000;
    int EXCEL_READ_SAX=0;
    int EXCEL_READ_GENERAL=1;

    void read(InputStream inputStream, ExcelData excelData);

    List<ExcelRowData> getRowDataList();

    List<ExcelError> getErrorList();

}
