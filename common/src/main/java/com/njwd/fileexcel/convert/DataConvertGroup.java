package com.njwd.fileexcel.convert;

import com.njwd.entity.basedata.excel.ExcelRule;
import com.njwd.utils.StringUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: 获取格式转换
 * @author: xdy
 * @create: 2019/5/23 10:26
 */
public class DataConvertGroup {

    private Map<String,DataConvert> map = new ConcurrentHashMap<>();

    /**
     * @description: 转换数据
     * @param: [convertType, source]
     * @return: com.njwd.fileexcel.convert.DataConvert.ConvertResult
     * @author: xdy        
     * @create: 2019-06-06 11-06 
     */
    public ConvertResult convert(String convertType, Object source){
        return convert(convertType,source,false);
    }

    public ConvertResult convert(String convertType, Object source,boolean reverse){
        if(StringUtil.isEmpty(convertType)){
            ConvertResult convertResult = new ConvertResult();
            convertResult.setSource(source);
            convertResult.setOk(true);
            return convertResult;
        }
        DataConvert dataConvert = findDataConvert(convertType,reverse);
        return dataConvert.convert(source);
    }
    
    /**
     * @description: 获取转换工具
     * @param: [convertType]
     * @return: com.njwd.fileexcel.convert.DataConvert
     * @author: xdy        
     * @create: 2019-06-06 11-07 
     */
    public DataConvert findDataConvert(String convertType,boolean reverse){
        DataConvert dataConvert = map.get(convertType);
        if(dataConvert!=null){
            return dataConvert;
        }
        if(convertType.startsWith(ExcelRule.CONVERT_TYPE_AUX_DATA)
                ||convertType.startsWith(ExcelRule.CONVERT_TYPE_SYSTEM_DATA)
                ||convertType.startsWith(ExcelRule.CONVERT_TYPE_BUSINESS_DATA)){
            dataConvert = new MappingDataConvert(convertType,reverse);
            map.put(convertType,dataConvert);
        }else {
            dataConvert = new DefaultDataConvert();
        }
        return dataConvert;
    }


}
