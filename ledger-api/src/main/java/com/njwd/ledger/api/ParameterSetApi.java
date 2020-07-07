package com.njwd.ledger.api;

import com.njwd.entity.ledger.dto.ParameterSetDto;
import com.njwd.entity.ledger.vo.ParameterSetVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * @Description 参数设置 Api
 * @Date 2019/10/16 15:11
 * @Author 郑勇浩
 */
@RequestMapping("ledger/parameterSet")
public interface ParameterSetApi {

	/**
	 * @Description 初始化参数设置
	 * @Author 郑勇浩
	 * @Data 2019/10/16 19:57
	 * @Param [param]
	 * @return com.njwd.support.Result<java.lang.Boolean>
	 */
	@RequestMapping("initParameterSet")
	Result<Boolean> initParameterSet(@RequestBody ParameterSetDto param);

	/**
	 * @Description 删除总账个性化账簿设置
	 * @Author 郑勇浩
	 * @Data 2019/10/18 15:34
	 * @Param [param]
	 * @return boolean
	 */
	@RequestMapping("deleteParameterSet")
	Result<Boolean> deleteParameterSet(@RequestBody ParameterSetDto param);

	/**
	 * @Description 修改总账参数设置
	 * @Author 郑勇浩
	 * @Data 2019/10/21 14:36
	 * @Param [param]
	 * @return com.njwd.support.Result<java.lang.Boolean>
	 */
	@RequestMapping("updateParameterSet")
	Result<Boolean> updateParameterSet(@RequestBody ParameterSetDto param);

	/**
	 * @Description 查询总账参数(分GroupCode)
	 * @Author 郑勇浩
	 * @Data 2019/10/17 15:53
	 * @Param [param]
	 * @return com.njwd.support.Result<java.util.Map < java.lang.String, java.util.List < com.njwd.entity.ledger.vo.ParameterSetVo>>>
	 */
	@RequestMapping("findParameterSet")
	Result<Map<String, List<ParameterSetVo>>> findParameterSet(@RequestBody ParameterSetDto param);

	/**
	 * @Description 查询总账参数(分账簿)
	 * @Author 郑勇浩
	 * @Data 2019/10/17 15:56
	 * @Param [param]
	 * @return com.njwd.support.Result<java.util.Map < java.lang.Long, java.util.List < com.njwd.entity.ledger.vo.ParameterSetVo>>>
	 */
	@RequestMapping("findParameterSetByGroupCode")
	Result<Map<Long, List<ParameterSetVo>>> findParameterSetByGroupCode(@RequestBody ParameterSetDto param);

	/**
	 * @Description 查询对应配置的结果
	 * @Author 郑勇浩
	 * @Data 2019/10/18 16:27
	 * @Param [param]
	 * @return com.njwd.support.Result<java.util.Map < java.lang.Long, java.util.List < com.njwd.entity.ledger.vo.ParameterSetVo>>>
	 */
	@RequestMapping("findParameterSetValue")
	Result<ParameterSetVo> findParameterSetValue(@RequestBody ParameterSetDto param);
}
