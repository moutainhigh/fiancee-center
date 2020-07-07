package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.AccountingStandardCurrency;
import lombok.Getter;
import lombok.Setter;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/11/19
 */
@Getter
@Setter
public class AccountingStandardCurrencyVo extends AccountingStandardCurrency {
	private String currencyCode;
	private String currencyName;
}
