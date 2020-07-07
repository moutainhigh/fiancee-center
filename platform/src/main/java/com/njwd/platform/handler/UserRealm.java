package com.njwd.platform.handler;

import com.njwd.entity.platform.dto.SysUserDto;
import com.njwd.entity.platform.remore.resp.UserLogin;
import com.njwd.entity.platform.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.platform.service.ShiroConfigService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

/**
 * 定义shiro查询用户信息,用户权限等逻辑
 *
 * @author: zhuzs
 * @date: 2019-11-12
 */
@Component
public class UserRealm extends AuthorizingRealm {
    @Resource
    @Lazy
    private ShiroConfigService shiroConfigService;

    /**
     * 将当前用户的权限添加到shiro的授权对象中
     *
     * @param: [principalCollection]
     * @return: org.apache.shiro.authz.AuthorizationInfo
     * @author: zhuzs
     * @date: 2019-11-11
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        if (principalCollection == null) {
            throw new ServiceException(ResultCode.SYS_USER_INVALID);
        }
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        SysUserDto sysUserDto = (SysUserDto) getAvailablePrincipal(principalCollection);

        Set<String> permissionDefinitions = shiroConfigService.findPermissionDefinitionsByUserId(sysUserDto);
        simpleAuthorizationInfo.setStringPermissions(permissionDefinitions);

        return simpleAuthorizationInfo;
    }

    /**
     * 自动登录
     *
     * @param:
     * @return:
     * @author: zhuzs
     * @date: 2019-11-11
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
     *
     * @param: [authenticationToken]
     * @return: org.apache.shiro.authc.AuthenticationInfo
     * @author: zhuzs
     * @date: 2019-11-11
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        // 存入基本信息
        SysUserVo sysUserVo = new SysUserVo();
        AutoLoginToken autoLoginToken = (AutoLoginToken) authenticationToken;
        UserLogin userLogin = autoLoginToken.getUserLogin();
        sysUserVo.setUserId(userLogin.getUser_id());
        sysUserVo.setName(userLogin.getUser_name());
        sysUserVo.setAccount(userLogin.getAccount());
        sysUserVo.setMobile(userLogin.getMobile());
        sysUserVo.setAdminType(userLogin.getAdmin_type());


        return new SimpleAuthenticationInfo(sysUserVo, "", getName());
    }

}
