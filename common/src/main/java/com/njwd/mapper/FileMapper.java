package com.njwd.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.njwd.entity.basedata.excel.ExcelRowData;
import com.njwd.entity.basedata.excel.ExcelRule;
import com.njwd.entity.basedata.excel.ExcelTemplate;
import com.njwd.fileexcel.add.CommonColumn;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/5/17 14:46
 */
public interface FileMapper extends BaseMapper<ExcelTemplate> {

    List<ExcelRule> findExcelRule(@Param(Constants.WRAPPER) Wrapper wrapper);

    int insertBusinessDataBatch(@Param("table") String table, @Param("columns") List<String> columns, @Param("rowDataList") List<ExcelRowData> rowDataList);

    int insertBusinessExtendDataBatch(@Param("table") String table, @Param("columns") List<String> columns, @Param("rowDataList") List<ExcelRowData> rowDataList, @Param("extendColumnList") List<CommonColumn> extendColumnList);

    int insertBusinessData(@Param("table") String table, @Param("columns") List<String> columns, @Param("rowData") ExcelRowData rowData);

    int insertBusinessExtendData(@Param("table") String table, @Param("columns") List<String> columns, @Param("rowData") ExcelRowData rowData, @Param("extendColumnList") List<CommonColumn> extendColumnList);

    int findBusinessCount(@Param("table") String table, @Param(Constants.WRAPPER) Wrapper wrapper);

}
