package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.SysUserMenu;
import lombok.Data;

/**
 * @author zhuzs
 * @date 2019-11-12 10:38
 */
@Data
public class SysUserMenuVo extends SysUserMenu {
    private static final long serialVersionUID = 7037339075835676939L;
    private Long parentId;
    private int type;
}

