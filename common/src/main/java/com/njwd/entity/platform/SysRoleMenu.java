package com.njwd.entity.platform;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 角色/岗位——权限 关联表
 * @author xyyxhcj@qq.com
 * @since 2019/5/22
 */
@Data
public class SysRoleMenu implements Serializable {
    private Long roleId;

    private Long menuId;

    private int isHalf;

    private Long creatorId;

    private Date createTime;

    private static final long serialVersionUID = 7595576525514205776L;
}