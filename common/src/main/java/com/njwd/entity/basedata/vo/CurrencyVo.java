package com.njwd.entity.basedata.vo;

import com.njwd.common.Constant;
import com.njwd.entity.basedata.Currency;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @Description 币种 vo
 * @Date 2019/11/27 9:21
 * @Author 郑勇浩
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CurrencyVo extends Currency {

	/**
	 * 对应币种 的 账簿总数
	 */
	private Integer accountBookCount;

	/**
	 * 对应币种 的 科目数
	 */
	private Integer accountSubjectCount;

	/**
	 * 创建公司名称
	 */
	private String createCompanyName;

	/**
	 * 资料类型
	 */
	private String dataType;

	/**
	 * 对应舍入类型
	 */
	private String roundingTypeStr;

	/**
	 * 禁用者名称
	 */
	private String disabledUserName;

	/**
	 * 禁用时间
	 */
	private Date disabledTime;

	/**
	 * 启用者名称
	 */
	private String enabledUserName;

	/**
	 * 启用时间
	 */
	private Date enabledTime;

	/**
	 * 删除者名称
	 */
	private String deletedUserName;

	/**
	 * 删除时间
	 */
	private Date deletedTime;

	public String getCreateCompanyName() {
		return Constant.BlocInfo.BLOCNAME;
	}

	public String getDataType() {
		return Constant.dataType.SHRETYPE_NAME;
	}

	public String getIsEnableStr() {
		return this.getIsEnable() != null && Constant.Is.YES.equals(this.getIsEnable()) ? "已生效" : "已失效";
	}

	public String getRoundingTypeStr() {
		if (this.getRoundingType() == null) {
			return null;
		}
		//舍入类型
		if (this.getRoundingType() == (byte) 1) {
			return Constant.RoundingType.ONE;
		} else if (this.getRoundingType().equals((byte) 2)) {
			return Constant.RoundingType.TWO;
		} else if (this.getRoundingType().equals((byte) 3)) {
			return Constant.RoundingType.THREE;
		} else {
			return Constant.RoundingType.ZERO;
		}
	}

}
