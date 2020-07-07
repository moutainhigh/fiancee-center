package com.njwd.fileexcel.add;

import com.njwd.entity.basedata.excel.ExcelRowData;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.fileexcel.extend.ColumnExtend;
import com.njwd.fileexcel.extend.ExtendFactory;
import com.njwd.mapper.FileMapper;
import com.njwd.utils.SpringUtils;
import com.njwd.utils.UserUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/6 12:00
 */
public class SingleTableDataAdd implements DataAdd{

    private List<CommonColumn> commonColumns = new ArrayList<>();

    private FileMapper fileMapper;

    private String table;

    private List<String> columns;

    private DataAdd dataAdd;

    private ColumnExtend columnExtend;

    private boolean isExtendColumn = false;

    SingleTableDataAdd(String templateType,String businessTable,List<String> businessColumns){
        table = businessTable;
        columns = businessColumns;
        SysUserVo userVo = UserUtils.getUserVo();
        /**SysUserVo userVo = new SysUserVo();
        userVo.setUserId(-1L);
        userVo.setName("游客");
        userVo.setRootEnterpriseId(-1L);**/
        Date currentDate = new Date();
        commonColumns.add(new CommonColumn("creator_id",userVo.getUserId()));
        commonColumns.add(new CommonColumn("creator_name",userVo.getName()));
        commonColumns.add(new CommonColumn("create_time",currentDate));
        columnExtend = ExtendFactory.getColumnExtend(templateType);
        if(columnExtend!=null){
            columnExtend.getCommonColumn(commonColumns);
            //扩展字段
            if(columnExtend.getExtendColumn()!=null&&!columnExtend.getExtendColumn().isEmpty()){
                isExtendColumn = true;
                columns.addAll(columnExtend.getExtendColumn());
            }
        }
        this.fileMapper = SpringUtils.getBean(FileMapper.class);
        this.dataAdd = getDataAdd();
    }

    @Override
    public int addBatch(List<ExcelRowData> rowDataList) {
        //扩展数据
        extendData(rowDataList);
        return dataAdd.addBatch(rowDataList);
    }

    @Override
    public int add(ExcelRowData rowData) {
        return dataAdd.add(rowData);
    }

    private DataAdd getDataAdd(){
        if(commonColumns.isEmpty()) {
            return getDefaultDataAdd();
        } else {
            return getExtendDataAdd();
        }
    }

    private void extendData(List<ExcelRowData> rowDataList){
        if(!isExtendColumn
                ||columnExtend==null
                ||rowDataList.size()==0) {
            return;
        }
        if(columns.size()==rowDataList.get(0).getExcelCellDataList().size()) {
            return ;
        }
        columnExtend.extendData(rowDataList);
    }

    private DataAdd getDefaultDataAdd(){
        return new DataAdd() {
            @Override
            public int addBatch(List<ExcelRowData> rowDataList) {
                return fileMapper.insertBusinessDataBatch(table,columns,rowDataList);
            }

            @Override
            public int add(ExcelRowData rowData) {
                return fileMapper.insertBusinessData(table,columns,rowData);
            }
        };
    }

    private DataAdd getExtendDataAdd(){
        return new DataAdd() {
            @Override
            public int addBatch(List<ExcelRowData> rowDataList) {
                return fileMapper.insertBusinessExtendDataBatch(table,columns,rowDataList,commonColumns);
            }

            @Override
            public int add(ExcelRowData rowData) {
                return fileMapper.insertBusinessExtendData(table,columns,rowData,commonColumns);
            }
        };
    }

}
