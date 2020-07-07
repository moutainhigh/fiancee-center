package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.LogConstant;
import com.njwd.entity.platform.dto.TaxSystemDto;
import com.njwd.entity.platform.vo.SysUserVo;
import com.njwd.entity.platform.vo.TaxSystemVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.logger.SenderService;
import com.njwd.platform.service.TaxSystemService;
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
 * @Description 税收制度 controller
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@RestController
@RequestMapping("taxSystem")
public class TaxSystemController extends BaseController {

	@Resource
	private TaxSystemService taxSystemService;
	@Resource
	private SenderService senderService;

	/**
	 * @Description 新增税制
	 * @Author 郑勇浩
	 * @Data 2019/11/13 17:46
	 * @Param [param]
	 * @return com.njwd.support.Result<java.lang.Long>
	 */
	@PostMapping("addTaxSystem")
	public Result<Long> addTaxSystem(@RequestBody TaxSystemDto param) {
		FastUtils.checkParams(param.getName(), param.getAreaId(), param.getCurrencyId());
		//用户信息
		SysUserVo userInfo = UserUtil.getUserVo();
		param.setCreatorId(userInfo.getUserId());
		param.setCreatorName(userInfo.getName());
		//新增
		if (taxSystemService.addTaxSystem(param) > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.taxSystem, LogConstant.operation.add, LogConstant.operation.add_type, param.getId().toString()));
		}
		return ok(param.getId());
	}

	/**
	 * @Description 更新税制
	 * @Author 郑勇浩
	 * @Data 2019/11/14 14:57
	 * @Param [param]
	 * @return com.njwd.support.Result<java.lang.Long>
	 */
	@PostMapping("updateTaxSystem")
	public Result<Long> updateTaxSystem(@RequestBody TaxSystemDto param) {
		FastUtils.checkParams(param.getId(), param.getName(), param.getAreaId(), param.getCurrencyId(), param.getVersion());
		//用户信息
		SysUserVo userInfo = UserUtil.getUserVo();
		param.setUpdatorId(userInfo.getUserId());
		param.setUpdatorName(userInfo.getName());
		//更新
		if (taxSystemService.updateTaxSystem(param) > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.taxSystem, LogConstant.operation.update, LogConstant.operation.update_type, param.getId().toString()));
		}
		return ok(param.getId());
	}

	/**
	 * @Description 批量删除税制
	 * @Author 郑勇浩
	 * @Data 2019/11/12 16:05
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.support.BatchResult>
	 */
	@PostMapping("deleteTaxSystem")
	public Result<BatchResult> deleteTaxSystem(@RequestBody TaxSystemDto param) {
		//非空校验
		changeListNotNull(param);
		//批量删除
		BatchResult result = taxSystemService.deleteTaxSystem(param);
		if (result.getSuccessList().size() > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.taxSystem, LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 批量审核税制
	 * @Author 郑勇浩
	 * @Data 2019/11/12 16:05
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.support.BatchResult>
	 */
	@PostMapping("approvedTaxSystem")
	public Result<BatchResult> approvedTaxSystem(@RequestBody TaxSystemDto param) {
		//非空校验
		changeListNotNull(param);
		//批量审核
		BatchResult result = taxSystemService.approvedTaxSystem(param);
		if (result.getSuccessList().size() > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.taxSystem, LogConstant.operation.approve, LogConstant.operation.approve_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 批量反审核税制
	 * @Author 郑勇浩
	 * @Data 2019/11/12 16:05
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.support.BatchResult>
	 */
	@PostMapping("disapprovedTaxSystem")
	public Result<BatchResult> disapprovedTaxSystem(@RequestBody TaxSystemDto param) {
		//非空校验
		changeListNotNull(param);
		//批量反审核
		BatchResult result = taxSystemService.disapprovedTaxSystem(param);
		if (result.getSuccessList().size() > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.taxSystem, LogConstant.operation.reversalApprove, LogConstant.operation.reversalApprove_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 批量发布税制
	 * @Author 郑勇浩
	 * @Data 2019/11/13 11:08
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.support.BatchResult>
	 */
	@PostMapping("releasedTaxSystem")
	public Result<BatchResult> releasedTaxSystem(@RequestBody TaxSystemDto param) {
		//非空校验
		changeListNotNull(param);
		//批量发布
		BatchResult result = taxSystemService.releasedTaxSystem(param);
		if (result.getSuccessList().size() > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.taxSystem, LogConstant.operation.release, LogConstant.operation.release_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}


	/**
	 * @Description 查询税制
	 * @Author 郑勇浩
	 * @Data 2019/11/13 18:04
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.entity.platform.vo.TaxSystemVo>
	 */
	@RequestMapping("findTaxSystem")
	public Result<TaxSystemVo> findTaxSystem(@RequestBody TaxSystemDto param) {
		FastUtils.checkParams(param.getId());
		return ok(taxSystemService.findTaxSystem(param));
	}

	/**
	 * @Description 查询税制[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/13 17:15
	 * @Param [param]
	 * @return com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page < com.njwd.entity.platform.vo.TaxSystemVo>>
	 */
	@RequestMapping("findTaxSystemPage")
	public Result<Page<TaxSystemVo>> findTaxSystemPage(@RequestBody TaxSystemDto param) {
		return ok(taxSystemService.findTaxSystemPage(param));
	}

	/**
	 * @Description 查询税制[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/13 17:15
	 * @Param [param]
	 * @return com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page < com.njwd.entity.platform.vo.TaxSystemVo>>
	 */
	@RequestMapping("findTaxSystemList")
	public Result<List<TaxSystemVo>> findTaxSystemList(@RequestBody TaxSystemDto param) {
		return ok(taxSystemService.findTaxSystemList(param));
	}

	/**
	 * @Description 导出EXCEL
	 * @Author 郑勇浩
	 * @Data 2019/11/19 17:36
	 * @Param [response, param]
	 */
	@RequestMapping("exportExcel")
	public void exportExcel(HttpServletResponse response, @RequestBody TaxSystemDto param) {
		taxSystemService.exportExcel(response, param);
	}

	/**
	 * @Description 批量数组不能为空
	 * @Author 郑勇浩
	 * @Data 2019/11/12 17:13
	 * @Param [param]
	 */
	private void changeListNotNull(TaxSystemDto param) {
		//判断数组是否为空
		if (CollectionUtils.isEmpty(param.getChangeList())) {
			throw new ServiceException(ResultCode.PARAMS_NOT);
		}
		//判断id是否为空
		param.getChangeList().forEach(o -> FastUtils.checkParams(o.getId(), o.getVersion()));
	}

}
