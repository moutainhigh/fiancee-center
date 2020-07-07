package com.njwd.fileexcel.extend;

import com.njwd.annotation.ExcelCell;
import com.njwd.entity.basedata.excel.ExcelCellData;
import com.njwd.entity.basedata.excel.ExcelRowData;
import com.njwd.entity.basedata.excel.ExcelRule;
import com.njwd.fileexcel.convert.ConvertResult;
import com.njwd.fileexcel.convert.DataConvertGroup;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/11 17:21
 */
public class ExtendUtil {

    DataConvertGroup dataConvertGroup = new DataConvertGroup();
    Map<String,Integer> indexMap = new ConcurrentHashMap<>();
    Map<String,String> convertTypeMap = new ConcurrentHashMap<>();

    private static final Map<String,List<ExcelField>> excelFieldMap = new ConcurrentHashMap<>();
    
    /**
     * @description: 获取某列值
     * @param: [excelRowData, rules, columnName]
     * @return: java.lang.Object 
     * @author: xdy        
     * @create: 2019-06-11 17-50 
     */
    public Object getValue(ExcelRowData excelRowData, List<ExcelRule> rules,String columnName){
        Integer index = findIndex(rules,columnName);
        if(index.equals(-1)) {
            return null;
        }
        Object result = excelRowData.getExcelCellDataList().get(index).getData();
        String convertType = convertTypeMap.get(columnName);
        if(convertType!=null){
            ConvertResult convertResult = dataConvertGroup.convert(convertType,result);
            if(convertResult.isOk()) {
                result = convertResult.getTarget();
            }else {
                result = null;
            }
        }
        return result;
    }

    /**
     * @description: 获取列索引
     * @param: [rules, columnName]
     * @return: java.lang.Integer 
     * @author: xdy        
     * @create: 2019-06-11 17-50 
     */
    private Integer findIndex(List<ExcelRule> rules,String columnName){
        Integer index = indexMap.get(columnName);
        if(index==null){
            index = -1;
            String convertType = null;
            for(int i=0;i<rules.size();i++){
                ExcelRule rule = rules.get(i);
                if(rule.getBusinessColumn().equals(columnName)){
                    index = i;
                    convertType =rule.getConvertType();
                    break;
                }
            }
            indexMap.put(columnName,index);
            convertTypeMap.put(columnName,convertType);
        }
        return index;
    }

    /**
     * ExcelRowData 转为业务实体
     * @param dataType
     * @param excelRowData
     * @return
     */
    public static Object toBusinessData(Class dataType,ExcelRowData excelRowData,ToEntityHandler toEntityHandler,List<String> titleList){
        Object obj = excelRowData.getBusinessData();
        if(obj!=null) {
            return obj;
        }
        if(toEntityHandler!=null){
            obj = toEntityHandler.toEntity(excelRowData,titleList);
            excelRowData.setBusinessData(obj);
            return obj;
        }
        if(dataType==null)
            return null;
        try {
            obj =  dataType.newInstance();
            List<ExcelField> fields = findExcelFields(dataType);
            for(ExcelField excelField:fields){
                ExcelCellData cellData = excelRowData.getExcelCellDataList().get(excelField.getIndex());

                if(!excelField.isRedundancy()) {
                    setFieldData(excelField.getField(),obj,cellData.getData());
                } else {
                    setFieldData(excelField.getField(),obj,cellData.getOldData());
                }

            }
            excelRowData.setBusinessData(obj);
        } catch (Exception e) {

        }
        return obj;
    }

    /**
     * 字段赋值
     * @param field
     * @param obj
     * @param data
     */
    public static void setFieldData(Field field,Object obj,Object data){
        try {
            if(field.getType().getTypeName().equals(data.getClass().getTypeName())){
                field.set(obj,data);
            }else{
                if(field.getType().getTypeName().equals(String.class.getTypeName())){
                    field.set(obj,String.valueOf(data));
                }else if(field.getType().getTypeName().equals(Long.class.getTypeName())){
                    field.set(obj,Long.valueOf(String.valueOf(data)));
                }else if(field.getType().getTypeName().equals(Byte.class.getTypeName())){
                    field.set(obj,Byte.valueOf(String.valueOf(data)));
                }else if(field.getType().getTypeName().equals(Double.class.getTypeName())){
                    field.set(obj,Double.valueOf(String.valueOf(data)));
                }else if(field.getType().getTypeName().equals(BigDecimal.class.getTypeName())){
                    field.set(obj,new BigDecimal(String.valueOf(data)));
                }
            }
        }catch (Exception e){

        }
    }

    /**
     * 获取excel字段
     * @param dataType
     * @return
     */
    public static List<ExcelField> findExcelFields(Class dataType){
        String typeName = dataType.getTypeName();
        List<ExcelField> excelFields = excelFieldMap.get(typeName);
        if(excelFields!=null) {
            return excelFields;
        }
        excelFields = new ArrayList<>();
        while (dataType!=null){
            Field[] fields = dataType.getDeclaredFields();
            for(Field field:fields){
                ExcelCell excelCell = field.getAnnotation(ExcelCell.class);
                if(excelCell!=null){
                    field.setAccessible(true);
                    ExcelField excelField = new ExcelField();
                    excelField.setField(field);
                    excelField.setIndex(excelCell.index());
                    excelField.setRedundancy(excelCell.redundancy());
                    excelFields.add(excelField);
                }
            }
            dataType = dataType.getSuperclass();
        }
        excelFieldMap.put(typeName,excelFields);
        return  excelFields;
    }

    /**
     * 获取excel字段
     * @param dataType
     * @param fieldName
     * @return
     */
    public static ExcelField findExcelField(Class dataType,String fieldName){
        if(dataType==null)
            return null;
        List<ExcelField> fields = findExcelFields(dataType);
        ExcelField excelField = null;
        if(fields!=null){
            for(ExcelField field:fields){
                if(field.getField().getName().equals(fieldName)){
                    excelField = field;
                    break;
                }
            }
        }
        return excelField;
    }

}
