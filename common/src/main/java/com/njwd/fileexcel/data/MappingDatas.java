package com.njwd.fileexcel.data;

import com.njwd.entity.basedata.excel.ExcelRule;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/10 16:58
 */
public class MappingDatas {

    /**
     * @description: 创建映射数据
     * @param: [dataType]
     * @return: com.njwd.fileexcel.data.MappingData
     * @author: xdy
     * @create: 2019-06-10 17-20
     */
    public static MappingData newMappingData(String dataType,boolean reverse){
        if(dataType.startsWith(ExcelRule.CONVERT_TYPE_AUX_DATA)){
            String auxDataType = dataType.substring(ExcelRule.CONVERT_TYPE_AUX_DATA.length());
            return new AuxData(auxDataType,reverse);
        }else if(dataType.startsWith(ExcelRule.CONVERT_TYPE_SYSTEM_DATA)){
            return new SystemData(dataType,reverse);
        }else if(dataType.startsWith(ExcelRule.CONVERT_TYPE_BUSINESS_DATA)){
            return new BusinessData(dataType,reverse);
        }
        return new DefaultData();
    }

    public static MappingData newMappingData(String dataType){
        return newMappingData(dataType,false);
    }

}
