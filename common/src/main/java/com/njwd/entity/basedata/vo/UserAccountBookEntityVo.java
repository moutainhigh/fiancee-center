package com.njwd.entity.basedata.vo;

import com.njwd.entity.basedata.UserAccountBookEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/06/20
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserAccountBookEntityVo extends UserAccountBookEntity {
	private static final long serialVersionUID = -2243343716804925713L;
	private String name;
}
