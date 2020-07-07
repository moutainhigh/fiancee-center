package com.njwd.fileexcel.extend;

import com.njwd.fileexcel.check.CheckContext;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/20 9:03
 */
public interface CheckExtend {

    /**
     * 扩展列的校验
     * @param checkContext
     */
    void check(CheckContext checkContext);

    default boolean isSystemCheck(){
        return true;
    }

}
