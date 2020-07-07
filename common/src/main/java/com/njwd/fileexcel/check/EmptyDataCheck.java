package com.njwd.fileexcel.check;

import com.njwd.common.Constant;
import com.njwd.entity.basedata.excel.ExcelRule;

/**
 * @description: 校验空值
 * @author: xdy
 * @create: 2019/6/4 16:30
 */
public class EmptyDataCheck extends AbstractDataCheck{

    /**
     * @description: 校验空值
     * @param: [data, rule]
     * @return: com.njwd.fileexcel.check.DataCheck.CheckResult
     * @author: xdy        
     * @create: 2019-06-05 16-07 
     */
    @Override
    public CheckResult check(Object data, ExcelRule rule) {
        CheckResult checkResult = new CheckResult();
        if (data==null) {
            if (Constant.Is.YES.equals(rule.getIsEmpty()) ){
                checkResult.setOk(true);
                return checkResult;
            } else {
                checkResult.setOk(false);
                checkResult.setErrorMsg(Constant.ExcelErrorMsg.ERROR_EMPTY);
                return checkResult;
            }

        }
        return checkNext(data,rule);
    }
}
