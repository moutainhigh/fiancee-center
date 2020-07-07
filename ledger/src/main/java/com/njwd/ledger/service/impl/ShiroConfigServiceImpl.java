package com.njwd.ledger.service.impl;

import com.njwd.entity.basedata.dto.SysUserDto;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.ledger.cloudclient.SysMenuFeignClient;
import com.njwd.ledger.cloudclient.SysUserFeignClient;
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
    private SysMenuFeignClient sysMenuFeignClient;
    @Resource
    private SysUserFeignClient sysUserFeignClient;
    @Override
    public Set<String> getPermissionDefinitionsByUserId(SysUserVo sysUserVo) {
        return sysMenuFeignClient.getPermissionsByUserId(sysUserVo).getData();
    }

    @Override
    public SysUserVo findUserVo(SysUserDto sysUserDto) {
        return sysUserFeignClient.findUserVo(sysUserDto).getData();
    }
}
