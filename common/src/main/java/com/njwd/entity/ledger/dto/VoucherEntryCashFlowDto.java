package com.njwd.entity.ledger.dto;

import com.njwd.entity.ledger.vo.VoucherEntryCashFlowVo;
import lombok.Getter;
import lombok.Setter;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/07/30
 */
@Getter
@Setter
public class VoucherEntryCashFlowDto extends VoucherEntryCashFlowVo {
    private static final long serialVersionUID = 2229496348204240607L;
    /**
     * 分析的对方分录的序号
     **/
    private Integer oppositeRowNum;
    /**
     * 是否修改 1是 0否
     */
    private byte isModify;

    /**
     * 关联分录
     **/
    private VoucherEntryDto entry;

    /**
     * 关联对方分录
     **/
    private VoucherEntryDto oppositeEntry;
}
