package com.njwd.basedata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.service.CurrencyService;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.basedata.dto.CurrencyDto;
import com.njwd.entity.basedata.vo.CurrencyVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.logger.SenderService;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
import com.njwd.utils.UserUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description 币种 controller
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@RestController
@RequestMapping("currency")
public class CurrencyController extends BaseController {

	@Resource
	private CurrencyService currencyService;
	@Resource
	private SenderService senderService;

	/**
	 * @Description 批量引入币种
	 * @Author 郑勇浩
	 * @Data 2019/11/28 15:47
	 * @Param [param]
	 * @return com.njwd.support.Result<java.lang.Long>
	 */
	@PostMapping("addCurrencyBatch")
	public Result<Integer> addCurrencyBatch(@RequestBody CurrencyDto param) {
		isAdmin(param);
		//非空验证
		if (CollectionUtils.isEmpty(param.getChangeList())) {
			throw new ServiceException(ResultCode.PARAMS_NOT);
		}
		int result = currencyService.addCurrencyBatch(param);
		if (result > 0) {
			List<Long> idList = param.getChangeList().stream().map(CurrencyVo::getId).collect(Collectors.toList());
			senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.currency, LogConstant.operation.addBatch, LogConstant.operation.addBatch_type, idList.toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 更新币种
	 * @Author 郑勇浩
	 * @Data 2019/11/14 14:57
	 * @Param [param]
	 * @return com.njwd.support.Result<java.lang.Long>
	 */
	@PostMapping("updateCurrency")
	public Result<Long> updateCurrency(@RequestBody CurrencyDto param) {
		isAdmin(param);
		//必填
		FastUtils.checkParams(param.getId(), param.getPrecision(), param.getUnitPrecision(), param.getRoundingType(), param.getVersion());
		//登录用户
		SysUserVo userInfo = UserUtils.getUserVo();
		param.setUpdatorId(userInfo.getUserId());
		param.setUpdatorName(userInfo.getName());
		//更新
		if (currencyService.updateCurrency(param) > 0) {
			senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.currency, LogConstant.operation.update, LogConstant.operation.update_type, param.getId().toString()));
		}
		return ok(param.getId());
	}

	/**
	 * @Description 批量删除币种
	 * @Author 郑勇浩
	 * @Data 2019/12/2 9:52
	 * @Param
	 */
	@PostMapping("deleteBankAccountBatch")
	public Result<BatchResult> deleteBankAccountBatch(@RequestBody CurrencyDto bto) {
		//ids 为空直接返回
		changeListNotNull(bto);
		//DELETE 批量删除
		BatchResult result = currencyService.deleteCurrencyBatch(bto);
		if (result.getSuccessList().size() > 0) {
			senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.currency, LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 批量反禁用币种
	 * @Author 郑勇浩
	 * @Data 2019/12/2 9:57
	 * @Param [bto]
	 * @return com.njwd.support.Result<com.njwd.support.BatchResult>
	 */
	@PostMapping("enableBankAccountBatch")
	public Result<BatchResult> enableBankAccountBatch(@RequestBody CurrencyDto bto) {
		//ids 为空直接返回
		changeListNotNull(bto);
		//DELETE 批量删除
		BatchResult result = currencyService.enableCurrencyBatch(bto);
		if (result.getSuccessList().size() > 0) {
			senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.currency, LogConstant.operation.antiForbiddenBatch, LogConstant.operation.antiForbidden_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 禁用币种
	 * @Author 郑勇浩
	 * @Data 2019/12/2 9:57
	 * @Param [bto]
	 * @return com.njwd.support.Result<com.njwd.support.BatchResult>
	 */
	@PostMapping("disableBankAccountBatch")
	public Result<BatchResult> disableBankAccountBatch(@RequestBody CurrencyDto bto) {
		//ids 为空直接返回
		changeListNotNull(bto);
		//DELETE 批量删除
		BatchResult result = currencyService.disableCurrencyBatch(bto);
		if (result.getSuccessList().size() > 0) {
			senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.currency, LogConstant.operation.forbiddenBatch, LogConstant.operation.forbiddenBatch_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 查询币种
	 * @Author 郑勇浩
	 * @Data 2019/11/27 16:41
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.entity.platform.vo.CurrencyVo>
	 */
	@RequestMapping("findCurrency")
	public Result<CurrencyVo> findCurrency(@RequestBody CurrencyDto param) {
		FastUtils.checkParams(param.getId());
		return ok(currencyService.findCurrency(param));
	}

	/**
	 * @Description 查询币种[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/27 17:34
	 * @Param [param]
	 * @return com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page < com.njwd.entity.basedata.vo.CurrencyVo>>
	 */
	@RequestMapping("findCurrencyPage")
	public Result<Page<CurrencyVo>> findCurrencyPage(@RequestBody CurrencyDto param) {
		return ok(currencyService.findCurrencyPage(param));
	}

	/**
	 * @Description 查询平台币种[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/28 10:10
	 * @Param [param]
	 * @return com.njwd.support.Result<java.util.List < com.njwd.entity.basedata.vo.CurrencyVo>>
	 */
	@RequestMapping("findPlatformCurrencyList")
	public Result findPlatformCurrencyList(@RequestBody CurrencyDto param) {
		return currencyService.findPlatformCurrencyList(param);
	}

	/**
	 * @Description Excel 导出
	 * @Author 郑勇浩
	 * @Data 2019/12/3 15:58
	 * @Param [currencyDto, response]
	 */
	@RequestMapping("exportExcel")
	public void exportExcel(@RequestBody CurrencyDto currencyDto, HttpServletResponse response) {
		SysUserVo operator = UserUtils.getUserVo();
		currencyDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		currencyService.exportExcel(response, currencyDto);
	}

	/**
	 * @Description 判断是否管理员
	 * @Author 郑勇浩
	 * @Data 2019/11/29 10:32
	 * @Param [param]
	 */
	private void isAdmin(CurrencyDto param) {
		//如果是租户端
		if (param.getIsEnterpriseAdmin().equals(Constant.Is.NO)) {
			throw new ServiceException(ResultCode.PERMISSION_NOT);
		}
	}

	/**
	 * @Description 批量数组不能为空
	 * @Author 郑勇浩
	 * @Data 2019/12/2 9:54
	 * @Param [param]
	 */
	private void changeListNotNull(CurrencyDto param) {
		//判断数组是否为空
		if (CollectionUtils.isEmpty(param.getChangeList())) {
			throw new ServiceException(ResultCode.PARAMS_NOT);
		}
		//判断id是否为空
		param.getChangeList().forEach(o -> FastUtils.checkParams(o.getId(), o.getVersion()));
	}

}
