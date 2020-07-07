package com.njwd.basedata.controller;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.service.SubjectSynergyService;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.SubjectSynergy;
import com.njwd.entity.platform.dto.SubjectSynergyDto;
import com.njwd.entity.platform.vo.SubjectSynergyVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.logger.SenderService;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 科目协同配置-租户端
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
	public Result<Long> add(@RequestBody SubjectSynergyDto subjectSynergyDto) {
		SysUserVo operator = UserUtils.getUserVo();
		// 鉴权
		ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
		Long id = syncLock(operator, () -> subjectSynergyService.add(subjectSynergyDto, operator));
		// 记录日志
		senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
				LogConstant.sysName.FinanceBackSys, LogConstant.menuName.subjectSynergy,
				LogConstant.operation.add, LogConstant.operation.add_type, id.toString()));
		return ok(id);
	}

	@RequestMapping("update")
	public Result<Long> update(@RequestBody SubjectSynergyDto subjectSynergyDto) {
		SysUserVo operator = UserUtils.getUserVo();
		// 鉴权
		ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
		syncLock(operator, () -> subjectSynergyService.update(subjectSynergyDto, operator));
		// 记录日志
		senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
				LogConstant.sysName.FinanceBackSys, LogConstant.menuName.subjectSynergy,
				LogConstant.operation.update, LogConstant.operation.update_type, subjectSynergyDto.getId().toString()));
		return ok(subjectSynergyDto.getId());
	}

	@RequestMapping("findDetail")
	public Result<SubjectSynergyVo> findDetail(@RequestBody SubjectSynergy subjectSynergy) {
		return ok(subjectSynergyService.findDetail(subjectSynergy));
	}

	private Long syncLock(SysUserVo operator, RedisUtils.LockProcess<Long> lockProcess) {
		return RedisUtils.lock(String.format(Constant.LockKey.SUBJECT_SYNERGY, operator.getRootEnterpriseId()), Constant.SysConfig.REDIS_LOCK_TIMEOUT, lockProcess);
	}

	/**
	 * 查询科目协同列表
	 * 刘遵通
	 * @param subjectSynergyDto
	 * @return
	 */
	@RequestMapping("findSubjectSynergyList")
	public Result<Page<SubjectSynergyVo>> findSubjectSynergyList(@RequestBody SubjectSynergyDto subjectSynergyDto){
		Long rootEnterpriseId = UserUtils.getUserVo().getRootEnterpriseId();
		subjectSynergyDto.setRootEnterpriseId(rootEnterpriseId);
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
		Long rootEnterpriseId = UserUtils.getUserVo().getRootEnterpriseId();
		subjectSynergyDto.setRootEnterpriseId(rootEnterpriseId);
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
		return ok(batchResult);
	}
	/**
	 * 启用或者反启用
	 * 刘遵通
	 * @param subjectSynergyDto
	 * @return
	 */
	@RequestMapping("reversalOrEnableSubjectSynergy")
	public Result<BatchResult> reversalOrEnableSubjectSynergy(@RequestBody SubjectSynergyDto subjectSynergyDto){
		Long rootEnterpriseId = UserUtils.getUserVo().getRootEnterpriseId();
		subjectSynergyDto.setRootEnterpriseId(rootEnterpriseId);
		//获取参数集合
		List<SubjectSynergyDto> subjectSynergyDtoList = subjectSynergyDto.getEditList();
		//subjectSynergyDtoList 为空直接返回
		if(CollectionUtils.isEmpty(subjectSynergyDtoList)){
			throw new ServiceException(ResultCode.PARAMS_NOT);
		}
		//循环遍历参数集合，校验参数是否有值
		for (SubjectSynergyDto s :subjectSynergyDtoList){
			FastUtils.checkParams(s.getId(),s.getVersion(),s.getPeriodYearNum(),s.getStatus());
		}
		Integer status = subjectSynergyDto.getStatus();
		BatchResult batchResult = null;
		//status 0反启用 1启用
		if(status == 1){
			batchResult = subjectSynergyService.enableSubjectSynergy(subjectSynergyDto);
		}else{
			batchResult = subjectSynergyService.reversalEnableSubjectSynergy(subjectSynergyDto);
		}
		return ok(batchResult);
	}
}
