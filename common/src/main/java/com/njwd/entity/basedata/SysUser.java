package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/5/21
 */
@Data
public class SysUser implements Serializable {
	/**
	 * 用户id 从平台同步
	 */
	@TableId(type = IdType.INPUT)
	private Long userId;

	/**
	 * 用户名
	 */
	private String name;

	/**
	 * 用户手机
	 */
	private String mobile;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 创建人编码
	 */
	private Long creatorId;
	private String creatorName;

	private static final long serialVersionUID = 1L;
}