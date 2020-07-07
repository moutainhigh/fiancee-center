package com.njwd.entity.platform.vo;

import com.njwd.common.Constant;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.platform.TaxCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description 税种 vo
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaxCategoryVo extends TaxCategory {

	/**
	 * 税收制度名称
	 */
	private String taxSystemName;

	/**
	 * 对应舍入类型
	 */
	private String roundingTypeStr;

	/**
	 * 对应是否增值税
	 */
	private String isVatStr;

	/**
	 * 对应审核状态
	 */
	private String isApprovedStr;

	/**
	 * 对应发布状态
	 */
	private String isReleasedStr;


	public String getRoundingTypeStr() {
		if (this.getRoundingType() == null) {
			return null;
		}
		//舍入类型
		if (this.getRoundingType() == (byte) 1) {
			return PlatformConstant.RoundingType.ONE;
		} else if (this.getRoundingType().equals((byte) 2)) {
			return PlatformConstant.RoundingType.TWO;
		} else if (this.getRoundingType().equals((byte) 3)) {
			return PlatformConstant.RoundingType.THREE;
		} else {
			return PlatformConstant.RoundingType.ZERO;
		}
	}

	public String getIsVatStr() {
		if (this.getIsVat() == null) {
			return null;
		}
		//是否增值税
		if (this.getIsVat().equals(Constant.Number.INITIAL)) {
			return PlatformConstant.IsVat.YES;
		} else {
			return PlatformConstant.IsVat.NO;
		}
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
