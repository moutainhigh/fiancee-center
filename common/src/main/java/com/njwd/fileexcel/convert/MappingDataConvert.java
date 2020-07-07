package com.njwd.fileexcel.convert;

import com.njwd.fileexcel.data.MappingData;
import com.njwd.fileexcel.data.MappingDatas;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/10 9:28
 */
public class MappingDataConvert extends AbstractDataConvert{

    private MappingData mappingData;

    MappingDataConvert(String dataType,boolean reverse){
        mappingData = MappingDatas.newMappingData(dataType,reverse);
    }

    @Override
    public ConvertResult unsafeDataConvert(Object source) {
        ConvertResult result = new ConvertResult();
        Object target = mappingData.mapping(source);
        if(target==null){
            result.setOk(false);
            result.setSource(source);
            result.setErrorMsg("不存在对应的转换值");
            return  result;
        }
        result.setOk(true);
        result.setSource(source);
        result.setTarget(target);
        return result;
    }

}
