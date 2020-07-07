package com.njwd.fileexcel.check;

import com.njwd.entity.basedata.excel.ExcelRule;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/4 16:59
 */
public class StringDataCheck extends AbstractDataCheck{

    private String errorMsg = "数据类型不对，应该为字符串类型";
    
    /**
     * @description: 检验是否为字符
     * @param: [data, rule]
     * @return: com.njwd.fileexcel.check.DataCheck.CheckResult
     * @author: xdy        
     * @create: 2019-06-05 16-11 
     */
    @Override
    public CheckResult check(Object data, ExcelRule rule) {
        Object d = data;
        if(data instanceof Long){
            d = String.valueOf(data);
        } else if(data instanceof Double){
            if((double)data%1==0){
                d = String.valueOf(((Double) data).intValue());
            }else {
                CheckResult result = new CheckResult();
                result.setOk(false);
                result.setErrorMsg(errorMsg);
                return result;
            }
        }
        if(!(d instanceof String)){
            CheckResult result = new CheckResult();
            result.setOk(false);
            result.setErrorMsg(errorMsg);
            return result;
        }
        return checkNext(d,rule);
    }
}
