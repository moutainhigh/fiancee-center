package com.njwd.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.RootEnterpriseDto;
import com.njwd.entity.platform.vo.RootEnterpriseVo;
import com.njwd.entity.platform.vo.SysSystemVo;

import java.util.List;

/**
 * 租户
 *
 * @author zhuzs
 * @date 2019-11-15 10:18
 */
public interface EnterpriseService {

    /**
     * 获取租户列表
     *
     * @param: [sysUserDto]
     * @return: List<SysUserVo>
     * @author: zhuzs
     * @date: 2019-11-13
     */
    Page<RootEnterpriseVo> findEnterprisePage(RootEnterpriseDto enterpriseDto);

    /**
     * 获取 已购买子系统列表
     *
     * @param: [rootEnterpriseDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.SysSystemVo>
     * @author: zhuzs
     * @date: 2019-11-15
     */
    List<SysSystemVo> findEnableSystemList(RootEnterpriseDto rootEnterpriseDto);
}
