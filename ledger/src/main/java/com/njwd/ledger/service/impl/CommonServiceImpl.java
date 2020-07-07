package com.njwd.ledger.service.impl;

import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.dto.ParameterSetDto;
import com.njwd.entity.ledger.vo.ParameterSetVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.ledger.service.CommonService;
import com.njwd.ledger.service.ParameterSetService;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/09/20
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CommonServiceImpl implements CommonService {
	@Resource
	private ParameterSetService parameterSetService;

	/**
	 * 获取总账参数设置
	 *
	 * @param operator operator
	 * @return com.njwd.entity.ledger.vo.ParameterSetVo
	 * @author xyyxhcj@qq.com
	 * @date 2019/9/20 9:44
	 **/
	@Override
	public @NotNull ParameterSetVo getParameterSet(@Nullable SysUserVo operator) {
		if (operator == null) {
			operator = UserUtils.getUserVo();
		}
		ParameterSetDto parameterSetDto = new ParameterSetDto();
		parameterSetDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		ParameterSetVo parameterSet = parameterSetService.findParameterSet(parameterSetDto);
		if (parameterSet == null) {
			throw new ServiceException(ResultCode.PARAMETER_SET_NOT_EXIST);
		}
		return parameterSet;
	}
}
