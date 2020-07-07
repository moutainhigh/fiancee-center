package com.njwd.fileexcel.export;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.njwd.entity.base.ManagerInfo;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.fileexcel.convert.ConvertContext;
import com.njwd.fileexcel.convert.ConvertResult;
import com.njwd.fileexcel.convert.DataConvertGroup;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/3 14:39
 */
public class DataGetGroup implements DataGet {

    public Map<String,DataGet> group = new HashMap<>();
    public Map<String,DataGetField> fields = new HashMap<>();


    DataGet methodDataGet(Class classz,String fieldName){
        Method method = null;
        try {
            method = classz.getMethod("get"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1));
        } catch (NoSuchMethodException e) {
            //e.printStackTrace();
        }
        if(method==null) {
            return null;
        }
        Method finalMethod = method;
        JsonFormat jsonFormat = null;
        try {
            Type type = method.getAnnotatedReturnType().getType();
            if(type.getTypeName().equals(Date.class.getTypeName())){
                jsonFormat = finalMethod.getAnnotation(JsonFormat.class);
                if(jsonFormat==null){
                    Field field = findField(classz,fieldName);
                    if(field!=null)
                        jsonFormat = field.getAnnotation(JsonFormat.class);
                }
            }
        }catch (Exception e){
            //e.printStackTrace();
        }
        JsonFormat finalJsonFormat = jsonFormat;
        return (instance, f) -> {
            try {
                Object o = finalMethod.invoke(instance);
                if(o!=null&&o instanceof Date&&finalJsonFormat!=null){
                    try {
                        String pattern = finalJsonFormat.pattern();
                        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                        o = sdf.format(o);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                return o;
            } catch (Exception e) {

            }
            return  null;
        };
    }

    Field findField(Class classz,String fieldName){
        try {
            if(classz==null||fieldName==null)
                return null;
            return classz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return findField(classz.getSuperclass(),fieldName);
        }
    }

    DataGet fieldDataGet(Class classz,String fieldName){
        Field field = findField(classz,fieldName);
        if(field==null) {
            return null;
        }
        Field finalField = field;
        JsonFormat jsonFormat = null;
        try {
            if(finalField.getType() == Date.class)
                jsonFormat = finalField.getAnnotation(JsonFormat.class);
        }catch (Exception e){
            //e.printStackTrace();
        }
        JsonFormat finalJsonFormat = jsonFormat;
        finalField.setAccessible(true);
        return (instance, f)->{
            try {
                Object o = finalField.get(instance);
                if(o!=null&&o instanceof Date&&finalJsonFormat!=null){
                    try {
                        String pattern = finalJsonFormat.pattern();
                        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                        o = sdf.format(o);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                return o;
            } catch (IllegalAccessException e) {

            }
            return null;
        };
    }

    DataGet findDataGet(Object instance,String fieldName){
        DataGet dataGet =  group.get(fieldName);
        if(dataGet!=null){
            return dataGet;
        }
        if(StringUtils.isBlank(fieldName)){
            return  (i, f) -> null;
        }
        Class classz = instance.getClass();
        dataGet = methodDataGet(classz,fieldName);
        if(dataGet==null) {
            dataGet = fieldDataGet(classz,fieldName);
        }
        if(dataGet==null) {
            dataGet = (i, f) -> null;
        }
        group.put(fieldName,dataGet);
        return dataGet;
    }

    @Override
    public Object get(Object instance,String fieldName) {
        DataGetField dataGetField = fields.get(fieldName);
        if(dataGetField==null){
            Pattern r = Pattern.compile("([a-zA-Z]*)\\[([0-9]+)\\]$");
            Matcher m = r.matcher(fieldName);
            dataGetField = new DataGetField();
            if(m.find()){
                dataGetField.setFieldName(m.group(1));
                dataGetField.setIndex(Integer.valueOf(m.group(2)));
            }else{
                r = Pattern.compile("([a-zA-Z]*)\\.([a-zA-Z]*)");
                m = r.matcher(fieldName);
                if(m.find()){
                    dataGetField.setFieldName(m.group(1));
                    dataGetField.setKey(m.group(2));
                }else {
                    dataGetField.setFieldName(fieldName);
                }
            }
            fields.put(fieldName,dataGetField);
        }
        DataGet dataGet =  findDataGet(instance,dataGetField.getFieldName());
        Object o = dataGet.get(instance,dataGetField.getFieldName());
        if(o==null)
            return null;
        if(o instanceof Date){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            o = sdf.format(o);
        }else if(o instanceof List && dataGetField.getIndex()!=null){
            List l = (List) o;
            if(dataGetField.getIndex()<l.size())
                o = l.get(dataGetField.getIndex());
        }else if(o instanceof ManagerInfo && StringUtils.isNoneBlank(dataGetField.getKey())){
            o = get(o,dataGetField.getKey());
        }
        return  o;
    }

    public <T> List<List<Object>> get(List<T> datas, ExcelColumn[] excelColumns){
        List<ConvertContext> convertContexts  = new ArrayList<>();
        for(int i=0;i<excelColumns.length;i++){
            ExcelColumn excelColumn = excelColumns[i];
            if(StringUtils.isNoneBlank(excelColumn.getConvertType())){
                ConvertContext convertContext = new ConvertContext();
                convertContext.setIndex(i);
                convertContext.setType(excelColumn.getConvertType());
                convertContexts.add(convertContext);
            }
        }
        DataConvertGroup convertGroup=null;
        if(!convertContexts.isEmpty()){
            convertGroup = new DataConvertGroup();
        }
        List<List<Object>> result = new ArrayList<>();
        for(T data:datas){
            List<Object> row = new ArrayList<>();
            for(ExcelColumn excelColumn:excelColumns){
                row.add(get(data,excelColumn.getField()));
            }
            for(ConvertContext convertContext:convertContexts){
                ConvertResult convertResult = convertGroup.convert(convertContext.getType(),row.get(convertContext.getIndex()),true);
                if(convertResult.isOk()) {
                    row.set(convertContext.getIndex(),convertResult.getTarget());
                }
            }
            result.add(row);
        }
        return result;
    }

}
