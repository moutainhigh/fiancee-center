package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @Description 币种
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wd_currency")
public class Currency extends BaseModel implements Serializable {

	private static final long serialVersionUID = 3555444712433335606L;

	/**
	 * 编码
	 */
	private String code;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * ISO货币代码
	 */
	private String isoCode;

	/**
	 * 货币符号
	 */
	private String symbol;

	/**
	 * 金额精度
	 */
	@TableField(value = "`precision`")
	private Byte precision;

	/**
	 * 单价精度
	 */
	private Byte unitPrecision;

	/**
	 * 舍入类型 0：四舍五入、1：向上舍入、2：向下舍入、3：四舍六入五成双
	 */
	private Byte roundingType;

	/**
	 * 启用标识 0：禁用、1：启用
	 */
	private Byte isEnable;

	/**
	 * 审核状态 0：未审核、1：已审核
	 */
	private Byte isApproved;

	/**
	 * 发布状态 0：未发布、1：已发布
	 */
	private Byte isReleased;

	/**
	 * 管理信息
	 */
	private ManagerInfo manageInfo;

	/**
	 * 租户id
	 */
	private Long rootEnterpriseId;

	/**
	 * 平台Id
	 */
	private Long platformId;

}
