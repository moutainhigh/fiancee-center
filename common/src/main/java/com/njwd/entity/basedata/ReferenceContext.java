package com.njwd.entity.basedata;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 引用批量结果
 * @author: xdy
 * @create: 2019/6/28 9:54
 */
@Getter
@Setter
@ToString
public class ReferenceContext {

    /**
     * 未被引用的业务模块ID
     */
    private List<Long> notReferences = new ArrayList<>();
    /**
     * 未被引用的业务模块CODE
     */
    private List<String> notReferenceCodes = new ArrayList<>();
    /**
     * 被引用的业务模块ID及说明
     */
    private List<ReferenceDescription> references = new ArrayList<>();

}
