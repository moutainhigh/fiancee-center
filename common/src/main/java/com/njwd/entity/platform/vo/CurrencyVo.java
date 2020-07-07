package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.Currency;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description 币种 vo
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CurrencyVo extends Currency {
	private static final long serialVersionUID = -8288126615339487152L;
}
