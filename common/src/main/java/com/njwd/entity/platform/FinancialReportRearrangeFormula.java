package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FinancialReportRearrangeFormula implements Serializable {
    /**
    * 主键 默认自动递增
    */
    private Long id;

    /**
    * 报告项目库ID  
    */
    private Long itemId;

    /**
    * 对方报告项目库ID
    */
    private Long othersideItemId;

    /**
    * 公式科目编码 科目
    */
    private String formulaItemCode;

    /**
    * 对方公式科目编码 对方科目
    */
    private String othersideFormulaItemCode;

    /**
     * 运算标识 0：加、1：减
     */
    private Byte operator;

    /**
     * 是否对方科目 0：否，1：是
     */
    private Byte isOther;

    /**
    * 创建时间
    */
    private Date createTime;

}