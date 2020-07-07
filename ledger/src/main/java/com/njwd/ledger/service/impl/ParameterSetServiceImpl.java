package com.njwd.ledger.service.impl;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.ParameterSet;
import com.njwd.entity.ledger.ParameterSetSub;
import com.njwd.entity.ledger.dto.ParameterSetDto;
import com.njwd.entity.ledger.dto.ParameterSetSubDto;
import com.njwd.entity.ledger.dto.VoucherDto;
import com.njwd.entity.ledger.vo.ParameterSetVo;
import com.njwd.entity.platform.Subject;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.dto.SubjectDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.ledger.cloudclient.AccountSubjectFeignClient;
import com.njwd.ledger.cloudclient.SubjectFeignClient;
import com.njwd.ledger.mapper.ParameterSetMapper;
import com.njwd.ledger.mapper.ParameterSetSubMapper;
import com.njwd.ledger.service.ParameterSetService;
import com.njwd.ledger.service.ParameterSetSubService;
import com.njwd.ledger.service.VoucherService;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.StringUtil;
import com.njwd.utils.UserUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 参数设置主表 ServiceImpl
 * @Date 2019/10/16 15:11
 * @Author 郑勇浩
 */
@Service
public class ParameterSetServiceImpl implements ParameterSetService {
	@Resource
	private ParameterSetMapper parameterSetMapper;
	@Resource
	private ParameterSetSubMapper parameterSetSubMapper;
	@Resource
	private ParameterSetService parameterSetService;
	@Resource
	private ParameterSetSubService parameterSetSubService;
	@Resource
	private VoucherService voucherService;
	@Resource
	private AccountSubjectFeignClient accountSubjectFeignClient;
	@Resource
	private SubjectFeignClient subjectFeignClient;

