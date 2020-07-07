package com.njwd.fileexcel.extend;

import com.njwd.entity.basedata.excel.ExcelRowData;
import com.njwd.fileexcel.add.CommonColumn;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/11 13:50
 */
public interface ColumnExtend {

    /**
     * 单表通用的字段数据
     * 比如 创建者 创建时间等
     * @param commonColumns
     */
    void getCommonColumn(List<CommonColumn> commonColumns);

    /**
     * 单表扩展字段
     * @return
     */
    List<String> getExtendColumn();

    /**
     * 单表每行扩展字段数据
     * @param rowDataList
     */
    void extendData(List<ExcelRowData> rowDataList);

}
