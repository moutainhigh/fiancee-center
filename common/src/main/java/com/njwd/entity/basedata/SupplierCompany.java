package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description 客户供应商子表
 * @Author 朱小明
 * @Date 2019/7/2 14:07
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wd_supplier_company")
public class SupplierCompany extends CustomerSupplierCompany {

    /**
     * 供应商表ID
     */
    private Long supplierId;
}