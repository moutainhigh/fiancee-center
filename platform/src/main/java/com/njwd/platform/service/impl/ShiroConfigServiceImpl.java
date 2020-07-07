package com.njwd.platform.service.impl;

import com.njwd.entity.platform.dto.SysUserDto;
import com.njwd.entity.platform.vo.SysUserVo;
import com.njwd.platform.service.ShiroConfigService;
import com.njwd.platform.service.SysMenuService;
import com.njwd.platform.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Set;

/**
 * shiro查询实现类
 *
 * @author xyyxhcj@qq.com
 * @since 2019/08/02
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ShiroConfigServiceImpl implements ShiroConfigService {
    @Resource
    private SysUserService sysUserService;

    @Resource
    private SysMenuService sysMenuService;

    @Override
    public Set<String> findPermissionDefinitionsByUserId(SysUserDto sysUserDto) {
        return sysMenuService.findPermissionDefinitionsByUserId(sysUserDto);
    }

}
