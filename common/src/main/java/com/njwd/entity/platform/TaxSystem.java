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
 * @Description 税收制度
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wd_tax_system")
public class TaxSystem extends BaseModel implements Serializable {

	private static final long serialVersionUID = -5968652796229417045L;

	/**
	 * 编码
	 */
	private String code;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 地区 【区域】表ID
	 */
	private Long areaId;

	/**
	 * 地区
	 */
	private String area;

	/**
	 * 纳税币种 【币种】表ID
	 */
	private Long currencyId;

	/**
	 * 税额精度
	 */
	@TableField(value = "`precision`")
	private Byte precision;

	/**
	 * 舍入类型 0：四舍五入、1：舍去、2：进位
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

}
