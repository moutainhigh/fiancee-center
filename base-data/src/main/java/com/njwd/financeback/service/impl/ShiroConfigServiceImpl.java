package com.njwd.financeback.service.impl;

import com.njwd.entity.basedata.dto.SysUserDto;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.financeback.mapper.SysUserMapper;
import com.njwd.financeback.service.SysMenuService;
import com.njwd.service.ShiroConfigService;
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
    private SysMenuService sysMenuService;
    @Resource
    private SysUserMapper sysUserMapper;
    @Override
    public Set<String> getPermissionDefinitionsByUserId(SysUserVo sysUserVo) {
        return sysMenuService.getPermissionDefinitionsByUserId(sysUserVo);
    }

    @Override
    public SysUserVo findUserVo(SysUserDto sysUserDto) {
        return sysUserMapper.findUserVo(sysUserDto);
    }
}
