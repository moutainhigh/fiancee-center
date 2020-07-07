package com.njwd.basedata.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.basedata.ReferenceDescription;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Description 共通代码的service
 * @Author 朱小明
 * @Date 2019/8/15 16:00
 **/
@Service
public interface BaseCustomService {
    /**
     * @return int 数据库影响数量
     * @Description 批量操作启用禁用
     * @Author 朱小明
     * @Date 2019/8/15 20:14
     * @Param [entity:为要启用禁用表的实体类,使用DTO也可以
     * ,isEnable 1:启用 0:禁用]
     **/
    <T extends BaseModel> int batchEnable(@NotNull T entity, Byte isEnable, BaseMapper<T> mapper, List<ReferenceDescription> successDetailsList);

}
