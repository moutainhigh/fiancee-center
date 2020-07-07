package com.njwd.fileexcel.check;

import com.njwd.fileexcel.extend.CheckHandler;
import com.njwd.fileexcel.extend.ToEntityHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/20 9:09
 */
public class CheckContext {

    private Class dataType;
    private Map<String, CheckHandler> handlerMap = new HashMap<>();
    private ToEntityHandler toEntityHandler;
    private Map<String,Object> customParams = new HashMap<>();
    private Map<Integer,CheckContext> checkContextMap = new HashMap<>();
    private final String EXCEL_ROW_CHECK = "excel_row_check";
    private String fileName;

    private CheckContext getCheckContext(Integer sheetNum){
        if(sheetNum==null||sheetNum<0)
            sheetNum = 0;
        CheckContext checkContext = checkContextMap.get(sheetNum);
        if(checkContext==null){
            checkContext = new CheckContext();
            checkContextMap.put(sheetNum,checkContext);
        }
        return checkContext;
    }

    public Set<Integer> getCheckContextKeys(){
        return  checkContextMap.keySet();
    }

    private Class getDataType() {
        return dataType;
    }

    public Class getSheetDataType(Integer sheetNum){
        return getCheckContext(sheetNum).getDataType();
    }

    private void setDataType(Class dataType) {
        this.dataType = dataType;
    }

    public void setSheetDataType(Class dataType){
        setSheetDataType(1,dataType);
    }

    public void setSheetDataType(Integer sheetNum,Class dataType) {
        getCheckContext(sheetNum).setDataType(dataType);
    }

    private void addHandler(String fieldName,CheckHandler checkHandler){
        handlerMap.put(fieldName,checkHandler);
    }

    public CheckContext addSheetHandler(CheckHandler checkHandler){
        return addSheetHandler(1,EXCEL_ROW_CHECK,checkHandler);
    }

    public CheckContext addSheetHandler(String fieldName,CheckHandler checkHandler){
        return addSheetHandler(1,fieldName,checkHandler);
    }

    public CheckContext addSheetHandler(Integer sheetNum,CheckHandler checkHandler){
        return addSheetHandler(sheetNum,EXCEL_ROW_CHECK,checkHandler);
    }

    public CheckContext addSheetHandler(Integer sheetNum,String fieldName,CheckHandler checkHandler){
        getCheckContext(sheetNum).addHandler(fieldName,checkHandler);
        return this;
    }


    private Set<String> getHandlerKeys(){
        return this.handlerMap.keySet();
    }

    public Set<String> getSheetHandlerKeys(Integer sheetNum){
        return getCheckContext(sheetNum).getHandlerKeys();
    }

    private CheckHandler getHandler(String key){
        return this.handlerMap.get(key);
    }

    public CheckHandler getSheetHandler(Integer sheetNum,String key){
        return getCheckContext(sheetNum).getHandler(key);
    }

    private ToEntityHandler getToEntityHandler() {
        return toEntityHandler;
    }

    public ToEntityHandler getSheetToEntityHandler(Integer sheetNum){
        return  getCheckContext(sheetNum).getToEntityHandler();
    }

    private void setToEntityHandler(ToEntityHandler toEntityHandler) {
        this.toEntityHandler = toEntityHandler;
    }

    public void setSheetToEntityHandler(ToEntityHandler toEntityHandler){
        setSheetToEntityHandler(1,toEntityHandler);
    }

    public void setSheetToEntityHandler(Integer sheetNum,ToEntityHandler toEntityHandler) {
        getCheckContext(sheetNum).setToEntityHandler(toEntityHandler);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Object getParamValue(String name){
        if(customParams==null||name==null) {
            return null;
        }
        return customParams.get(name);
    }

    public String getStringValue(String name){
        if(getParamValue(name)==null) {
            return null;
        }
        return getParamValue(name).toString();
    }

    public Long getLongValue(String name){
        if(getStringValue(name)==null) {
            return null;
        }
        try {
            return Long.valueOf(getStringValue(name));
        }catch (Exception e){
            return null;
        }
    }

    public Integer getIntegerValue(String name){
        if(getStringValue(name)==null) {
            return null;
        }
        try {
            return Integer.valueOf(getStringValue(name));
        }catch (Exception e){
            return null;
        }
    }

    public Byte getByteValue(String name){
        if(getStringValue(name)==null) {
            return null;
        }
        try {
            return Byte.valueOf(getStringValue(name));
        }catch (Exception e){
            return null;
        }
    }

    public void setCustomParams(Map<String,Object> customParams){
        this.customParams = customParams;
    }

}
