package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.SysUser;
import com.njwd.entity.platform.SysUserEnterprise;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * @author: zhuzs
 * @date: 2019-11-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserVo extends SysUser {
    private static final long serialVersionUID = -500935279529732491L;

    /**
     * 用于接受 Core 返回的用户ID
     */
    private Long user_id;

    /**
     * 密码
     */
    private String password;

    /**
     * 0：普通用户，1：租户管理员，2：系统超级管理员
     */
    private Integer adminType;

    /**
     * 已分配的租户信息
     */
    private List<SysUserEnterprise> assignedEnterpriseList;

    /**
     * 已分配的租户名称
     */
    private String enterpriseNamesStr;

    /**
     * 已配置 用户—菜单/权限
     */
    private List<SysMenuVo> sysMenuList ;

    /**
     * 有权的目录/模块/菜单并集code
     */
    private String sysMenuCodes;

    /**
     * 有权的按钮权限,末级菜单(key)分类
     */
    private Map<String, StringBuilder> sysButtonMap;

    /**
     * 用户列表
     */
    private List<SysUserVo> sysUserVoList;

    private List<SysUserVo> records;

    /**
     * 岗位/角色ID
     */
    private List<Long> roleIdList;

    private Integer infoNum;

    private Long pageNo;

    private Long pageSize;

    private Long totalPage;

    private Long totalRecord;

    /**
     * 财务后台登录token
     */
    private String authorization;

    /**
     * shiro 缓存标识
     */
    public String getAuthCacheKey() {

        return getUserId() == null ? null : getUserId().toString();
    }
}