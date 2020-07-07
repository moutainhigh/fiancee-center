package com.njwd.entity.platform.remore.resp;

import lombok.Data;

/**
 * 接收登录时的用户校验数据
 * 
 * @author: zhuzs
 * @date: 2019-11-12 
 */
@Data
public class UserLogin {

	/**
	 * 账号
	 */
	private String account;

	/**
	 * 密码
	 */
	private String password;
	/**
	 *
	 */
	private String sign;
	/**
	 * 时间戳
	 */
	private Long timestamp;
	private String system_code;
	private String root_enterprise_name;
	private String user_name;
	private int admin_type;
	private String email;
	private Long user_id;
	private Long root_enterprise_id;
	private String mobile;
}
