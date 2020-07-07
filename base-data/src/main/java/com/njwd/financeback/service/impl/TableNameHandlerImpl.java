package com.njwd.financeback.service.impl;

import com.baomidou.mybatisplus.extension.parsers.ITableNameHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Service;

/**
 * @description: 动态表名处理器
 * @author: fancl
 * @create: 2019-05-23
 */
@Service
public class TableNameHandlerImpl implements ITableNameHandler {
    /**
     * 生成动态表名
     * @param metaObject
     * @param sql
     * @param tableName
     * @return
     */
    @Override
    public String dynamicTableName(MetaObject metaObject, String sql, String tableName) {
//        String basicMonth = BaseController.getRequest().getParameter("basicMonth");
        return null;
    }
}
