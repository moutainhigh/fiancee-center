package com.njwd.entity.basedata.excel;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/14 9:30
 */
@Getter
@Setter
public class ExcelRequest {

    private String uuid;
    private String templateType;
    private String fileName;
    private Map<String,Object> customParams;
    private Long accountBookEntityId;

}
