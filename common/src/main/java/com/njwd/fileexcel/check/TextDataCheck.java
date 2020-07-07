package com.njwd.fileexcel.check;

import com.njwd.entity.basedata.excel.ExcelRule;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/4 17:12
 */
public class TextDataCheck extends AbstractDataCheck{

    private final String DATA_DIGITAL="digital";
    private final String DATA_LETTER="letter";
    private final String DATA_DIGITAL_LETTER="digital_letter";

    private String dataRegex;
    private String errorMsg;

    TextDataCheck(String dataRange){
        switch (dataRange){
            case DATA_DIGITAL:
                dataRegex="[0-9]+";
                errorMsg="只能输入数字";
                break;
            case DATA_LETTER:
                dataRegex="[a-zA-Z]+";
                errorMsg="只能输入字母";
                break;
            case DATA_DIGITAL_LETTER:
                dataRegex="[0-9a-zA-Z]+";
                errorMsg="只能输入数字和字母";
                break;
            default:
                dataRegex=".+";
                break;
        }
    }
    
    /**
     * @description: 校验值范围
     * @param: [data, rule]
     * @return: com.njwd.fileexcel.check.DataCheck.CheckResult
     * @author: xdy        
     * @create: 2019-06-05 16-13 
     */
    @Override
    public CheckResult check(Object data, ExcelRule rule) {
        String d = (String) data;
        if(!d.matches(dataRegex)){
            CheckResult checkResult = new CheckResult();
            checkResult.setOk(false);
            checkResult.setErrorMsg(errorMsg);
            return checkResult;
        }
        return checkNext(data,rule);
    }
}
