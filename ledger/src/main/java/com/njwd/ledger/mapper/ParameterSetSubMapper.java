package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.ledger.ParameterSetSub;
import com.njwd.entity.ledger.vo.ParameterSetVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description 参数设置子表 Mapper
 * @Date 2019/10/16 15:18
 * @Author 郑勇浩
 */
public interface ParameterSetSubMapper extends BaseMapper<ParameterSetSub> {

	/**
	 * @Description 批量新增参数设置子表行
	 * @Author 郑勇浩
	 * @Data 2019/10/17 10:15
	 * @Param [parameterSetDto]
	 * @return int
	 */
	int insertParameterSetSubBatch(@Param("parameterSetVoList") List<ParameterSetVo> parameterSetVoList);

	/**
	 * @Description 批量更新参数设置子表行
	 * @Author 郑勇浩
	 * @Data 2019/10/18 10:36
	 * @Param [parameterSetVoList]
	 * @return int
	 */
	int updateParameterSetSubBatch(@Param("parameterSetVoList") List<ParameterSetVo> parameterSetVoList);
}
