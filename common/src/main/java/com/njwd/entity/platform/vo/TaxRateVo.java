package com.njwd.entity.platform.vo;

import com.njwd.common.Constant;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.platform.TaxRate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description 税率 vo
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaxRateVo extends TaxRate {

	/**
	 * 税收制度
	 */
	private String taxSystemName;

	/**
	 * 税种
	 */
	private String taxCategoryName;

	/**
	 * 纳税人资格
	 */
	private String taxpayerQualName;

	/**
	 * 对应税率
	 */
	private String taxRateStr;

	/**
	 * 对应审核状态
	 */
	private String isApprovedStr;

	/**
	 * 对应发布状态
	 */
	private String isReleasedStr;

	public String getTaxRateStr() {
		if (this.getTaxRate() == null) {
			return null;
		}
		return String.format("%.2f", this.getTaxRate()) + PlatformConstant.TaxRate.PERCENT;
	}

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

	public String getIsReleasedStr() {
		if (this.getIsReleased() == null) {
			return null;
		}
		//发布状态
		if (this.getIsReleased().equals(Constant.Is.YES)) {
			return PlatformConstant.ReleasedStatus.YES;
		} else {
			return PlatformConstant.ReleasedStatus.NO;
		}
	}

}
