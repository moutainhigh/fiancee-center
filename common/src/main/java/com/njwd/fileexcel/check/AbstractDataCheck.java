package com.njwd.fileexcel.check;

import com.njwd.entity.basedata.excel.ExcelRule;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/4 16:14
 */
public abstract class AbstractDataCheck implements DataCheckChannel{

    private DataCheckChannel next;
    
    /**
     * @description: 校验下一个规则
     * @param: [data, rule]
     * @return: com.njwd.fileexcel.check.DataCheck.CheckResult
     * @author: xdy        
     * @create: 2019-06-05 16-07 
     */
    public CheckResult checkNext(Object data,ExcelRule rule){
        if(getNext()==null){
            CheckResult checkResult = new CheckResult();
            checkResult.setOk(true);
            return checkResult;
        }
        return getNext().check(data,rule);
    }
    
    @Override
    public DataCheck getNext() {
        return this.next;
    }

    @Override
    public DataCheckChannel setNext(DataCheckChannel dataCheck) {
        if(!this.equals(dataCheck)&&dataCheck!=null){
            this.next = dataCheck;
            return this.next;
        }
        return this;
    }

}
