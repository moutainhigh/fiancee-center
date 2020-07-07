package com.njwd.entity.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/11/19
 */
@Getter
@Setter
@ToString
public class AccountingStandardCurrency {
    /**
    * 主键 默认自动递增
    */
    private Long id;

    /**
    * 会计准则 【会计准则】表ID
    */
    private Long accStandardId;

    /**
    * 币种编码 【币种】表ID
    */
    private Long currencyId;

    /**
    * 是否默认
    */
    private Byte isDefault;
}
