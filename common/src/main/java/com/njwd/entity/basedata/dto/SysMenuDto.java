package com.njwd.entity.basedata.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.vo.SysMenuVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/05/20
 */
@Getter
@Setter
public class SysMenuDto extends SysMenuVo {
	private static final long serialVersionUID = -2182842831380861784L;
	private Page<SysMenuVo> page = new Page<>();
	/**
	 * 批量修改时传参
	 */
	private List<Long> menuIds;

	/**
	 * 企业ID
	 */
	private Long rootEnterpriseId;

	/**
	 * 子系统名称
	 */
	private String typeName;

	/**
	 * 菜单编码或者名称
	 */
	private String codeOrName;

}
