package com.njwd.entity.ledger.dto;

import com.njwd.annotation.ExcelCell;
import com.njwd.entity.ledger.BalanceCashFlowInit;
import com.njwd.entity.ledger.vo.BalanceCashFlowInitVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author lj
 * @Description 现金流量期初初始化
 * @Date:15:21 2019/7/25
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class BalanceCashFlowInitDto extends BalanceCashFlowInitVo {
    /**
     * 公司id
     */
    private Long companyId;

    /**
     * 内部往来
     */
    private Byte isInteriorContact;

    /**
     * 0：流出、1：流入
     */
    private Byte cashFlowDirection;

    /**
     * 现金流量项目编码
     */
    @ExcelCell(index = 0)
    private String cashFlowCode;

    /**
     * 是否末级
     */
    private Byte isFinal;

    /**
     *批量插入科目信息
     **/
    private List<BalanceCashFlowInitDto> balanceCashFlowInits;
}
