package com.njwd.entity.basedata.vo;

import com.njwd.entity.basedata.AccountingItemValue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Description 自定义核算项目大区值Vo 返回数据用
 * @Author 薛永利
 * @Date 2019/6/26 14:23
 */
@Data
@ToString(callSuper =  true)
@EqualsAndHashCode(callSuper = true)
public class AccountingItemValueVo extends AccountingItemValue {

    private static final long serialVersionUID = 1265759534142256347L;
    /**
     * 数据状态
     */
    private String statusName;
    /**
     * 归属公司
     */
    private String companyName;
    /**
     * 使用公司
     */
    private String useCompanyName;

}
