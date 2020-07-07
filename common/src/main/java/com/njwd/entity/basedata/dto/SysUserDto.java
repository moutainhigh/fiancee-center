package com.njwd.entity.basedata.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.CommParams;
import com.njwd.entity.basedata.remote.resp.UserLogin;
import com.njwd.entity.basedata.vo.SysUserVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/05/21
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysUserDto extends SysUserVo {
	private static final long serialVersionUID = -2403500392208285805L;
	private Page<SysUserVo> page = new Page<>();
	private CommParams commParams = new CommParams();
	/**
	 * 根据公司id(key)对角色分类,value为公司下有权的roleId数组
	 */
	private Map<Long, Long[]> sysRoleAssignMap;
	/**
	 * 根据公司id(key)对核算账簿主体分类,value为公司下的accountBookEntityId数组
	 */
	private Map<Long, Long[]> accountBookEntityMap;
	/**
	 * 用于批量引入用户,批量用户赋权
	 */
	private List<SysUserDto> sysUserDtoList;
	private Byte isEnable;

	/**
	 * 登录验证所需参数
	 */
	private UserLogin userLogin;
	private Long[] companyIds;
	private Long roleId;
	/**
	 * 批量设置的岗位ids
	 */
	private Long[] assignRoleIds;
	/**
	 * 查询上一条/下一条的mobile
	 */
	private String currentMobile;
	/**
	 * 账簿主体id
	 */
	private Long accountBookEntityId;
}
