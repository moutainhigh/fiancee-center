package com.njwd.entity.basedata.vo;

import com.njwd.entity.basedata.CommonAbstract;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

/**
 * @Description 常用摘要 vo
 * @Date 2019/7/25 9:48
 * @Author 郑勇浩
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CommonAbstractVo extends CommonAbstract {

	/**
	 * 创建集团/公司名称
	 */
	private String createEnterpriseName;

	/**
	 *  使用集团/公司名称
	 */
	private String useEnterpriseName;

	/**
	 * 最后更新时间
	 */
	private Date lastUpdateTime;

	/**
	 * 返回abstractCode字段内容
	 */
	private String code;

	public String getCode() {
		return super.getAbstractCode();
	}
}
