package com.njwd.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.util.JSONPObject;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Jackson工具类
 *
 * @author xyyxhcj@qq.com
 * @since 2018/2/6
 */
public class JsonUtils {
    /**
     * 定义处理JSON数据的jackson对象
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);
    public static final ObjectMapper NON_NULL_MAPPER;
    public static final ObjectMapper SNAKE_CASE_MAPPER;
    public static final ObjectMapper NON_NULL_SNAKE_CASE_MAPPER;

    static {
        enableEnumUse2String();
        NON_NULL_MAPPER = build(new ObjectMapper(), JsonInclude.Include.NON_NULL, null);
        SNAKE_CASE_MAPPER = build(new ObjectMapper(), null, PropertyNamingStrategy.SNAKE_CASE);
        NON_NULL_SNAKE_CASE_MAPPER = build(new ObjectMapper(), JsonInclude.Include.NON_NULL, PropertyNamingStrategy.SNAKE_CASE);
    }

    /**
     * 将对象转换成json字符串,可用于Map To Json
     *
     * @param data data
     * @return json
     */
    public static String object2Json(@NotNull Object data) {
        // 设置输出时包含属性的风格
        return object2Str(MAPPER, data, "write to json string error:");
    }

    /**
     * 将对象转换成json字符串,可用于Map To Json
     * 排除掉null值
     * @param data data
     * @return json
     */
    public static String object2JsonIgNull(@NotNull Object data) {
        // 设置输出时包含属性的风格
        return object2Str(NON_NULL_MAPPER, data, "write to json string error:");
    }


    /**
     * 将对象转换成文本,可用于Map To Json/xml
     * 自定义转换格式时,由外部传入已配置的mapper,防止不断创建mapper对象
     *
     * @param mapper xmlMapper->xml objectMapper->json
     * @param data   data
     * @return json
     */
    public static String object2Str(@NotNull ObjectMapper mapper, @NotNull Object data) {
        return object2Str(mapper, data, "write to string error:");
    }

    /**
     * 根据不同的ObjectMapper输出对应的文本
     *
     * @param mapper xmlMapper->xml objectMapper->json
     * @param data   data
     * @param msg    exceptionMsg
     * @return str
     */
    private static String object2Str(@NotNull ObjectMapper mapper, @NotNull Object data, String msg) {
        String jsonString = null;
        try {
            jsonString = mapper.writeValueAsString(data);
        } catch (IOException e) {
            LOGGER.warn(msg + data, e);
        }
        return jsonString;
    }

    /**
     * 根据不同的ObjectMapper实现不同格式的转换
     *
     * @param mapper   xmlMapper->xml objectMapper->json
     * @param jsonData data
     * @param msg      exceptionMsg
     * @return str
     */
    public static <T> T str2Pojo(@NotNull ObjectMapper mapper, String jsonData, Class<T> beanType, String msg) {
        if (StringUtils.isEmpty(jsonData)) {
            return null;
        }
        // 忽略字符串中存在,Java对象实际没有的属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        T result = null;
        try {
            result = mapper.readValue(jsonData, beanType);
        } catch (IOException e) {
            LOGGER.warn(msg + jsonData, e);
        }
        return result;
    }

    /**
     * 将json格式字符串转换成对象,可用于Json To Map(泛型为'String,Map')
     *
     * @param jsonData jsonData
     * @param beanType beanType
     * @param <T>      T
     * @return return
     */
    public static <T> T json2Pojo(String jsonData, Class<T> beanType) {
        return str2Pojo(MAPPER, jsonData, beanType, "parse json string error:");
    }

    /**
     * json转list
     *
     * @param jsonData jsonData
     * @param beanType beanType
     * @param <T>      List
     * @return List
     */
    public static <T> T json2List(String jsonData, Class beanType) {
        return json2JavaType(jsonData, buildCollectionType(List.class, beanType));
    }

    /**
     * 将json数据转换成pojo对象list/Map
     * 反序列化复杂Collection: List<Bean>, contructCollectionType(),contructMapType()
     * 通过buildCollectionType获取
     * 'public static <T> List<T> jsonToList(String jsonData, Class<T> beanType)'
     *
     * @param jsonData jsonData
     * @param javaType 通过buildCollectionType/buildMapType获取
     * @param <T>      T
     * @return return
     */
    public static <T> T json2JavaType(String jsonData, JavaType javaType) {
        if (StringUtils.isEmpty(jsonData)) {
            return null;
        }
        T result = null;
        try {
            result = MAPPER.readValue(jsonData, javaType);
        } catch (IOException e) {
            LOGGER.warn("parse json string error:" + jsonData, e);
        }
        return result;
    }

