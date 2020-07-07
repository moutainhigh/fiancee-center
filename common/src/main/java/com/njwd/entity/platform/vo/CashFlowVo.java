package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.CashFlow;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author lj
 * @Description 现金流量项目表
 * @Date:15:36 2019/6/12
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class CashFlowVo extends CashFlow {
    private static final long serialVersionUID = -1745577565311286888L;

    /**
     * 会计准则名称
     */
    private String accStandardName;
    /**
     * 账簿类型名称
     */
    private String accountTypeName;
    /**
     * 归属现金流量项目表
     */
    private String parentName;

    /**
     * 会计准则编码
     */
    private String accStandardCode;
}