	/**
	 * @Description 初始化总账参数
	 * @Author 郑勇浩
	 * @Data 2019/10/17 13:51
	 * @Param [param]
	 * @return boolean
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean initParameterSet(ParameterSetDto param) {
		//查询是否已经初始化
		int count = parameterSetMapper.selectParameterSetCount(param);
		if (count > 0) {
			return true;
		}
		//批量新增主表数据　返回主表ID
		int insertNum = parameterSetMapper.insertParameterSetBatch(param.getParameterSetVoList());
		if (insertNum != param.getParameterSetVoList().size()) {
			throw new ServiceException(ResultCode.PARAMETER_SET_INIT_FAIL);
		}
		//批量新增子表数据
		insertNum = parameterSetSubService.insertParameterSetSubBatch(param.getParameterSetVoList());
		if (insertNum != param.getParameterSetVoList().size()) {
			throw new ServiceException(ResultCode.PARAMETER_SET_INIT_FAIL);
		}
		return true;
	}

	/**
	 * @Description 删除总账个性化账簿设置
	 * @Author 郑勇浩
	 * @Data 2019/10/18 15:34
	 * @Param [param]
	 * @return boolean
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@Caching(evict = {@CacheEvict(value = Constant.RedisCache.PARAMETER_SET, key = "#param.rootEnterpriseId"),
			@CacheEvict(value = Constant.RedisCache.PARAMETER_SET_VALUE, key = "#param.rootEnterpriseId")})
	public boolean deleteParameterSet(ParameterSetDto param) {
		//查询数据
		List<ParameterSetVo> allList = parameterSetService.findAllParameterSet(param);
		List<Long> deleteList = new ArrayList<>();

		//获取待删除的subId对应数据
		for (Long id : param.getSubIdList()) {
			if (id == null) {
				continue;
			}

			for (ParameterSetVo psv : allList) {
				if (id.equals(psv.getSubId())) {
					//租户总账参数无法删除
					if (psv.getAccountBookId().equals(Constant.Number.ZEROL)) {
						throw new ServiceException(ResultCode.PARAMETER_SET_ADMIN_DELETE_FAIL);
					}
					//存在无法删除
					if (psv.getModifyType().equals(Constant.ParameterSetModifyType.CAN_NOT_EDIT)) {
						throw new ServiceException(
								psv.getGroupName() + ResultCode.CREDENTIAL_WORD_TYPE_DELETE_FAIL.message,
								ResultCode.CREDENTIAL_WORD_TYPE_DELETE_FAIL);
					}
					deleteList.add(psv.getSubId());
					break;
				}
			}
		}

		//正常不会进入
		if (CollectionUtils.isEmpty(deleteList)) {
			return true;
		}

		param.setSubIdList(deleteList);
		parameterSetMapper.deleteParameterSetBatch(param);
		return true;
	}

	/**
	 * @Description 修改总账参数设置
	 * @Author 郑勇浩
	 * @Data 2019/10/17 16:46
	 * @Param [param]
	 * @return boolean
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@Caching(evict = {@CacheEvict(value = Constant.RedisCache.PARAMETER_SET, key = "#param.rootEnterpriseId"),
			@CacheEvict(value = Constant.RedisCache.PARAMETER_SET_VALUE, key = "#param.rootEnterpriseId")})
	public boolean updateParameterSet(ParameterSetDto param) {
		//查询数据
		List<ParameterSetVo> existsList = parameterSetService.findAllParameterSet(param);
		//新增或更新数据
		ParameterSetSubDto subParam = new ParameterSetSubDto();
		param.setIdList(new ArrayList<>());
		// 凭证
		Map<Long, String> checkVoucherMap = new LinkedHashMap<>();
		// 本年利润 利润分配 以前年度损益调整
		Map<String, List<Long>> checkDataMap = new HashMap<>(Constant.Number.THREE);
		Map<String, String> keyNameMap = new HashMap<>(Constant.Number.THREE);
		//分类数据
		String nowKey;
		//科目表id　暂时仅有一个
		ParameterSetVo subjectParam = existsList.stream().filter(o -> o.getAccountBookId().equals(Constant.Number.ZEROL) && o.getKey().equals(Constant.ParameterSetKey.ACC_SUBJECT_ID)).findFirst().orElse(null);
		Long subjectId = subjectParam == null ? null : subjectParam.getValue();
		ParameterSetVo dataBaseData;
		for (ParameterSetVo psv : param.getParameterSetVoList()) {
			psv.setRootEnterpriseId(param.getRootEnterpriseId());

			//查找数据
			dataBaseData = existsList.stream().filter(o -> o.getId().equals(psv.getId()) && o.getAccountBookId().equals(psv.getAccountBookId())).findFirst().orElse(null);

			//判断是否需要验证存在凭证
			if (psv.getModifyType().equals(Constant.ParameterSetModifyType.CAN_NOT_EDIT)) {
				//如果是新增数据 或者是更新数据切和原有数据不同（数据被改变）
				if (dataBaseData == null || (!psv.getValue().equals(dataBaseData.getValue()))) {
					checkVoucherMap.put(psv.getAccountBookId(), psv.getAccountBookName());
				}
			}

			if (dataBaseData == null) {
				subParam.getInsertList().add(psv);
				continue;
			}
			//如果结果没被修改则忽略
			if (psv.getValue().equals(dataBaseData.getValue())) {
				continue;
			}
			//判断版本
			if (!psv.getAccountBookId().equals(Constant.Number.ZEROL) && !psv.getVersion().equals(dataBaseData.getVersion())) {
				throw new ServiceException(ResultCode.IS_CHANGE);
			}

			//本年利润 利润分配 以前年度损益调整
			nowKey = dataBaseData.getKey();
			if (nowKey.equals(Constant.ParameterSetKey.LR_ACC_SUBJECT_ID)
					|| nowKey.equals(Constant.ParameterSetKey.FP_ACC_SUBJECT_ID)
					|| nowKey.equals(Constant.ParameterSetKey.SY_ACC_SUBJECT_ID)) {
				if (!checkDataMap.containsKey(nowKey)) {
					checkDataMap.put(nowKey, new ArrayList<>());
					keyNameMap.put(nowKey, dataBaseData.getName());
				}
				checkDataMap.get(nowKey).add(psv.getValue());
			}

			param.getIdList().add(psv.getId());
			existsList.remove(dataBaseData);
			subParam.getUpdateList().add(psv);
		}
		//判断凭证
		checkVoucher(checkVoucherMap);
		//判断损益结转科目
		checkData(checkDataMap, keyNameMap, subjectId);

		//新增或更新子表数据
		parameterSetSubService.updateParameterSetSubBatch(subParam);
		//更新主表数据
		if (!CollectionUtils.isEmpty(param.getIdList())) {
			parameterSetMapper.updateParameterSetBatch(param);
		}
		return true;
	}

	/**
	 * @Description 查询所有的总账参数
	 * @Author 郑勇浩
	 * @Data 2019/10/18 16:04
	 * @Param [param]
	 * @return java.util.List<com.njwd.entity.ledger.vo.ParameterSetVo>
	 */
	@Override
	@Cacheable(value = Constant.RedisCache.PARAMETER_SET, key = "#param.rootEnterpriseId", unless = "#result == null")
	public List<ParameterSetVo> findAllParameterSet(ParameterSetDto param) {
		List<ParameterSetVo> returnList = parameterSetMapper.selectParameterSet(param);
		if (CollectionUtils.isEmpty(returnList)) {
			throw new ServiceException(ResultCode.VOUCHER_EXIST_RIGHT);
		}

		Long subjectId = null;
		//查询结果
		List<Long> accountSubjectIdList = new ArrayList<>();
		for (ParameterSetVo nowData : returnList) {
			//科目ID
			if (nowData.getKey().equals(Constant.ParameterSetKey.ACC_SUBJECT_ID)) {
				subjectId = nowData.getValue();
			}
			//会计科目ID
			if (nowData.getKey().equals(Constant.ParameterSetKey.LR_ACC_SUBJECT_ID)
					|| nowData.getKey().equals(Constant.ParameterSetKey.FP_ACC_SUBJECT_ID)
					|| nowData.getKey().equals(Constant.ParameterSetKey.SY_ACC_SUBJECT_ID)) {
				accountSubjectIdList.add(nowData.getValue());
			}
		}

		if (subjectId == null) {
			throw new ServiceException(ResultCode.VOUCHER_EXIST_RIGHT);
		}
		//科目表对应的信息
		Subject subject = findSubject(subjectId);
		//会计科目对应的信息
		List<AccountSubjectVo> accountSubjectVoList = findAccountSubjectList(subjectId, accountSubjectIdList);
		//数据赋值
		for (ParameterSetVo nowData : returnList) {
			//科目ID
			if (nowData.getKey().equals(Constant.ParameterSetKey.ACC_SUBJECT_ID)
					&& StringUtil.isNotBlank(subject)
					&& StringUtil.isNotBlank(subject.getId())
					&& nowData.getValue().equals(subject.getId())) {
				nowData.setValueStr(subject.getSubjectName());
				continue;
			}
			//会计科目对应信息
			for (AccountSubjectVo accountSubjectVo : accountSubjectVoList) {
				//会计科目ID
				if ((nowData.getKey().equals(Constant.ParameterSetKey.LR_ACC_SUBJECT_ID)
						|| nowData.getKey().equals(Constant.ParameterSetKey.FP_ACC_SUBJECT_ID)
						|| nowData.getKey().equals(Constant.ParameterSetKey.SY_ACC_SUBJECT_ID))
						&& nowData.getValue().equals(accountSubjectVo.getId())) {
					nowData.setValueCode(accountSubjectVo.getCode());
					nowData.setValueStr(accountSubjectVo.getName());
				}
			}
		}
		return returnList;
	}

