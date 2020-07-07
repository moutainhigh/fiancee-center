package com.njwd.entity.basedata.vo;

import com.njwd.entity.basedata.SysRole;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/05/20
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysRoleVo extends SysRole {
	private static final long serialVersionUID = 7101740228182367496L;
	private Long companyId;
	/**
	 * 该岗位下的权限树
	 */
	private List<SysMenuVo> sysMenuList;

	/**
	 * 岗位编码或者名称
	 */
	private String codeOrName;
}
