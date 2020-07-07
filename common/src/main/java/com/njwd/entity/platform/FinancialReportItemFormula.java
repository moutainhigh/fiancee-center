package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author liuxiang
 * @Description 财务报告明细公式
 * @Date:17:05 2019/8/1
 **/
@Data
public class FinancialReportItemFormula implements Serializable {
    private static final long serialVersionUID = 9044433156912170958L;
    /**
     * 主键 默认自动递增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 明细项目ID 【财务报告明细表】表ID
     */
    @TableField(value = "item_set_id")
    private Long itemSetId;

    /**
     * 公式类型 0：科目或项目、1：项目行
     */
    @TableField(value = "formula_type")
    private Byte formulaType;

    /**
     * 公式项目编码 科目、现金流量项目、报告项目
     */
    @TableField(value = "formula_item_code")
    private String formulaItemCode;

    /**
     * 运算标识 0：加、1：减
     */
    @TableField(value = "operator")
    private Byte operator;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;
}