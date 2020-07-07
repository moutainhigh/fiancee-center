package com.njwd.common;

import lombok.Data;

/**
 * 通用查询参数
 *
 * @author xyyxhcj@qq.com
 * @since 2019/05/31
 */
@Data
public class CommParams {
	/**
	 * 进行'或'匹配的查询值
	 */
	private String orMatch;
	/**
	 * 进行'或'匹配的表字段名
	 */
	private String[] orColumn;
}
