package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.base.BaseModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description 共通Mapper
 * @Author 朱小明
 * @Date 2019/8/15 20:17
 **/
public interface BaseCustomMapper<T extends BaseModel> extends BaseMapper<T> {

    /**
     * @Description 批量启用
     * @Author 朱小明
     * @Date 2019/8/15 20:17
     * @Param [entity, list]
     * @return int
     **/
    int batchEnabled(@Param("entity")T entity, @Param("manageList")List<Object> list);

    /**
     * @Description 批量禁用
     * @Author 朱小明
     * @Date 2019/8/15 20:17
     * @Param [entity, list]
     * @return int
     **/
    int batchDisabled(@Param("entity")T entity, @Param("manageList")List<Object> list);
}
