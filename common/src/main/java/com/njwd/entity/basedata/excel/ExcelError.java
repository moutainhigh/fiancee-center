package com.njwd.entity.basedata.excel;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ExcelError implements Serializable {

    private int sheetNum;
    private String sheetName;
    private int rowNum;
    private int cellNum;
    private Object data;
    private String errorMsg;

}
