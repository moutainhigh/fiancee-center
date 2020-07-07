package com.njwd.service;

import com.njwd.entity.basedata.dto.SysUserDto;
import com.njwd.entity.basedata.vo.SysUserVo;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * shiro配置实现类
 *
 * @author xyyxhcj@qq.com
 * @since 2019-08-01
 */

public interface ShiroConfigService {
    /**
     * 获取权限列表
     *
     * @param sysUserVo userId
     * @return set
     */
    Set<String> getPermissionDefinitionsByUserId(SysUserVo sysUserVo);

    /**
     * 查询用户信息
     *
     * @param sysUserDto sysUserDto
     * @return sysUserVo
     */
    SysUserVo findUserVo(@Param("sysUserDto") SysUserDto sysUserDto);
}
