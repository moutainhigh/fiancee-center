package com.njwd.fileexcel.convert;

import com.njwd.fileexcel.extend.ConvertHandler;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/20 14:01
 */
@Getter
@Setter
public class ConvertContext {

    private int index;
    private String type;
    private boolean redundancy;//是否冗余
    private Class dataType;
    private Field field;
    private Map<String, ConvertHandler> beforeCheckConverts = new HashMap<>();
    private Map<String, ConvertHandler> beforeAddConverts = new HashMap<>();
    private ConvertHandler convertHandler;

    public static final int CONVERT_BEFORE_CHECK = 0;
    public static final int CONVERT_BEFORE_ADD = 1;

    public ConvertContext(){}

    public ConvertContext(int index, String type) {
        this.index = index;
        this.type = type;
    }


    public ConvertContext addHandler(String fieldName,ConvertHandler convertHandler){
        addHandler(fieldName,convertHandler,CONVERT_BEFORE_ADD);
        return this;
    }

    public void addHandler(String fieldName,ConvertHandler convertHandler,int convertTime){
        if(CONVERT_BEFORE_CHECK==convertTime){
            beforeCheckConverts.put(fieldName,convertHandler);
        }else {
            beforeAddConverts.put(fieldName,convertHandler);
        }
    }


}
