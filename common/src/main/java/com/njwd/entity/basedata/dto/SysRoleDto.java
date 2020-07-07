package com.njwd.entity.basedata.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.CommParams;
import com.njwd.entity.basedata.vo.SysRoleVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/05/20
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysRoleDto extends SysRoleVo {
	private static final long serialVersionUID = -3275108429290469257L;
	private Page<SysRoleVo> page = new Page<>();
	private CommParams commParams = new CommParams();

	/**
	 * 批量修改时传参
	 */
	private List<Long> roleIds;
	/**
	 * 权限的父id
	 */
	private Long menuParentId;
	/**
	 * 查询上一条/下一条的code
	 */
	private String currentCode;
}
