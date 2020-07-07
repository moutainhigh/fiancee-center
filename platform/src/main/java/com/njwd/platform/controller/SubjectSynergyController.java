package com.njwd.platform.controller;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.platform.SubjectSynergy;
import com.njwd.entity.platform.dto.SubjectSynergyDto;
import com.njwd.entity.platform.vo.SubjectSynergyVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.logger.SenderService;
import com.njwd.platform.service.SubjectSynergyService;
import com.njwd.platform.utils.UserUtil;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
import com.njwd.utils.RedisUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 科目协同配置-平台
 *
 * @author xyyxhcj@qq.com
 * @since 2019/10/25
 */
@RestController
@RequestMapping("subjectSynergy")
public class SubjectSynergyController extends BaseController {
	@Resource
	private SubjectSynergyService subjectSynergyService;
	@Resource
	private SenderService senderService;
	@RequestMapping("add")
	public Result<Long> add(@RequestBody SubjectSynergy subjectSynergy) {
		// 鉴权
		//ShiroUtils.checkRole(Constant.ShiroAdminDefi.SYS_ADMIN, null);
		Long id = syncLock(() -> subjectSynergyService.add(subjectSynergy));
		// 记录日志 todo
		return ok(id);
	}

	@RequestMapping("update")
	public Result<Long> update(@RequestBody SubjectSynergy subjectSynergy) {
		// 鉴权
		//ShiroUtils.checkRole(Constant.ShiroAdminDefi.SYS_ADMIN, null);
		syncLock(() -> subjectSynergyService.update(subjectSynergy));
		// 记录日志 todo
		return ok(subjectSynergy.getId());
	}

	@RequestMapping("findDetail")
	public Result<SubjectSynergyVo> findDetail(@RequestBody SubjectSynergy subjectSynergy) {
		return ok(subjectSynergyService.findDetail(subjectSynergy));
	}

	private Long syncLock(RedisUtils.LockProcess<Long> lockProcess) {
		return RedisUtils.lock(String.format(Constant.LockKey.SUBJECT_SYNERGY, Constant.IsCompany.GROUP_ID), Constant.SysConfig.REDIS_LOCK_TIMEOUT, lockProcess);
	}

	/**
	 * 查询科目协同列表
	 * 刘遵通
	 * @param subjectSynergyDto
	 * @return
	 */
	@RequestMapping("findSubjectSynergyList")
	public Result<Page<SubjectSynergyVo>> findSubjectSynergyList(@RequestBody SubjectSynergyDto subjectSynergyDto){
		//Long rootEnterpriseId = UserUtil.getUserVo().getRootEnterpriseId();
		//subjectSynergyDto.setRootEnterpriseId(rootEnterpriseId);
		return ok(subjectSynergyService.findSubjectSynergyList(subjectSynergyDto));
	}

