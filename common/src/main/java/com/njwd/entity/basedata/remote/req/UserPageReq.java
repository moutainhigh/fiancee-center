package com.njwd.entity.basedata.remote.req;

import lombok.Data;

/**
 * 查询统一门户未引入的用户参数
 *
 * @author xyyxhcj@qq.com
 * @since 2019/05/27
 */
@Data
@SuppressWarnings("all")
public class UserPageReq {
	/**
	 * 入参：system_code、timestamp、sign、root_enterprise_id、name、mobile、userIds,name_mobile
	 */
	private String sign;
	private Long timestamp;
	private String system_code;
	private String name;
	private String mobile;
	private String name_mobile;
	/**
	 * 当前租户已引入的用户ids
	 */
	private String userIds;
	private Long root_enterprise_id;
	private long pageNo;
	private long pageSize;
}
