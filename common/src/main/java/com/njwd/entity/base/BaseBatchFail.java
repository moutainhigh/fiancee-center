package com.njwd.entity.base;

import com.njwd.utils.FastUtils;
import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * 批量处理校验逻辑个体
 *
 * @author xyyxhcj@qq.com
 * @since 2019/11/15
 */
public abstract class BaseBatchFail<T, E extends T> {
	/**
	 * 获取实体ID
	 *
	 * @param e e
	 * @return I
	 * @author xyyxhcj@qq.com
	 * @date 2019/11/15 10:11
	 **/
	public abstract Long getId(E e);

	/**
	 * 获取待操作数据ids
	 *
	 * @param eList eList
	 * @return java.util.LinkedHashSet<java.lang.Long>
	 * @author xyyxhcj@qq.com
	 * @date 2019/11/20 11:29
	 **/
	public LinkedHashSet<Long> getCheckIds(List<E> eList) {
		LinkedHashSet<Long> checkIds = new LinkedHashSet<>();
		for (E e : eList) {
			Long id = getId(e);
			FastUtils.checkParams(id);
			checkIds.add(id);
		}
		return checkIds;
	}

	public List<BaseEachFail<T, E>> checkFails = new LinkedList<>();

	@Getter
	public abstract static class BaseEachFail<T, E extends T> {
		/**
		 * 失败信息
		 **/
		private String failMsg;

		protected BaseEachFail(String failMsg) {
			this.failMsg = failMsg;
		}

		/**
		 * 操作是否失败
		 *
		 * @param t t
		 * @param e e
		 * @return boolean
		 * @author xyyxhcj@qq.com
		 * @date 2019/11/15 10:09
		 **/
		public abstract boolean isFail(T t, E e);
	}
}
