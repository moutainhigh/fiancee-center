package com.njwd.support;


import com.njwd.entity.basedata.ReferenceDescription;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @Description 批量处理返回实体
 * @Author 朱小明
 * @Date 2019/7/4 9:00
 **/
@Data
public class BatchResult {

    /**
     * 失败详情List
     **/
    private List<ReferenceDescription> failList = new ArrayList<>();

    /**
     * 成功详情List,默认为空
     **/
    private List<Long> successList = Collections.emptyList();

    private List<Long> failIdList = Collections.emptyList();
    ;

    /**
     * 成功详情List
     */
    private List<ReferenceDescription> successDetailsList = new ArrayList<>();

    private Map<Long, ?> failDict;

}
