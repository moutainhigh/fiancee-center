package com.njwd.entity.ledger;

import lombok.Getter;
import lombok.Setter;

/**
 * 总账参数设置 子表
 * @author xyyxhcj@qq.com
 * @date 2019/10/16 14:21
 **/
@Getter
@Setter
public class ParameterSetSub {
	/**
	 * 主键 默认自动递增
	 */
	private Long id;

	/**
	 * 租户ID
	 */
	private Long rootEnterpriseId;

	/**
	 * 总账参数主表ID [wd_parameter_set] id
	 */
	private Long setId;

	/**
	 * 账簿ID 0表示租户默认配置,否则是账簿ID
	 */
	private Long accountBookId;

	/**
	 * 账簿名
	 */
	private String accountBookName;

	/**
	 * 参数值
	 */
	private Long value;
}
