package com.njwd.entity.basedata.excel;

import lombok.Getter;
import lombok.Setter;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/12 16:43
 */
@Getter
@Setter
public class ExcelColumn {

    private String field;
    private String title;
    private String convertType;

    public ExcelColumn(String field, String title) {
        this.field = field;
        this.title = title;
    }

    public ExcelColumn(String field, String title, String convertType) {
        this.field = field;
        this.title = title;
        this.convertType = convertType;
    }

}
