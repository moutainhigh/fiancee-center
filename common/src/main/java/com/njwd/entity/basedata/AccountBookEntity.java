package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.TableField;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 核算主体
 *
 * @Author: Zhuzs
 * @Date: 2019-06-05 11:21
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountBookEntity extends BaseModel {
    private static final long serialVersionUID = -2007067579817436854L;

    /**
     * 企业ID
     */
    @NotNull
    private Long rootEnterpriseId;

    /**
     * 核算账簿ID
     */
    private Long accountBookId;

    /**
     * 核算账簿
     */
    private String accountBookName;

    /**
     * 核算主体类型
     */
    private Long form;

    /**
     * 核算主体ID（主体信息来源：公司/业务单元）
     */
    private Long entityId;

    /**
     * 操作信息:禁用人,禁用时间等
     */
    @TableField(exist = false)
    private ManagerInfo manageInfo;

}
