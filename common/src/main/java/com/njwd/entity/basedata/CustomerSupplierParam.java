package com.njwd.entity.basedata;

import lombok.Data;

/**
 * @Description 调用客户供应商平台参数
 * @Date 2019/7/5 15:04
 * @Author 朱小明
 */
@Data
public class CustomerSupplierParam {
    /**
     *  创建人ID
     */
    private  String creator_id;

    /**
     * 创建人名称
     */
    private String creator_name;
    /**
     * 企业id
     */
    private String root_enterprise_id;
    /**
     * 名称
     */
    private String name;
    /**
     * 社会统一信用代码，
     */
    private String uniCode;
    /**
     * 纳税人识别号，
     */
    private String taxPayerNumber;
    /**
     * 账户名称，
     */
    private String accountName;
    /**
     * 开户行，
     */
    private String openingBank;
    /**
     * 开户账号，
     */
    private String bankAccount;
    /**
     * 公司地址，
     */
    private String address;
    /**
     * 联系电话
     */
    private String linktel;
}
