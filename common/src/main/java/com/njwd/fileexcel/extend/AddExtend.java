package com.njwd.fileexcel.extend;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/18 11:51
 */
public interface AddExtend<T> {

    /**
     * 批量录入
     * @param datas
     * @return
     */
    int addBatch(List<T> datas);

    /**
     * 逐条录入
     * @param data
     * @return
     */
    int add(T data);

    /**
     * @description: 是否分多批次，false全部一次性录入
     * @param: []
     * @return: boolean
     * @author: xdy
     * @create: 2019-10-23 10:49
     */
    default boolean isMultiBatch(){
        return true;
    }

}
