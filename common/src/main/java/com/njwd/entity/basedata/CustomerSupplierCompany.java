package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.TableField;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description 客户供应商子表
 * @Author 朱小明
 * @Date 2019/7/2 14:07
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerSupplierCompany extends BaseModel {

    /**
     * 归属公司 【公司】表ID
     */
    private Long companyId;

    /**
     * 操作信息:禁用人,禁用时间等
     */
    private ManagerInfo manageInfo;

    /**
     * 启用标识
     */
    private Byte isEnable;

    private static final long serialVersionUID = 1L;
}