package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.TableName;
import com.njwd.entity.base.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @Description 国家地区
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wd_area")
public class Area extends BaseModel implements Serializable {

	/**
	 * 国家编码
	 */
	private String code;
	/**
	 * 国家地区名
	 */
	private String areaName;

	/**
	 * 审核状态 0：未审核、1：已审核
	 */
	private Byte isApproved;

	/**
	 * 扩展信息
	 */
	private Object manageInfo;

}
