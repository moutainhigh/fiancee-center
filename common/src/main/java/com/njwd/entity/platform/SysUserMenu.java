package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户—菜单/权限 关联表
 *
 * @author: zhuzs
 * @date: 2019-11-12
 */
@Data
public class SysUserMenu implements Serializable {
	private static final long serialVersionUID = -1127352076566616790L;
	/**
	 * 用户id 从平台同步
	 */
	@TableId(type = IdType.INPUT)
	private Long userId;

	/**
	 * 菜单/权限 ID
	 */
	private Long menuId;

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