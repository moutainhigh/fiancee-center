package com.njwd.fileexcel.export;

import lombok.Getter;
import lombok.Setter;

/**
 * @description:
 * @author: xdy
 * @create: 2019/9/5 15:09
 */
@Getter
@Setter
public class DataGetField {

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 索引
     */
    private Integer index;

    /**
     * key
     */
    private String key;

}
