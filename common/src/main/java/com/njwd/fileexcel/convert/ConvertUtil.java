package com.njwd.fileexcel.convert;

import com.njwd.common.Constant;
import com.njwd.entity.basedata.excel.ExcelCellData;
import com.njwd.entity.basedata.excel.ExcelError;
import com.njwd.entity.basedata.excel.ExcelRowData;
import com.njwd.fileexcel.extend.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/21 9:44
 */
public class ConvertUtil {

    private static final Logger logger = LoggerFactory.getLogger(ConvertUtil.class);

    public static List<ConvertContext> findExtendConverts(String templateType,int convertTime){
        List<ConvertContext> convertContextList = new ArrayList<>();
        ConvertExtend convertExtend =  ExtendFactory.getConvertExtend(templateType);
        if(convertExtend!=null){
            ConvertContext convertContext = new ConvertContext();
            convertExtend.convert(convertContext);
            Map<String, ConvertHandler> convertHandlerMap = null;
            if(convertTime==ConvertContext.CONVERT_BEFORE_CHECK) {
                convertHandlerMap = convertContext.getBeforeCheckConverts();
            } else if(convertTime==ConvertContext.CONVERT_BEFORE_ADD) {
                convertHandlerMap = convertContext.getBeforeAddConverts();
            }

            if(convertContext.getDataType()!=null
                    &&convertHandlerMap!=null
                    &&!convertHandlerMap.isEmpty()){
                Class dataType = convertContext.getDataType();
                Set<String> keys = convertHandlerMap.keySet();
                for(String key:keys){
                    ExcelField excelField = ExtendUtil.findExcelField(dataType,key);
                    if(excelField!=null){
                        ConvertContext context = new ConvertContext();
                        context.setDataType(dataType);
                        context.setIndex(excelField.getIndex());
                        context.setConvertHandler(convertHandlerMap.get(key));
                        convertContextList.add(context);
                    }
                }
            }
        }
        return convertContextList;
    }

    public static ExcelRowData convertData(List<ConvertContext> convertContexts, ExcelRowData excelRowData, List<ExcelError> errorList){
        ExcelRowData result = excelRowData;
        ExcelCellData cellData = null;
        try {
            if(convertContexts!=null&&!convertContexts.isEmpty()&&excelRowData!=null){
                Object data = ExtendUtil.toBusinessData(convertContexts.get(0).getDataType(),excelRowData,null,null);
                for(ConvertContext convertContext:convertContexts){
                    int index = convertContext.getIndex();
                    cellData = excelRowData.getExcelCellDataList().get(index);
                    String target = convertContext.getConvertHandler().convert(data);
                    cellData.setConverted(true);
                    cellData.setOldData(cellData.getData());
                    cellData.setData(target);
                    if(excelRowData.getBusinessData()!=null){
                        ExtendUtil.setFieldData(convertContext.getField(),excelRowData.getBusinessData(),target);
                    }
                }
            }
        }catch (Exception e){
            result = null;
            if(errorList!=null){
                ExcelError excelError = new ExcelError();
                excelError.setRowNum(excelRowData.getRowNum());
                if(cellData!=null){
                    excelError.setCellNum(cellData.getCellNum());
                    excelError.setData(cellData.getImportData());
                }
                excelError.setErrorMsg(String.format(Constant.ExcelErrorMsg.ERROR_CONVERT,"扩展异常"));
                errorList.add(excelError);
            }
            logger.error("值转换失败:{}",excelRowData);
        }
        return result;
    }

}
