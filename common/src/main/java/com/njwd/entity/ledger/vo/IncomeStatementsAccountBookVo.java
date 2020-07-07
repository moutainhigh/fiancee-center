package com.njwd.entity.ledger.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
* @description: 核算账簿信息
* @author LuoY
* @date 2019/8/8 11:22
*/
@Getter
@Setter
public class IncomeStatementsAccountBookVo {
    /**
     * 核算账簿ID
     */
    private Long accountBookId;

    /**
     * 账簿名称
     */
    private String accountBookName;

    /**
     * 核算账簿对应的公司id
     */
    private Long companyId;

    /**
     * 基准科目表id
     */
    private Long subjectId;

    /**
     * 币种id
     */
    private Long currencyId;

    /**
     * 币种名称
     */
    private String currencyName;

    /**
     *  核算主体
     */
    private List<IncomeStatementsAccountEntityVo> incomeStatementsAccountEntityVos;
}
