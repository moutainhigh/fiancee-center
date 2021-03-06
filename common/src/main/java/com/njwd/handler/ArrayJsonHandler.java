package com.njwd.handler;

import com.alibaba.fastjson.JSONArray;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Description
 *  用以mysql中json格式的字段，进行转换的自定义转换器，转换为实体类的JSONArray属性
 *  MappedTypes注解中的类代表此转换器可以自动转换为的java对象
 *  MappedJdbcTypes注解中设置的是对应的jdbctype
 * @Author 朱小明
 * @Date 2019/8/15 10:15
 **/
@MappedTypes(JSONArray.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
@SuppressWarnings("unused")
public class ArrayJsonHandler extends BaseTypeHandler<JSONArray> {
    /**
     * @Description 设置非空参数
     * @Author 朱小明
     * @Date 2019/8/15 10:16
     * @Param [ps, i, parameter, jdbcType]
     * @return void
     **/
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, JSONArray parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, String.valueOf(parameter.toJSONString()));
    }
    /**
     * @Description 根据列名，获取可以为空的结果
     * @Author 朱小明
     * @Date 2019/8/15 10:16
     * @Param [rs, columnName]
     * @return com.alibaba.fastjson.JSONArray
     **/
    @Override
    public JSONArray getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String sqlJson = rs.getString(columnName);
        if (null != sqlJson){
            return JSONArray.parseArray(sqlJson);
        }
        return null;
    }
    /**
     * @Description 根据列索引，获取可以为空的结果
     * @Author 朱小明
     * @Date 2019/8/15 10:16
     * @Param [rs, columnIndex]
     * @return com.alibaba.fastjson.JSONArray
     **/
    @Override
    public JSONArray getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String sqlJson = rs.getString(columnIndex);
        if (null != sqlJson){
            return JSONArray.parseArray(sqlJson);
        }
        return null;
    }

    @Override
    public JSONArray getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String sqlJson = cs.getString(columnIndex);
        if (null != sqlJson){
            return JSONArray.parseArray(sqlJson);
        }
        return null;
    }

}