	/**
	 * 删除
	 * 刘遵通
	 * @param subjectSynergyDto
	 * @return
	 */
	@RequestMapping("deleteSubjectSynergy")
	public Result<BatchResult> deleteSubjectSynergy(@RequestBody SubjectSynergyDto subjectSynergyDto){
		//Long rootEnterpriseId = UserUtil.getUserVo().getRootEnterpriseId();
		//subjectSynergyDto.setRootEnterpriseId(rootEnterpriseId);
		//获取参数集合
		List<SubjectSynergyDto> subjectSynergyDtoList = subjectSynergyDto.getEditList();
		//subjectSynergyDtoList 为空直接返回
		if(CollectionUtils.isEmpty(subjectSynergyDtoList)){
			throw new ServiceException(ResultCode.PARAMS_NOT);
		}
		//循环遍历参数集合，校验参数是否有值
		for (SubjectSynergyDto s :subjectSynergyDtoList){
			FastUtils.checkParams(s.getId(),s.getVersion());
		}
		BatchResult batchResult = subjectSynergyService.deleteSubjectSynergy(subjectSynergyDto);
		senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
				LogConstant.sysName.PlatformSys, LogConstant.menuName.subjectSynergy,
				LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, null));
		return ok(batchResult);
	}

	/**
	 * 审核
	 * 刘遵通
	 * @param subjectSynergyDto
	 * @return
	 */
	@RequestMapping("checkApprove")
	public Result<BatchResult> checkApprove(@RequestBody SubjectSynergyDto subjectSynergyDto){
		//Long rootEnterpriseId = UserUtil.getUserVo().getRootEnterpriseId();
		//subjectSynergyDto.setRootEnterpriseId(rootEnterpriseId);
		//获取参数集合
		List<SubjectSynergyDto> subjectSynergyDtoList = subjectSynergyDto.getEditList();
		//subjectSynergyDtoList 为空直接返回
		if(CollectionUtils.isEmpty(subjectSynergyDtoList)){
			throw new ServiceException(ResultCode.PARAMS_NOT);
		}
		//循环遍历参数集合，校验参数是否有值
		for (SubjectSynergyDto s :subjectSynergyDtoList){
			FastUtils.checkParams(s.getId(),s.getVersion());
		}
		BatchResult batchResult = subjectSynergyService.checkApprove(subjectSynergyDto);
		senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
				LogConstant.sysName.PlatformSys, LogConstant.menuName.subjectSynergy,
				LogConstant.operation.approve, LogConstant.operation.approve_type, null));
		return ok(batchResult);
	}

	/**
	 * 反审核
	 * 刘遵通
	 * @param subjectSynergyDto
	 * @return
	 */
	@RequestMapping("reversalApprove")
	public Result<BatchResult> reversalApprove(@RequestBody SubjectSynergyDto subjectSynergyDto){
		//Long rootEnterpriseId = UserUtil.getUserVo().getRootEnterpriseId();
		//subjectSynergyDto.setRootEnterpriseId(rootEnterpriseId);
		//获取参数集合
		List<SubjectSynergyDto> subjectSynergyDtoList = subjectSynergyDto.getEditList();
		//subjectSynergyDtoList 为空直接返回
		if(CollectionUtils.isEmpty(subjectSynergyDtoList)){
			throw new ServiceException(ResultCode.PARAMS_NOT);
		}
		//循环遍历参数集合，校验参数是否有值
		for (SubjectSynergyDto s :subjectSynergyDtoList){
			FastUtils.checkParams(s.getId(),s.getVersion());
		}
		BatchResult batchResult = subjectSynergyService.reversalApprove(subjectSynergyDto);
		senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
				LogConstant.sysName.PlatformSys, LogConstant.menuName.subjectSynergy,
				LogConstant.operation.reversalApprove, LogConstant.operation.reversalApprove_type, null));

		return ok(batchResult);
	}

	/**
	 * 发布
	 * 刘遵通
	 * @param subjectSynergyDto
	 * @return
	 */
	@RequestMapping("release")
	public Result<BatchResult> release(@RequestBody SubjectSynergyDto subjectSynergyDto){
		//Long rootEnterpriseId = UserUtil.getUserVo().getRootEnterpriseId();
		//subjectSynergyDto.setRootEnterpriseId(rootEnterpriseId);
		//获取参数集合
		List<SubjectSynergyDto> subjectSynergyDtoList = subjectSynergyDto.getEditList();
		//subjectSynergyDtoList 为空直接返回
		if(CollectionUtils.isEmpty(subjectSynergyDtoList)){
			throw new ServiceException(ResultCode.PARAMS_NOT);
		}
		//循环遍历参数集合，校验参数是否有值
		for (SubjectSynergyDto s :subjectSynergyDtoList){
			FastUtils.checkParams(s.getId(),s.getVersion());
		}
		BatchResult batchResult = subjectSynergyService.release(subjectSynergyDto);
		senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
				LogConstant.sysName.PlatformSys, LogConstant.menuName.subjectSynergy,
				LogConstant.operation.release, LogConstant.operation.release_type, null));
		return ok(batchResult);
	}
}
