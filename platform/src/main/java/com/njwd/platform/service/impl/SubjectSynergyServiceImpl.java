package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.platform.AccountSubject;
import com.njwd.entity.platform.SubjectSynergy;
import com.njwd.entity.platform.dto.SubjectSynergyDto;
import com.njwd.entity.platform.vo.SubjectSynergyVo;
import com.njwd.entity.platform.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.platform.mapper.AccountSubjectMapper;
import com.njwd.platform.mapper.BasePlatformMapper;
import com.njwd.platform.mapper.SubjectSynergyMapper;
import com.njwd.platform.service.MessageService;
import com.njwd.platform.service.SubjectSynergyService;
import com.njwd.platform.utils.UserUtil;
import com.njwd.support.BatchResult;
import com.njwd.utils.FastUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/10/25
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SubjectSynergyServiceImpl implements SubjectSynergyService {
	@Resource
	private SubjectSynergyMapper subjectSynergyMapper;
	@Resource
	private AccountSubjectMapper accountSubjectMapper;
	@Resource
	private MessageService messageService;
	@Resource
	private BasePlatformMapper basePlatformMapper;

	/**
	 * 添加科目协同配置
	 *
	 * @param subjectSynergy subjectSynergy
	 * @return java.lang.Long
	 * @author xyyxhcj@qq.com
	 * @date 2019/10/25 17:02
	 **/
	@Override
	public Long add(SubjectSynergy subjectSynergy) {
		checkEditValid(subjectSynergy);
		SysUserVo operator = UserUtil.getUserVo();
		subjectSynergy.setCreatorId(operator.getUserId());
		subjectSynergy.setCreatorName(operator.getName());
		// 生成code
		subjectSynergy.setCode(generateCode());
		subjectSynergyMapper.insert(subjectSynergy);
		return subjectSynergy.getId();
	}

	/**
	 * 平台的自增code
	 *
	 * @return java.lang.String
	 * @author xyyxhcj@qq.com
	 * @date 2019/10/28 16:25
	 **/
	private String generateCode() {
		String codePrefix = Constant.CodePrefix.SUBJECT_SYNERGY_PLATFORM;
		SubjectSynergy codeSubjectSynergy = subjectSynergyMapper.selectOne(new LambdaQueryWrapper<SubjectSynergy>()
				.select(SubjectSynergy::getCode)
				.likeRight(SubjectSynergy::getCode, codePrefix)
				.orderByDesc(SubjectSynergy::getCode)
				.last(Constant.ConcatSql.LIMIT_1));
		long codeNum = 1;
		if (codeSubjectSynergy != null) {
			String existCode = codeSubjectSynergy.getCode();
			// +=取流水号最大值
			codeNum += Long.parseLong(existCode.substring(codePrefix.length()));
		}
		return FastUtils.generateCode(codePrefix, Constant.CodeSuffix.SUBJECT_SYNERGY, codeNum);
	}

	/**
	 * 修改科目协同配置
	 *
	 * @param subjectSynergy subjectSynergy
	 * @return java.lang.Long
	 * @author xyyxhcj@qq.com
	 * @date 2019/10/28 9:12
	 *
	 **/
	@Override
	public Long update(SubjectSynergy subjectSynergy) {
		FastUtils.checkParams(subjectSynergy.getId());
		SubjectSynergy existSubjectSynergy = subjectSynergyMapper.selectById(subjectSynergy.getId());
		if (existSubjectSynergy == null || Constant.Is.YES.equals(existSubjectSynergy.getIsDel())) {
			throw new ServiceException(ResultCode.RECORD_NOT_EXIST);
		}
		// 校验版本号
		if (!existSubjectSynergy.getVersion().equals(subjectSynergy.getVersion())) {
			throw new ServiceException(ResultCode.VERSION_ERROR);
		}
		// 仅可编辑‘未审核’的数据
		if (Constant.Is.YES.equals(existSubjectSynergy.getIsApproved())) {
			throw new ServiceException(ResultCode.SYNERGY_ACCOUNT_SUBJECT_APPROVED);
		}
		checkEditValid(subjectSynergy);
		// 清空不可修改数据
		subjectSynergy.setRootEnterpriseId(null);
		subjectSynergy.setCode(null);
		subjectSynergy.setIsApproved(null);
		subjectSynergy.setIsReleased(null);
		subjectSynergy.setIsEnable(null);
		subjectSynergy.setIsDel(null);
		SysUserVo operator = UserUtil.getUserVo();
		subjectSynergy.setUpdatorId(operator.getUserId());
		subjectSynergy.setUpdatorName(operator.getName());
		subjectSynergy.setUpdateTime(new Date());
		subjectSynergyMapper.updateById(subjectSynergy);
		return subjectSynergy.getId();
	}

	/**
	 * 查详情
	 *
	 * @param subjectSynergy subjectSynergy
	 * @return com.njwd.entity.platform.vo.SubjectSynergyVo
	 * @author xyyxhcj@qq.com
	 * @date 2019/10/28 14:54
	 **/
	@Override
	public SubjectSynergyVo findDetail(SubjectSynergy subjectSynergy) {
		FastUtils.checkParams(subjectSynergy.getId());
		SubjectSynergyVo subjectSynergyVo = subjectSynergyMapper.findById(subjectSynergy.getId());
		FastUtils.checkNull(subjectSynergyVo);
		return subjectSynergyVo;
	}

	/**
	 * 校验数据是否可用
	 *
	 * @param subjectSynergy subjectSynergy
	 * @author xyyxhcj@qq.com
	 * @date 2019/10/28 9:20
	 **/
	private void checkEditValid(SubjectSynergy subjectSynergy) {
		Long subjectId = subjectSynergy.getSubjectId();
		Long srcAccountSubjectId = subjectSynergy.getSrcAccountSubjectId();
		Long destAccountSubjectId = subjectSynergy.getDestAccountSubjectId();
		FastUtils.checkParams(subjectId, srcAccountSubjectId, destAccountSubjectId, subjectSynergy.getSrcAuxiliarySource(), subjectSynergy.getDestAuxiliarySource(), subjectSynergy.getSrcAuxiliaryName(), subjectSynergy.getDestAuxiliaryName(), subjectSynergy.getName());
		if (srcAccountSubjectId.equals(destAccountSubjectId)) {
			throw new ServiceException(ResultCode.SYNERGY_ACCOUNT_SUBJECT_SELECTED_SAME);
		}
		// 校验两个科目是否均为一级
		List<AccountSubject> accountSubjects = accountSubjectMapper.selectList(new LambdaQueryWrapper<AccountSubject>()
				.eq(AccountSubject::getSubjectId, subjectId)
				.in(AccountSubject::getId, srcAccountSubjectId, destAccountSubjectId)
				.ne(AccountSubject::getLevel, Constant.Level.ONE)
				.select(BaseModel::getId));
		if (!accountSubjects.isEmpty()) {
			throw new ServiceException(ResultCode.SYNERGY_ACCOUNT_SUBJECT_ONLY_FIRST, accountSubjects);
		}
		// 一套会计科目表下的协同关系不可重复(重复：一组关系中任意一方的数据均不可在一套科目表下再次使用
		LambdaQueryWrapper<SubjectSynergy> queryWrapper = new LambdaQueryWrapper<SubjectSynergy>()
				.and(tQueryWrapper -> tQueryWrapper
						.or(wrapper -> wrapper.in(SubjectSynergy::getSrcAccountSubjectId, srcAccountSubjectId, destAccountSubjectId))
						.or(wrapper -> wrapper.in(SubjectSynergy::getDestAccountSubjectId, srcAccountSubjectId, destAccountSubjectId)))
				.eq(SubjectSynergy::getSubjectId, subjectId)
				.eq(SubjectSynergy::getIsDel, Constant.Is.NO);
		if (subjectSynergy.getId() != null) {
			queryWrapper.ne(SubjectSynergy::getId, subjectSynergy.getId());
		}
		List<SubjectSynergy> subjectSynergies = subjectSynergyMapper.selectList(queryWrapper);
		if (!subjectSynergies.isEmpty()) {
			throw new ServiceException(ResultCode.SYNERGY_ACCOUNT_SUBJECT_ID_USED, subjectSynergies);
		}
	}

	/**
	 *查询所有科目协同
	 * 刘遵通
	 * @param subjectSynergyDto
	 * @return
	 */
	@Override
	public Page<SubjectSynergyVo> findSubjectSynergyList(SubjectSynergyDto subjectSynergyDto) {
		return subjectSynergyMapper.findSubjectSynergyList(subjectSynergyDto.getPage(),subjectSynergyDto);
	}
	/**
	 * 删除
	 * 刘遵通
	 * @param subjectSynergyDto
	 * @return
	 */
	@Override
	public BatchResult deleteSubjectSynergy(SubjectSynergyDto subjectSynergyDto) {
		//初始化返回对象
		BatchResult result = new BatchResult();
		result.setFailList(new ArrayList<>());
		result.setSuccessList(new ArrayList<>());
		List<SubjectSynergyVo> subjectSynergys= new ArrayList<SubjectSynergyVo>();
		//根据id查询出所有的数据 进行校验
		List<SubjectSynergyVo> subjectSynergyList = subjectSynergyMapper.findSubjectSynergyListById(subjectSynergyDto);
		for(SubjectSynergyVo subjectSynergy : subjectSynergyList){
			ReferenceDescription rd = new ReferenceDescription();
			//把list对象转换成map
			Map<Long, SubjectSynergyDto> map = subjectSynergyDto.getEditList().stream().collect(Collectors.toMap(SubjectSynergyDto::getId, a -> a, (k1, k2) -> k1));
			Integer version = map.get(subjectSynergy.getId()).getVersion();
			//校验版本号
			if(!subjectSynergy.getVersion().equals(version)){
				rd.setBusinessId(subjectSynergy.getId());
				rd.setReferenceDescription("其他用户正在操作,请稍候再使用!");
				result.getFailList().add(rd);
				continue;
			}
			//1.校验当前数据是否审核,审核状态0待审核 1已审核 如果已审核，则提示报错‘该数据已审核，无法删除，请先反审核’
			else if(subjectSynergy.getIsApproved() == Constant.Is.YES){
				rd.setBusinessId(subjectSynergy.getId());
				rd.setReferenceDescription("该数据已审核,无法删除,请先反审核!");
				result.getFailList().add(rd);
				continue;
			}
			//2.已‘发布’的数据，点击‘删除’，则提示报错‘该数据已发布，无法删除’
			else if(subjectSynergy.getIsReleased() == Constant.Is.YES){
				rd.setBusinessId(subjectSynergy.getId());
				rd.setReferenceDescription("该数据已发布,无法删除!");
				result.getFailList().add(rd);
				continue;
			}
			//添加成功的
			subjectSynergys.add(subjectSynergy);
			rd.setBusinessId(subjectSynergy.getId());
			rd.setReferenceDescription("删除成功!");
			result.getSuccessDetailsList().add(rd);
		}
		//防止没有数据
		if (result.getSuccessDetailsList().size() == 0) {
			return result;
		}
		subjectSynergyDto.setIsDel(Constant.Is.YES);
		int i = subjectSynergyMapper.deleteSubjectSynergy(subjectSynergyDto,subjectSynergys);
		System.out.println(i);
		return result;
	}

	/**
	 * 审核
	 * 刘遵通
	 * @param subjectSynergyDto
	 * @return
	 */
	@Override
	public BatchResult checkApprove(SubjectSynergyDto subjectSynergyDto) {
		//初始化返回对象
		BatchResult result = new BatchResult();
		result.setFailList(new ArrayList<>());
		result.setSuccessList(new ArrayList<>());
		List<SubjectSynergyVo> subjectSynergys= new ArrayList<SubjectSynergyVo>();
		//根据id查询出所有的数据 进行校验
		List<SubjectSynergyVo> subjectSynergyList = subjectSynergyMapper.findSubjectSynergyListById(subjectSynergyDto);
		for(SubjectSynergyVo subjectSynergy : subjectSynergyList){
			ReferenceDescription rd = new ReferenceDescription();
			//把list对象转换成map
			Map<Long, SubjectSynergyDto> map = subjectSynergyDto.getEditList().stream().collect(Collectors.toMap(SubjectSynergyDto::getId, a -> a, (k1, k2) -> k1));
			Integer version = map.get(subjectSynergy.getId()).getVersion();
			//校验版本号
			if(!subjectSynergy.getVersion().equals(version)){
				rd.setBusinessId(subjectSynergy.getId());
				rd.setReferenceDescription("其他用户正在操作,请稍候再使用!");
				result.getFailList().add(rd);
				continue;
			}
			//1.校验当前数据是否审核,审核状态0待审核 1已审核 如果已审核，则提示报错‘该数据已审核,无需审核’
			else if(subjectSynergy.getIsApproved() == Constant.Is.YES){
				rd.setBusinessId(subjectSynergy.getId());
				rd.setReferenceDescription("该数据已审核,无需审核!");
				result.getFailList().add(rd);
				continue;
			}
			//2.已‘发布’的数据，点击‘审核’，则提示报错‘该数据已发布，无法审核’
			else if(subjectSynergy.getIsReleased() == Constant.Is.YES){
				rd.setBusinessId(subjectSynergy.getId());
				rd.setReferenceDescription("该数据已发布,无法审核!");
				result.getFailList().add(rd);
				continue;
			}
			//添加成功的
			subjectSynergys.add(subjectSynergy);
			rd.setBusinessId(subjectSynergy.getId());
			rd.setReferenceDescription("审核成功!");
			result.getSuccessDetailsList().add(rd);
		}
		//防止没有数据
		if (result.getSuccessDetailsList().size() == 0) {
			return result;
		}
		subjectSynergyDto.setIsApproved(Constant.Is.YES);
		int i = subjectSynergyMapper.deleteSubjectSynergy(subjectSynergyDto,subjectSynergys);
		System.out.println(i);
		return result;
	}

	/**
	 * 反审核
	 * 刘遵通
	 * @param subjectSynergyDto
	 * @return
	 */
	@Override
	public BatchResult reversalApprove(SubjectSynergyDto subjectSynergyDto) {
		//初始化返回对象
		BatchResult result = new BatchResult();
		result.setFailList(new ArrayList<>());
		result.setSuccessList(new ArrayList<>());
		List<SubjectSynergyVo> subjectSynergys= new ArrayList<SubjectSynergyVo>();
		//根据id查询出所有的数据 进行校验
		List<SubjectSynergyVo> subjectSynergyList = subjectSynergyMapper.findSubjectSynergyListById(subjectSynergyDto);
		for(SubjectSynergyVo subjectSynergy : subjectSynergyList){
			ReferenceDescription rd = new ReferenceDescription();
			//把list对象转换成map
			Map<Long, SubjectSynergyDto> map = subjectSynergyDto.getEditList().stream().collect(Collectors.toMap(SubjectSynergyDto::getId, a -> a, (k1, k2) -> k1));
			Integer version = map.get(subjectSynergy.getId()).getVersion();
			//校验版本号
			if(!subjectSynergy.getVersion().equals(version)){
				rd.setBusinessId(subjectSynergy.getId());
				rd.setReferenceDescription("其他用户正在操作,请稍候再使用!");
				result.getFailList().add(rd);
				continue;
			}
			//1.校验当前数据是否审核,审核状态0待审核 1已审核 如果未审核，则提示报错‘该数据未审核，无需反审核’
			else if(subjectSynergy.getIsApproved() == Constant.Is.NO){
				rd.setBusinessId(subjectSynergy.getId());
				rd.setReferenceDescription("该数据未审核,无需反审核!");
				result.getFailList().add(rd);
				continue;
			}
			//2.已‘发布’的数据，点击‘反审核’，则提示报错‘该数据已发布，无法反审核’
			else if(subjectSynergy.getIsReleased() == Constant.Is.YES){
				rd.setBusinessId(subjectSynergy.getId());
				rd.setReferenceDescription("该数据已发布,无法反审核!");
				result.getFailList().add(rd);
				continue;
			}
			//添加成功的
			subjectSynergys.add(subjectSynergy);
			rd.setBusinessId(subjectSynergy.getId());
			rd.setReferenceDescription("反审核成功!");
			result.getSuccessDetailsList().add(rd);
		}
		//防止没有数据
		if (result.getSuccessDetailsList().size() == 0) {
			return result;
		}
		subjectSynergyDto.setIsApproved(Constant.Is.NO);
		int i = subjectSynergyMapper.deleteSubjectSynergy(subjectSynergyDto,subjectSynergys);
		System.out.println(i);
		return result;
	}

	/**
	 * 发布
	 * @param subjectSynergyDto
	 * @return
	 */
	@Override
	public BatchResult release(SubjectSynergyDto subjectSynergyDto) {
		//初始化返回对象
		BatchResult result = new BatchResult();
		result.setFailList(new ArrayList<>());
		result.setSuccessList(new ArrayList<>());
		List<SubjectSynergyVo> subjectSynergys= new ArrayList<SubjectSynergyVo>();
		//根据id查询出所有的数据 进行校验
		List<SubjectSynergyVo> subjectSynergyList = subjectSynergyMapper.findSubjectSynergyListById(subjectSynergyDto);
		for(SubjectSynergyVo subjectSynergy : subjectSynergyList){
			ReferenceDescription rd = new ReferenceDescription();
			//把list对象转换成map
			Map<Long, SubjectSynergyDto> map = subjectSynergyDto.getEditList().stream().collect(Collectors.toMap(SubjectSynergyDto::getId, a -> a, (k1, k2) -> k1));
			Integer version = map.get(subjectSynergy.getId()).getVersion();
			//校验版本号
			if(!subjectSynergy.getVersion().equals(version)){
				rd.setBusinessId(subjectSynergy.getId());
				rd.setReferenceDescription("其他用户正在操作,请稍候再使用!");
				result.getFailList().add(rd);
				continue;
			}
			//审核状态0待审核 1已审核
			else if(subjectSynergy.getIsApproved() == Constant.Is.NO){
				rd.setBusinessId(subjectSynergy.getId());
				rd.setReferenceDescription("该数据未审核,不能发布!");
				result.getFailList().add(rd);
				continue;
			}
			//添加成功的
			subjectSynergys.add(subjectSynergy);
		/*	rd.setBusinessId(subjectSynergy.getId());
			rd.setReferenceDescription("发布成功!");
			result.getSuccessDetailsList().add(rd);*/
			rd.setBusinessId(subjectSynergy.getId());
			result.getSuccessList().add(rd.getBusinessId());
		}
		//防止没有数据
		if (result.getSuccessDetailsList().size() == 0) {
			return result;
		}
		//发送消息
		messageService.sendMessage(PlatformConstant.MessageType.SYSTEM_NOTICE,subjectSynergyDto.getMessageDto());
		basePlatformMapper.batchProcess(result.getSuccessList(), PlatformConstant.OperateType.RELEASED, UserUtil.getUserVo(), PlatformConstant.TableName.SUBJECT_SYNERGY);

	/*	subjectSynergyDto.setIsReleased(Constant.Is.YES);
		int i = subjectSynergyMapper.deleteSubjectSynergy(subjectSynergyDto,subjectSynergys);
		System.out.println(i);*/
		return result;
	}
}


