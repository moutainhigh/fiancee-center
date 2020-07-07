package com.njwd.entity.basedata.vo;

import com.njwd.annotation.ExcelCell;
import com.njwd.entity.basedata.CustomerSupplier;
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
public class CustomerSupplierVo extends CustomerSupplier {

    /**
     *  被引用的详情
     **/
    private List<CustomerSupplier> citeList;

    /**
     * 删除条数
     **/
    private int delCount;

    /**
     * 是否被引用 0：否，1：是
     */
    private Byte isCite;

    /**
     * 是否内部客户 0：否、1：是
     */
    private String isInternalCustomer;

//    public String getIsInternalCustomer(){
//        if (Constant.CustomerSupplier.IS_INTERNAL_YES.equals(isInternalCustomer)){
//            return Constant.CustomerSupplier.IS_INTERNAL_YES_NAME;
//        } else {
//            return Constant.CustomerSupplier.IS_INTERNAL_NO_NAME;
//        }
//    }

    /**
     * 是否内部客户 0：否、1：是
     */
    private String isInternalCustomerName;

//    public String getIsInternalCustomerName(){
//        if (Constant.CustomerSupplier.IS_INTERNAL_YES.equals(isInternalCustomer)){
//            return Constant.CustomerSupplier.IS_INTERNAL_YES_NAME;
//        } else {
//            return Constant.CustomerSupplier.IS_INTERNAL_NO_NAME;
//        }
//    }

    /**
     * 是否内部供应商 0：否、1：是
     */
    private String isInternalSupplier;

//    public String getIsInternalSupplier(){
//        if (Constant.CustomerSupplier.IS_INTERNAL_YES.equals(isInternalCustomer)){
//            return Constant.CustomerSupplier.IS_INTERNAL_YES_NAME;
//        } else {
//            return Constant.CustomerSupplier.IS_INTERNAL_NO_NAME;
//        }
//    }

    /**
     * 是否内部供应商 0：否、1：是
     */
    private String isInternalSupplierName;

//    public String getIsInternalSupplierName(){
//        if (Constant.CustomerSupplier.IS_INTERNAL_YES.equals(isInternalSupplier)){
//            return Constant.CustomerSupplier.IS_INTERNAL_YES_NAME;
//        } else {
//            return Constant.CustomerSupplier.IS_INTERNAL_NO_NAME;
//        }
//    }

    /**
     * 类型   0：企业、1：个人
     */
    @ExcelCell(index = 1)
    private String customerType;

//    public String getCustomerType(){
//        if (Constant.CustomerSupplier.ENTERPRISE.equals(customerType)){
//            return Constant.CustomerSupplier.ENTERPRISE_NAME;
//        } else {
//            return Constant.CustomerSupplier.PERSONAL_NAME;
//        }
//    }

    /**
     * 类型   0：企业、1：个人
     */
    private String customerTypeName;

//    public String getCustomerTypeName(){
//        if (Constant.CustomerSupplier.ENTERPRISE.equals(customerType)){
//            return Constant.CustomerSupplier.ENTERPRISE_NAME;
//        } else {
//            return Constant.CustomerSupplier.PERSONAL_NAME;
//        }
//    }

    /**
     * 类型   0：企业、1：个人
     */
    @ExcelCell(index = 1)
    private String supplierType;

//    public String getSupplierType(){
//        if (Constant.CustomerSupplier.ENTERPRISE.equals(supplierType)){
//            return Constant.CustomerSupplier.ENTERPRISE_NAME;
//        } else {
//            return Constant.CustomerSupplier.PERSONAL_NAME;
//        }
//    }

    /**
     * 类型   0：企业、1：个人
     */
    private String supplierTypeName;

//    public String getSupplierTypeName(){
//        if (Constant.CustomerSupplier.ENTERPRISE.equals(supplierType)){
//            return Constant.CustomerSupplier.ENTERPRISE_NAME;
//        } else {
//            return Constant.CustomerSupplier.PERSONAL_NAME;
//        }
//    }

    /**
     *  被引用条数

     **/
    private int citeCount;

    /**
     *  公司名称
     **/
    @ExcelCell(index = 0)
    private String companyName;


    /**
     *  使用公司名称

     **/
    private String useCompanyName;

    /**
     *  使用公司ID

     **/
    private Long useCompanyId;

    private List<CustomerSupplierCompany> customerSupplierCompanyList;

    /**
     * 关联内部公司ID
     */
    private Long innerCompanyId;

    /**
     * 关联内部公司id名称
     */
    private String innerCompanyName;

    /**
     * 关联内部公司ID集合
     */
    private List<Long> innerCompanyIds;

    /**
     * 使用公司ids
     */
    private String useCompanyIdString;

}
