package com.njwd.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.cloudclient.CommonAbstractFeignClient;
import com.njwd.basedata.mapper.CommonAbstractMapper;
import com.njwd.basedata.service.CommonAbstractService;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.CommonAbstract;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.dto.CommonAbstractDto;
import com.njwd.entity.basedata.vo.CommonAbstractVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.RedisUtils;
import com.njwd.utils.ShiroUtils;
import com.njwd.utils.UserUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description 常用摘要 service impl.
 * @Date 2019/7/25 9:35
 * @Author 郑勇浩
 */
@Service
public class CommonAbstractServiceImpl implements CommonAbstractService {

	@Resource
	private CommonAbstractMapper commonAbstractMapper;
	@Resource
	private CommonAbstractService commonAbstractService;
	@Resource
	private CommonAbstractFeignClient commonAbstractFeignClient;

	/**
	 * @return int
	 * @Description 新增常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/7/25 11:02
	 * @Param [commonAbstractDto]
	 */
	@Override
	public int addCommonAbstract(CommonAbstractDto commonAbstractDto) {
		//权限校验
		checkUser(commonAbstractDto.getIsEnterpriseAdmin(), Constant.MenuDefine.COMMON_ABSTRACT_EDIT, commonAbstractDto.getUseEnterpriseId());

		//USER INFO
		SysUserVo operator = UserUtils.getUserVo();
		commonAbstractDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		commonAbstractDto.setCreatorId(operator.getUserId());
		commonAbstractDto.setCreatorName(operator.getName());

		//集团共享数据
		if (commonAbstractDto.getUseEnterpriseId().equals(Constant.Number.ZEROL)) {
			commonAbstractDto.setCreateEnterpriseId(Constant.Number.ZEROL);
		}

		//校验 摘要内容 重复
		checkData(commonAbstractDto);

		//新增
		return commonAbstractMapper.insert(commonAbstractDto);
	}

	/**
	 * @return int
	 * @Description 删除常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/7/25 11:01
	 * @Param [bto]
	 */
	@Override
	public int deleteCommonAbstract(CommonAbstractDto dto) {
		//查询该数据状态
		CommonAbstract commonAbstract = commonAbstractService.findCommonAbstract(dto);
		if (commonAbstract == null) {
			throw new ServiceException(ResultCode.RECORD_NOT_EXIST);
		}

		//查询该用户是否有权限 删除集团共享的
		if (dto.getIsEnterpriseAdmin().equals(Constant.Is.NO) && commonAbstract.getUseEnterpriseId().equals(Constant.Number.ZEROL)) {
			throw new ServiceException(ResultCode.COMMON_ABSTRACT_NO_ROLE_DELETE);
		}

		//权限校验
		checkUser(dto.getIsEnterpriseAdmin(), Constant.MenuDefine.COMMON_ABSTRACT_DELETE, commonAbstract.getUseEnterpriseId());

		//判断是否被删除
		if (commonAbstract.getIsDel().equals(Constant.Is.YES)) {
			throw new ServiceException(ResultCode.IS_DEL);
		}

		//删除
		CommonAbstract sqlParam = new CommonAbstract();
		sqlParam.setId(dto.getId());
		sqlParam.setRootEnterpriseId(dto.getRootEnterpriseId());

		FastUtils.copyProperties(dto, commonAbstract);
		commonAbstract.setRootEnterpriseId(null);
		commonAbstract.setIsDel(Constant.Is.YES);
		//清除缓存
		RedisUtils.remove(Constant.RedisCache.COMMON_ABSTRACT, dto.getId());
		return commonAbstractMapper.update(commonAbstract, new QueryWrapper<>(sqlParam));
	}

