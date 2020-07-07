package com.njwd.fileexcel.add;

import com.njwd.entity.basedata.excel.ExcelData;
import com.njwd.entity.basedata.excel.ExcelTemplate;
import com.njwd.fileexcel.extend.AddExtendProxy;
import com.njwd.fileexcel.extend.ExtendFactory;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/6 14:27
 */
public class DataAdds {

    public static DataAdd newDataAdd(ExcelData excelData){
        //String templateType, String businessTable, List<String> businessColumns
        ExcelTemplate excelTemplate = excelData.getExcelTemplate();
        DataAdd dataAdd;
        AddExtendProxy addExtendProxy = ExtendFactory.getAddExtendProxy(excelTemplate.getType());
        if(addExtendProxy!=null){
            addExtendProxy.setTitleList(excelData.getTitleList(1));
            dataAdd = addExtendProxy;
        }else {
            dataAdd = new SingleTableDataAdd(excelTemplate.getType(),excelTemplate.getBusinessTable(),excelTemplate.getBusinessColumns());
        }
        return dataAdd;
    }

}
