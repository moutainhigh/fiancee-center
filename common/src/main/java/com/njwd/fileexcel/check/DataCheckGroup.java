package com.njwd.fileexcel.check;

import com.njwd.entity.basedata.excel.ExcelData;
import com.njwd.entity.basedata.excel.ExcelRowData;
import com.njwd.entity.basedata.excel.ExcelRule;
import com.njwd.entity.basedata.excel.ExcelTemplate;
import com.njwd.fileexcel.extend.*;
import com.njwd.utils.StringUtil;
import net.jodah.typetools.TypeResolver;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/5 9:49
 */
public class DataCheckGroup implements DataCheck{

    private Map<Long,DataCheck> dataCheckMap = new ConcurrentHashMap<>();//默认校验

    private ExcelTemplate excelTemplate;

    private CheckExtend checkExtend;

    private ExcelData excelData;

    private CheckContext checkContext;


    DataCheckGroup(ExcelData excelData){
        this.excelData = excelData;
        this.excelTemplate = excelData.getExcelTemplate();
        this.checkExtend = ExtendFactory.getCheckExtend(excelTemplate.getType());
        //初始化校验扩展
        initHandler();
    }
    

    /**
     * @description: 系统配置校验数据
     * @param: [data, rule]
     * @return: com.njwd.fileexcel.check.DataCheck.CheckResult
     * @author: xdy        
     * @create: 2019-06-05 17-38 
     */
    @Override
    public CheckResult check(Object data, ExcelRule rule) {
        CheckResult res;
        try {
            DataCheck dataCheck = findDataCheck(rule);
            res = dataCheck.check(data,rule);
        } catch (Exception e) {
            res = new CheckResult();
            res.setOk(false);
            res.setErrorMsg("系统校验异常"+e.toString());
        }
        return res;
    }
    
    /**
     * @description: 自定义校验数据
     * @param: [excelRowData]
     * @return: com.njwd.fileexcel.check.CheckResult 
     * @author: xdy        
     * @create: 2019-10-18 10:14 
     */
    public CheckResult check(ExcelRowData excelRowData){
        CheckResult res = CheckResult.ok();

            if(checkContext!=null){
                ToEntityHandler toEntityHandler = this.checkContext.getSheetToEntityHandler(excelRowData.getSheetNum());
                Class dataType = this.checkContext.getSheetDataType(excelRowData.getSheetNum());
                Object obj = ExtendUtil.toBusinessData(dataType,excelRowData,toEntityHandler,excelData.getTitleList(excelRowData.getSheetNum()));
                Set<String> keys = this.checkContext.getSheetHandlerKeys(excelRowData.getSheetNum());
                for(String key:keys){
                    try {
                        CheckHandler checkHandler = this.checkContext.getSheetHandler(excelRowData.getSheetNum(),key);
                        res = checkHandler.check(obj);
                    } catch (Exception e) {
                        e.printStackTrace();
                        res.setOk(false);
                        if(e.getMessage()!=null)
                            res.setErrorMsg(e.getMessage());
                        else
                            res.setErrorMsg("自定义校验异常"+e.toString());
                    }
                    if(!res.isOk()){
                        ExcelField excelField = ExtendUtil.findExcelField(dataType,key);
                        if(excelField!=null)
                            res.setErrorCol(excelField.getIndex());
                        break;
                    }
                }
            }
        return res;
    }
    
    /**
     * @description: 获取校验方法
     * @param: [rule]
     * @return: com.njwd.fileexcel.check.DataCheck
     * @author: xdy        
     * @create: 2019-06-05 17-39 
     */
    private DataCheck findDataCheck(ExcelRule rule){
        DataCheckChannel dataCheck = (DataCheckChannel) dataCheckMap.get(rule.getId());
        if(dataCheck==null){
            DataCheckChannel tail;
            tail = dataCheck = new EmptyDataCheck();
            switch (rule.getColType()) {
                case ExcelRule.COL_TYPE_NUMBER:
                    break;
                case ExcelRule.COL_TYPE_DATE:
                    //dataCheck.setNext(new );
                    break;
                case ExcelRule.COL_TYPE_STRING:
                default:
                    //字符串类型
                    tail = tail.setNext(new StringDataCheck());
                    //数据范围
                    if(StringUtil.isNotEmpty(rule.getDataRange())){
                        if(rule.getDataRange().equals(ExcelRule.DATA_RANGE_DIGITAL)
                            ||rule.getDataRange().equals(ExcelRule.DATA_RANGE_LETTER)
                            ||rule.getDataRange().equals(ExcelRule.DATA_RANGE_DIGITAL_LETTER)){
                            tail = tail.setNext(new TextDataCheck(rule.getDataRange()));
                        }else if(rule.getDataRange().startsWith(ExcelRule.DATA_RANGE_AUX_DATA)
                        ||rule.getDataRange().startsWith(ExcelRule.DATA_RANGE_SYSTEM_DATA)
                        ||rule.getDataRange().startsWith(ExcelRule.DATA_RANGE_BUSINESS_DATA)){
                            tail = tail.setNext(new MappingDataCheck(rule.getDataRange()));
                        }
                    }
                    //数据排除
                    if(StringUtil.isNotEmpty(rule.getDataExclude())){
                        tail = tail.setNext(new ExcludeDataCheck(rule.getDataExclude()));
                    }
                    //是否唯一
                    if(rule.getIsUnique()==ExcelRule.IS_UNIQUE_YES){
                        tail = tail.setNext(new UniqueDataCheck(this.excelTemplate));
                    }
                    //长度判断
                    if(rule.getLengthType()!=ExcelRule.LENGTH_TYPE_IGNORE){
                        tail = tail.setNext(new LengthDataCheck(rule.getDataLength()));
                    }
                    break;
            }
            dataCheckMap.put(rule.getId(),dataCheck);
        }
        return dataCheck;
    }

    /**
     * 初始化校验扩展
     */
    private void initHandler(){
        if(this.checkExtend!=null){
            this.checkContext = new CheckContext();
            checkContext.setCustomParams(this.excelData.getCustomParams());
            checkContext.setFileName(this.excelData.getFileName());
            checkExtend.check(checkContext);
            Set<Integer> sheetNums = checkContext.getCheckContextKeys();
            for(Integer sheetNum:sheetNums){
                Set<String> handlerKeys = checkContext.getSheetHandlerKeys(sheetNum);
                for(String key:handlerKeys){
                    CheckHandler checkHandler = checkContext.getSheetHandler(sheetNum,key);
                    Class<?>[] typeArgs = TypeResolver.resolveRawArguments(CheckHandler.class, checkHandler.getClass());
                    checkContext.setSheetDataType(sheetNum,typeArgs[0]);
                    break;
                }
            }
        }
    }

}
