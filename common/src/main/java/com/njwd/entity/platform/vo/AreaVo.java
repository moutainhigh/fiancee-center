package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.Area;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description 国家地区 vo
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AreaVo extends Area {

	/**
	 * 国家地区名 name 前端用
	 */
	private String name;
}
