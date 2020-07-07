package com.njwd.ledger.service;

import com.njwd.entity.base.BaseModel;

import javax.validation.constraints.NotNull;

public interface BaseLedgerService {

    /**
     * @description 判断该条数据manage_info字段是否为null
     * @author fancl
     * @date 2019/8/27
     * @param 
     * @return 
     */
    <T extends BaseModel> Object judgeNull(@NotNull T entity);


    /**
     * @description 初始化json字段
     * @author fancl
     * @date 2019/8/27
     * @param
     * @return
     */
    <T extends BaseModel> int initJson(@NotNull T entity);

    /**
     * @description manage_info通用Json字段信息修改
     * @author fancl
     * @date 2019/8/26
     * @param entity 要修改的实体
     * @Param type 类型 ,标识要修改哪个属性集合
     * @return
     */
    <T extends BaseModel> int updateManageInfo(@NotNull T entity, String type);
}
