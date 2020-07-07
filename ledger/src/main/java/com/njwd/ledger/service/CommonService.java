package com.njwd.ledger.service;

import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.vo.ParameterSetVo;

/**
 * 公共资源
 *
 * @author xyyxhcj@qq.com
 * @since 2019-09-20
 */

public interface CommonService {
	/**
	 * 获取总账参数设置
	 *
	 * @param operator operator
	 * @return com.njwd.entity.ledger.vo.ParameterSetVo
	 * @author xyyxhcj@qq.com
	 * @date 2019/9/20 9:44
	 **/
	ParameterSetVo getParameterSet(SysUserVo operator);
}
