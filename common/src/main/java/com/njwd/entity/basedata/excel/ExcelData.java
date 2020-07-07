package com.njwd.entity.basedata.excel;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

@Getter
@Setter
public class ExcelData implements Serializable {

    private String fileName;
    private boolean systemCheck;
    private List<ExcelRowData> excelRowDataList;
    private List<ExcelError> excelErrorList;
    private ExcelTemplate excelTemplate;
    private List<ExcelRule> excelRuleList;
    private Map<String,Object> customParams;
    private boolean multiSheet;
    private Map<Integer,List<String>> titleListMap = new HashMap<>();

    public void addTitleList(Integer sheetNum,List<String> titleList){
        this.titleListMap.put(sheetNum,titleList);
    }

    public List<String> getTitleList(Integer sheetNum){
        return this.titleListMap.get(sheetNum);
    }

    public Map<Integer,List<String>> getTitleListMap(){
        return this.titleListMap;
    }


}
