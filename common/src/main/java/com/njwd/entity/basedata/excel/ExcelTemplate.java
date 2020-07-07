package com.njwd.entity.basedata.excel;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @description: excel模板
 * @author: xdy
 * @create: 2019/5/21 9:16
 */
@Getter
@Setter
public class ExcelTemplate implements Serializable {

    private Long id;
    private String type;
    private String name;
    private String templatePath;
    private String businessTable;
    private Byte isLogicDel;
    @TableField(exist = false)
    private List<String> businessColumns;
/*    @TableField(exist = false)
    private Map<String,Object> customParams;*/

    //public static final String TEMPLATE_COMPANY = "company";
    //public static final String TEMPLATE_BUSINESS_UNIT = "business_unit";

}
