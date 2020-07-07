package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.LogConstant;
import com.njwd.entity.platform.dto.TaxCategoryDto;
import com.njwd.entity.platform.vo.SysUserVo;
import com.njwd.entity.platform.vo.TaxCategoryVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.logger.SenderService;
import com.njwd.platform.service.TaxCategoryService;
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
 * @Description 税种 controller
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@RestController
@RequestMapping("taxCategory")
public class TaxCategoryController extends BaseController {

	@Resource
	private TaxCategoryService taxCategoryService;
	@Resource
	private SenderService senderService;

	/**
	 * @Description 新增税种
	 * @Author 郑勇浩
	 * @Data 2019/11/13 14:17
	 * @Param [param]
	 * @return com.njwd.support.Result<java.lang.Long>
	 */
	@PostMapping("addTaxCategory")
	public Result<Long> addTaxCategory(@RequestBody TaxCategoryDto param) {
		FastUtils.checkParams(param.getName(), param.getTaxSystemId(), param.getPrecision(), param.getRoundingType(), param.getIsVat());
		//用户信息
		SysUserVo userInfo = UserUtil.getUserVo();
		param.setCreatorId(userInfo.getUserId());
		param.setCreatorName(userInfo.getName());
		//新增
		if (taxCategoryService.addTaxCategory(param) > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.taxCategory, LogConstant.operation.add, LogConstant.operation.add_type, param.getId().toString()));
		}

		return ok(param.getId());
	}

	/**
	 * @Description 更新税种
	 * @Author 郑勇浩
	 * @Data 2019/11/13 14:17
	 * @Param [param]
	 * @return com.njwd.support.Result<java.lang.Long>
	 */
	@PostMapping("updateTaxCategory")
	public Result<Long> updateTaxCategory(@RequestBody TaxCategoryDto param) {
		FastUtils.checkParams(param.getId(), param.getName(), param.getTaxSystemId(), param.getPrecision(), param.getRoundingType(), param.getIsVat(), param.getVersion());
		//用户信息
		SysUserVo userInfo = UserUtil.getUserVo();
		param.setUpdatorId(userInfo.getUserId());
		param.setUpdatorName(userInfo.getName());
		//更新
		if (taxCategoryService.updateTaxCategory(param) > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.taxCategory, LogConstant.operation.update, LogConstant.operation.update_type, param.getId().toString()));
		}
		return ok(param.getId());
	}

	/**
	 * @Description 批量删除税种
	 * @Author 郑勇浩
	 * @Data 2019/11/12 16:05
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.support.BatchResult>
	 */
	@PostMapping("deleteTaxCategory")
	public Result<BatchResult> deleteTaxCategory(@RequestBody TaxCategoryDto param) {
		//非空校验
		changeListNotNull(param);
		//批量删除
		BatchResult result = taxCategoryService.deleteTaxCategory(param);
		if (result.getSuccessList().size() > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.taxCategory, LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 批量审核税种
	 * @Author 郑勇浩
	 * @Data 2019/11/12 16:05
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.support.BatchResult>
	 */
	@PostMapping("approvedTaxCategory")
	public Result<BatchResult> approvedTaxCategory(@RequestBody TaxCategoryDto param) {
		//非空校验
		changeListNotNull(param);
		//批量审核
		BatchResult result = taxCategoryService.approvedTaxCategory(param);
		if (result.getSuccessList().size() > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.taxCategory, LogConstant.operation.approve, LogConstant.operation.approve_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 批量反审核税种
	 * @Author 郑勇浩
	 * @Data 2019/11/12 16:05
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.support.BatchResult>
	 */
	@PostMapping("disapprovedTaxCategory")
	public Result<BatchResult> disapprovedTaxCategory(@RequestBody TaxCategoryDto param) {
		//非空校验
		changeListNotNull(param);
		//批量反审核
		BatchResult result = taxCategoryService.disapprovedTaxCategory(param);
		if (result.getSuccessList().size() > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.taxCategory, LogConstant.operation.reversalApprove, LogConstant.operation.reversalApprove_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 批量发布
	 * @Author 郑勇浩
	 * @Data 2019/11/13 11:08
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.support.BatchResult>
	 */
	@PostMapping("releasedTaxCategory")
	public Result<BatchResult> releasedTaxCategory(@RequestBody TaxCategoryDto param) {
		//非空校验
		changeListNotNull(param);
		//批量发布
		BatchResult result = taxCategoryService.releasedTaxCategory(param);
		if (result.getSuccessList().size() > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.taxCategory, LogConstant.operation.release, LogConstant.operation.release_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 查询税种
	 * @Author 郑勇浩
	 * @Data 2019/11/12 15:30
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.entity.platform.vo.TaxCategoryVo>
	 */
	@RequestMapping("findTaxCategory")
	public Result<TaxCategoryVo> findTaxCategory(@RequestBody TaxCategoryDto param) {
		FastUtils.checkParams(param.getId());
		return ok(taxCategoryService.findTaxCategory(param));
	}

	/**
	 * @Description 查询税种[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 9:46
	 * @Param [currencyDto]
	 * @return com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page < com.njwd.entity.platform.vo.CurrencyVo>>
	 */
	@RequestMapping("findTaxCategoryPage")
	public Result<Page<TaxCategoryVo>> findTaxCategoryPage(@RequestBody TaxCategoryDto param) {
		return ok(taxCategoryService.findTaxCategoryPage(param));
	}

	/**
	 * @Description 查询税种[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 14:33
	 * @Param [param]
	 * @return com.njwd.support.Result<java.util.List < com.njwd.entity.basedata.TaxCategory>>
	 */
	@RequestMapping("findTaxCategoryList")
	public Result<List<TaxCategoryVo>> findTaxCategoryList(@RequestBody TaxCategoryDto param) {
		return ok(taxCategoryService.findTaxCategoryList(param));
	}

	/**
	 * @Description 导出EXCEL
	 * @Author 郑勇浩
	 * @Data 2019/11/19 17:36
	 * @Param [response, param]
	 */
	@RequestMapping("exportExcel")
	public void exportExcel(HttpServletResponse response, @RequestBody TaxCategoryDto param) {
		taxCategoryService.exportExcel(response, param);
	}

	/**
	 * @Description 批量数组不能为空
	 * @Author 郑勇浩
	 * @Data 2019/11/12 17:13
	 * @Param [param]
	 */
	private void changeListNotNull(TaxCategoryDto param) {
		//判断数组是否为空
		if (CollectionUtils.isEmpty(param.getChangeList())) {
			throw new ServiceException(ResultCode.PARAMS_NOT);
		}
		//判断id是否为空
		param.getChangeList().forEach(o -> FastUtils.checkParams(o.getId(), o.getVersion()));
	}

}
