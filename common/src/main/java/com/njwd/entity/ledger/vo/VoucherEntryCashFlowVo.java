package com.njwd.entity.ledger.vo;

import com.njwd.entity.ledger.VoucherEntryCashFlow;
import lombok.Getter;
import lombok.Setter;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/07/30
 */
@Getter
@Setter
public class VoucherEntryCashFlowVo extends VoucherEntryCashFlow {
    private static final long serialVersionUID = -1186965226867481830L;

    /**
     * 现金流量编码
     */
    private String cashFlowCode;

    /**
     * 现金流量名称
     */
    private String cashFlowName;

    /**
     * 现金流量全名
     */
    private String cashFlowFullName;

    /**
     * 0：流出、1：流入
     */
    private Byte cashFlowDirection;
}