    /**
     * 使用Enum的toString函數读写Enum, 未设置时使用Enum的name()函數來读写Enum
     */
    public static void enableEnumUse2String() {
        MAPPER.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
    }

    /**
     * 将json格式字符串转换成对象,可用于Json To Map(泛型为'String,Bean')
     *
     * @param jsonData      jsonData
     * @param typeReference 可传入new TypeReference[LinkedHashMap[String, Bean]]() {}
     * @param <T>           T
     * @return return
     */
    public static <T> T json2MapPojo(String jsonData, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(jsonData)) {
            return null;
        }
        T t = null;
        try {
            t = MAPPER.readValue(jsonData, typeReference);
        } catch (IOException e) {
            LOGGER.warn("json to map error:" + jsonData, e);
        }
        return t;
    }

    /**
     * 构造Collection类型.
     */
    public static JavaType buildCollectionType(Class<? extends Collection> collectionClass, Class<?> elementClass) {
        return MAPPER.getTypeFactory().constructCollectionType(collectionClass, elementClass);
    }

    /**
     * 构造Map类型.
     */
    public static JavaType buildMapType(Class<? extends Map> mapClass, Class<?> keyClass, Class<?> valueClass) {
        return MAPPER.getTypeFactory().constructMapType(mapClass, keyClass, valueClass);
    }

    /**
     * 当JSON里只含有Bean的部分属性時，更新一個已存在Bean，只覆盖該部分的属性.
     */
    public static void update(String jsonData, Object object) {
        if (StringUtils.isEmpty(jsonData)) {
            return;
        }
        try {
            MAPPER.readerForUpdating(object).readValue(jsonData);
        } catch (IOException e) {
            LOGGER.warn("update json string:" + jsonData + " to object:" + object + " error.", e);
        }
    }

    /**
     * 輸出JsonP格式數據.
     */
    public static String toJsonP(String functionName, Object object) {
        return object2Json(new JSONPObject(functionName, object));
    }




    /**
     * json转为 JsonNode
     *
     * @param json json
     * @return JsonNode
     */
    public static JsonNode readTree(String json) {
        JsonNode jsonNode = null;
        try {
            jsonNode = MAPPER.readTree(json);
        } catch (IOException e) {
            LOGGER.warn("json to jsonNode error:" + json, e);
        }
        return jsonNode;
    }

    /**
     * 构建所需的mapper
     *
     * @param include                Include.NON_NULL Include.NON_EMPTY
     * @param propertyNamingStrategy convertType:PropertyNamingStrategy.SNAKE_CASE
     * @return json
     */
    public static <T extends ObjectMapper> ObjectMapper build(T mapper, JsonInclude.Include include, PropertyNamingStrategy propertyNamingStrategy) {
        // 设置输出时包含属性的风格
        if (include != null) {
            mapper.setSerializationInclusion(include);
        }
        if (propertyNamingStrategy != null) {
            mapper.setPropertyNamingStrategy(propertyNamingStrategy);
        }
        return mapper;
    }


    @Data
    public static class Result<T>{
        String status;
        Long code;
        String message;
        T data;
    }

    /**
     * platform平台数据转换为List
     * @param data
     * @param tClass
     * @param <T>
     * @return
     */
    public static  <T> List<T> platformJsonToArray(String data,Class<T> tClass){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JavaType listJavaType = mapper.getTypeFactory().constructParametricType(List.class, tClass);
        JavaType javaType = mapper.getTypeFactory().constructParametricType(Result.class,listJavaType);
        try {
            Result t = mapper.readValue(data,javaType);
            if(t.getCode().equals(200L)) {
                return (List<T>) t.getData();
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * platform平台数据转换为Object
     * @param data
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> T platformJsonToObject(String data,Class<T> tClass){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JavaType javaType = mapper.getTypeFactory().constructParametricType(Result.class,tClass);
        try {
            Result t = mapper.readValue(data,javaType);
            if(t.getCode().equals(200L)) {
                return (T) t.getData();
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
