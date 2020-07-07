package com.njwd.entity.basedata.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.vo.UserRoleVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author: Zhuzs
 * @Date: 2019-05-29 10:03
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserRoleDto extends UserRoleVo {
    private static final long serialVersionUID = 1621572174791447834L;
    private Page<UserRoleVo> page = new Page();
    /**
     * 公司ID 集合
     */
    private List<Integer> companyIdList;

    /**
     * 用户ID 集合
     */
    private List<Integer> userIdList;

    /**
     * 岗位/角色ID 集合
     */
    private List<Integer> roleIdList;

    /**
     * 菜单ID 集合
     */
    private List<Integer> menuIdList;

    /**
     * 子系统名称 集合
     */
    private List<String> sysNameList;
}

