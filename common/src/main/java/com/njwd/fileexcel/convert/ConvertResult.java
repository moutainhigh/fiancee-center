package com.njwd.fileexcel.convert;

import lombok.Getter;
import lombok.Setter;

/**
 * @description:
 * @author: xdy
 * @create: 2019/7/2 11:58
 */
@Getter
@Setter
public class ConvertResult {

    /**
     * 是否成功
     */
    private boolean ok;
    /**
     * 源值
     */
    private Object source;
    /**
     * 目标值
     */
    private Object target;
    /**
     * 错误信息
     */
    private String errorMsg;

}
