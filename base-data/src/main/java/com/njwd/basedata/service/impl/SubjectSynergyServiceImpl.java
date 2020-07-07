package com.njwd.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.cloudclient.SubjectSynergyFeignClient;
import com.njwd.basedata.cloudclient.VoucherFeignClient;
import com.njwd.basedata.mapper.AccountSubjectMapper;
import com.njwd.basedata.mapper.SubjectSynergyMapper;
import com.njwd.basedata.service.SequenceService;
import com.njwd.basedata.service.SubjectSynergyService;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.dto.VoucherDto;
import com.njwd.entity.ledger.vo.VoucherVo;
import com.njwd.entity.platform.AccountSubject;
import com.njwd.entity.platform.SubjectSynergy;
import com.njwd.entity.platform.dto.SubjectSynergyDto;
import com.njwd.entity.platform.vo.SubjectSynergyVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
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
 * @since 2019/10/28
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SubjectSynergyServiceImpl implements SubjectSynergyService {
	@Resource
	private SubjectSynergyMapper subjectSynergyMapper;
	@Resource
	private AccountSubjectMapper accountSubjectMapper;
	@Resource
	private SequenceService sequenceService;
	@Resource
	private SubjectSynergyFeignClient subjectSynergyFeignClient;
	@Resource
	private VoucherFeignClient voucherFeignClient;

	/**
	 * 添加科目协同配置
	 *
	 * @param subjectSynergyDto subjectSynergyDto
	 * @param operator          operator
	 * @return java.lang.Long
	 * @author xyyxhcj@qq.com
	 * @date 2019/10/25 17:02
	 **/
	@Override
	public Long add(SubjectSynergyDto subjectSynergyDto, SysUserVo operator) {
		checkEditValid(subjectSynergyDto, operator);
		SubjectSynergy subjectSynergy = new SubjectSynergy();
		FastUtils.copyProperties(subjectSynergyDto, subjectSynergy);
		subjectSynergy.setRootEnterpriseId(operator.getRootEnterpriseId());
		subjectSynergy.setCreatorId(operator.getUserId());
		subjectSynergy.setCreatorName(operator.getName());
		// 生成code
		subjectSynergy.setCode(sequenceService.getCode(Constant.BaseCodeRule.SUBJECT_SYNERGY, Constant.BaseCodeRule.LENGTH_FOUR, operator.getRootEnterpriseId(), Constant.BaseCodeRule.ENTERPRISE));
		subjectSynergyMapper.insert(subjectSynergy);
		return subjectSynergy.getId();
	}

	/**
	 * 修改科目协同配置
	 *
	 * @param subjectSynergyDto subjectSynergyDto
	 * @param operator          operator
	 * @return java.lang.Long
	 * @author xyyxhcj@qq.com
	 * @date 2019/10/28 9:12
	 **/
	@Override
	public Long update(SubjectSynergyDto subjectSynergyDto, SysUserVo operator) {
		FastUtils.checkParams(subjectSynergyDto.getId());
		SubjectSynergy existSubjectSynergy = subjectSynergyMapper.selectById(subjectSynergyDto.getId());
		if (existSubjectSynergy == null || Constant.Is.YES.equals(existSubjectSynergy.getIsDel())) {
			throw new ServiceException(ResultCode.RECORD_NOT_EXIST);
		}
		if (!existSubjectSynergy.getVersion().equals(subjectSynergyDto.getVersion())) {
			throw new ServiceException(ResultCode.VERSION_ERROR);
		}
		// 仅可编辑‘未审核’的数据?
		if (Constant.Is.YES.equals(existSubjectSynergy.getIsApproved())) {
			throw new ServiceException(ResultCode.SYNERGY_ACCOUNT_SUBJECT_APPROVED);
		}
		checkEditValid(subjectSynergyDto, operator);
		SubjectSynergy subjectSynergy = new SubjectSynergy();
		FastUtils.copyProperties(subjectSynergyDto, subjectSynergy);
		// 清空不可修改数据
		subjectSynergy.setRootEnterpriseId(null);
		subjectSynergy.setCode(null);
		subjectSynergy.setIsApproved(null);
		subjectSynergy.setIsReleased(null);
		subjectSynergy.setIsEnable(null);
		subjectSynergy.setIsDel(null);
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
	 * @param subjectSynergyDto subjectSynergyDto
	 * @param operator          operator
	 * @author xyyxhcj@qq.com
	 * @date 2019/10/28 9:20
	 **/
	private void checkEditValid(SubjectSynergyDto subjectSynergyDto, SysUserVo operator) {
		Long subjectId = subjectSynergyDto.getSubjectId();
		Long srcAccountSubjectId = subjectSynergyDto.getSrcAccountSubjectId();
		Long destAccountSubjectId = subjectSynergyDto.getDestAccountSubjectId();
		Long platformId = subjectSynergyDto.getPlatformId();
		FastUtils.checkParams(subjectId, srcAccountSubjectId, destAccountSubjectId, subjectSynergyDto.getSrcAuxiliarySource(), subjectSynergyDto.getDestAuxiliarySource(), subjectSynergyDto.getSrcAuxiliaryName(), subjectSynergyDto.getDestAuxiliaryName(), platformId, subjectSynergyDto.getName());
		// 校验两个科目是否可用
		AccountSubject srcAccountSubject = accountSubjectMapper.selectById(srcAccountSubjectId);
		checkSubjectValid(srcAccountSubjectId, srcAccountSubject);
		AccountSubject destAccountSubject = accountSubjectMapper.selectById(destAccountSubjectId);
		checkSubjectValid(destAccountSubjectId, destAccountSubject);
		// 一套会计科目表下的协同关系不可重复(重复：一组关系中任意一方的数据均不可在一套科目表下再次使用
		LambdaQueryWrapper<SubjectSynergy> queryWrapper = new LambdaQueryWrapper<SubjectSynergy>()
				.and(tQueryWrapper -> tQueryWrapper
						.or(wrapper -> wrapper.in(SubjectSynergy::getSrcAccountSubjectId, srcAccountSubjectId, destAccountSubjectId))
						.or(wrapper -> wrapper.in(SubjectSynergy::getDestAccountSubjectId, srcAccountSubjectId, destAccountSubjectId)))
				.eq(SubjectSynergy::getSubjectId, subjectId)
				.eq(SubjectSynergy::getRootEnterpriseId, operator.getRootEnterpriseId())
				.eq(SubjectSynergy::getIsDel, Constant.Is.NO);
		if (subjectSynergyDto.getId() != null) {
			queryWrapper.ne(SubjectSynergy::getId, subjectSynergyDto.getId());
		}
		List<SubjectSynergy> subjectSynergies = subjectSynergyMapper.selectList(queryWrapper);
		if (!subjectSynergies.isEmpty()) {
			throw new ServiceException(ResultCode.SYNERGY_ACCOUNT_SUBJECT_ID_USED, subjectSynergies);
		}
		// 校验是否为平台配置的下级科目
		SubjectSynergy subjectSynergyQuery = new SubjectSynergy();
		subjectSynergyQuery.setId(platformId);
		Result<SubjectSynergyVo> platFormDetailResult = subjectSynergyFeignClient.findDetail(subjectSynergyQuery);
		if (platFormDetailResult == null || platFormDetailResult.getData() == null) {
			throw new ServiceException(ResultCode.PLATFORM_FAILURE);
		}
		// 校验配置是否已发布
		if (Constant.Is.NO.equals(platFormDetailResult.getData().getIsReleased())) {
			throw new ServiceException(ResultCode.SYNERGY_ACCOUNT_SUBJECT_SELECTED_ERROR);
		}
		checkPlatformAuxiliaryConfig(subjectSynergyDto.getSrcAuxiliarySource(), platFormDetailResult.getData().getSrcAuxiliarySource());
		checkPlatformAuxiliaryConfig(subjectSynergyDto.getDestAuxiliarySource(), platFormDetailResult.getData().getDestAuxiliarySource());
		checkPlatformAccSubjectConfig(srcAccountSubject, platFormDetailResult.getData().getSrcAccountSubjectCode());
		checkPlatformAccSubjectConfig(destAccountSubject, platFormDetailResult.getData().getDestAccountSubjectCode());
	}

	/**
	 * 校验sourceTable是否一致
	 *
	 * @param auxiliarySource         租户选择
	 * @param platformAuxiliarySource 平台配置
	 * @author xyyxhcj@qq.com
	 * @date 2019/10/29 18:16
	 **/
	private void checkPlatformAuxiliaryConfig(String auxiliarySource, String platformAuxiliarySource) {
		if (!auxiliarySource.equals(platformAuxiliarySource)) {
			throw new ServiceException(ResultCode.SYNERGY_ACCOUNT_SUBJECT_AUXILIARY_ERROR);
		}
	}

	/**
	 * 校验所选科目是否为平台配置的末级
	 *
	 * @param selectedAccountSubject     selectedAccountSubject
	 * @param platformAccountSubjectCode platformAccountSubjectCode
	 * @author xyyxhcj@qq.com
	 * @date 2019/10/28 17:50
	 **/
	private void checkPlatformAccSubjectConfig(AccountSubject selectedAccountSubject, String platformAccountSubjectCode) {
		if (!platformAccountSubjectCode.startsWith(selectedAccountSubject.getCode())) {
			throw new ServiceException(ResultCode.SYNERGY_ACCOUNT_SUBJECT_ID_ERROR, selectedAccountSubject);
		}
	}

	/**
	 * 校验科目ID是否可用
	 *
	 * @param srcAccountSubjectId srcAccountSubjectId
	 * @param srcAccountSubject   srcAccountSubject
	 * @author xyyxhcj@qq.com
	 * @date 2019/10/28 17:43
	 **/
	private void checkSubjectValid(Long srcAccountSubjectId, AccountSubject srcAccountSubject) {
		if (srcAccountSubject == null || Constant.Is.YES.equals(srcAccountSubject.getIsDel())) {
			throw new ServiceException(ResultCode.RECORD_NOT_EXIST, srcAccountSubjectId);
		}
		if (Constant.Is.NO.equals(srcAccountSubject.getIsFinal())) {
			throw new ServiceException(ResultCode.ACCOUNT_SUBJECT_IS_LAST, srcAccountSubject);
		}
	}
	/**
	 * 查询所有科目协同
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
			//1.校验当前数据是否启用，如已启用，则无法删除该数据
			else if(subjectSynergy.getIsEnable() == Constant.Is.YES){
				rd.setBusinessId(subjectSynergy.getId());
				rd.setReferenceDescription("该数据已启用,无法删除!");
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
		int i = subjectSynergyMapper.updateOrdeleteSubjectSynergy(subjectSynergyDto,subjectSynergys);
		System.out.println(i);
		return result;
	}

	/**
	 * 启用
	 * @param subjectSynergyDto
	 * @return
	 */
	@Override
	public BatchResult enableSubjectSynergy(SubjectSynergyDto subjectSynergyDto) {
		//初始化返回对象
		BatchResult result = new BatchResult();
		result.setFailList(new ArrayList<>());
		result.setSuccessList(new ArrayList<>());
		List<Long> subjectIdList = null;
		//根据id查询出所有的数据 进行校验
		List<SubjectSynergyVo> subjectSynergyList = subjectSynergyMapper.findSubjectSynergyListById(subjectSynergyDto);
		for(SubjectSynergyVo subjectSynergyVo : subjectSynergyList){
			subjectIdList.add(subjectSynergyVo.getSubjectId());
		}
		//获取凭证数据
		List<VoucherVo> voucherList = findVoucherList(subjectSynergyDto, subjectIdList);
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
			//校验启用状态
			else if(subjectSynergy.getIsEnable() == Constant.Is.YES){
				StringBuffer sb = new StringBuffer();
				sb.append(subjectSynergy.getCode()).append(" ");
				rd.setBusinessId(subjectSynergy.getId());
				rd.setReferenceDescription("编码 "+sb.toString()+ " 已启用!");
				result.getFailList().add(rd);
				continue;
			}
		}
		//没数据 == 可以进行启用
		// 检查选择的启用期间，获取租户下所有账簿中协同科目id集合 去凭证表查询 如果有数据 代表有发生额，则不能进行启用
		//无数据 代表无发生额 可以进行启用
		if(voucherList == null || voucherList.size() == 0){
			for(SubjectSynergyVo subjectSynergyVo : subjectSynergyList){
				ReferenceDescription rd = new ReferenceDescription();
				rd.setBusinessId(subjectSynergyVo.getId());
				rd.setReferenceDescription("启用成功!");
				result.getSuccessDetailsList().add(rd);
			}
			subjectSynergyDto.setIsEnable(Constant.Is.YES);
			int i = subjectSynergyMapper.updateOrdeleteSubjectSynergy(subjectSynergyDto,subjectSynergyList);
			System.out.println(i);
			return result;
		}else{
			// 把科目协同list集合转换成map数据  和 凭证数据进行比较  如果有相同的  就代表发生余额 则启用失败
			Map<Long, SubjectSynergyVo> map = subjectSynergyList.stream().collect(Collectors.toMap(SubjectSynergyVo::getRootEnterpriseId, a -> a, (k1, k2) -> k1));
			for(VoucherVo voucherVo : voucherList){
				ReferenceDescription rd = new ReferenceDescription();
				SubjectSynergyVo subjectSynergyVo = map.get(voucherVo.getRootEnterpriseId());
				if(subjectSynergyVo != null){
					rd.setBusinessId(subjectSynergyVo.getId());
					rd.setReferenceDescription("启用失败!");
					result.getFailList().add(rd);
					subjectSynergyList.remove(subjectSynergyVo);
				}
			}
			for(SubjectSynergyVo subjectSynergyVo : subjectSynergyList){
				ReferenceDescription rd = new ReferenceDescription();
				rd.setBusinessId(subjectSynergyVo.getId());
				rd.setReferenceDescription("启用成功!");
				result.getSuccessDetailsList().add(rd);
			}
			subjectSynergyDto.setIsEnable(Constant.Is.YES);
			int i = subjectSynergyMapper.updateOrdeleteSubjectSynergy(subjectSynergyDto,subjectSynergyList);
			System.out.println(i);
			return result;
		}
	}
	/**
	 * 反启用
	 * 刘遵通
	 * @param subjectSynergyDto
	 * @return
	 */
	@Override
	public BatchResult reversalEnableSubjectSynergy(SubjectSynergyDto subjectSynergyDto) {
		//初始化返回对象
		BatchResult result = new BatchResult();
		result.setFailList(new ArrayList<>());
		result.setSuccessList(new ArrayList<>());
		List<Long> subjectIdList = null;
		//根据id查询出所有的数据 进行校验
		List<SubjectSynergyVo> subjectSynergyList = subjectSynergyMapper.findSubjectSynergyListById(subjectSynergyDto);
		for(SubjectSynergyVo subjectSynergyVo : subjectSynergyList){
			subjectIdList.add(subjectSynergyVo.getSubjectId());
		}
		//获取凭证数据
		List<VoucherVo> voucherList = findVoucherList(subjectSynergyDto, subjectIdList);
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
			//校验启用状态
			else if(subjectSynergy.getIsEnable() == Constant.Is.NO){
				StringBuffer sb = new StringBuffer();
				sb.append(subjectSynergy.getCode()).append(" ");
				rd.setBusinessId(subjectSynergy.getId());
				rd.setReferenceDescription("编码 "+sb.toString()+ " 已反启用!");
				result.getFailList().add(rd);
				continue;
			}
		}
		//没数据 == 可以进行反启用
		// 检查选择的启用期间，获取租户下所有账簿中协同科目id集合 去凭证表查询 如果有数据 代表有发生额，则不能进行反启用
		//无数据 代表无发生额 可以进行反启用
		if(voucherList == null || voucherList.size() == 0){
			for(SubjectSynergyVo subjectSynergyVo : subjectSynergyList){
				ReferenceDescription rd = new ReferenceDescription();
				rd.setBusinessId(subjectSynergyVo.getId());
				rd.setReferenceDescription("反启用成功!");
				result.getSuccessDetailsList().add(rd);
			}
			subjectSynergyDto.setIsEnable(Constant.Is.NO);
			int i = subjectSynergyMapper.updateOrdeleteSubjectSynergy(subjectSynergyDto,subjectSynergyList);
			System.out.println(i);
			return result;
		}else{
			// 把科目协同list集合转换成map数据  和 凭证数据进行比较  如果有相同的  就代表发生余额 则反启用失败
			Map<Long, SubjectSynergyVo> map = subjectSynergyList.stream().collect(Collectors.toMap(SubjectSynergyVo::getRootEnterpriseId, a -> a, (k1, k2) -> k1));
			for(VoucherVo voucherVo : voucherList){
				ReferenceDescription rd = new ReferenceDescription();
				SubjectSynergyVo subjectSynergyVo = map.get(voucherVo.getRootEnterpriseId());
				if(subjectSynergyVo != null){
					rd.setBusinessId(subjectSynergyVo.getId());
					rd.setReferenceDescription("反启用失败!");
					result.getFailList().add(rd);
					subjectSynergyList.remove(subjectSynergyVo);
				}
			}
			for(SubjectSynergyVo subjectSynergyVo : subjectSynergyList){
				ReferenceDescription rd = new ReferenceDescription();
				rd.setBusinessId(subjectSynergyVo.getId());
				rd.setReferenceDescription("反启用成功!");
				result.getSuccessDetailsList().add(rd);
			}
			subjectSynergyDto.setIsEnable(Constant.Is.NO);
			int i = subjectSynergyMapper.updateOrdeleteSubjectSynergy(subjectSynergyDto,subjectSynergyList);
			System.out.println(i);
			return result;
		}
	}

	/**
	 * 获取凭证数据
	 * @param subjectSynergyDto
	 * @param subjectIdList
	 * @return
	 */
	private List<VoucherVo> findVoucherList(SubjectSynergyDto subjectSynergyDto,List<Long> subjectIdList){
		VoucherDto voucher = new VoucherDto();
		voucher.setRootEnterpriseId(subjectSynergyDto.getRootEnterpriseId());
		voucher.setPeriodYearNum(subjectSynergyDto.getPeriodYearNum());
		voucher.setSubjectIdList(subjectIdList);
		//查询凭证数据
		// 检查选择的启用期间，获取租户下所有账簿中协同科目id集合 去凭证表查询 如果有数据 代表有发生额，则不能进行启用 或者 反启用
		//无数据 代表无发生额 可以进行启用 或者 反启用
		List<VoucherVo> voucherList = voucherFeignClient.findVoucherByRootIdAndSubjectId(voucher);
		return voucherList;
	}
}
