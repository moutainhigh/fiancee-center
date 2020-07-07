package com.njwd.platform.service;

import com.njwd.entity.platform.dto.SysUserDto;
import com.njwd.entity.platform.vo.SysUserVo;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * shiro配置实现类
 *
 * @author: zhuzs
 * @date: 2019-11-12
 */
public interface ShiroConfigService {
    /**
     * 获取权限列表
     *
     * @param sysUserDto userId
     * @return set
     */
    Set<String> findPermissionDefinitionsByUserId(SysUserDto sysUserDto);

}
