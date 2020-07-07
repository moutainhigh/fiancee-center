package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.base.BaseModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description 总账修改manage_info共通Mapper
 * @Author fancl
 * @Date 2019/8/15 20:17
 **/
public interface BaseLedgerMapper<T extends BaseModel> extends BaseMapper<T> {


    /**
     * @description 判断某条数据manage_info是否为Null
     * @author fancl
     * @date 2019/8/27
     * @param
     * @return
     */
    Object judgeNull(@Param("entity") T entity);


    int initJson(@Param("entity") T entity);


    /**
     * @description manage_info通用信息修改
     * @author fancl
     * @date 2019/8/23
     * @param
     * @return
     */
    int updateManageInfo(@Param("entity") T entity, @Param("manageList") List<Object> list);
}
