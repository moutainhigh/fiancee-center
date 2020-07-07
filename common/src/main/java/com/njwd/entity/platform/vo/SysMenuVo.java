package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.SysMenu;
import lombok.Data;

import java.util.List;

/**
 * 权限
 *
 * @author: zhuzs
 * @date: 2019-11-13
 */
@Data
public class SysMenuVo extends SysMenu {
    private static final long serialVersionUID = 5294378638756319763L;
    private List<SysMenuVo> sysMenuList;

    private Long userId;

    private String userName;

    private String mobile;

    /**
     * 功能模块
     */
    private String fLevelName;
    /**
     * 功能菜单
     */
    private String tLevelName;

    /**
     * 功能按钮
     */
    private String childName;
}