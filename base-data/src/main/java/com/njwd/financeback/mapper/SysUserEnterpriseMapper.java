package com.njwd.financeback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.basedata.SysUserEnterprise;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/5/23
 */
public interface SysUserEnterpriseMapper extends BaseMapper<SysUserEnterprise> {
    /**
     * 批量禁用启用
     *
     * @param sysUserEnterprise sysUserEnterprise
     * @param managerList       managerList
     * @param disable           true禁用 false启用
     * @return int
     * @author xyyxhcj@qq.com
     * @date 2019/9/9 11:10
     **/
    int batchDisableOrEnable(@Param("sysUserEnterprise") SysUserEnterprise sysUserEnterprise, @Param("managerList") List<Object> managerList, @Param("disable") boolean disable);
}