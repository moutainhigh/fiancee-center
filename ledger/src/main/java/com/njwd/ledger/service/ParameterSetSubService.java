package com.njwd.ledger.service;

import com.njwd.entity.ledger.dto.ParameterSetSubDto;
import com.njwd.entity.ledger.vo.ParameterSetVo;

import java.util.List;

/**
 * @Description 参数设置子表 Service
 * @Date 2019/10/16 15:11
 * @Author 郑勇浩
 */
public interface ParameterSetSubService {

	/**
	 * @Description 批量新增参数设置子表
	 * @Author 郑勇浩
	 * @Data 2019/10/17 11:46
	 * @Param [param]
	 * @return int
	 */
	int insertParameterSetSubBatch(List<ParameterSetVo> parameterSetVoList);

	/**
	 * @Description 批量修改参数设置子表
	 * @Author 郑勇浩
	 * @Data 2019/10/18 10:48
	 * @Param [parameterSetDto, existsList]
	 */
	void updateParameterSetSubBatch(ParameterSetSubDto parameterSetSubDt);
}
