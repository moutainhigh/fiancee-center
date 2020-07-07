package com.njwd.utils;


import com.njwd.entity.base.SysLogCommon;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author xyyxhcj@qq.com
 * @since 2018-08-28
 */
@Component
@SuppressWarnings("all")
public class UserUtils {
    /**
     * 获取UserInfo
     */
    @NotNull
    public static SysUserVo getUserVo() {
        // 从shiro中获取
        PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();
        if (principals != null) {
            Object userVo = principals.getPrimaryPrincipal();
            return (SysUserVo) userVo;
        } else {
            throw new ServiceException(ResultCode.SYS_USER_INVALID);
        }
    }

    /**
     * @param ip          ip地址
     * @param sysName     子系统名称：财务后台 、总账
     * @param menuName    菜单名：公司、业务单元等
     * @param operation   操作名：新增、修改、删除，其他
     * @param operateType 操作类型：add、update、delete、other
     * @return
     */
    public static SysLogCommon getUserLogInfo(String ip, String sysName, String menuName, String operation, String operateType) {
        SysLogCommon sysLogCommon = new SysLogCommon(ip, sysName, menuName, operation, operateType);
        sysLogCommon.setCreateTime(new Date());
        sysLogCommon.setStatus((byte) 0);
        //获取用户信息
        // 从shiro中获取
        PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();
        if (principals != null) {
            Object userObj = principals.getPrimaryPrincipal();
            SysUserVo userVo = (SysUserVo) userObj;
            sysLogCommon.setCreatorId(userVo.getUserId());
            sysLogCommon.setCreatorName(userVo.getName());
            sysLogCommon.setRootEnterpriseId(userVo.getRootEnterpriseId());
            sysLogCommon.setMobile(userVo.getMobile());
            sysLogCommon.setCreatorAccount(userVo.getAccount());
        }
        return sysLogCommon;
    }


    /**
     * 该方法增加影响记录Id：recordId
     *
     * @param ip          ip地址
     * @param sysName     子系统名称：财务后台 、总账
     * @param menuName    菜单名：公司、业务单元等
     * @param operation   操作名：新增、修改、删除，其他
     * @param operateType 操作类型：add、update、delete、other
     * @param recordId recordId所影响记录的id
     * @return
     */
    public static SysLogCommon getUserLogInfo2(String ip, String sysName, String menuName, String operation, String operateType,String recordIds) {
        SysLogCommon sysLogCommon = new SysLogCommon(ip, sysName, menuName, operation, operateType);
        sysLogCommon.setCreateTime(new Date());
        sysLogCommon.setStatus((byte) 0);
        sysLogCommon.setRecordId(recordIds);
        //获取用户信息
        // 从shiro中获取
        PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();
        if (principals != null) {
            Object userObj = principals.getPrimaryPrincipal();
            SysUserVo userVo = (SysUserVo) userObj;
            sysLogCommon.setCreatorId(userVo.getUserId());
            sysLogCommon.setCreatorName(userVo.getName());
            sysLogCommon.setRootEnterpriseId(userVo.getRootEnterpriseId());
            sysLogCommon.setMobile(userVo.getMobile());
            sysLogCommon.setCreatorAccount(userVo.getAccount());
        }
        return sysLogCommon;
    }
}