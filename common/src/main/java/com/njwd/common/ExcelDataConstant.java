package com.njwd.common;

/**
 * @description: excel数据转换,
 *               aux_data前缀转换类型使用辅助资料数据进行转换，辅助资料数据在平台的wd_sys_aux_data表中
 *               system_data前缀转换类型使用系统内置的数据进行转换，系统内置数据在SystemData.java文件中
 * @author: xdy
 * @create: 2019/7/2 9:27
 */
public interface ExcelDataConstant {


    //=========================辅助资料数据转换=======================
    String AUX_DATA_COMPANY_TYPE = "aux_data_company_type";
    String AUX_DATA_FORM = "aux_data_form";


    //=========================系统内置数据转换=======================
    /**
     * 是否启用
     */
    String SYSTEM_DATA_IS_ENABLE = "system_data_is_enable";
    /**
     * 是否审核
     */
    String SYSTEM_DATA_IS_APPROVED= "system_data_is_approved";
    /**
     * 是否审核
     */
    String SYSTEM_DATA_IS_RELEASED= "system_data_is_released";
    /**
     * 是否删除
     */
    String SYSTEM_DATA_IS_DEL = "system_data_is_del";
    /**
     * 是否基准
     */
    String SYSTEM_DATA_IS_BASE = "system_data_is_base";
    /**
     * 余额方向
     */
    String SYSTEM_DATA_BALANCE_DIRECTION="system_data_balance_direction";
    /**
     * 发生额方向
     */
    String SYSTEM_DATA_OCCURRENCE_DIRECTION = "system_data_occurrence_direction";
    /**
     * 是否建账
     */
    String SYSTEM_DATA_IS_ACCOUNTING = "system_data_is_accounting";
    /**
     * 编码类型
     */
    String SYSTEM_DATA_CODE_TYPE = "system_data_code_type";
    /**
     * 是否选中
     */
    String SYSTEM_DATA_IS_SELECTED = "system_data_is_selected";
    /**
     * 内部往来
     */
    String SYSTEM_DATA_IS_INTERIOR = "system_data_is_interior";
    /**
     * 是否预置
     */
    String SYSTEM_DATA_IS_INIT = "system_data_is_init";
    /**
     * 是否末级
     */
    String SYSTEM_DATA_IS_FINAL = "system_data_is_final";
    /**
     * 现金流方向
     */
    String SYSTEM_DATA_CASH_FLOW_DIRECTION = "system_data_cash_flow_direction";
    /**
     * 客户类型
     */
    String SYSTEM_DATA_CUSTOMER_TYPE = "system_data_customer_type";
    /**
     * 供应商类型
     */
    String SYSTEM_DATA_SUPPLIER_TYPE = "system_data_supplier_type";
    /**
     * 是否内部客户
     */
    String SYSTEM_DATA_IS_INTERNAL_CUSTOMER = "system_data_is_internal_customer";
    /**
     * 分类
     */
    String SYSTEM_DATA_ACCOUNT_CATEGORY = "system_data_account_category";

    /**
     * 客户供应商数据类型
     */
    String SYSTEM_DATA_DATA_TYPE = "system_data_data_type";
    /**
     * 是否核算主体
     */
    String SYSTEM_DATA_IS_ACCOUNT_ENTITY = "system_data_is_account_entity";
    /**
     * 是否分账
     */
    String SYSTEM_DATA_HAS_SUB_ACCOUNT = "system_data_has_sub_account";
    /**
     * 资料类型
     */
    String SYSTEM_DATA_BASE_DATA_TYPE = "system_data_base_data_type";
    /**
     * 来源方式 0：手工、1：协同、2：损益结转、3：冲销、4：业务系统
     */
    String SYSTEM_DATA_SOURCE_TYPE = "system_data_source_type";

    /**
     * 舍入规则
     */
    String SYSTEM_DATA_ROUNDING_TYPE = "system_data_rounding_type";


    //=========================业务数据转换=======================
    String BUSINESS_DATA_COMPANY = "business_data_company";

}
