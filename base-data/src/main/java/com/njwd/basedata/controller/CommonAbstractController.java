package com.njwd.basedata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.service.CommonAbstractService;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.basedata.dto.CommonAbstractDto;
import com.njwd.entity.basedata.vo.CommonAbstractVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.logger.SenderService;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @Description 常用摘要 controller.
 * @Date 2019/7/25 9:33
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
	 * @Data 2019/7/25 10:08
	 * @Param [bto]
	 * @return java.lang.String
	 */
	@PostMapping("addCommonAbstract")
	public Result<Long> addCommonAbstract(@RequestBody CommonAbstractDto dto) {
		//必填
		FastUtils.checkParams(dto.getCreateEnterpriseId(), dto.getUseEnterpriseId(), dto.getAbstractContent());
		//V1.011 摘要内容 助记码 前后空格自动取消
		dto.setAbstractContent(dto.getAbstractContent().trim());
		if (!dto.getAbstractCode().isEmpty()) {
			dto.setAbstractCode(dto.getAbstractCode().trim());
		}

		//ADD
		int result = RedisUtils.lock(String.format(Constant.LockKey.COMMON_ABSTRACT_UNIQUE, dto.getCreateEnterpriseId(), dto.getAbstractContent()), Constant.SysConfig.REDIS_LOCK_TIMEOUT, () -> commonAbstractService.addCommonAbstract(dto));
		if (result > 0) {
			//LOG
			senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.commonAbstract, LogConstant.operation.add, LogConstant.operation.add_type, dto.getId().toString()));
		}
		return ok(dto.getId());
	}

	/**
	 * @Description 删除常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/7/25 10:58
	 * @Param [bto]
	 * @return java.lang.String
	 */
	@PostMapping("deleteCommonAbstract")
	public Result<Integer> deleteCommonAbstract(@RequestBody CommonAbstractDto dto) {
		//必填
		FastUtils.checkParams(dto.getId());

		setUpdateUserInfo(dto);
		//UPDATE 删除
		int result = commonAbstractService.deleteCommonAbstract(dto);
		if (result > 0) {
			//LOG
			senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.commonAbstract, LogConstant.operation.delete, LogConstant.operation.delete_type, dto.getId().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 批量删除
	 * @Author 郑勇浩
	 * @Data 2019/7/25 11:52
	 * @Param [dto]
	 * @return java.lang.String
	 */
	@PostMapping("deleteCommonAbstractBatch")
	public Result<BatchResult> deleteCommonAbstractBatch(@RequestBody CommonAbstractDto dto) {
		//ids 为空直接返回
		if (CollectionUtils.isEmpty(dto.getIdList())) {
			throw new ServiceException(ResultCode.PARAMS_NOT);
		}

		setUpdateUserInfo(dto);
		//UPDATE 批量删除
		BatchResult result = commonAbstractService.deleteCommonAbstractBatch(dto);
		if (result.getSuccessList().size() > 0) {
			//LOG
			senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.commonAbstract, LogConstant.operation.delete, LogConstant.operation.delete_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 更新常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/7/25 11:25
	 * @Param [CommonAbstractDto]
	 * @return java.lang.String
	 */
	@PostMapping("updateCommonAbstract")
	public Result<Long> updateCommonAbstract(@RequestBody CommonAbstractDto dto) {
		//必填
		FastUtils.checkParams(dto.getId(), dto.getCreateEnterpriseId(), dto.getUseEnterpriseId(), dto.getAbstractContent());
		//V1.011 摘要内容 助记码 前后空格自动取消
		dto.setAbstractContent(dto.getAbstractContent().trim());
		if (!dto.getAbstractCode().isEmpty()) {
			dto.setAbstractCode(dto.getAbstractCode().trim());
		}

		setUpdateUserInfo(dto);
		//UPDATE
		long result = RedisUtils.lock(String.format(Constant.LockKey.COMMON_ABSTRACT_UNIQUE, dto.getCreateEnterpriseId(), dto.getAbstractContent()), Constant.SysConfig.REDIS_LOCK_TIMEOUT, () -> commonAbstractService.updateCommonAbstract(dto));
		if (result > 0) {
			//LOG
			senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.commonAbstract, LogConstant.operation.update, LogConstant.operation.update_type, dto.getId().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 查询常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/7/25 14:28
	 * @Param [CommonAbstractDto]
	 * @return java.lang.String
	 */
	@RequestMapping("findCommonAbstract")
	public Result<CommonAbstractVo> findCommonAbstract(@RequestBody CommonAbstractDto dto) {
		//必填
		FastUtils.checkParams(dto.getId());
		//查询常用摘要
		CommonAbstractVo resultData = commonAbstractService.findCommonAbstract(dto);
		return ok(resultData);
	}

	/**
	 * @Description 查询常用摘要分页
	 * @Author 郑勇浩
	 * @Data 2019/7/25 14:28
	 * @Param [CommonAbstractDto]
	 * @return java.lang.String
	 */
	@RequestMapping("findCommonAbstractPage")
	public Result<Page<CommonAbstractVo>> findCommonAbstractPage(@RequestBody CommonAbstractDto dto) {
		SysUserVo operator = UserUtils.getUserVo();
		dto.setRootEnterpriseId(operator.getRootEnterpriseId());
		//user 根据用户id查询
		if (dto.getIsEnterpriseAdmin().equals(Constant.Is.NO)) {
			dto.setUserId(operator.getUserId());
		}
		return ok(commonAbstractService.findCommonAbstractPage(dto));
	}

	/**
	 * @Description 设置用户更新信息
	 * @Author 郑勇浩
	 * @Data 2019/7/3 14:47
	 * @Param [bankAccountDto]
	 */
	private void setUpdateUserInfo(CommonAbstractDto dto) {
		//登录用户
		SysUserVo operator = UserUtils.getUserVo();
		dto.setRootEnterpriseId(operator.getRootEnterpriseId());
		dto.setUpdatorId(operator.getUserId());
		dto.setUpdatorName(operator.getName());
		dto.setUpdateTime(new Date(System.currentTimeMillis() / 1000 * 1000));
	}

	/**
	 * @description: 获取待引用常用摘要
	 * @param: [dto]
	 * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.basedata.vo.CommonAbstractVo>>
	 * @author: xdy
	 * @create: 2019-11-14 09:43
	 */
	@RequestMapping("findBringInCommonAbstract")
	public Result<List<CommonAbstractVo>> findBringInCommonAbstract(@RequestBody CommonAbstractDto dto){
		return ok(commonAbstractService.findBringInCommonAbstract(dto));
	}
}
