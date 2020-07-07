package com.njwd.fileexcel.extend;

import com.njwd.annotation.ExcelExtend;
import com.njwd.utils.SpringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/18 14:30
 */
@Component
public class ExtendFactory implements ApplicationContextAware {

    private static final Map<String,AddExtendProxy> addExtendCache = new HashMap<>();
    private static final Map<String,CheckExtend> checkExtendCache = new HashMap<>();
    private static final Map<String,ConvertExtend> convertExtendCache = new HashMap<>();
    private static final Map<String,ColumnExtend> columnExtendCache = new HashMap<>();
    private static final Map<String,DownloadExtend> downloadExtendCache = new HashMap<>();
    private static final Map<String,AddMultiExtend> addMultiExtendCache = new HashMap<>();


    public static ColumnExtend getColumnExtend(String templateType){
        return columnExtendCache.get(templateType);
    }

    public static AddExtendProxy getAddExtendProxy(String templateType){
        return addExtendCache.get(templateType);
    }

    public static CheckExtend getCheckExtend(String templateType){
        return checkExtendCache.get(templateType);
    }

    public static ConvertExtend getConvertExtend(String templateType){return convertExtendCache.get(templateType);}

    public static DownloadExtend getDownloadExtend(String templateType){return downloadExtendCache.get(templateType);}

    public static AddMultiExtend getAddMultiExtend(String templateType){return addMultiExtendCache.get(templateType);}

    /**
     * 设置扩展对象
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String,Object> beans = applicationContext.getBeansWithAnnotation(ExcelExtend.class);
        for(Object bean:beans.values()){
            try {
                //获取代理对象原对象
                bean = SpringUtils.getTarget(bean);
            } catch (Exception e) {
                //e.printStackTrace();
            }
            Class classz = bean.getClass();
            ExcelExtend annotation = (ExcelExtend) classz.getAnnotation(ExcelExtend.class);
            String templateType = annotation.type();

            for(Type type:classz.getGenericInterfaces()){
                 if(type instanceof ParameterizedType){
                    ParameterizedType parameterizedType = (ParameterizedType) type;

                    if(parameterizedType.getTypeName().startsWith(AddExtend.class.getTypeName())){
                        Class dataType  =   (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
                        AddExtendProxy addExtendProxy = new AddExtendProxy((AddExtend) bean,dataType);
                        addExtendCache.put(templateType,addExtendProxy);
                    }

                }else{
                    if(type.getTypeName().equals(ColumnExtend.class.getTypeName())){
                        columnExtendCache.put(templateType,(ColumnExtend)bean);
                    }
                    if(type.getTypeName().equals(CheckExtend.class.getTypeName())){
                        checkExtendCache.put(templateType,(CheckExtend) bean);
                    }
                    if(type.getTypeName().equals(ConvertExtend.class.getTypeName())){
                        convertExtendCache.put(templateType, (ConvertExtend) bean);
                    }
                    if(type.getTypeName().equals(DownloadExtend.class.getTypeName())){
                        downloadExtendCache.put(templateType, (DownloadExtend) bean);
                    }
                    if(type.getTypeName().equals(AddMultiExtend.class.getTypeName())){
                        addMultiExtendCache.put(templateType,(AddMultiExtend) bean);
                    }
                }
            }
        }
    }




}
