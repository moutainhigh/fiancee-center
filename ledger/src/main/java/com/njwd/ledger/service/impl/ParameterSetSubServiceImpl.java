package com.njwd.ledger.service.impl;

import com.alibaba.excel.util.CollectionUtils;
import com.njwd.entity.ledger.dto.ParameterSetSubDto;
import com.njwd.entity.ledger.vo.ParameterSetVo;
import com.njwd.ledger.mapper.ParameterSetSubMapper;
import com.njwd.ledger.service.ParameterSetSubService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 参数设置子表 ServiceImpl
 * @Date 2019/10/16 15:11
 * @Author 郑勇浩
 */
@Service
public class ParameterSetSubServiceImpl implements ParameterSetSubService {

	@Resource
	private ParameterSetSubMapper parameterSetSubMapper;

	@Override
	public int insertParameterSetSubBatch(List<ParameterSetVo> parameterSetVoList) {
		return parameterSetSubMapper.insertParameterSetSubBatch(parameterSetVoList);
	}

	@Override
	public void updateParameterSetSubBatch(ParameterSetSubDto param) {
		// 新增或更新数据
		if (!CollectionUtils.isEmpty(param.getInsertList())) {
			parameterSetSubMapper.insertParameterSetSubBatch(param.getInsertList());
		}
		if (!CollectionUtils.isEmpty(param.getUpdateList())) {
			parameterSetSubMapper.updateParameterSetSubBatch(param.getUpdateList());
		}
	}


}
