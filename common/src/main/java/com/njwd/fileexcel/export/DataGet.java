package com.njwd.fileexcel.export;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/3 14:37
 */
public interface DataGet {

    Object get(Object instance, String fieldName);

    long MIN_PAGE_SIZE = 1;

    long MAX_PAGE_SIZE = 100_000L;

}
