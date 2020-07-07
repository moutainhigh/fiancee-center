package com.njwd.handler;

import com.njwd.common.Constant;
import com.njwd.entity.basedata.dto.SysUserDto;
import com.njwd.entity.basedata.remote.resp.UserLogin;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.exception.UserException;
import com.njwd.service.ShiroConfigService;
import com.njwd.utils.RedisUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 定义shiro查询用户信息,用户的角色和权限,校验密码等逻辑
 * CasRealm
 *
 * @author CJ
 */
@Component
public class UserRealm extends AuthorizingRealm implements Serializable {
    private static final long serialVersionUID = -1786769257975208759L;
    @Resource
    @Lazy
    private ShiroConfigService shiroConfigService;

    /**
     * 将当前用户的角色及权限添加到shiro的授权对象中
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        if (principalCollection == null) {
            throw new ServiceException(ResultCode.SYS_USER_INVALID);
        }
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        SysUserVo sysUserVo = (SysUserVo) getAvailablePrincipal(principalCollection);
        // 将用户的业务管理员角色和权限存到SimpleAuthenticationInfo中
        Set<String> roleDefinitions = new HashSet<>();
        // 如果管理员类型为业务管理员,则添加shiro角色标识
        SysUserDto sysUserDto = new SysUserDto();
        sysUserDto.setRootEnterpriseId(sysUserVo.getRootEnterpriseId());
        sysUserDto.setUserId(sysUserVo.getUserId());
        SysUserVo existSysUserVo = shiroConfigService.findUserVo(sysUserDto);
        boolean isEnable = existSysUserVo != null && Constant.Is.YES.equals(existSysUserVo.getIsEnable());
        if (isEnable && Constant.Is.YES.equals(existSysUserVo.getIsAdmin())) {
            roleDefinitions.add(sysUserVo.getRootEnterpriseId().toString() + Constant.Character.COLON + Constant.ShiroAdminDefi.BUSINESS_ADMIN);
        }
        // 根据sysUserVo的角色标识判断是否平台管理员/租户管理员,手动设置角色(不存库)
        if (sysUserVo.getAdminType() != null) {
            switch (sysUserVo.getAdminType()) {
                case Constant.AdminType.SYS_ADMIN:
                    roleDefinitions.add(Constant.ShiroAdminDefi.SYS_ADMIN);
                    break;
                case Constant.AdminType.ENTERPRISE_ADMIN:
                    roleDefinitions.add(sysUserVo.getRootEnterpriseId().toString() + Constant.Character.COLON + Constant.ShiroAdminDefi.ENTERPRISE_ADMIN);
                    roleDefinitions.add(sysUserVo.getRootEnterpriseId().toString() + Constant.Character.COLON + Constant.ShiroAdminDefi.BUSINESS_ADMIN);
                    break;
                default:
            }
        }
        simpleAuthorizationInfo.setRoles(roleDefinitions);
        if (isEnable) {
            Set<String> permissionDefinitions = shiroConfigService.getPermissionDefinitionsByUserId(sysUserVo);
            simpleAuthorizationInfo.setStringPermissions(permissionDefinitions);
        }
        return simpleAuthorizationInfo;
    }

    /**
     * 自动登录
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class AutoLoginToken extends UsernamePasswordToken {
        private static final long serialVersionUID = 4209120367456669554L;
        private UserLogin userLogin;

        public AutoLoginToken(UserLogin userLogin) {
            super(userLogin.getUser_id().toString(), "");
            this.userLogin = userLogin;
        }
    }

    /**
     * 登录调用方法
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        SysUserDto sysUserDto = new SysUserDto();
        AutoLoginToken autoLoginToken = (AutoLoginToken) authenticationToken;
        UserLogin userLogin = autoLoginToken.getUserLogin();
        sysUserDto.setUserId(userLogin.getUser_id());
        if (userLogin.getRoot_enterprise_id() == null) {
            userLogin.setRoot_enterprise_id(0L);
        }
        sysUserDto.setRootEnterpriseId(userLogin.getRoot_enterprise_id());
        SysUserVo sysUserVo;
        // 平台管理员及租户管理员用户数据不存库
        if (Constant.CoreAdminType.ADMIN_ROLE == userLogin.getAdmin_type()) {
            sysUserVo = getAdminUserVo(userLogin, Constant.AdminType.SYS_ADMIN);
        } else if (Constant.CoreAdminType.ENTERPRISE_ROLE == userLogin.getAdmin_type()) {
            sysUserVo = getAdminUserVo(userLogin, Constant.AdminType.ENTERPRISE_ADMIN);
        } else {
            sysUserVo = shiroConfigService.findUserVo(sysUserDto);
            if (sysUserVo != null && Constant.Is.NO.equals(sysUserVo.getIsDel())) {
                if (!Constant.Is.YES.equals(sysUserVo.getIsEnable())) {
                    throw new UserException(ResultCode.SYS_USER_DISABLE);
                }
                sysUserVo.setAccount(userLogin.getAccount());
            } else {
                throw new UserException(ResultCode.USER_NOT_EXIST);
            }
        }
        return new SimpleAuthenticationInfo(sysUserVo, "", getName());
    }

    private SysUserVo getAdminUserVo(UserLogin userLogin, int adminType) {
        SysUserVo sysUserVo = new SysUserVo();
        sysUserVo.setAdminType(adminType);
        sysUserVo.setUserId(userLogin.getUser_id());
        sysUserVo.setRootEnterpriseId(userLogin.getRoot_enterprise_id());
        sysUserVo.setIsEnable(Constant.Is.YES);
        sysUserVo.setName(userLogin.getUser_name());
        sysUserVo.setMobile(userLogin.getMobile());
        sysUserVo.setAccount(userLogin.getAccount());
        return sysUserVo;
    }

    public void clearCachedAuthorizationInfo(Long rootEnterpriseId, Long userId) {
        // userId为null时清空租户下的所有权限缓存
        if (userId == null) {
            RedisUtils.removeKeys(String.format(Constant.ShiroConfig.AUTH_CACHE_KEY_REMOVE, rootEnterpriseId), (long) Short.MAX_VALUE);
        } else {
            SysUserVo sysUserVo = new SysUserVo();
            sysUserVo.setRootEnterpriseId(rootEnterpriseId);
            sysUserVo.setUserId(userId);
            super.clearCachedAuthorizationInfo(new SimplePrincipalCollection(sysUserVo, getName()));
        }
    }

    public void clearCachedAuthList(Long rootEnterpriseId, @NotNull List<Long> userIds) {
        SysUserVo sysUserVo = new SysUserVo();
        sysUserVo.setRootEnterpriseId(rootEnterpriseId);
        for (Long userId : userIds) {
            sysUserVo.setUserId(userId);
            super.clearCachedAuthorizationInfo(new SimplePrincipalCollection(sysUserVo, getName()));
        }
    }
}
