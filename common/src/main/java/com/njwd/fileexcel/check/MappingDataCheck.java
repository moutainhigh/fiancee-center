package com.njwd.fileexcel.check;

import com.njwd.entity.basedata.excel.ExcelRule;
import com.njwd.fileexcel.data.MappingData;
import com.njwd.fileexcel.data.MappingDatas;

/**
 * @description: 
 * @author: xdy
 * @create: 2019/6/4 17:29
 */
public class MappingDataCheck extends AbstractDataCheck{

    private MappingData mappingData;

    MappingDataCheck(String dataType){
        mappingData = MappingDatas.newMappingData(dataType);
    }

    
    /**
     * @description: 校验辅助资料
     * @param: [data, rule]
     * @return: com.njwd.fileexcel.check.DataCheck.CheckResult
     * @author: xdy        
     * @create: 2019-06-05 16-09 
     */
    @Override
    public CheckResult check(Object data, ExcelRule rule) {
        if(!mappingData.contains(data)){
            CheckResult checkResult = new CheckResult();
            checkResult.setOk(false);
            checkResult.setErrorMsg("数据不在取值范围内");
            return checkResult;
        }
        return checkNext(data,rule);
    }
}
