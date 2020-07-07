package com.njwd.ledger.controller;

import com.alibaba.excel.util.CollectionUtils;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.ParameterSetSub;
import com.njwd.entity.ledger.dto.ParameterSetDto;
import com.njwd.entity.ledger.vo.ParameterSetVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.ledger.service.ParameterSetService;
import com.njwd.logger.SenderService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description 参数设置 Controller
 * @Date 2019/10/16 15:11
 * @Author 郑勇浩
 */
@RestController
@RequestMapping("parameterSet")
public class ParameterSetController extends BaseController {

	@Resource
	private SenderService senderService;
	@Resource
	private ParameterSetService parameterSetService;

	/**
	 * @Description 初始化参数设置
	 * @Author 郑勇浩
	 * @Data 2019/10/16 19:57
	 * @Param [param]
	 * @return com.njwd.support.Result<java.lang.Boolean>
	 */
	@RequestMapping("initParameterSet")
	public Result<Boolean> initParameterSet(@RequestBody ParameterSetDto param) {
		// 非空验证
		List<ParameterSetVo> parameterSetVoList = param.getParameterSetVoList();
		if (CollectionUtils.isEmpty(parameterSetVoList)) {
			throw new ServiceException(ResultCode.PARAMS_NOT);
		}

		SysUserVo operator = UserUtils.getUserVo();
		for (ParameterSetVo psv : parameterSetVoList) {
			//必填校验
			FastUtils.checkParams(psv.getGroupCode(), psv.getGroupName(), psv.getGroupDesc(),
					psv.getKey(), psv.getName(), psv.getDesc(), psv.getModifyType(), psv.getIsPersonal(), psv.getValue());
			// 内容赋值
			if (psv.getRowNum() == null) {
				psv.setRowNum(Constant.Number.ANTI_INITLIZED);
			}
			psv.setCreatorId(operator.getUserId());
			psv.setCreatorName(operator.getName());
			psv.setRootEnterpriseId(operator.getRootEnterpriseId());
			psv.setAccountBookId(Constant.Number.ZEROL);
			psv.setAccountBookName(Constant.Character.NULL_VALUE);
			psv.setVersion(Constant.Number.ZERO);
		}
		param.setRootEnterpriseId(operator.getRootEnterpriseId());
		// 初始化操作
		boolean returnResult = RedisUtils.lock(String.format(Constant.LockKey.PARAMETERSET, operator.getRootEnterpriseId()), Constant.SysConfig.REDIS_LOCK_TIMEOUT, () -> parameterSetService.initParameterSet(param));
		// LOG
		if (returnResult) {
			List<Long> idList = param.getParameterSetVoList().stream().map(ParameterSetVo::getId).collect(Collectors.toList());
			senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.parameterSet, LogConstant.operation.init, LogConstant.operation.init_type, idList.toString()));
		}
		return ok(returnResult);
	}

	/**
	 * @Description 删除总账个性化账簿设置
	 * @Author 郑勇浩
	 * @Data 2019/10/18 15:34
	 * @Param [param]
	 * @return boolean
	 */
	@RequestMapping("deleteParameterSet")
	public Result<Boolean> deleteParameterSet(@RequestBody ParameterSetDto param) {
		// 非空验证
		if (CollectionUtils.isEmpty(param.getSubIdList())) {
			throw new ServiceException(ResultCode.PARAMS_NOT);
		}
		// 用户信息
		SysUserVo operator = UserUtils.getUserVo();
		param.setRootEnterpriseId(operator.getRootEnterpriseId());
		param.setUpdatorId(operator.getUpdatorId());
		param.setUpdatorName(operator.getUpdatorName());
		// 删除操作
		boolean returnResult = RedisUtils.lock(String.format(Constant.LockKey.PARAMETERSET, operator.getRootEnterpriseId()), Constant.SysConfig.REDIS_LOCK_TIMEOUT, () -> parameterSetService.deleteParameterSet(param));
		// LOG
		if (returnResult) {
			senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.parameterSet, LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, param.getSubIdList().toString()));
		}
		return ok(returnResult);
	}

	/**
	 * @Description 修改总账参数设置
	 * @Author 郑勇浩
	 * @Data 2019/10/21 14:36
	 * @Param [param]
	 * @return com.njwd.support.Result<java.lang.Boolean>
	 */
	@RequestMapping("updateParameterSet")
	public Result<Boolean> updateParameterSet(@RequestBody ParameterSetDto param) {
		// 非空验证
		FastUtils.checkParams(param.getIsPersonal());
		List<ParameterSetVo> parameterSetVoList = param.getParameterSetVoList();
		if (CollectionUtils.isEmpty(parameterSetVoList)) {
			return ok(Boolean.TRUE);
		}
		for (ParameterSetVo psv : parameterSetVoList) {
			FastUtils.checkParams(psv.getId(), psv.getAccountBookId(), psv.getModifyType(), psv.getValue());
			//租户:账簿名称不必填 账簿:账簿名称必填
			if (psv.getAccountBookId().equals(Constant.Number.ZEROL)) {
				psv.setAccountBookName(Constant.Character.NULL_VALUE);
			} else if (StringUtil.isBlank(psv.getAccountBookName())) {
				throw new ServiceException(ResultCode.PARAMS_NOT);
			}
		}
		// 用户信息
		SysUserVo operator = UserUtils.getUserVo();
		param.setRootEnterpriseId(operator.getRootEnterpriseId());
		param.setUpdatorId(operator.getUpdatorId());
		param.setUpdatorName(operator.getUpdatorName());
		// 批量新增或更新
		boolean returnResult = RedisUtils.lock(String.format(Constant.LockKey.PARAMETERSET, operator.getRootEnterpriseId()), Constant.SysConfig.REDIS_LOCK_TIMEOUT, () -> parameterSetService.updateParameterSet(param));
		// LOG
		if (returnResult) {
			senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.parameterSet, LogConstant.operation.update, LogConstant.operation.update_type, param.getIdList().toString()));
		}
		return ok(returnResult);
	}

	/**
	 * @Description 查询总账参数(分GroupCode)
	 * @Author 郑勇浩
	 * @Data 2019/10/17 15:53
	 * @Param [param]
	 * @return com.njwd.support.Result<java.util.Map < java.lang.String, java.util.List < com.njwd.entity.ledger.vo.ParameterSetVo>>>
	 */
	@RequestMapping("findParameterSet")
	public Result<Map<String, List<ParameterSetVo>>> findParameterSet(@RequestBody ParameterSetDto param) {
		// 租户级别
		SysUserVo operator = UserUtils.getUserVo();
		param.setRootEnterpriseId(operator.getRootEnterpriseId());
		param.setAccountBookId(Constant.Number.ZEROL);
		return ok(parameterSetService.findParameterSetAdmin(param));
	}

	/**
	 * @Description 查询总账参数(分账簿)
	 * @Author 郑勇浩
	 * @Data 2019/10/17 15:56
	 * @Param [param]
	 * @return com.njwd.support.Result<java.util.Map < java.lang.Long, java.util.List < com.njwd.entity.ledger.vo.ParameterSetVo>>>
	 */
	@RequestMapping("findParameterSetByGroupCode")
	public Result<Map<Long, List<ParameterSetVo>>> findParameterSetByGroupCode(@RequestBody ParameterSetDto param) {
		FastUtils.checkParams(param.getGroupCode());
		// 查询个性化
		SysUserVo operator = UserUtils.getUserVo();
		param.setRootEnterpriseId(operator.getRootEnterpriseId());
		return ok(parameterSetService.findParameterSetPersonal(param));
	}

	/**
	 * @Description 查询对应配置的结果
	 * @Author 郑勇浩
	 * @Data 2019/10/18 16:27
	 * @Param [param]
	 * @return com.njwd.support.Result<java.util.Map < java.lang.Long, java.util.List < com.njwd.entity.ledger.vo.ParameterSetVo>>>
	 */
	@RequestMapping("findParameterSetValue")
	public Result<ParameterSetVo> findParameterSetValue(@RequestBody ParameterSetDto param) {
		return ok(parameterSetService.findParameterSetValue(param));
	}

	/**
	 * @Description 查询所有的总账参数
	 * @Author 郑勇浩
	 * @Data 2019/10/28 16:15
	 * @Param [parameterSetDto]
	 * @return com.njwd.entity.ledger.vo.ParameterSetVo
	 */
	@RequestMapping("findParameterSetAll")
	public Result<Map<String, Map<Long, ParameterSetSub>>> findParameterSetAll(@RequestBody ParameterSetDto parameterSetDto) {
		SysUserVo operator = UserUtils.getUserVo();
		parameterSetDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		//查询groupKey为key的账簿对应value map
		ParameterSetVo returnVo = parameterSetService.findParameterSet(parameterSetDto);
		if (returnVo == null) {
			return null;
		}
		return ok(returnVo.getParamDict());
	}
}
