package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.TableName;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @Description 税率
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wd_tax_rate")
public class TaxRate extends BaseModel {

	/**
	 * 编码
	 */
	private String code;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 税率 百分制值
	 */
	private BigDecimal taxRate;

	/**
	 * 税制 【税收制度】表ID
	 */
	private Long taxSystemId;

	/**
	 * 税种 【税种】表ID
	 */
	private Long taxCategoryId;

	/**
	 * 纳税人资格 【辅助资料】表ID
	 */
	private Long taxpayerQual;

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
