package com.njwd.fileexcel.check;

import com.njwd.entity.basedata.excel.ExcelRule;
import com.njwd.utils.StringUtil;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/5 9:15
 */
public class LengthDataCheck extends AbstractDataCheck{

    private Integer length;
    private Integer[] lengths;
    private final int less_equals = 1;
    private final int equals_in = 2;
    private String errorMsg1;
    private String errorMsg2;


    LengthDataCheck(String dataLength){
        if(StringUtil.isNotEmpty(dataLength)){
            String[] ls = dataLength.split(",");
            lengths = new Integer[ls.length];
            errorMsg2 = "只能使用指定长度";
            for(int i=0;i<ls.length;i++){
                lengths[i] = Integer.valueOf(ls[i]);
                errorMsg2+=ls[i]+",";
            }
            if(lengths.length>0){
                length = lengths[0];
                errorMsg1 = String.format("不能超过长度%d",length);
            }
        }
    }

    /**
     * @description: 校验长度
     * @param: [data, rule]
     * @return: com.njwd.fileexcel.check.DataCheck.CheckResult
     * @author: xdy        
     * @create: 2019-06-05 16-10 
     */
    @Override
    public CheckResult check(Object data, ExcelRule rule) {
        String d = String.valueOf(data);
        switch (rule.getLengthType()){
            case less_equals:
                if(d.length()>length){
                    CheckResult checkResult = new CheckResult();
                    checkResult.setOk(false);
                    checkResult.setErrorMsg(errorMsg1);
                    return checkResult;
                }
                break;
            case equals_in:
                boolean isIn = false;
                for(Integer len:lengths){
                    if(len.equals(d.length())){
                        isIn = true;
                        break;
                    }
                }
                if(!isIn){
                    CheckResult checkResult = new CheckResult();
                    checkResult.setOk(false);
                    checkResult.setErrorMsg(errorMsg2);
                    return checkResult;
                }
                break;
        }
        return checkNext(data,rule);
    }

}
