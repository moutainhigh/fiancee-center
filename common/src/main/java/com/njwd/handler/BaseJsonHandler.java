package com.njwd.handler;

import com.njwd.entity.base.ManagerInfo;
import com.njwd.utils.JsonUtils;
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
 *  用以mysql中json格式的字段，进行转换的自定义转换器，转换为实体类的JSONObject属性
 *  MappedTypes注解中的类代表此转换器可以自动转换为的java对象
 *  MappedJdbcTypes注解中设置的是对应的jdbctype
 * @Author 朱小明
 * @Date 2019/8/15 10:12
 **/

@MappedTypes(ManagerInfo.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class BaseJsonHandler extends BaseTypeHandler<ManagerInfo>{

    /**
     * @Description 设置非空参数
     * @Author 朱小明
     * @Date 2019/8/15 10:13
     * @Param [ps, i, parameter, jdbcType]
     * @return void
     **/
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ManagerInfo parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, JsonUtils.object2JsonIgNull(parameter));
    }

    /**
     * @Description 根据列名，获取可以为空的结果
     * @Author 朱小明
     * @Date 2019/8/15 10:14
     * @Param [rs, columnName]
     * @return com.alibaba.fastjson.JSONObject
     **/
    @Override
    public ManagerInfo getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String sqlJson = rs.getString(columnName);
        if (null != sqlJson){
            return JsonUtils.json2Pojo(sqlJson, ManagerInfo.class);
        }
        return null;
    }

    /**
     * @Description 根据列索引，获取可以为空的结果
     * @Author 朱小明
     * @Date 2019/8/15 10:14
     * @Param [rs, columnIndex]
     * @return com.alibaba.fastjson.JSONObject
     **/
    @Override
    public ManagerInfo getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String sqlJson = rs.getString(columnIndex);
        if (null != sqlJson){
            return JsonUtils.json2Pojo(sqlJson, ManagerInfo.class);
        }
        return null;
    }

    @Override
    public ManagerInfo getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String sqlJson = cs.getString(columnIndex);
        if (null != sqlJson){
            return JsonUtils.json2Pojo(sqlJson, ManagerInfo.class);
        }
        return null;
    }
}