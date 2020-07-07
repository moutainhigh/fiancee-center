package com.njwd.fileexcel.extend;

import com.njwd.fileexcel.check.CheckResult;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/20 9:11
 */
public interface CheckHandler<T> {

    /**
     * 通用规则校验成功的列数据再进行自定义规则校验
     * @param data
     * @return
     */
    CheckResult check(T data);

}
