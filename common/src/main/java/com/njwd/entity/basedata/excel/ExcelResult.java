package com.njwd.entity.basedata.excel;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @description: excel校验或导入的结果
 * @author: xdy
 * @create: 2019/5/20 9:04
 */
@Getter
@Setter
public class ExcelResult implements Serializable {

    /**
     * 是否全部校验或导入成功
     */
    private int isOk;
    private String message;
    private String uuid;

}
