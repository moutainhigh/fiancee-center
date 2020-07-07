package com.njwd.fileexcel.check;

import com.njwd.entity.basedata.excel.ExcelRule;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/4 17:44
 */
public class ExcludeDataCheck extends AbstractDataCheck{

    private String excludeRegex;
    private String errorMsg;

    ExcludeDataCheck(String excludeData){
        this.excludeRegex = String.format("[^%s]+",excludeData);
        this.errorMsg = String.format("不能含有%s字符",excludeData);
    }

    /**
     * @description: 校验排除值
     * @param: [data, rule]
     * @return: com.njwd.fileexcel.check.DataCheck.CheckResult
     * @author: xdy
     * @create: 2019-06-05 16-10
     */
    @Override
    public CheckResult check(Object data, ExcelRule rule) {
        String d = (String) data;
        if(!d.matches(excludeRegex)){
            CheckResult checkResult = new CheckResult();
            checkResult.setOk(false);
            checkResult.setErrorMsg(errorMsg);
            return checkResult;
        }
        return checkNext(data,rule);
    }
}
