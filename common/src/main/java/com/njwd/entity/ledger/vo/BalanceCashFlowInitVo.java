package com.njwd.entity.ledger.vo;

import com.njwd.annotation.ExcelCell;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.ledger.BalanceCashFlowInit;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author lj
 * @Description 现金流量期初初始化
 * @Date:15:21 2019/7/25
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class BalanceCashFlowInitVo extends BalanceCashFlowInit {

    /**
     * 核算账簿
     */
    private String accountBookName;

    /**
     * 是否分账核算
     */
    private Byte hasSubAccount;

    /**
     * 现金流量项目表ID
     */
    private Long cashFlowId;

    /**
     * 现金流量项目表名称
     */
    @ExcelCell(index = 1)
    private String cashFlowName;

    /**
     * 核算主体NAME
     */
    private String accountBookEntityName;

    /**
     * 核算主体CODE
     */
    private String accountBookEntityCode;

    /**
     * 核算账簿下的核算主体
     **/
    private List<AccountBookEntityVo> entityList;

    /**
     * 现金流量启用标识 0:否；1:是
     */
    private Byte cashFlowEnableStatus;
}
