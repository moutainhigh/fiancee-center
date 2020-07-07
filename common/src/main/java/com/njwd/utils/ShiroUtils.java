package com.njwd.utils;

import com.njwd.common.Constant;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.support.BatchResult;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * shiro工具类
 *
 * @author xyyxhcj@qq.com
 * @since 2019/05/24
 */

public class ShiroUtils {
	/**
	 * 校验角色
	 *
	 * @param roleDefi shiro角色标识
	 */
	public static void checkRole(@NotNull String roleDefi, @Nullable Long rootEnterpriseId) {
		if (!hasRole(roleDefi, rootEnterpriseId)) {
			throw new ServiceException(ResultCode.PERMISSION_NOT);
		}
	}

	/**
	 * 判断是否有对应的身份，用于接口适配，动态根据用户的身份查询对应数据
	 * @param roleDefi roleDefi
	 * @param rootEnterpriseId rootEnterpriseId
	 * @return boolean
	 */
	public static boolean hasRole(@NotNull String roleDefi, @Nullable Long rootEnterpriseId) {
		if (rootEnterpriseId != null) {
			roleDefi = rootEnterpriseId.toString() + Constant.Character.COLON + roleDefi;
		}
		Subject subject = SecurityUtils.getSubject();
		return subject.hasRole(roleDefi);
	}

	/**
	 * 校验权限
	 *
	 * @param menuDefi      shiro权限标识
	 * @param dataCompanyId 被操作数据所归属的公司id
	 */
	public static void checkPerm(@NotNull String menuDefi, @NotNull Long dataCompanyId) {
		if (!hasPerm(menuDefi, dataCompanyId)) {
			throw new ServiceException(ResultCode.PERMISSION_NOT);
		}
	}

	/**
	 * 批量处理校验权限
	 *
	 * @param menuDefi      shiro权限标识
	 * @param dataCompanyId 被操作数据所归属的公司id
	 * @return  是否有权限
	 */
	public static boolean hasPerm(@NotNull String menuDefi, @NotNull Long dataCompanyId) {
		Subject subject = SecurityUtils.getSubject();
		return subject.isPermitted(menuDefi + Constant.Character.COLON + dataCompanyId.toString());
	}

	/**
	 * 获取多个公司下有权限的公司id
	 *
	 * @param menuDefi       shiro权限标识
	 * @param dataCompanyIds 待鉴权的多个公司id
	 * @return 有权的公司id集合
	 */
	public static List<Long> filterPerm(@NotNull String menuDefi, @NotNull List<Long> dataCompanyIds) {
		Subject subject = SecurityUtils.getSubject();
		List<Long> resultList = new LinkedList<>();
		for (Long dataCompanyId : dataCompanyIds) {
			if (subject.isPermitted(menuDefi + Constant.Character.COLON + dataCompanyId.toString())) {
				resultList.add(dataCompanyId);
			}
		}
		return resultList;
	}

	/**
	 * 批量操作时过滤掉无权限的数据
	 *
	 * @param checkPermList    checkPermList
	 * @param menuDefine       权限定义
	 * @param checkPermSupport 提供两个方法的实现,用于从单条数据中获取ID和公司ID
	 * @return com.njwd.support.BatchResult
	 * @author xyyxhcj@qq.com
	 * @date 2019/9/10 15:43
	 **/
	public static <T> BatchResult filterNotPermData(final List<T> checkPermList, final String menuDefine, final CheckPermSupport<T> checkPermSupport) {
		BatchResult batchResult = new BatchResult();
		List<ReferenceDescription> failList = new LinkedList<>();
		batchResult.setFailList(failList);
		ListIterator<T> iterator = checkPermList.listIterator();
		while (iterator.hasNext()) {
			T next = iterator.next();
			Long companyId = checkPermSupport.getCompanyId(next);
			if (companyId == null) {
				throw new ServiceException(ResultCode.DATA_ERROR);
			}
			boolean hasPerm = ShiroUtils.hasPerm(menuDefine, companyId);
			if (!hasPerm) {
				ReferenceDescription failDescription = new ReferenceDescription();
				failDescription.setBusinessId(checkPermSupport.getBusinessId(next));
				failDescription.setReferenceDescription(ResultCode.PERMISSION_NOT.message);
				failList.add(failDescription);
				iterator.remove();
			}
		}
		return batchResult;
	}

	public interface CheckPermSupport<T> {
		/**
		 * 获取异常描述返回的ID
		 *
		 * @param t t
		 * @return java.lang.Long
		 * @author xyyxhcj@qq.com
		 * @date 2019/9/16 9:33
		 **/
		Long getBusinessId(T t);

		/**
		 * 获取公司ID
		 *
		 * @param t t
		 * @return java.lang.Long
		 * @author xyyxhcj@qq.com
		 * @date 2019/9/16 9:34
		 **/
		Long getCompanyId(T t);
	}
}
