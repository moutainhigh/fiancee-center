package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.TableName;
import com.njwd.annotation.ExcelCell;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description 客户供应商
 * @Author 朱小明
 * @Date 2019/7/4 18:05
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wd_supplier")
public class Supplier extends CustomerSupplier {

    /**
     * 类型 0：企业、1：个人
     */
    @ExcelCell(index = 2)
    private Byte supplierType;

    /**
     * 内部供应商 0：否、1：是
     */
    private Byte isInternalSupplier;

    /**
     * 关联内部公司ID
     */
    private Long innerCompanyId;
}