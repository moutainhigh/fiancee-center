package com.njwd.entity.basedata.remote.resp;

import lombok.Data;

/**
 * 接收登录时的用户校验数据
 *
 * @author xyyxhcj@qq.com
 * @since 2019/05/27
 */
@Data
@SuppressWarnings("all")
public class UserLogin {

	/**
	 * sign : 9861B7B2416F088AC6F3D21F65CE4BDA
	 * timestamp : 1558598527
	 * system_code : njwd_finance
	 * root_enterprise_name : LV正式1226
	 * user_name : 吕军喜
	 * admin_type : -1
	 * email :
	 * account : lvjunxi
	 * user_id : 2312
	 * root_enterprise_id : 2334
	 * mobile : 18912945952
	 */

	private String sign;
	private Long timestamp;
	private String system_code;
	private String root_enterprise_name;
	private String user_name;
	private int admin_type;
	private String email;
	private String account;
	private Long user_id;
	private Long root_enterprise_id;
	private String mobile;
}
