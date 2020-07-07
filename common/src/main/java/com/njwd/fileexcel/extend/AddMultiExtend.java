package com.njwd.fileexcel.extend;

/**
 * @description:
 * @author: xdy
 * @create: 2019/10/18 17:03
 */
public interface AddMultiExtend {

    void add(AddContext addContext);

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