	/**
	 * @return com.njwd.system.support.BatchResult
	 * @Description 批量删除
	 * @Author 郑勇浩
	 * @Data 2019/7/25 13:33
	 * @Param [commonAbstractDto]
	 */
	@Override
	public BatchResult deleteCommonAbstractBatch(CommonAbstractDto dto) {
		//初始化
		BatchResult result = new BatchResult();
		result.setFailList(new ArrayList<>());
		result.setSuccessList(new ArrayList<>());

		//查询idList 的状态
		List<CommonAbstract> commonAbstractList = commonAbstractMapper.findStatusByIdList(dto);

		//循环添加错误
		for (CommonAbstract commonAbstract : commonAbstractList) {

			//查询该用户是否有权限 删除集团共享的
			if (dto.getIsEnterpriseAdmin().equals(Constant.Is.NO) && commonAbstract.getUseEnterpriseId().equals(Constant.Number.ZEROL)) {
				addFailResult(result, commonAbstract.getId(), ResultCode.COMMON_ABSTRACT_NO_ROLE_DELETE.message);
				continue;
			}

			//判断是否有该数据的权限
			if (dto.getIsEnterpriseAdmin().equals(Constant.Is.NO) && !ShiroUtils.hasPerm(Constant.MenuDefine.COMMON_ABSTRACT_DELETE, commonAbstract.getUseEnterpriseId())) {
				addFailResult(result, commonAbstract.getId(), ResultCode.PERMISSION_NOT.message);
				continue;
			}

			//判断删除状态
			if (commonAbstract.getIsDel().equals(Constant.Is.YES)) {
				addFailResult(result, commonAbstract.getId(), ResultCode.IS_DEL.message);
				continue;
			}

			//添加成功的
			result.getSuccessList().add(commonAbstract.getId());
		}
		//防止没有数据
		if (result.getSuccessList().size() == 0) {
			return result;
		}

		//SQL PARAM
		CommonAbstract sqlParam = new CommonAbstract();
		sqlParam.setRootEnterpriseId(dto.getRootEnterpriseId());
		//生成更新条件
		dto.setIsDel(Constant.Is.YES);
		dto.setRootEnterpriseId(null);
		commonAbstractMapper.update(dto, new QueryWrapper<>(sqlParam).in("id", result.getSuccessList()));
		//清除成功修改的redis缓存
		RedisUtils.removeBatch(Constant.RedisCache.COMMON_ABSTRACT, result.getSuccessList());
		return result;
	}

	/**
	 * @return int
	 * @Description 更新常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/7/25 11:31
	 * @Param [commonAbstractDto]
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public long updateCommonAbstract(CommonAbstractDto commonAbstractDto) {
		//权限校验
		checkUser(commonAbstractDto.getIsEnterpriseAdmin(), Constant.MenuDefine.COMMON_ABSTRACT_EDIT, commonAbstractDto.getUseEnterpriseId());

		//查询该数据状态
		CommonAbstract commonAbstract = commonAbstractService.findCommonAbstract(commonAbstractDto);
		if (commonAbstract == null) {
			throw new ServiceException(ResultCode.RECORD_NOT_EXIST);
		}

		if (commonAbstract.getIsDel().equals(Constant.Is.YES)) {
			throw new ServiceException(ResultCode.IS_DEL);
		}
		//集团共享数据
		if (commonAbstractDto.getUseEnterpriseId().equals(Constant.Number.ZEROL)) {
			commonAbstractDto.setCreateEnterpriseId(Constant.Number.ZEROL);
		}

		//校验常用摘要内容
		checkData(commonAbstractDto);

		CommonAbstract sqlParam = new CommonAbstract();
		sqlParam.setId(commonAbstractDto.getId());
		//如果没有最后更新时间，但数据库有 或者最后更新时间与数据库更新时间不同
		if (commonAbstractDto.getLastUpdateTime() == null && commonAbstract.getUpdateTime() != null) {
			throw new ServiceException(ResultCode.IS_CHANGE);
		}
		if (commonAbstractDto.getLastUpdateTime() != null && !commonAbstractDto.getLastUpdateTime().equals(commonAbstract.getUpdateTime())) {
			throw new ServiceException(ResultCode.IS_CHANGE);
		}

		commonAbstractDto.setId(null);
		//更新常用摘要
		commonAbstractMapper.update(commonAbstractDto, new QueryWrapper<>(sqlParam));
		commonAbstractDto.setId(sqlParam.getId());
		//清除缓存
		RedisUtils.remove(Constant.RedisCache.COMMON_ABSTRACT, commonAbstractDto.getId());
		return commonAbstractDto.getUpdateTime().getTime();
	}

	/**
	 * @return com.njwd.ledger.entity.vo.CommonAbstractVo
	 * @Description 查询常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/7/25 14:05
	 * @Param [commonAbstractDto]
	 */
	@Override
	@Cacheable(value = Constant.RedisCache.COMMON_ABSTRACT, key = "#commonAbstractDto.id", unless = "#result == null")
	public CommonAbstractVo findCommonAbstract(CommonAbstractDto commonAbstractDto) {
		//查询当前对象
		CommonAbstractVo commonAbstractVo = commonAbstractMapper.findOne(commonAbstractDto);
		if (commonAbstractVo == null) {
			return null;
		}
		commonAbstractDto.setId(commonAbstractVo.getId());
		return commonAbstractVo;
	}

