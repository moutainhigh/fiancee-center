package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.AccountingStandard;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author lj
 * @Description //TODO
 * @Date:15:27 2019/6/12
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountingStandardVo extends AccountingStandard {
    private static final long serialVersionUID = 3065062242782563126L;

    private String currencyCode;
    private String currencyName;
    private String taxSystemName;
    private List<AccountingStandardCurrencyVo> accStandardCurrencyList;
}
