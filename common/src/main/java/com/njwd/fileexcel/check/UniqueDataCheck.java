package com.njwd.fileexcel.check;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.excel.ExcelRule;
import com.njwd.entity.basedata.excel.ExcelTemplate;
import com.njwd.mapper.FileMapper;
import com.njwd.utils.SpringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/5 9:01
 */
public class UniqueDataCheck extends AbstractDataCheck{

    private List<Object> uniqueCodes = new ArrayList<>();
    private String errorMsg = "数据有重复";
    private FileMapper fileMapper;
    private String table;
    private Byte isLogicDel;


    UniqueDataCheck(ExcelTemplate excelTemplate){
        fileMapper = SpringUtils.getBean(FileMapper.class);
        this.table = excelTemplate.getBusinessTable();
        this.isLogicDel = excelTemplate.getIsLogicDel();
    }
    
    /**
     * @description: 检验唯一值
     * @param: [data, rule]
     * @return: com.njwd.fileexcel.check.DataCheck.CheckResult
     * @author: xdy        
     * @create: 2019-06-05 16-13 
     */
    @Override
    public CheckResult check(Object data, ExcelRule rule) {
        if(uniqueCodes.contains(data)||isDbContain(data,rule)){
            CheckResult result = new CheckResult();
            result.setOk(false);
            result.setErrorMsg(errorMsg);
            return result;
        }
        uniqueCodes.add(data);
        return checkNext(data,rule);
    }
    
    /**
     * @description: 数据库是否存在该值
     * @param: [data, rule]
     * @return: boolean 
     * @author: xdy        
     * @create: 2019-06-05 16-13 
     */
    public boolean isDbContain(Object data, ExcelRule rule){
        Wrapper w = Wrappers.query().eq(rule.getBusinessColumn(),data);
        if(Constant.Is.YES.equals(isLogicDel)) {
            ((QueryWrapper) w).eq(Constant.ColumnName.IS_DEL,Constant.Is.NO);
        }
        if(fileMapper.findBusinessCount(table,w)>0) {
            return true;
        }
        return false;
    }



}
