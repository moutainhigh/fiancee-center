package com.njwd.entity.basedata.excel;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class ExcelCellData implements Serializable {
    private int cellNum;
    private Object data;
    private Object oldData;
    private boolean converted;

    public Object getImportData(){
        if(converted) {
            return oldData;
        }
        return data;
    }

}
