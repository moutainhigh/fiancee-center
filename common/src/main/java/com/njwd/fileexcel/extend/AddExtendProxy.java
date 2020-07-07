package com.njwd.fileexcel.extend;

import com.njwd.entity.basedata.excel.ExcelRowData;
import com.njwd.fileexcel.add.DataAdd;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/19 13:36
 */
public class AddExtendProxy implements DataAdd {

    private AddExtend addExtend;
    private Class dataType;
    private ToEntityHandler toEntityHandler;
    private List<String> titleList;

    AddExtendProxy(AddExtend addExtend,Class dataType){
        this.addExtend = addExtend;
        this.dataType = dataType;
    }

    @Override
    public int addBatch(List<ExcelRowData> datas) {
        return addExtend.addBatch(toBusinessEntity(datas));
    }

    @Override
    public int add(ExcelRowData data) {
        return addExtend.add(toBusinessEntity(data));
    }

    private List toBusinessEntity(List<ExcelRowData> datas){
        List businessDatas = new ArrayList();
        for(ExcelRowData data:datas){
            businessDatas.add(toBusinessEntity(data));
        }
        return businessDatas;
    }

    public void setToEntityHandler(ToEntityHandler toEntityHandler) {
        this.toEntityHandler = toEntityHandler;
    }

    private Object toBusinessEntity(ExcelRowData data){
        return ExtendUtil.toBusinessData(dataType,data,toEntityHandler,titleList);
    }

    public void setTitleList(List<String> titleList) {
        this.titleList = titleList;
    }

}
