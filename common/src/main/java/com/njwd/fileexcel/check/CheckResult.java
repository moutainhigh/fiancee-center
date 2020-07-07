package com.njwd.fileexcel.check;

import lombok.Getter;
import lombok.Setter;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/20 9:04
 */
@Getter
@Setter
public class CheckResult {

    /**
     * 校验结果标识
     */
    private boolean ok = false;
    /**
     * 错误原因
     */
    private String errorMsg;
    /**
     * 错误的列
     */
    private int errorCol = -1;

    public static CheckResult ok(){
        CheckResult checkResult = new CheckResult();
        checkResult.setOk(true);
        return  checkResult;
    }

    public static CheckResult error(String errorMsg){
        CheckResult checkResult = new CheckResult();
        checkResult.setOk(false);
        checkResult.setErrorMsg(errorMsg);
        return  checkResult;
    }

}
