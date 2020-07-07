package com.njwd.entity.basedata.dto;

import com.njwd.entity.basedata.CustomerSupplierCompany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;


/**
 * @author zxm
 * @date 2019/6/13
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CustomerSupplierCompanyDto extends CustomerSupplierCompany {

    /**
     * id集合
     */
    private List<Long> ids;

    /**
     * 客户表ID
     */
    private Long customerId;

    /**
     * 供应商表ID
     */
    private Long supplierId;

    /**
     * 客户id集合
     */
    private List<Long> customerIds;

    /**
     * 供应商id集合
     */
    private List<Long> supplierIds;

    /**
     * 公司id集合
     */
    private List<Long> companyIds;

}
