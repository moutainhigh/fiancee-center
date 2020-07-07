package com.njwd.entity.ledger;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;


/**
 * @description:
 * @author: xdy
 * @create: 2019/10/21 15:04
 */
@Getter
@Setter
public class BalanceInitExcelContext {

    boolean parse;

    private Map<String,Integer> auxiliaryIndexMap;

    /**
     * 期初余额
     */
    private Integer openingBalanceIndex;

    /**
     * 本年借方
     */
    private Integer thisYearDebitAmountIndex;

    /**
     * 本年贷方
     */
    private Integer thisYearCreditAmountIndex;

}
