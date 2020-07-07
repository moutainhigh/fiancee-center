package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户
 *
 * @author: zhuzs
 * @date: 2019-11-12
 */
@Data
public class SysUser extends BaseEntity {
	private static final long serialVersionUID = 3934215600609474231L;
	/**
	 * 用户id 从平台同步
	 */
	@TableId(type = IdType.INPUT)
	private Long userId;

	/**
	 * 用户名
	 */
	private String account;

	/**
	 * 手机号
	 */
	private String mobile;

	/**
	 * 员工姓名
	 */
	private String name;

	/**
	 * 创建人ID
	 */
	private Long creatorId;

	/**
	 * 创建人姓名
	 */
	private String creatorName;

	/**
	 * 创建时间
	 */
	private Date createTime;

}