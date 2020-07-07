package com.njwd.config;


import com.njwd.common.Constant;
import com.njwd.handler.UserRealm;
import com.njwd.utils.FastUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;
import java.util.UUID;

/**
 * shiro配置
 *
 * @author xyyxhcj@qq.com
 * @since 2018-08-31
 */
@Component
public class ShiroConfig {
	@Bean
	public RedisSessionDAO redisSessionDAO(RedisManager redisManager) {
		RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
		redisSessionDAO.setRedisManager(redisManager);
		redisSessionDAO.setKeyPrefix(Constant.ShiroConfig.SESSION_KEY_PREFIX);
		redisSessionDAO.setExpire(Constant.ShiroConfig.EXPIRE);
		return redisSessionDAO;
	}

	@Bean
	public static DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
		DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
		defaultAdvisorAutoProxyCreator.setUsePrefix(true);
		return defaultAdvisorAutoProxyCreator;
	}

	@Bean
	public RedisManager redisManager(RedisProperties redisProperties) {
		RedisManager redisManager = new RedisManager();
		FastUtils.copyProperties(redisProperties, redisManager);
		return redisManager;
	}

	@Bean
	public SessionsSecurityManager securityManager(UserRealm sysUserRealm, RedisManager redisManager, RedisSessionDAO redisSessionDAO) {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		securityManager.setRealm(sysUserRealm);
		// 设置redis缓存管理器
		RedisCacheManager cacheManager = new RedisCacheManager();
		cacheManager.setKeyPrefix(Constant.ShiroConfig.CACHE_KEY_PREFIX);
		cacheManager.setRedisManager(redisManager);
		securityManager.setCacheManager(cacheManager);
		// 设置redis session管理器,尝试从请求头中获取sessionId
		DefaultWebSessionManager sessionManager = new HeaderWebSessionManager();
		sessionManager.setSessionDAO(redisSessionDAO);
		securityManager.setSessionManager(sessionManager);
		return securityManager;
	}

	@Bean
	public ShiroFilterChainDefinition shiroFilterChainDefinition() {
		DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();
		// 配置所有请求路径都可以匿名访问,再通过注解进行细粒度控制
		chainDefinition.addPathDefinition("/**", "anon");
		return chainDefinition;
	}

	/**
	 * 从请求头中获取 sessionId
	 */
	private static class HeaderWebSessionManager extends DefaultWebSessionManager {
		public HeaderWebSessionManager() {
			// 关闭shiro的session扫描
			setSessionValidationSchedulerEnabled(false);
		}

		@Override
		protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
			String token = WebUtils.toHttp(request).getHeader(Constant.ShiroConfig.SESSION_ID_KEY);
			// 如果请求头中有值则从请求头中获取
			if (StringUtils.isEmpty(token)) {
				token = UUID.randomUUID().toString();
			}
			request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE, "url");
			request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID, token);
			request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID, Boolean.TRUE);
			return token;

		}
		@Override
		protected void validate(Session session, SessionKey key) throws InvalidSessionException {
		}
	}
}
