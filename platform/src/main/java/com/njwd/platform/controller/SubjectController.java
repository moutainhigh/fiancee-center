package com.njwd.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.platform.Subject;
import com.njwd.entity.platform.dto.SubjectDto;
import com.njwd.entity.platform.vo.SubjectVo;
import com.njwd.logger.SenderService;
import com.njwd.platform.service.SubjectService;
import com.njwd.platform.utils.UserUtil;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
import com.njwd.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author lj
 * @Description 科目
 * @Date:14:20 2019/6/13
 **/
@RestController
@RequestMapping("subject")
public class SubjectController extends BaseController {
	@Autowired
	private SubjectService subjectService;
	@Resource
	private SenderService senderService;

	/**
	 * @Description 查询科目表列表
	 * @Author liuxiang
	 * @Date:15:15 2019/7/2
	 * @Param [subjectDto]
	 * @return java.lang.String
	 **/
	@PostMapping("findSubjectList")
	public Result<List<SubjectVo>> findSubjectList(@RequestBody SubjectDto subjectDto) {
		return ok(subjectService.findSubjectList(subjectDto));
	}

	/**
	 * @Description 查询科目表列表
	 * @Author liuxiang
	 * @Date:15:15 2019/7/2
	 * @Param [subjectDto]
	 * @return java.lang.String
	 **/
	@PostMapping("findSubjectListPage")
	public Result<Page<SubjectVo>> findSubjectListPage(@RequestBody SubjectDto subjectDto) {
		return ok(subjectService.findSubjectListPage(subjectDto));
	}

	@PostMapping("findPage")
	public Result<Page<SubjectVo>> findPage(@RequestBody SubjectDto subjectDto) {
		return ok(subjectService.findPage(subjectDto));
	}

	@PostMapping("add")
	public Result<Long> add(@RequestBody SubjectDto subjectDto) {
		FastUtils.checkParams(subjectDto.getAccStandardId());
		Long result = RedisUtils.lock(String.format(Constant.LockKey.PLATFORM_SUBJECT, subjectDto.getAccStandardId()), Constant.SysConfig.REDIS_LOCK_TIMEOUT,
				() -> subjectService.add(subjectDto));
		senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
				LogConstant.menuName.subject, LogConstant.operation.add, LogConstant.operation.add_type, result.toString()));
		return ok(result);
	}

	@PostMapping("getReleaseBaseSubject")
	public Result<Subject> getReleaseBaseSubject(@RequestBody SubjectDto subjectDto) {
		FastUtils.checkParams(subjectDto.getAccStandardId());
		return ok(subjectService.getOne(new LambdaQueryWrapper<Subject>()
				.eq(Subject::getAccStandardId, subjectDto.getAccStandardId())
				.eq(Subject::getIsBase, Constant.Is.YES)
				.eq(Subject::getIsReleased, Constant.Is.YES)));
	}

	@PostMapping("update")
	public Result<Long> update(@RequestBody SubjectDto subjectDto) {
		FastUtils.checkParams(subjectDto.getAccStandardId());
		Long result = RedisUtils.lock(String.format(Constant.LockKey.PLATFORM_SUBJECT, subjectDto.getAccStandardId()), Constant.SysConfig.REDIS_LOCK_TIMEOUT,
				() -> subjectService.update(subjectDto));
		senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
				LogConstant.menuName.subject, LogConstant.operation.update, LogConstant.operation.update_type, result.toString()));
		return ok(result);
	}

	@PostMapping("delete")
	public Result<BatchResult> delete(@RequestBody SubjectDto subjectDto) {
		BatchResult result = subjectService.delete(subjectDto);
		if (!result.getSuccessList().isEmpty()) {
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.subject, LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	@PostMapping("approve")
	public Result<BatchResult> approve(@RequestBody SubjectDto subjectDto) {
		BatchResult result = subjectService.approve(subjectDto);
		if (!result.getSuccessList().isEmpty()) {
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.subject, LogConstant.operation.approve, LogConstant.operation.approve_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	@PostMapping("reversalApprove")
	public Result<BatchResult> reversalApprove(@RequestBody SubjectDto subjectDto) {
		BatchResult result = subjectService.reversalApprove(subjectDto);
		if (!result.getSuccessList().isEmpty()) {
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.subject, LogConstant.operation.reversalApprove, LogConstant.operation.reversalApprove_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	@PostMapping("release")
	public Result<BatchResult> release(@RequestBody SubjectDto subjectDto) {
		BatchResult result = subjectService.release(subjectDto);
		if (!result.getSuccessList().isEmpty()) {
			senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.PlatformSys,
					LogConstant.menuName.subject, LogConstant.operation.release, LogConstant.operation.release_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	@PostMapping("findDetail")
	public Result<SubjectVo> findDetail(@RequestBody SubjectDto subjectDto) {
		return ok(subjectService.findDetail(subjectDto));
	}

	@RequestMapping("exportExcel")
	public void exportExcel(@RequestBody SubjectDto subjectDto, HttpServletResponse response) {
		subjectService.exportExcel(subjectDto, response);
	}
}

