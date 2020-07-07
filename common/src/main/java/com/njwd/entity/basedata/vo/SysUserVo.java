package com.njwd.entity.basedata.vo;

import com.njwd.common.Constant;
import com.njwd.entity.base.ManagerInfo;
import com.njwd.entity.basedata.SysUser;
import com.njwd.entity.platform.SysUserEnterprise;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/05/21
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysUserVo extends SysUser {
	private static final long serialVersionUID = 1801017165360920794L;
	/**
	 * 财务后台登录token
	 */
	private String authorization;

	/**
	 * 根据公司id(key)对角色分类,不关联公司的角色key为0
	 */
	private Map<Long, List<SysRoleVo>> sysRoleMap;
	/**
	 * 根据公司id(key)对权限分类,不关联公司的权限key为0
	 */
	private Map<Long, List<SysMenuVo>> sysMenuMap;
	/**
	 * 根据公司id(key)对公司分类
	 */
	private Map<Long, CompanyVo> companyVoMap;
	/**
	 * 如果用户为租户/业务管理员,则返回租户下的所有公司列表
	 */
	private List<CompanyVo> allCompanyList;
	/**
	 * 根据公司id(key)对账簿主体分类
	 */
	private Map<Long, List<UserAccountBookEntityVo>> userAccountBookEntityVoMap;
	/**
	 * 有权的目录/模块/菜单并集code
	 */
	private String sysMenuCodes;
	/**
	 * 有权的按钮权限,根据公司id(key)分类
	 */
	private Map<Long, StringBuilder> sysButtonMap;
	/**
	 * 租户id
	 */
	private Long rootEnterpriseId;
	/**
	 * 平台管理员/租户管理员
	 */
	private Integer adminType;

	/**
	 * 是否生效 0：否、1：是
	 */
	private Byte isEnable;
	private String account;
	private String email;
	private Long defaultCompanyId;
	private String roleIds;
	/**
	 * 是否默认上一条凭证摘要：0否 1是
	 */
	private Byte isLastAbstract;
	/**
	 * 凭证新增日期设置：0系统日期 1上一张凭证日期
	 */
	private Byte voucherDateType;
	/**
	 * 凭证列表的用户配置数据(json串)
	 **/
	private String voucherListConfig;
	/**
	 * 是否业务管理员：0否 1是
	 */
	private Byte isAdmin;
	/**
	 * 有权的公司名称
	 */
	private String companyNames;
	/**
	 * 有权的公司编码
	 */
	private String companyCodes;
	/**
	 * 有权的核算主体名称
	 **/
	private String accountBookEntityNames;
	/**
	 * 导出时数据状态
	 */
	private String isEnableName;

	/**
	 * 修改人编码
	 */
	private Long updatorId;

	private String updatorName;

	/**
	 * 修改时间
	 */
	private Date updateTime;
	/**
	 * 登录后返回的权限树,根据公司id(key)分类
	 */
	private Map<Long, List<SysMenuVo>> menuTreeMap;
	/**
	 * shiro 缓存标识
	 */
	public String getAuthCacheKey() {
		return String.format(Constant.ShiroConfig.AUTH_CACHE_KEY, rootEnterpriseId, getUserId(), adminType == null ? -1 : adminType);
	}

	/**
	 * 用户名称或者手机号
	 */
	private String nameOrMobile;
	private Long companyId;
	private ManagerInfo manageInfo;
	/**
	 * 批量修改时传参
	 */
	private List<Long> userIds;

	/**
	 * 待新增 租户ID List
	 */
	private List<Long> toAssEnterIdList;

	public Byte getIsDel() {
		return getRootEnterpriseId() == null ? Constant.Is.YES : Constant.Is.NO;
	}
}
