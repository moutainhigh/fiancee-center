package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.TableName;
import com.njwd.annotation.ExcelCell;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description 客户
 * @Author 朱小明
 * @Date 2019/8/12 18:55
 **/
@Getter
@Setter
@TableName("wd_customer")
public class Customer extends CustomerSupplier {

    /**
     * 类型 0：企业、1：个人
     */
    @ExcelCell(index = 2)
    private Byte customerType;

    /**
     * 内部客户 0：否、1：是
     */
    private Byte isInternalCustomer;

    /**
     * 关联内部公司ID
     */
    private Long innerCompanyId;
}