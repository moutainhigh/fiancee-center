package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.njwd.entity.base.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @Description 税种
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wd_tax_category")
public class TaxCategory extends BaseModel implements Serializable {

	/**
	 * 编码
	 */
	private String code;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 税制 【税收制度】表ID
	 */
	private Long taxSystemId;

	/**
	 * 税额精度
	 */
	@TableField(value = "`precision`")
	private Byte precision;

	/**
	 * 舍入类型 0：四舍五入、1：向上舍入、2：向下舍入、3：四舍六入五成双
	 */
	private Byte roundingType;

	/**
	 * 是否增值税 0：否、1：是
	 */
	private Byte isVat;

	/**
	 * 审核状态 0未审核 1已审核
	 */
	private Byte isApproved;

	/**
	 * 发布状态 0未发布 1已发布
	 */
	private Byte isReleased;

	/**
	 * 启用标识 0：禁用、1：启用
	 */
	private Boolean isEnable;

	/**
	 * 扩展信息
	 */
	private Object manageInfo;

	/**
	 * 租户ID
	 */
	private Long rootEnterpriseId;

	/**
	 * 平台ID
	 */
	private Long platformId;

}
