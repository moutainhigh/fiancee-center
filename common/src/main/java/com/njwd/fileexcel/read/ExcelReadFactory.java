package com.njwd.fileexcel.read;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/13 15:41
 */
public class ExcelReadFactory {

    public static ExcelRead getExcelRead(int readType){
        if(readType==ExcelRead.EXCEL_READ_SAX) {
            return new SaxExcelRead();
        }
        return new GeneralExcelRead();
    }

}
