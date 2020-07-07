package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.LogConstant;
import com.njwd.entity.platform.dto.TaxRateDto;
import com.njwd.entity.platform.vo.SysUserVo;
import com.njwd.entity.platform.vo.TaxRateVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.logger.SenderService;
import com.njwd.platform.service.TaxRateService;
import com.njwd.platform.utils.UserUtil;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description 税率 controller
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@RestController
@RequestMapping("taxRate")
public class TaxRateController extends BaseController {

	@Resource
	private TaxRateService taxRateService;
	@Resource
	private SenderService senderService;

	/**
	 * @Description 新增税率
	 * @Author 郑勇浩
	 * @Data 2019/11/15 15:25
	 * @Param [param]
	 * @return com.njwd.support.Result<java.lang.Long>
	 */
	@PostMapping("addTaxRate")
	public Result<Long> addTaxRate(@RequestBody TaxRateDto param) {
		FastUtils.checkParams(param.getTaxSystemId(), param.getTaxCategoryId(), param.getTaxRate());
		//用户信息
		SysUserVo userInfo = UserUtil.getUserVo();
		param.setCreatorId(userInfo.getUserId());
		param.setCreatorName(userInfo.getName());
		//新增
		if (taxRateService.addTaxRate(param) > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.taxRate, LogConstant.operation.add, LogConstant.operation.add_type, param.getId().toString()));
		}
		return ok(param.getId());
	}

	/**
	 * @Description 更新税率
	 * @Author 郑勇浩
	 * @Data 2019/11/14 14:57
	 * @Param [param]
	 * @return com.njwd.support.Result<java.lang.Long>
	 */
	@PostMapping("updateTaxRate")
	public Result<Long> updateTaxRate(@RequestBody TaxRateDto param) {
		FastUtils.checkParams(param.getId(), param.getTaxSystemId(), param.getTaxCategoryId(), param.getTaxRate(), param.getVersion());
		//用户信息
		SysUserVo userInfo = UserUtil.getUserVo();
		param.setUpdatorId(userInfo.getUserId());
		param.setUpdatorName(userInfo.getName());
		//更新
		if (taxRateService.updateTaxRate(param) > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.taxRate, LogConstant.operation.update, LogConstant.operation.update_type, param.getId().toString()));
		}
		return ok(param.getId());
	}

	/**
	 * @Description 批量删除税率
	 * @Author 郑勇浩
	 * @Data 2019/11/12 16:05
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.support.BatchResult>
	 */
	@PostMapping("deleteTaxRate")
	public Result<BatchResult> deleteTaxRate(@RequestBody TaxRateDto param) {
		//非空校验
		changeListNotNull(param);
		//批量删除
		BatchResult result = taxRateService.deleteTaxRate(param);
		if (result.getSuccessList().size() > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.taxRate, LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 批量审核税率
	 * @Author 郑勇浩
	 * @Data 2019/11/12 16:05
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.support.BatchResult>
	 */
	@PostMapping("approvedTaxRate")
	public Result<BatchResult> approvedTaxRate(@RequestBody TaxRateDto param) {
		//非空校验
		changeListNotNull(param);
		//批量审核
		BatchResult result = taxRateService.approvedTaxRate(param);
		if (result.getSuccessList().size() > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.taxRate, LogConstant.operation.approve, LogConstant.operation.approve_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 批量反审核税率
	 * @Author 郑勇浩
	 * @Data 2019/11/12 16:05
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.support.BatchResult>
	 */
	@PostMapping("disapprovedTaxRate")
	public Result<BatchResult> disapprovedTaxRate(@RequestBody TaxRateDto param) {
		//非空校验
		changeListNotNull(param);
		//批量反审核
		BatchResult result = taxRateService.disapprovedTaxRate(param);
		if (result.getSuccessList().size() > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.taxRate, LogConstant.operation.reversalApprove, LogConstant.operation.reversalApprove_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 批量发布税率
	 * @Author 郑勇浩
	 * @Data 2019/11/13 11:08
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.support.BatchResult>
	 */
	@PostMapping("releasedTaxRate")
	public Result<BatchResult> releasedTaxRate(@RequestBody TaxRateDto param) {
		//非空校验
		changeListNotNull(param);
		//批量发布
		BatchResult result = taxRateService.releasedTaxRate(param);
		if (result.getSuccessList().size() > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.taxRate, LogConstant.operation.release, LogConstant.operation.release_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 查询税率
	 * @Author 郑勇浩
	 * @Data 2019/11/13 18:04
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.entity.platform.vo.TaxRateVo>
	 */
	@RequestMapping("findTaxRate")
	public Result<TaxRateVo> findTaxRate(@RequestBody TaxRateDto param) {
		FastUtils.checkParams(param.getId());
		return ok(taxRateService.findTaxRate(param));
	}

	/**
	 * @Description 查询税率[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/13 17:15
	 * @Param [param]
	 * @return com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page < com.njwd.entity.platform.vo.TaxRateVo>>
	 */
	@RequestMapping("findTaxRatePage")
	public Result<Page<TaxRateVo>> findTaxRatePage(@RequestBody TaxRateDto param) {
		return ok(taxRateService.findTaxRatePage(param));
	}

	/**
	 * @Description 查询税率[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/13 17:15
	 * @Param [param]
	 * @return com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page < com.njwd.entity.platform.vo.TaxRateVo>>
	 */
	@RequestMapping("findTaxRateList")
	public Result<List<TaxRateVo>> findTaxRateList(@RequestBody TaxRateDto param) {
		return ok(taxRateService.findTaxRateList(param));
	}

	/**
	 * @Description 导出EXCEL
	 * @Author 郑勇浩
	 * @Data 2019/11/19 17:36
	 * @Param [response, param]
	 */
	@RequestMapping("exportExcel")
	public void exportExcel(HttpServletResponse response, @RequestBody TaxRateDto param) {
		taxRateService.exportExcel(response, param);
	}

	/**
	 * @Description 批量数组不能为空
	 * @Author 郑勇浩
	 * @Data 2019/11/12 17:13
	 * @Param [param]
	 */
	private void changeListNotNull(TaxRateDto param) {
		//判断数组是否为空
		if (CollectionUtils.isEmpty(param.getChangeList())) {
			throw new ServiceException(ResultCode.PARAMS_NOT);
		}
		//判断id是否为空
		param.getChangeList().forEach(o -> FastUtils.checkParams(o.getId(), o.getVersion()));
	}

}