	/**
	 * @Description 查询租户级别总账参数[GroupCode为key]
	 * @Author 郑勇浩
	 * @Data 2019/10/17 15:53
	 * @Param [param]
	 * @return com.njwd.support.Result<java.util.Map < java.lang.String, java.util.List < com.njwd.entity.ledger.vo.ParameterSetVo>>>
	 */
	@Override
	public Map<String, List<ParameterSetVo>> findParameterSetAdmin(ParameterSetDto param) {
		List<ParameterSetVo> allList = parameterSetService.findAllParameterSet(param);
		//租户级别(accountBookId = 0)
		allList = allList.stream().filter(o -> o.getAccountBookId().equals(Constant.Number.ZEROL)).collect(Collectors.toList());
		//GroupCode分组
		return allList.stream().collect(Collectors.groupingBy(ParameterSetVo::getGroupCode, LinkedHashMap::new, Collectors.toList()));
	}


	/**
	 * @Description 查询个性化总账参数
	 * @Author 郑勇浩
	 * @Data 2019/10/17 15:56
	 * @Param [param]
	 * @return com.njwd.support.Result<java.util.Map < java.lang.Long, java.util.List < com.njwd.entity.ledger.vo.ParameterSetVo>>>
	 */
	@Override
	public Map<Long, List<ParameterSetVo>> findParameterSetPersonal(ParameterSetDto param) {
		//查询数据
		List<ParameterSetVo> allList = parameterSetService.findAllParameterSet(param);
		//GroupCode符合条件的账簿级别(accountBookId != 0)数据
		allList = allList.stream().filter(o -> !o.getAccountBookId().equals(Constant.Number.ZEROL) && o.getGroupCode().equals(param.getGroupCode())).collect(Collectors.toList());
		//AccountBookId分组
		return allList.stream().collect(Collectors.groupingBy(ParameterSetVo::getAccountBookId, LinkedHashMap::new, Collectors.toList()));
	}