	/**
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.ledger.entity.vo.CommonAbstractVo>
	 * @Description 查询常用摘要分页
	 * @Author 郑勇浩
	 * @Data 2019/7/25 14:52
	 * @Param [commonAbstractDto]
	 */
	@Override
	public Page<CommonAbstractVo> findCommonAbstractPage(CommonAbstractDto commonAbstractDto) {
		return commonAbstractMapper.findPage(commonAbstractDto.getPage(), commonAbstractDto);
	}
	
	/**
	 * @description: 获取待引用常用摘要
	 * @param: [commonAbstractDto]
	 * @return: java.util.List<com.njwd.entity.basedata.vo.CommonAbstractVo> 
	 * @author: xdy        
	 * @create: 2019-11-14 10:08 
	 */
	@Override
	public List<CommonAbstractVo> findBringInCommonAbstract(CommonAbstractDto commonAbstractDto) {
		SysUserVo userVo = UserUtils.getUserVo();
		commonAbstractDto.setRootEnterpriseId(userVo.getRootEnterpriseId());
		List<CommonAbstractVo> commonAbstractVoList = commonAbstractMapper.findPlatformCommonAbstract(commonAbstractDto);
		List<Long> platformIds = commonAbstractVoList.stream().map(i->i.getPlatformId()).collect(Collectors.toList());
		com.njwd.entity.platform.dto.CommonAbstractDto platformCommonAbstractDto = new com.njwd.entity.platform.dto.CommonAbstractDto();
		FastUtils.copyProperties(commonAbstractDto,platformCommonAbstractDto);
		platformCommonAbstractDto.setPlatformIds(platformIds);
		Result<List<CommonAbstractVo>> result = commonAbstractFeignClient.findCommonAbstractList(platformCommonAbstractDto);
		return result.getData();
	}

	/**
	 * @Description 公司内 重复性校验
	 * @Author 郑勇浩
	 * @Data 2019/7/25 10:32
	 * @Param [commonAbstractDto]
	 */
	private void checkData(CommonAbstractDto dto) {
		//校验 摘要内容重复
		Integer hasOne = commonAbstractMapper.findHasRepeat(dto);
		if (hasOne > 0) {
			throw new ServiceException(ResultCode.COMMON_ABSTRACT_CONTENT_EXIST);
		}
	}

	/**
	 * @Description 添加失败原因
	 * @Author 郑勇浩
	 * @Data 2019/9/10 15:03
	 * @Param [id, failMessage]
	 */
	private void addFailResult(BatchResult result, Long id, String failMessage) {
		ReferenceDescription fd = new ReferenceDescription();
		fd.setBusinessId(id);
		fd.setReferenceDescription(failMessage);
		result.getFailList().add(fd);
	}

	/**
	 * @Description 用户权限校验
	 * @Author 郑勇浩
	 * @Data 2019/9/10 11:31
	 * @Param [isEnterpriseAdmin, roleName, companyId]
	 */
	private void checkUser(Byte isEnterpriseAdmin, String roleName, Long companyId) {
		//业务端
		if (isEnterpriseAdmin.equals(Constant.Is.NO)) {
			ShiroUtils.checkPerm(roleName, companyId);
		}
	}
}
