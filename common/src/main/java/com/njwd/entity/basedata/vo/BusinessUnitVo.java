package com.njwd.entity.basedata.vo;

import com.njwd.entity.basedata.BusinessUnit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author: Zhuzs
 * @Date: 2019-05-16 16:16
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BusinessUnitVo extends BusinessUnit {

    private static final long serialVersionUID = -5033417310724591345L;
    /**
     * 公司名称
     */
    private String companyName;
    /**
     * 公司编码
     */
    private String companyCode;
    /**
     * 公司是否启用分账核算  0：否、1：是
     */
    private Byte companyHasSubAccount;

    /**
     * 资料类型 1.共享型,2.分配型,3.私有型
     */
    private Byte dataTypes;
}
