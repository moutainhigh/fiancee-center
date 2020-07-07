package com.njwd.entity.basedata.vo;

import com.njwd.entity.basedata.AccountBookSystem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 账簿启用子系统记录
 *
 * @Author: Zhuzs
 * @Date: 2019-06-21 11:30
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AccountBookSystemVo extends AccountBookSystem {
    private static final long serialVersionUID = -1914689243229524275L;

    /**
     * code
     */
    private String code;

    /**
     * 公司id
     */
    private String companyId;

}
