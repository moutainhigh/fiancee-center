package com.njwd.entity.basedata.vo;

import com.njwd.entity.basedata.SysMenu;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/05/20
 */
@Getter
@Setter
public class SysMenuVo extends SysMenu {
	private static final long serialVersionUID = 4951110138869772529L;
	private String parentName;
	private Long companyId;
	private List<SysMenuVo> sysMenuList;

	/**
	 * 是否半选: 1半选 0全选
	 */
	private Byte isHalf;
}
