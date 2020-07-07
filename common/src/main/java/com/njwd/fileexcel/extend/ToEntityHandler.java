package com.njwd.fileexcel.extend;

import com.njwd.entity.basedata.excel.ExcelRowData;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/10/18 11:16
 */
public interface ToEntityHandler {

    Object toEntity(ExcelRowData excelRowData, List<String> titleList);

}
