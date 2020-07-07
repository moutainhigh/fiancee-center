package com.njwd.entity.basedata;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @description: 被引用校验结果
 * @author: xdy
 * @create: 2019/6/27 17:04
 */
@Getter
@Setter
@ToString
public class ReferenceResult {

    /**
     * 是否被引用
     */
    private boolean reference;
    /**
     * 被引用说明
     */
    private String referenceDescription;

}
