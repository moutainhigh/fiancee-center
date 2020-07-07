package com.njwd.entity.ledger.vo;

import com.njwd.entity.ledger.ParameterSet;
import com.njwd.entity.ledger.ParameterSetSub;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

/**
 * 总账参数设置
 * @author xyyxhcj@qq.com
 * @date 2019/10/16 14:21
 **/
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ParameterSetVo extends ParameterSet {

	/**
	 * 字表ID
	 */
	private Long subId;

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

	/**
	 * 参数值对应编码
	 */
	private String valueCode;

	/**
	 * 参数值对应名称
	 */
	private String valueStr;

	/**
	 * 个性化账簿的数量
	 */
	private int count;

	/**
	 * 参数字典 一级key'参数标识' 二级key'账簿ID'
	 **/
	private Map<String, Map<Long, ParameterSetSub>> paramDict;

	/**
	 * 损益类科目明细数据
	 **/
	private Map<Long, Map<String, Object>> carrySubjects;

	/**
	 * 本年利润科目ID
	 */
	private Long lrAccSubjectId;

	/**
	 * 利润分配科目ID
	 */
	private Long fpAccSubjectId;

	/**
	 * 损益调整科目ID
	 */
	private Long syAccSubjectId;

}
