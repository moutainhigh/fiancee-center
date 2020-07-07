package com.njwd.platform.utils;

import com.njwd.common.Constant;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import javax.validation.constraints.NotNull;

/**
 * shiro工具类
 *
 * @author xyyxhcj@qq.com
 * @since 2019/05/24
 */

public class ShiroUtils {

	/**
	 * 校验权限
	 *
	 * @param menuDefi shiro权限标识
	 */
	public static void checkPerm(@NotNull String menuDefi) {
		if (!hasPerm(menuDefi)) {
			throw new ServiceException(ResultCode.PERMISSION_NOT);
		}
	}

	/**
	 * 批量处理校验权限
	 *
	 * @param menuDefi shiro权限标识
	 * @return  是否有权限
	 */
	public static boolean hasPerm(@NotNull String menuDefi) {
		Subject subject = SecurityUtils.getSubject();
		return subject.isPermitted(menuDefi);
	}


}
