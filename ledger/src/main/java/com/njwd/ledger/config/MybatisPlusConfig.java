package com.njwd.ledger.config;

import com.baomidou.mybatisplus.core.parser.ISqlParser;
import com.baomidou.mybatisplus.extension.parsers.BlockAttackSqlParser;
import com.baomidou.mybatisplus.extension.parsers.DynamicTableNameParser;
import com.baomidou.mybatisplus.extension.parsers.ITableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PerformanceInterceptor;
import com.njwd.fileexcel.export.DataGet;
import com.njwd.utils.UserUtils;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置mybatisPlus插件
 *
 * @author xyyxhcj@qq.com
 * @since 2018-08-25
 */
@EnableTransactionManagement
@SpringBootConfiguration
public class MybatisPlusConfig {
    /**
     * 分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        //拦截器处理链
        List<ISqlParser> sqlParserList = new ArrayList<>();
        // 动态表名拦截器
        DynamicTableNameParser dynamicTableNameParser = new DynamicTableNameParser();
        Map<String, ITableNameHandler> tableNameHandlerMap = new LinkedHashMap<>();
        ITableNameHandler tableNameHandler = (metaObject, sql, tableName) -> {
            Long rootEnterpriseId = UserUtils.getUserVo().getRootEnterpriseId();
            return String.format(tableName, rootEnterpriseId);
        };
        tableNameHandlerMap.put("wd_voucher_%s", tableNameHandler);
        tableNameHandlerMap.put("wd_voucher_entry_%s", tableNameHandler);
        tableNameHandlerMap.put("wd_voucher_entry_auxiliary_%s", tableNameHandler);
        tableNameHandlerMap.put("wd_voucher_entry_cash_flow_%s", tableNameHandler);
        tableNameHandlerMap.put("wd_voucher_entry_interior_%s", tableNameHandler);
        dynamicTableNameParser.setTableNameHandlerMap(tableNameHandlerMap);
        sqlParserList.add(dynamicTableNameParser);
        // 加入防止全表更新删除拦截器
        sqlParserList.add(new BlockAttackSqlParser());
        paginationInterceptor.setSqlParserList(sqlParserList);
        //导出数据时 500条限制问题
        paginationInterceptor.setLimit(DataGet.MAX_PAGE_SIZE);
        return paginationInterceptor;
    }

    /***
     * 性能分析拦截器,开发环境使用
     */
    @Bean
    public PerformanceInterceptor performanceInterceptor() {
        PerformanceInterceptor performanceInterceptor = new PerformanceInterceptor();
        // SQL是否格式化 默认false
        performanceInterceptor.setFormat(true);
        return performanceInterceptor;
    }
}
