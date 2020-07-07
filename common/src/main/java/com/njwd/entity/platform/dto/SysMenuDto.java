package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.njwd.entity.platform.vo.SysMenuVo;
import com.njwd.entity.platform.vo.SysUserVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author liuxiang
 * 前端入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMenuDto extends SysMenuVo {
    private static final long serialVersionUID = 6989541119752002754L;
    private Page<SysMenuVo> page = new Page<>();

    /**
     *
     */
    private Long pageNo;
    /**
     *
     */
    private Long pageSize;

    /**
     * 查询条件
     */
    private String name_mobile;

    /**
     * 查询条件 子系统编码或名称
     */
    private String codeOrName;

    /**
     * 菜单类型 1 一级菜单 2二级菜单 3三级菜单 4 通用权限 5 业务操作
     */
    private int menuType;

    /**
     * 用户 集合
     */
    private List<SysUserVo> userList;

    /**
     * 子系统 取自一级功能菜单
     */
    private List<Long> firstLevelMenuIdList;

    /**
     * 功能菜单 取自三级功能菜单
     */
    private List<Long> thirdLevelMenuIdList;

}