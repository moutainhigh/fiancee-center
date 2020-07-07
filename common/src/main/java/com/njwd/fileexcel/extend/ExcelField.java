package com.njwd.fileexcel.extend;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/19 14:37
 */
@Getter
@Setter
public class ExcelField {

    private int index;
    private boolean redundancy;
    private Field field;

}
