package com.njwd.fileexcel.data;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/10 16:30
 */
public interface MappingData {

    /**
     * @description: 映射数据
     * @param: [data]
     * @return: java.lang.Object 
     * @author: xdy        
     * @create: 2019-06-10 17-21 
     */
    Object mapping(Object data);

    /**
     * @description: 是否含有某值
     * @param: [data]
     * @return: boolean 
     * @author: xdy        
     * @create: 2019-06-10 17-21 
     */
    boolean contains(Object data);

}
