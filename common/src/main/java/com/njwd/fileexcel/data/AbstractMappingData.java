package com.njwd.fileexcel.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/10 16:32
 */
public abstract class AbstractMappingData implements MappingData{

    private Map<Object,Object> dataMap;
    protected String dataType;
    protected boolean reverse;

    AbstractMappingData(boolean reverse){
        this.reverse = reverse;
    }

    public AbstractMappingData() {}

    public Map<Object,Object> getData(){
        if(dataMap!=null) {
            return dataMap;
        }
        dataMap = findData();
        if(reverse) {
            reverseMap();
        }
        if(dataMap==null) {
            dataMap = new HashMap<>();
        }
        return dataMap;
    }

    /**
     * @description: 反转map的key和value
     * @param: []
     * @return: void 
     * @author: xdy        
     * @create: 2019-07-02 09-04 
     */
    public void reverseMap(){
        if(dataMap!=null){
            Map<Object,Object> map = new HashMap<>();
            Set<Object> keys = dataMap.keySet();
            for(Object key:keys){
                Object value = dataMap.get(key);
                //统一整数类型
                value = convertType(value);
                if(value!=null) {
                    map.put(value,key);
                }
            }
            dataMap = map;
        }
    }


    public abstract Map<Object,Object> findData();


    @Override
    public Object mapping(Object data) {
        data = convertType(data);
        return getData().get(data);
    }

    @Override
    public boolean contains(Object data) {
        data = convertType(data);
        return getData().keySet().contains(data);
    }
    
    /**
     * @description: 统一整数类型
     * @param: [data]
     * @return: java.lang.Object 
     * @author: xdy        
     * @create: 2019-07-02 09-14 
     */
    public Object convertType(Object data){
        if(data==null) {
            return null;
        }
        if(data instanceof  Byte||data instanceof Short||data instanceof Integer){
            data =  Long.valueOf(String.valueOf(data));
        }
        return data;
    }


}
