package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.ledger.ParameterSet;
import com.njwd.entity.ledger.dto.ParameterSetDto;
import com.njwd.entity.ledger.vo.ParameterSetVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description 参数设置 Mapper
 * @Date 2019/10/16 15:18
 * @Author 郑勇浩
 */
public interface ParameterSetMapper extends BaseMapper<ParameterSet> {

	/**
	 * @Description 查询租户总账参数选项
	 * @Author 郑勇浩
	 * @Data 2019/10/17 13:49
	 * @Param [parameterSetDto]
	 * @return java.util.HashMap<java.lang.String, java.util.List < com.njwd.entity.basedata.vo.ParameterSetVo>>
	 */
	List<ParameterSetVo> selectParameterSet(@Param("parameterSetDto") ParameterSetDto parameterSetDto);

	/**
	 * @Description 查询租户总账参数行
	 * @Author 郑勇浩
	 * @Data 2019/10/18 9:18
	 * @Param [parameterSetDto]
	 * @return java.util.List<com.njwd.entity.ledger.vo.ParameterSetVo>
	 */
	List<ParameterSetVo> selectParameterSetById(@Param("parameterSetDto") ParameterSetDto parameterSetDto);

	/**
	 * @Description 查询租户参数设置数量[判断是否已经初始化]
	 * @Author 郑勇浩
	 * @Data 2019/10/16 19:49
	 * @Param [parameterSetDto]
	 * @return int
	 */
	int selectParameterSetCount(@Param("parameterSetDto") ParameterSetDto parameterSetDto);

	/**
	 * @Description 批量新增参数设置行
	 * @Author 郑勇浩
	 * @Data 2019/10/17 10:15
	 * @Param [parameterSetDto]
	 * @return int
	 */
	int insertParameterSetBatch(@Param("parameterSetVoList") List<ParameterSetVo> parameterSetVoList);

	/**
	 * @Description 批量更新参数设置行
	 * @Author 郑勇浩
	 * @Data 2019/10/18 13:37
	 * @Param [parameterSetDto]
	 * @return int
	 */
	int updateParameterSetBatch(@Param("parameterSetDto") ParameterSetDto parameterSetDto);

	/**
	 * @Description 批量删除参数设置行
	 * @Author 郑勇浩
	 * @Data 2019/10/18 18:09
	 * @Param [parameterSetDto]
	 * @return int
	 */
	int deleteParameterSetBatch(@Param("parameterSetDto") ParameterSetDto parameterSetDto);
}
