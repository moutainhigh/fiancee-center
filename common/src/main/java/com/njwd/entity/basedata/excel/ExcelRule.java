package com.njwd.entity.basedata.excel;

import lombok.Data;

import java.io.Serializable;

@Data
public class ExcelRule implements Serializable {

    private Long id;
    private String type;
    private String name;
    private String colType;
    private String dataRange;
    private String dataExclude;
    private int lengthType;
    private String dataLength;
    private Byte isEmpty;
    private int isUnique;
    private int seri;
    private String businessColumn;
    private String redundancyColumn;
    private String convertType;
    
    public static final String COL_TYPE_STRING="string";
    public static final String COL_TYPE_NUMBER="number";
    public static final String COL_TYPE_DATE="date";

    public static final String DATA_RANGE_DIGITAL="digital";
    public static final String DATA_RANGE_LETTER="letter";
    public static final String DATA_RANGE_DIGITAL_LETTER="digital_letter";
    public static final String DATA_RANGE_AUX_DATA="aux_data_";
    public static final String DATA_RANGE_SYSTEM_DATA="system_data_";
    public static final String DATA_RANGE_BUSINESS_DATA="business_data_";

    public static final int IS_UNIQUE_NO = 0;
    public static final int IS_UNIQUE_YES = 1;

    public static final int LENGTH_TYPE_IGNORE=0;
    public static final int LENGTH_TYPE_LESS_EQUALS=1;
    public static final int LENGTH_TYPE_EQUALS_IN=2;

    public static final String CONVERT_TYPE_AUX_DATA = DATA_RANGE_AUX_DATA;
    public static final String CONVERT_TYPE_SYSTEM_DATA = DATA_RANGE_SYSTEM_DATA;
    public static final String CONVERT_TYPE_BUSINESS_DATA = DATA_RANGE_BUSINESS_DATA;

}
