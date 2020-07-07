package com.njwd.fileexcel.check;

import com.njwd.entity.basedata.excel.ExcelRule;

/**
 * @description: 校验数据
 * @author: xdy
 * @create: 2019/6/4 16:05
 */
public interface DataCheck {
    
    /**
     * @description: 校验数据
     * @param: [data, rule]
     * @return: com.njwd.fileexcel.check.DataCheck.CheckResult
     * @author: xdy        
     * @create: 2019-06-05 16-06 
     */
    CheckResult check(Object data, ExcelRule rule);

}
