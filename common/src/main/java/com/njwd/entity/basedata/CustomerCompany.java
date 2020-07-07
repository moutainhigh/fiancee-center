package com.njwd.entity.basedata;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description 客户子表
 * @Author 朱小明
 * @Date 2019/7/2 14:07
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerCompany extends CustomerSupplierCompany {
    /**
     * 客户表ID
     */
    private Long customerId;
}