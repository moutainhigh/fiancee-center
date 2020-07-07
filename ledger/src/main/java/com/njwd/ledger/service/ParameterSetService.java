package com.njwd.ledger.service;

import com.njwd.entity.ledger.dto.ParameterSetDto;
import com.njwd.entity.ledger.vo.ParameterSetVo;

import java.util.List;
import java.util.Map;

/**
 * @Description 参数设置 Service
 * @Date 2019/10/16 15:11
 * @Author 郑勇浩
 */
public interface ParameterSetService {

	/**
	 * @Description 初始化总账参数
	 * @Author 郑勇浩
	 * @Data 2019/10/17 13:51
	 * @Param [param]
	 * @return boolean
	 */
	boolean initParameterSet(ParameterSetDto param);

	/**
	 * @Description 修改总账参数设置
	 * @Author 郑勇浩
	 * @Data 2019/10/17 16:46
	 * @Param [param]
	 * @return boolean
	 */
	boolean updateParameterSet(ParameterSetDto param);

	/**
	 * @Description 删除总账个性化账簿设置
	 * @Author 郑勇浩
	 * @Data 2019/10/18 15:34
	 * @Param [param]
	 * @return boolean
	 */
	boolean deleteParameterSet(ParameterSetDto param);

	/**
	 * @Description 查询租户所有的总账参数
	 * @Author 郑勇浩
	 * @Data 2019/10/18 16:04
	 * @Param [param]
	 * @return java.util.List<com.njwd.entity.ledger.vo.ParameterSetVo>
	 */
	List<ParameterSetVo> findAllParameterSet(ParameterSetDto param);

	/**
	 * @Description 查询租户级别总账参数[GroupCode为key]
	 * @Author 郑勇浩
	 * @Data 2019/10/17 15:53
	 * @Param [param]
	 * @return com.njwd.support.Result<java.util.Map < java.lang.String, java.util.List < com.njwd.entity.ledger.vo.ParameterSetVo>>>
	 */
	Map<String, List<ParameterSetVo>> findParameterSetAdmin(ParameterSetDto param);

	/**
	 * @Description 查询个性化总账参数
	 * @Author 郑勇浩
	 * @Data 2019/10/17 15:56
	 * @Param [param]
	 * @return java.util.Map<java.lang.Long, java.util.List < com.njwd.entity.ledger.vo.ParameterSetVo>>
	 */
	Map<Long, List<ParameterSetVo>> findParameterSetPersonal(ParameterSetDto param);

	/**
	 * @Description 查询总账参数设置的值
	 * @Author 郑勇浩
	 * @Data 2019/10/18 16:30
	 * @Param [param]
	 * @return com.njwd.entity.ledger.vo.ParameterSetVo
	 */
	ParameterSetVo findParameterSetValue(ParameterSetDto param);

	/**
	 * 查询总账参数设置
	 *
	 * @param parameterSetDto parameterSetDto
	 * @return com.njwd.entity.ledger.vo.ParameterSetVo
	 * @author xyyxhcj@qq.com
	 * @date 2019/10/22 9:01
	 **/
	ParameterSetVo findParameterSet(ParameterSetDto parameterSetDto);
}
