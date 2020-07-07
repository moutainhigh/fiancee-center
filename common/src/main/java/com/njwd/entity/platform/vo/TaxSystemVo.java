package com.njwd.entity.platform.vo;

import com.njwd.common.Constant;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.platform.TaxSystem;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description 税收制度 vo
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaxSystemVo extends TaxSystem {

	private static final long serialVersionUID = -1367471334503256202L;

	/**
	 * 地区名称
	 */
	private String areaName;

	/**
	 * 币种名称
	 */
	private String currencyName;

	/**
	 * 单价精度
	 */
	private Byte unitPrecision;

	/**
	 * 对应审核状态
	 */
	private String isApprovedStr;

	public String getIsApprovedStr() {
		if (this.getIsApproved() == null) {
			return null;
		}
		//审核状态
		if (this.getIsApproved().equals(Constant.Is.YES)) {
			return PlatformConstant.ApprovedStatus.YES;
		} else {
			return PlatformConstant.ApprovedStatus.NO;
		}
	}

}
