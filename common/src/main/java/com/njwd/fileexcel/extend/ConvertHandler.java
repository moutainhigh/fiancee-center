package com.njwd.fileexcel.extend;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/21 8:52
 */
public interface ConvertHandler<T> {

    /**
     * 值转换
     * @param t
     * @return
     */
    String convert(T t);

}
