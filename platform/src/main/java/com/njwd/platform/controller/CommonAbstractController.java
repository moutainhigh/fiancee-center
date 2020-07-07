package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.platform.dto.CommonAbstractDto;
import com.njwd.entity.platform.vo.CommonAbstractVo;
import com.njwd.entity.platform.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.logger.SenderService;
import com.njwd.platform.service.CommonAbstractService;
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
 * @Description 常用摘要 controller
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@RestController
@RequestMapping("commonAbstract")
public class CommonAbstractController extends BaseController {

	@Resource
	private CommonAbstractService commonAbstractService;
	@Resource
	private SenderService senderService;

	/**
	 * @Description 新增常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/11/13 14:17
	 * @Param [param]
	 * @return com.njwd.support.Result<java.lang.Long>
	 */
	@PostMapping("addCommonAbstract")
	public Result<Long> addCommonAbstract(@RequestBody CommonAbstractDto param) {
		FastUtils.checkParams(param.getAbstractContent());
		if (param.getAbstractCode() == null) {
			param.setAbstractCode(Constant.Character.NULL_VALUE);
		}
		//用户信息
		SysUserVo userInfo = UserUtil.getUserVo();
		param.setCreatorId(userInfo.getUserId());
		param.setCreatorName(userInfo.getName());
		//新增
		if (commonAbstractService.addCommonAbstract(param) > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.commonAbstract, LogConstant.operation.add, LogConstant.operation.add_type, param.getId().toString()));
		}
		return ok(param.getId());
	}

	/**
	 * @Description 更新常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/11/13 14:17
	 * @Param [param]
	 * @return com.njwd.support.Result<java.lang.Long>
	 */
	@PostMapping("updateCommonAbstract")
	public Result<Long> updateCommonAbstract(@RequestBody CommonAbstractDto param) {
		FastUtils.checkParams(param.getId(), param.getAbstractContent(), param.getVersion());
		if (param.getAbstractCode() == null) {
			param.setAbstractCode(Constant.Character.NULL_VALUE);
		}
		//用户信息
		SysUserVo userInfo = UserUtil.getUserVo();
		param.setUpdatorId(userInfo.getUserId());
		param.setUpdatorName(userInfo.getName());
		//更新
		if (commonAbstractService.updateCommonAbstract(param) > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.commonAbstract, LogConstant.operation.update, LogConstant.operation.update_type, param.getId().toString()));
		}
		return ok(param.getId());
	}

	/**
	 * @Description 批量删除常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/11/12 16:05
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.support.BatchResult>
	 */
	@PostMapping("deleteCommonAbstract")
	public Result<BatchResult> deleteCommonAbstract(@RequestBody CommonAbstractDto param) {
		//非空校验
		changeListNotNull(param);
		//批量删除
		BatchResult result = commonAbstractService.deleteCommonAbstract(param);
		if (result.getSuccessList().size() > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.commonAbstract, LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 批量审核常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/11/12 16:05
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.support.BatchResult>
	 */
	@PostMapping("approvedCommonAbstract")
	public Result<BatchResult> approvedCommonAbstract(@RequestBody CommonAbstractDto param) {
		//非空校验
		changeListNotNull(param);
		//批量审核
		BatchResult result = commonAbstractService.approvedCommonAbstract(param);
		if (result.getSuccessList().size() > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.commonAbstract, LogConstant.operation.approve, LogConstant.operation.approve_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 批量反审核常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/11/12 16:05
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.support.BatchResult>
	 */
	@PostMapping("disapprovedCommonAbstract")
	public Result<BatchResult> disapprovedCommonAbstract(@RequestBody CommonAbstractDto param) {
		//非空校验
		changeListNotNull(param);
		//批量反审核
		BatchResult result = commonAbstractService.disapprovedCommonAbstract(param);
		if (result.getSuccessList().size() > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.commonAbstract, LogConstant.operation.reversalApprove, LogConstant.operation.reversalApprove_type, result.getSuccessList().toString()));
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
	@PostMapping("releasedCommonAbstract")
	public Result<BatchResult> releasedCommonAbstract(@RequestBody CommonAbstractDto param) {
		//非空校验
		changeListNotNull(param);
		//批量发布
		BatchResult result = commonAbstractService.releasedCommonAbstract(param);
		if (result.getSuccessList().size() > 0) {
			//日志
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.commonAbstract, LogConstant.operation.release, LogConstant.operation.release_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 查询常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/11/12 15:30
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.entity.platform.vo.CommonAbstractVo>
	 */
	@RequestMapping("findCommonAbstract")
	public Result<CommonAbstractVo> findCommonAbstract(@RequestBody CommonAbstractDto param) {
		FastUtils.checkParams(param.getId());
		return ok(commonAbstractService.findCommonAbstract(param));
	}

	/**
	 * @Description 查询常用摘要[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 9:46
	 * @Param [currencyDto]
	 * @return com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page < com.njwd.entity.platform.vo.CurrencyVo>>
	 */
	@RequestMapping("findCommonAbstractPage")
	public Result<Page<CommonAbstractVo>> findCommonAbstractPage(@RequestBody CommonAbstractDto param) {
		return ok(commonAbstractService.findCommonAbstractPage(param));
	}

	/**
	 * @Description 查询常用摘要[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 14:33
	 * @Param [param]
	 * @return com.njwd.support.Result<java.util.List < com.njwd.entity.basedata.CommonAbstract>>
	 */
	@RequestMapping("findCommonAbstractList")
	public Result<List<CommonAbstractVo>> findCommonAbstractList(@RequestBody CommonAbstractDto param) {
		return ok(commonAbstractService.findCommonAbstractList(param));
	}

	/**
	 * @Description 导出EXCEL
	 * @Author 郑勇浩
	 * @Data 2019/11/19 17:36
	 * @Param [response, param]
	 */
	@RequestMapping("exportExcel")
	public void exportExcel(HttpServletResponse response, @RequestBody CommonAbstractDto param) {
		commonAbstractService.exportExcel(response, param);
	}

	/**
	 * @Description 批量数组不能为空
	 * @Author 郑勇浩
	 * @Data 2019/11/12 17:13
	 * @Param [param]
	 */
	private void changeListNotNull(CommonAbstractDto param) {
		//判断数组是否为空
		if (CollectionUtils.isEmpty(param.getChangeList())) {
			throw new ServiceException(ResultCode.PARAMS_NOT);
		}
		//判断id是否为空
		param.getChangeList().forEach(o -> FastUtils.checkParams(o.getId(), o.getVersion()));
	}

}
