package com.njwd.entity.basedata.excel;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class ExcelRowData implements Serializable {

    private int sheetNum;
    private String sheetName;
    private int rowNum;
    private List<ExcelCellData> excelCellDataList;
    private Object businessData;

}
