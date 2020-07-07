package com.njwd.fileexcel.add;

import com.njwd.entity.basedata.excel.ExcelRowData;
import com.njwd.fileexcel.extend.AddExtend;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/6 11:58
 */
public interface DataAdd extends AddExtend<ExcelRowData> {

    int addBatch(List<ExcelRowData> rowDataList);

    int add(ExcelRowData rowData);





}
