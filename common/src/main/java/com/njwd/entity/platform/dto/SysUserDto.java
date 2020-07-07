package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.SysUserEnterprise;
import com.njwd.entity.platform.vo.SysMenuVo;
import com.njwd.entity.platform.vo.SysUserVo;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 用户
 *
 * @author: zhuzs
 * @date: 2019-11-12
 */
@Data
@ToString(callSuper = true)
public class SysUserDto extends SysUserVo {
    private static final long serialVersionUID = 4570845506045509540L;

    private Page<SysUserVo> page = new Page<>();

    /**
     * 员工姓名/手机号
     */
    private String name_mobile;

    /**
     * add_type：add新增/add_update引入
     */
    private String add_type;

    /**
     * 用户选择的 待分配租户列表
     */
    private List<SysUserEnterprise> selectedEnterList;

    /**
     * 新增用户 待配置菜单/权限ID
     */
    private List<Long> toAssMenuIdList;

    /**
     * 待复制 菜单/权限 List
     */
    private List<SysMenuVo> toAssMenuList;

    /**
     * 已删除 租户ids
     */
    private String del_ids;

    /**
     * 待新增 租户ids
     */
    private String add_ids;

    /**
     * 用户ID 字符串拼接
     */
    private String user_ids;

    /**
     * 待新增 租户List
     */
    private List<SysUserEnterprise> toAssEnterList;

    /**
     * 待新增 租户ID List
     */
    private List<Long> toAssEnterIdList;

    /**
     * 用户id 集合
     */
    private List<Long> userIdList;

    /**
     * 已删除 租户List
     */
    private List<SysUserEnterprise> delList;

    /**
     * 待复制 userID
     */
    private Long toAssUserId;

    /**
     * menu:查询非按钮菜单权限;button:按钮权限
     */
    private String selectType;

}
