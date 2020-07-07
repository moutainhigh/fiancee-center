package com.njwd.platform.service.impl;

import com.njwd.entity.platform.vo.SysRoleVo;
import com.njwd.platform.mapper.SysRoleMapper;
import com.njwd.platform.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhuzs
 * @date 2019-11-13 15:30
 */
@Service
public class SysRoleServiceImpl implements SysRoleService {
    @Autowired
    private SysRoleMapper sysRoleMapper;
    /**
     *
     *
     * @param: []
     * @return: java.util.List<com.njwd.entity.platform.vo.SysRoleVo>
     * @author: zhuzs
     * @date: 2019-11-13
     */
    @Override
    public List<SysRoleVo> findRoleMenuList() {
        return sysRoleMapper.findRoleMenuList();
    }
}

