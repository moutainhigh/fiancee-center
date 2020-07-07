package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.logger.SenderService;
import com.njwd.platform.service.AccountSubjectService;
import com.njwd.platform.utils.UserUtil;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
import com.njwd.utils.RedisUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * 会计科目
 *
 * @author xyyxhcj@qq.com
 * @since 2019/11/13
 */
@RestController
@RequestMapping("accountSubject")
public class AccountSubjectController extends BaseController {
	@Resource
	private AccountSubjectService accountSubjectService;
	@Resource
	private SenderService senderService;
	@RequestMapping("findPage")
	public Result<Page<AccountSubjectVo>> findPage(@RequestBody AccountSubjectDto<AccountSubjectVo> accountSubjectDto) {
		return ok(accountSubjectService.findPage(accountSubjectDto));
	}

	@RequestMapping("add")
	public Result<Long> add(@RequestBody AccountSubjectDto<AccountSubjectVo> accSubjectDto) {
		Long result = RedisUtils.lock(String.format(Constant.LockKey.PLATFORM_ACC_SUBJECT, accSubjectDto.getNeedIntroductionId()), Constant.SysConfig.REDIS_LOCK_TIMEOUT,
				() -> accountSubjectService.add(accSubjectDto));
		senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
				LogConstant.menuName.accoutSubject, LogConstant.operation.add, LogConstant.operation.add_type, result.toString()));
		return ok(result);
	}

	@RequestMapping("update")
	public Result<Long> update(@RequestBody AccountSubjectDto accSubjectDto) {
		Long result = RedisUtils.lock(String.format(Constant.LockKey.PLATFORM_ACC_SUBJECT, accSubjectDto.getNeedIntroductionId()), Constant.SysConfig.REDIS_LOCK_TIMEOUT,
				() -> accountSubjectService.update(accSubjectDto));
		senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
				LogConstant.menuName.accoutSubject, LogConstant.operation.update, LogConstant.operation.update_type, result.toString()));
		return ok(result);
	}

	@PostMapping("delete")
	public Result<BatchResult> delete(@RequestBody AccountSubjectDto<AccountSubjectVo> accSubjectDto) {
		BatchResult result = accountSubjectService.delete(accSubjectDto);
		if (!result.getSuccessList().isEmpty()) {
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.accoutSubject, LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	@PostMapping("approve")
	public Result<BatchResult> approve(@RequestBody AccountSubjectDto<AccountSubjectVo> accSubjectDto) {
		BatchResult result = accountSubjectService.approve(accSubjectDto);
		if (!result.getSuccessList().isEmpty()) {
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.accoutSubject, LogConstant.operation.approve, LogConstant.operation.approve_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	@PostMapping("reversalApprove")
	public Result<BatchResult> reversalApprove(@RequestBody AccountSubjectDto<AccountSubjectVo> accSubjectDto) {
		BatchResult result = accountSubjectService.reversalApprove(accSubjectDto);
		if (!result.getSuccessList().isEmpty()) {
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.accoutSubject, LogConstant.operation.reversalApprove, LogConstant.operation.reversalApprove_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	@PostMapping("release")
	public Result<BatchResult> release(@RequestBody AccountSubjectDto<AccountSubjectVo> accSubjectDto) {
		BatchResult result = accountSubjectService.release(accSubjectDto);
		if (!result.getSuccessList().isEmpty()) {
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.accoutSubject, LogConstant.operation.release, LogConstant.operation.release_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	@PostMapping("findDetail")
	public Result<AccountSubjectVo> findDetail(@RequestBody AccountSubjectDto<AccountSubjectVo> accSubjectDto) {
		return ok(accountSubjectService.findDetail(accSubjectDto));
	}

	/**
	 * 引入
	 *
	 * @param accSubjectDto accSubjectDto
	 * @return com.njwd.support.Result<com.njwd.support.BatchResult>
	 * @author xyyxhcj@qq.com
	 * @date 2019/11/20 17:56
	 **/
	@PostMapping("introduction")
	public Result<BatchResult> introduction(@RequestBody AccountSubjectDto<AccountSubjectVo> accSubjectDto) {
		FastUtils.checkParams(accSubjectDto.getBatchIds(), accSubjectDto.getNeedIntroductionId());
		BatchResult result = RedisUtils.lock(String.format(Constant.LockKey.PLATFORM_ACC_SUBJECT, accSubjectDto.getNeedIntroductionId()), Constant.SysConfig.REDIS_LOCK_TIMEOUT,
				() -> accountSubjectService.introduction(accSubjectDto));
		if (!result.getSuccessList().isEmpty()) {
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.accoutSubject, LogConstant.operation.introduction, LogConstant.operation.introduction_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	@RequestMapping("exportExcel")
	public void exportExcel(@RequestBody AccountSubjectDto<AccountSubjectVo> accountSubjectDto, HttpServletResponse response) {
		accountSubjectService.exportExcel(accountSubjectDto, response);
	}
}