	/**
	 * @Description 查询总账参数设置的值
	 * @Author 郑勇浩
	 * @Data 2019/10/18 16:30
	 * @Param [param]
	 * @return com.njwd.entity.ledger.vo.ParameterSetVo
	 */
	@Override
	public ParameterSetVo findParameterSetValue(ParameterSetDto param) {
		FastUtils.checkParams(param.getKey(), param.getAccountBookId());
		// 查询个性化
		SysUserVo operator = UserUtils.getUserVo();
		param.setRootEnterpriseId(operator.getRootEnterpriseId());
		//查询数据
		List<ParameterSetVo> allList = parameterSetService.findAllParameterSet(param);
		if (CollectionUtils.isEmpty(allList)) {
			return null;
		}

		//返回groupCode为key的Map数据
		ParameterSetVo returnData = null;
		for (ParameterSetVo psv : allList) {
			//key是否相同
			if (!psv.getKey().equals(param.getKey())) {
				continue;
			}
			//如果有账簿个性化则返回 否则 返回原有数据
			if (psv.getAccountBookId().equals(Constant.Number.ZEROL)) {
				returnData = psv;
			}
			if (psv.getAccountBookId().equals(param.getAccountBookId())) {
				return psv;
			}
		}
		return returnData;
	}

	/**
	 * 查询总账参数设置
	 *
	 * @param parameterSetDto parameterSetDto
	 * @return com.njwd.entity.ledger.vo.ParameterSetVo
	 * @author xyyxhcj@qq.com
	 * @date 2019/10/22 9:01
	 **/
	@Override
	@Cacheable(value = Constant.RedisCache.PARAMETER_SET_VALUE, key = "#parameterSetDto.rootEnterpriseId", unless = "#result == null")
	public ParameterSetVo findParameterSet(ParameterSetDto parameterSetDto) {
		Long rootEnterpriseId = parameterSetDto.getRootEnterpriseId();
		List<ParameterSet> parameterSets = parameterSetMapper.selectList(new LambdaQueryWrapper<ParameterSet>().eq(ParameterSet::getRootEnterpriseId, rootEnterpriseId));
		if (parameterSets.isEmpty()) {
			return null;
		}
		List<ParameterSetSub> parameterSetSubs = parameterSetSubMapper.selectList(new LambdaQueryWrapper<ParameterSetSub>().eq(ParameterSetSub::getRootEnterpriseId, rootEnterpriseId));
		Map<Long, ParameterSet> paramIdDict = parameterSets.stream().collect(Collectors.toMap(ParameterSet::getId, v -> v));
		// 一级key'参数标识' 二级key'账簿ID'
		Map<String, Map<Long, ParameterSetSub>> paramDict = new LinkedHashMap<>();
		parameterSetDto.setParamDict(paramDict);
		for (ParameterSetSub setSub : parameterSetSubs) {
			ParameterSet parameterSet = paramIdDict.get(setSub.getSetId());
			if (parameterSet == null) {
				continue;
			}
			paramDict.computeIfAbsent(parameterSet.getKey(), k -> new LinkedHashMap<>()).put(setSub.getAccountBookId(), setSub);
		}
		return parameterSetDto;
	}

	/**
	 * @Description 检查凭证
	 * @Author 郑勇浩
	 * @Data 2019/11/1 11:52
	 * @Param [accountBookIds]
	 */
	private void checkVoucher(Map<Long, String> checkVoucherMap) {
		//检查凭证是否存在
		if (MapUtils.isEmpty(checkVoucherMap)) {
			return;
		}

		//查询凭证
		VoucherDto param = new VoucherDto();
		param.setRootEnterpriseId(param.getRootEnterpriseId());
		param.setIsDel(Constant.Number.ANTI_INITLIZED);
		param.setAccountBookIds(new ArrayList<>(checkVoucherMap.keySet()));
		List<Long> hasVoucherIdList = voucherService.findHasVoucherByAccountBookId(param);
		if (CollectionUtils.isEmpty(hasVoucherIdList)) {
			return;
		}
		//如果有账簿级别的修改 并且账簿级别有数据
		if (checkVoucherMap.containsKey(Constant.Number.ZEROL) && hasVoucherIdList.size() > 0) {
			throw new ServiceException(ResultCode.EXIST_VOUCHER_ADMIN);
		}
		//判断对应的账簿是否有凭证
		for (Long accountBookId : param.getAccountBookIds()) {
			if (hasVoucherIdList.contains(accountBookId)) {
				throw new ServiceException(checkVoucherMap.get(accountBookId) + ResultCode.EXIST_VOUCHER_ACCOUNT_BOOK.message, ResultCode.EXIST_VOUCHER_ACCOUNT_BOOK);
			}
		}
	}

