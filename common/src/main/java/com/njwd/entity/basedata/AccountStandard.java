package com.njwd.entity.basedata;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 会计准则
 *
 * @author zhuzs
 * @date 2019-07-08 10:37
 */
@Data
public class AccountStandard implements Serializable {
    private static final long serialVersionUID = -2284682554690903470L;

    /**
     * 会计准则ID
     */
    private Long accStandardId;

    /**
     * 会计准则
     */
    private String accStandardName;

    /**
     * 税制集合
     */
    List<TaxSystem> taxSystems;

    /**
     * 本位币集合
     */
    List<Currency> currencys;
}