	/**
	 * @Description 检查数据[本年利润 利润分配 以前年度损益调整]
	 * @Author 郑勇浩
	 * @Data 2019/10/22 14:17
	 * @Param [checkDataMap, keyNameMap, subjectId]
	 */
	private void checkData(Map<String, List<Long>> checkDataMap, Map<String, String> keyNameMap, Long subjectId) {
		//获取科目id
		if (subjectId == null) {
			throw new ServiceException(ResultCode.PARAMETER_SET_SUBJECT_NOT_EXIST);
		}
		// 先把所有的id放一起 一次查询
		List<Long> ids = new ArrayList<>();
		for (List<Long> idList : checkDataMap.values()) {
			ids.addAll(idList);
		}
		if (CollectionUtils.isEmpty(ids)) {
			return;
		}
		//查询会计科目信息
		List<AccountSubjectVo> accountSubjectList = findAccountSubjectList(subjectId, ids);
		//判断结果
		String name;
		AccountSubjectVo accountSubject;
		for (String key : checkDataMap.keySet()) {
			name = keyNameMap.get(key);
			ids = checkDataMap.get(key);
			//判断是哪种类型的错误
			if (CollectionUtils.isEmpty(accountSubjectList)) {
				throw new ServiceException(name + ResultCode.ACCOUNT_SUBJECT_IS_DELETE.message, ResultCode.ACCOUNT_SUBJECT_IS_DELETE);
			}
			//获取map里面的id
			for (Long id : ids) {
				accountSubject = accountSubjectList.stream().filter(o -> o.getId().equals(id)).findFirst().orElse(null);
				//是否存在 启用
				if (accountSubject == null) {
					throw new ServiceException(name + ResultCode.ACCOUNT_SUBJECT_IS_DELETE.message, ResultCode.ACCOUNT_SUBJECT_IS_DELETE);
				}
				//以前年度损益调整科目
				if (!key.equals(Constant.ParameterSetKey.SY_ACC_SUBJECT_ID)) {
					//是否末级科目
					if (!accountSubject.getIsFinal().equals(Constant.Number.INITIAL)) {
						throw new ServiceException(name + ResultCode.ACCOUNT_SUBJECT_IS_LAST.message, ResultCode.ACCOUNT_SUBJECT_IS_LAST);
					}
				}
			}
		}
	}

	/**
	 * @Description 查询科目信息
	 * @Author 郑勇浩
	 * @Data 2019/10/24 16:24
	 * @Param [subjectId]
	 * @return java.util.List<com.njwd.entity.platform.vo.SubjectVo>
	 */
	private Subject findSubject(Long subjectId) {
		SubjectDto subjectDto = new SubjectDto();
		subjectDto.setId(subjectId);
		return subjectFeignClient.findSubject(subjectDto).getData();
	}

	/**
	 * @Description 查询会计科目信息
	 * @Author 郑勇浩
	 * @Data 2019/10/24 16:14
	 * @Param [accountSubjectIds]
	 * @return java.util.List<com.njwd.entity.platform.vo.AccountSubjectVo>
	 */
	private List<AccountSubjectVo> findAccountSubjectList(Long subjectId, List<Long> accountSubjectIds) {
		AccountSubjectDto accountSubjectParam = new AccountSubjectDto();
		accountSubjectParam.setSubjectId(subjectId);
		accountSubjectParam.setIds(accountSubjectIds);
		accountSubjectParam.setSubjectCodeOperator(Constant.Number.ANTI_INITLIZED);
		accountSubjectParam.setIsIncludeEnable(Constant.Number.ANTI_INITLIZED);
		//查询所有的结果
		Result<AccountSubjectVo> result = accountSubjectFeignClient.findSubjectInfoByParam(accountSubjectParam);
		if (result == null || result.getData() == null) {
			throw new ServiceException(ResultCode.OPERATION_FAILURE);
		}
		return result.getData().getAccountSubjectList();
	}
}
